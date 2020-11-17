package scw.beans.ioc.value;

import scw.beans.BeanDefinition;
import scw.beans.BeanFactory;
import scw.beans.annotation.Value;
import scw.core.ResolvableType;
import scw.event.EventListener;
import scw.event.support.DynamicValue;
import scw.mapper.Field;
import scw.util.PropertyPlaceholderHelper;
import scw.value.StringValue;
import scw.value.property.PropertyEvent;
import scw.value.property.PropertyFactory;

public class DefaultValueProcess extends AbstractValueProcesser {

	@Override
	protected void processInteranl(BeanDefinition beanDefinition, BeanFactory beanFactory,
			PropertyFactory propertyFactory, final Object bean, final Field field, Value value, final String name,
			String charsetName) throws Exception{
		if(field.getSetter().getType() == DynamicValue.class){
			ResolvableType valueType = ResolvableType.forType(field.getSetter().getGenericType());
			valueType = valueType.getGeneric(0);
			DynamicValue<Object> dynamicValue = propertyFactory.getDynamicValue(name, valueType.getType(), null);
			field.getSetter().set(bean, dynamicValue);
			//如果是一个动态值就不用进行监听了
			return ;
		}
		
		if(name.startsWith(PropertyPlaceholderHelper.PLACEHOLDER_PREFIX) && name.endsWith(PropertyPlaceholderHelper.PLACEHOLDER_SUFFIX)){
			String v = propertyFactory.resolvePlaceholders(name);
			set(bean, field, charsetName, new StringValue(v));
			return ;
		}
		
		scw.value.Value v = propertyFactory.get(name);
		set(bean, field, name, v);

		if (isRegisterListener(beanDefinition, field, value)) {
			propertyFactory.registerListener(name, new EventListener<PropertyEvent>() {

				public void onEvent(PropertyEvent event) {
					try {
						set(bean, field, name, event.getValue());
					} catch (Exception e) {
						logger.error(e, field);
					}
				}
			});
		}
	}

	protected void set(final Object bean, final Field field, final String name, scw.value.Value value)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Changes in progress name [{}] field [{}] value [{}]", name, field.getSetter(), value);
		}
		field.getSetter().set(bean, value == null ? null : value.getAsObject(field.getSetter().getGenericType()));
	}
}
