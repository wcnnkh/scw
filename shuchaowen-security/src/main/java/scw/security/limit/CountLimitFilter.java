package scw.security.limit;

import scw.aop.Filter;
import scw.aop.ProxyInvoker;
import scw.core.annotation.AnnotationUtils;
import scw.core.instance.InstanceFactory;
import scw.core.instance.annotation.Configuration;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.security.limit.annotation.CountLimitSecurity;

/**
 * count limit 实现
 * 
 * @author shuchaowen
 *
 */
@Configuration(order=Integer.MAX_VALUE)
public final class CountLimitFilter implements Filter {
	private static Logger logger = LoggerUtils.getLogger(CountLimitFilter.class);
	private final InstanceFactory instanceFactory;

	public CountLimitFilter(InstanceFactory instanceFactory) {
		this.instanceFactory = instanceFactory;
	}

	public Object doFilter(ProxyInvoker invoker, Object[] args) throws Throwable {
		CountLimitSecurity countLimitSecurity = AnnotationUtils.getAnnotation(CountLimitSecurity.class,
				invoker.getMethod(), invoker.getTargetClass());
		if (countLimitSecurity == null) {
			return invoker.invoke(args);
		}

		if (!instanceFactory.isInstance(countLimitSecurity.value())) {
			logger.warn("初始化失败：" + countLimitSecurity.value());
			throw new CountLimitException("系统错误");
		}

		CountLimitConfigFactory countLimitConfigFactory = instanceFactory.getInstance(countLimitSecurity.value());
		CountLimitConfig config = countLimitConfigFactory.getCountLimitConfig(invoker.getTargetClass(),
				invoker.getMethod(), args);
		if (config == null) {
			return invoker.invoke(args);
		}

		if (!instanceFactory.isInstance(countLimitSecurity.factory())) {
			logger.warn("初始化失败：" + countLimitSecurity.factory());
			throw new CountLimitException("系统错误");
		}

		CountLimitFactory countLimitFactory = instanceFactory.getInstance(countLimitSecurity.factory());
		long count = countLimitFactory.incrAndGet(config.getName(), config.getTimeout(), config.getTimeUnit());
		boolean b = count <= config.getMaxCount();
		if (logger.isDebugEnabled()) {
			logger.debug("count limit key={}, method={}, max={}, count={}", config.getName(), invoker.getMethod(),
					config.getMaxCount(), count);
		}

		if (b) {
			return invoker.invoke(args);
		}
		logger.warn("Too frequent operation max={}, count={}, method={}", config.getMaxCount(), count,
				invoker.getMethod());
		throw new CountLimitException("操作过于频繁");
	}

}