package scw.mvc.page;

import scw.beans.BeanFactory;
import scw.beans.BeanUtils;
import scw.value.property.PropertyFactory;

public final class ConfigurationPageFactory extends MultiPageFactory{
	private static final long serialVersionUID = 1L;

	public ConfigurationPageFactory(BeanFactory beanFactory, PropertyFactory propertyFactory){
		addAll(BeanUtils.loadAllService(PageFactory.class, beanFactory, propertyFactory));
	}
}
