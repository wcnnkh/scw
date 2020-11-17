package scw.logger.log4j;

import scw.logger.AbstractILoggerFactory;
import scw.logger.Logger;
import scw.logger.LoggerLevelManager;

public class Log4jLoggerFactory extends AbstractILoggerFactory {

	static {
		org.apache.log4j.Logger.class.getName();
		Log4jUtils.defaultInit();
	}

	public void destroy() {
		//LogManager.shutdown();
	}

	public Logger getLogger(String name, String placeholder) {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
		return new Log4jLogger(logger, LoggerLevelManager.getInstance().getLevel(name), placeholder);
	}
}
