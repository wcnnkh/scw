package scw.servlet;

import java.util.Iterator;

import javax.servlet.ServletContext;

import scw.core.utils.CollectionUtils;
import scw.util.MultiIterator;
import scw.value.property.ExtendPropertyFactory;

public class ServletContextPropertyFactory extends ExtendPropertyFactory {
	private final ServletContext servletContext;

	public ServletContextPropertyFactory(ServletContext servletContext) {
		super(true, true);
		this.servletContext = servletContext;
	}

	@Override
	protected Object getExtendValue(String key) {
		return servletContext.getInitParameter(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<String> iterator() {
		return new MultiIterator<String>(CollectionUtils.toIterator(servletContext.getInitParameterNames()), super.iterator());
	}
}
