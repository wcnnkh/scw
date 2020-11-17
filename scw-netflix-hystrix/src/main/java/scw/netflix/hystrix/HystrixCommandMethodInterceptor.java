package scw.netflix.hystrix;

import com.netflix.hystrix.HystrixCommand;

import scw.aop.MethodInterceptor;
import scw.aop.MethodInterceptorAccept;
import scw.aop.MethodInterceptorChain;
import scw.aop.MethodInvoker;
import scw.core.instance.annotation.Configuration;
import scw.netflix.hystrix.annotation.Hystrix;

@Configuration(order = Integer.MAX_VALUE)
public class HystrixCommandMethodInterceptor implements MethodInterceptor, MethodInterceptorAccept {
	private HystrixCommandFactory hystrixCommandFactory;

	public HystrixCommandMethodInterceptor(HystrixCommandFactory hystrixCommandFactory) {
		this.hystrixCommandFactory = hystrixCommandFactory;
	}
	
	public boolean isAccept(MethodInvoker invoker, Object[] args) {
		return invoker.getSourceClass().getAnnotation(Hystrix.class) != null;
	}

	public Object intercept(MethodInvoker invoker, Object[] args, MethodInterceptorChain chain) throws Throwable {
		HystrixCommand<?> command = hystrixCommandFactory.getHystrixCommandFactory(invoker, args, chain);
		if (command == null) {
			return chain.intercept(invoker, args);
		}
		return command.execute();
	}
}
