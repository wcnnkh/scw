package scw.embed.servlet;

import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface ServletContainerInitializerConfiguration {
	Collection<? extends ServletContainerInitializer> getServletContainerInitializers();
	
	Set<Class<?>> getClassSet();
}
