package scw.embed.servlet;

import javax.servlet.Servlet;

import scw.aop.annotation.AopEnable;
import scw.application.MainArgs;
import scw.beans.BeanFactory;
import scw.value.property.PropertyFactory;

@AopEnable(false)
public interface ServletEmbedded {
	void init(BeanFactory beanFactory, PropertyFactory propertyFactory, Servlet service, Class<?> mainClass,
			MainArgs args) throws Exception;

	void destroy() throws Exception;
}
