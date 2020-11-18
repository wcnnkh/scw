package scw.servlet;

import java.util.Collection;

import javax.servlet.Filter;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface FilterRegistration {
	static final String ALL = "/*";

	Filter getFilter();

	Collection<String> getUrlPatterns();
}
