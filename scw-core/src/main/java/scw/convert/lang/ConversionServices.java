package scw.convert.lang;

import java.util.Iterator;
import java.util.TreeSet;

import scw.convert.ConfigurableConversionService;
import scw.convert.ConversionService;
import scw.convert.ConversionServiceAware;
import scw.convert.ConverterNotFoundException;
import scw.convert.TypeDescriptor;
import scw.instance.Configurable;
import scw.instance.ConfigurableServices;
import scw.instance.ServiceLoaderFactory;
import scw.lang.LinkedThreadLocal;

public class ConversionServices extends ConvertibleConditionalComparator<Object>
		implements ConfigurableConversionService, Comparable<Object>, ConversionServiceAware,
		Iterable<ConversionService>, Configurable {
	private static final LinkedThreadLocal<ConversionService> NESTED = new LinkedThreadLocal<ConversionService>(
			ConversionServices.class.getName());
	private ConfigurableServices<ConversionService> conversionServices = new ConfigurableServices<>(
			ConversionService.class, (s) -> aware(s), () -> new TreeSet<>(this));
	private ConversionService awareConversionService = this;
	private ConversionService parentConversionService;

	public ConversionServices() {
	}

	public ConversionServices(ConversionService parentConversionServices) {
		this.parentConversionService = parentConversionServices;
	}

	@Override
	public void setConversionService(ConversionService conversionService) {
		this.awareConversionService = conversionService;
	}

	protected void aware(ConversionService conversionService) {
		if (conversionService instanceof ConversionServiceAware) {
			((ConversionServiceAware) conversionService).setConversionService(awareConversionService);
		}
	}

	public void addConversionService(ConversionService conversionService) {
		conversionServices.addService(conversionService);
	}

	@Override
	public void configure(ServiceLoaderFactory serviceLoaderFactory) {
		conversionServices.configure(serviceLoaderFactory);
	}

	@Override
	public Iterator<ConversionService> iterator() {
		return conversionServices.iterator();
	}

	public final boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		for (ConversionService service : this) {
			if (NESTED.exists(service)) {
				continue;
			}

			NESTED.set(service);
			try {
				if (service.canConvert(sourceType, targetType)) {
					return true;
				}
			} finally {
				NESTED.remove(service);
			}
		}

		if (parentConversionService != null && parentConversionService.canConvert(sourceType, targetType)) {
			return true;
		}

		return canDirectlyConvert(sourceType, targetType);
	}

	public final Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		TypeDescriptor sourceTypeToUse = sourceType;
		if (sourceType == null && source != null) {
			sourceTypeToUse = TypeDescriptor.forObject(source);
		}

		for (ConversionService service : this) {
			if (NESTED.exists(service)) {
				continue;
			}

			NESTED.set(service);
			try {
				if (service.canConvert(sourceType, targetType)) {
					return service.convert(source, sourceTypeToUse, targetType);
				}
			} finally {
				NESTED.remove(service);
			}
		}

		if (parentConversionService != null && parentConversionService.canConvert(sourceTypeToUse, targetType)) {
			return parentConversionService.convert(source, sourceTypeToUse, targetType);
		}

		if (canDirectlyConvert(sourceTypeToUse, targetType)) {
			return source;
		}

		throw new ConverterNotFoundException(sourceTypeToUse, targetType);
	}

	public int compareTo(Object o) {
		for (ConversionService service : this) {
			if (ConvertibleConditionalComparator.INSTANCE.compare(service, o) == 1) {
				return 1;
			}
		}
		return -1;
	}
}
