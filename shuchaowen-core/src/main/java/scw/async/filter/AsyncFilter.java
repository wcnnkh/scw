package scw.async.filter;

import scw.aop.Context;
import scw.aop.Filter;
import scw.aop.FilterChain;
import scw.aop.Invoker;
import scw.core.instance.NoArgsInstanceFactory;
import scw.core.instance.annotation.Configuration;
import scw.lang.NotSupportedException;

@Configuration(order = Integer.MAX_VALUE)
public final class AsyncFilter implements Filter {
	private static ThreadLocal<Boolean> TAG_THREAD_LOCAL = new ThreadLocal<Boolean>();
	private final NoArgsInstanceFactory instanceFactory;

	public AsyncFilter(NoArgsInstanceFactory instanceFactory) {
		this.instanceFactory = instanceFactory;
	}

	public static boolean isStartAsync() {
		Boolean tag = TAG_THREAD_LOCAL.get();
		return tag != null && tag;
	}

	public static void startAsync() {
		TAG_THREAD_LOCAL.set(true);
	}

	public static void endAsync() {
		TAG_THREAD_LOCAL.set(false);
	}

	public Object doFilter(Invoker invoker, Context context,
			FilterChain filterChain) throws Throwable {
		Async async = context.getMethod().getAnnotation(Async.class);
		if (async == null) {
			return filterChain.doFilter(invoker, context);
		}
		
		if (isStartAsync()) {
			return filterChain.doFilter(invoker, context);
		}

		if (!instanceFactory.isInstance(async.service())) {
			throw new NotSupportedException("not support async: "
					+ context.getMethod());
		}

		AsyncService asyncService = instanceFactory
				.getInstance(async.service());
		AsyncRunnableMethod asyncRunnableMethod = asyncService.create(async,
				context);
		startAsync();
		try {
			asyncService.service(asyncRunnableMethod);
		} finally{
			endAsync();
		}
		return null;
	}
}