package scw.logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Handler;

import scw.core.utils.CollectionUtils;

public final class LoggerFactory {
	private static final java.util.logging.Logger ROOT_LOGGER = java.util.logging.Logger
			.getLogger(LoggerFactory.class.getName());
	private static final ILoggerFactory LOGGER_FACTORY = CollectionUtils
			.first(ServiceLoader.load(ILoggerFactory.class));
	private volatile static Map<String, Logger> loggerMap = new HashMap<String, Logger>();

	static {
		// 使用spi机制加载handlers
		List<Handler> handlers = CollectionUtils.toList(ServiceLoader.load(Handler.class));
		if (!CollectionUtils.isEmpty(handlers)) {
			// 存在自定义handler的情况不使用父级的handler
			ROOT_LOGGER.setUseParentHandlers(false);
			for (Handler handler : handlers) {
				ROOT_LOGGER.info("Use logger handler [" + handler + "]");
				ROOT_LOGGER.addHandler(handler);
			}
		}

		// 是否存在第三方日志系统
		if (LOGGER_FACTORY == null) {

		} else {
			ROOT_LOGGER.info("Use logger factory [" + LOGGER_FACTORY + "]");
		}
	}

	/**
	 * 获取根日志记录器
	 * 
	 * @return
	 */
	public static java.util.logging.Logger getRootLogger() {
		return ROOT_LOGGER;
	}

	public static Logger getLogger(String name) {
		Logger cacheLogger = loggerMap.get(name);
		if(cacheLogger == null) {
			synchronized (loggerMap) {
				cacheLogger = loggerMap.get(name);
				if(cacheLogger == null) {
					if (LOGGER_FACTORY == null) {
						java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
						java.util.logging.Logger parent = logger.getParent();
						if (parent != ROOT_LOGGER) {
							logger.setParent(ROOT_LOGGER);
						}
						cacheLogger = new JdkLogger(logger);
					} else {
						cacheLogger = LOGGER_FACTORY.getLogger(name);
					}
					
					loggerMap.put(name, cacheLogger);
				}
			}
		}
		return cacheLogger;
	}

	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}
	
	private LoggerFactory() {
		throw new RuntimeException();
	};
}
