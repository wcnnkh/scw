package scw.core.reflect;

import java.util.concurrent.Callable;

public interface Invocation extends Callable<Object>, MethodHolder{
	Object getInstance();
	
	Object[] getArgs();
}
