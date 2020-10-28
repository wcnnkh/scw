package scw.complete.method.async;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface AsyncMethodService {
	void service(AsyncMethodCompleteTask asyncComplete) throws Exception;
}
