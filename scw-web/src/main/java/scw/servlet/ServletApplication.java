package scw.servlet;

import javax.servlet.ServletContext;

import scw.application.CommonApplication;
import scw.beans.xml.XmlBeanFactory;
import scw.core.GlobalPropertyFactory;
import scw.core.utils.StringUtils;

public class ServletApplication extends CommonApplication {

	public ServletApplication(ServletContext servletContext) {
		super(getConfigXml(servletContext));
		setBasePackageName(servletContext);
	}
	
	public  void setBasePackageName(ServletContext servletContext){
		String name = servletContext.getInitParameter("packageName");
		if(StringUtils.isNotEmpty(name)){
			GlobalPropertyFactory.getInstance().setBasePackageName(name);
		}
	}

	/**
	 * 兼容老版本
	 * 
	 * @param servletContext
	 * @return
	 */
	public static String getConfigXml(ServletContext servletContext) {
		String config = servletContext.getInitParameter("shuchaowen");
		if (config == null) {
			config = servletContext.getInitParameter("scw");
		}

		if (config == null) {
			config = servletContext.getInitParameter(XmlBeanFactory.CONFIG_NAME);
		}
		return config;
	}
}
