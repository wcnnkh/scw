package scw.core.instance;

import java.util.Iterator;

import scw.core.parameter.ParameterDescriptors;

public interface InstanceBuilder<T> extends Iterable<ParameterDescriptors>{
	Class<? extends T> getTargetClass();
	
	boolean isInstance();
	
	T create() throws Throwable;

	T create(Object... params) throws Throwable;

	T create(Class<?>[] parameterTypes, Object... params) throws Throwable;
	
	Iterator<ParameterDescriptors> iterator();
}
