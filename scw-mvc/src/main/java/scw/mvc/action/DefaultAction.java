package scw.mvc.action;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import scw.beans.BeanDefinition;
import scw.beans.BeanFactory;
import scw.core.instance.InstanceIterable;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.http.HttpMethod;
import scw.http.server.HttpControllerDescriptor;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.mvc.annotation.ActionInterceptors;
import scw.mvc.annotation.Controller;
import scw.mvc.annotation.Methods;
import scw.value.property.PropertyFactory;

public class DefaultAction extends BeanAction {
	private static Logger logger = LoggerFactory.getLogger(DefaultAction.class);
	protected Collection<HttpControllerDescriptor> httpHttpControllerDescriptors = new HashSet<HttpControllerDescriptor>(
			8);
	protected Collection<HttpControllerDescriptor> sourceClassHttpControllerDescriptors = new HashSet<HttpControllerDescriptor>(
			8);
	protected Collection<HttpControllerDescriptor> methodHttpControllerDescriptors = new HashSet<HttpControllerDescriptor>(
			8);
	private Iterable<ActionInterceptor> actionInterceptors;
	
	public DefaultAction(BeanFactory beanFactory, PropertyFactory propertyFactory, Class<?> targetClass,
			Method method) {
		super(beanFactory, targetClass, method);
		Controller classController = getSourceClass()
				.getAnnotation(Controller.class);
		Controller methodController = getAnnotatedElement()
				.getAnnotation(Controller.class);
		
		String controller = classController.value();
		controller = propertyFactory.resolvePlaceholders(controller);
		
		httpHttpControllerDescriptors.addAll(createHttpControllerDescriptors(
				StringUtils.mergePath("/", controller,
						methodController.value()), getControllerHttpMethods()));
		sourceClassHttpControllerDescriptors.addAll(createHttpControllerDescriptors(
				StringUtils.mergePath("/", controller),
				Arrays.asList(classController.methods())));
		methodHttpControllerDescriptors.addAll(createHttpControllerDescriptors(
				methodController.value(),
				Arrays.asList(methodController.methods())));
		String[] names = getActionInterceptorNames();
		this.actionInterceptors = new InstanceIterable<ActionInterceptor>(beanFactory, Arrays.asList(names), true);
	}
	
	public Iterable<? extends ActionInterceptor> getActionInterceptors() {
		return actionInterceptors;
	}
	
	@Override
	public void optimization() {
		this.httpHttpControllerDescriptors = Arrays.asList(httpHttpControllerDescriptors.toArray(new HttpControllerDescriptor[0]));
		this.sourceClassHttpControllerDescriptors = Arrays.asList(sourceClassHttpControllerDescriptors.toArray(new HttpControllerDescriptor[0]));
		this.methodHttpControllerDescriptors = Arrays.asList(methodHttpControllerDescriptors.toArray(new HttpControllerDescriptor[0]));
	}

	protected Collection<HttpControllerDescriptor> createHttpControllerDescriptors(
			String controller, Collection<HttpMethod> httpMethods) {
		if (controller == null || CollectionUtils.isEmpty(httpMethods)) {
			return Arrays.asList(new HttpControllerDescriptor(controller, HttpMethod.GET));
		}
		List<HttpControllerDescriptor> descriptors = new LinkedList<HttpControllerDescriptor>();
		for (HttpMethod httpMethod : httpMethods) {
			descriptors.add(new HttpControllerDescriptor(controller, httpMethod));
		}
		return descriptors;
	}

	protected String[] getActionInterceptorNames() {
		LinkedHashSet<String> sets = new LinkedHashSet<String>();
		ActionInterceptors actionInterceptors = getSourceClass().getAnnotation(
				ActionInterceptors.class);
		if (actionInterceptors != null) {
			for (Class<? extends ActionInterceptor> f : actionInterceptors.value()) {
				BeanDefinition definition = getBeanFactory().getDefinition(f);
				if(definition == null){
					logger.warn("not support interceptor: {}", f);
					continue;
				}

				sets.remove(definition.getId());
				sets.add(definition.getId());
			}
		}

		Controller controller = getSourceClass().getAnnotation(
				Controller.class);
		if (controller != null) {
			for (Class<? extends ActionInterceptor> f : controller.interceptors()) {
				BeanDefinition definition = getBeanFactory().getDefinition(f);
				if(definition == null){
					logger.warn("not support interceptor: {}", f);
					continue;
				}

				sets.remove(definition.getId());
				sets.add(definition.getId());
			}
		}

		actionInterceptors = getAnnotatedElement().getAnnotation(ActionInterceptors.class);
		if (actionInterceptors != null) {
			sets.clear();
			for (Class<? extends ActionInterceptor> f : actionInterceptors.value()) {
				BeanDefinition definition = getBeanFactory().getDefinition(f);
				if(definition == null){
					logger.warn("not support interceptor: {}", f);
					continue;
				}

				sets.remove(definition.getId());
				sets.add(definition.getId());
			}
		}

		controller = getAnnotatedElement()
				.getAnnotation(Controller.class);
		if (controller != null) {
			for (Class<? extends ActionInterceptor> f : controller.interceptors()) {
				BeanDefinition definition = getBeanFactory().getDefinition(f);
				if(definition == null){
					logger.warn("not support interceptor: {}", f);
					continue;
				}

				sets.remove(definition.getId());
				sets.add(definition.getId());
			}
		}
		return sets.toArray(new String[0]);
	}

	private Collection<HttpMethod> getControllerHttpMethods() {
		Controller classController = getSourceClass()
				.getAnnotation(Controller.class);
		Controller methodController = getAnnotatedElement()
				.getAnnotation(Controller.class);
		Methods methods = getAnnotatedElement().getAnnotation(
				Methods.class);
		Set<HttpMethod> httpMethods = new HashSet<HttpMethod>();
		if (methods == null) {
			if (classController != null) {
				for (scw.http.HttpMethod requestType : classController
						.methods()) {
					httpMethods.add(requestType);
				}
			}
		} else {
			for (scw.http.HttpMethod requestType : methods.value()) {
				httpMethods.add(requestType);
			}
		}

		if (methodController != null) {
			for (scw.http.HttpMethod requestType : methodController
					.methods()) {
				httpMethods.add(requestType);
			}
		}

		if (httpMethods.isEmpty()) {
			httpMethods.add(scw.http.HttpMethod.GET);
		}
		return httpMethods;
	}
	
	public Collection<HttpControllerDescriptor> getHttpControllerDescriptors() {
		return httpHttpControllerDescriptors;
	}

	public Collection<HttpControllerDescriptor> getSourceClassHttpControllerDescriptors() {
		return sourceClassHttpControllerDescriptors;
	}

	public Collection<HttpControllerDescriptor> getMethodHttpControllerDescriptors() {
		return methodHttpControllerDescriptors;
	}
}
