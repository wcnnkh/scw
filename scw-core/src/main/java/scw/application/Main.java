package scw.application;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface Main {
	void main(MainApplication application) throws Throwable;
}
