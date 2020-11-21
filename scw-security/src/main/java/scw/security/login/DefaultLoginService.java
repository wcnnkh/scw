package scw.security.login;

import scw.core.utils.StringUtils;
import scw.data.TemporaryCache;
import scw.data.memory.MemoryDataTemplete;
import scw.logger.Logger;
import scw.logger.LoggerUtils;

public class DefaultLoginService<T> extends AbstractLoginService<T> {
	private Logger logger = LoggerUtils.getLogger(getClass());
	private static final String DEFAULT_PREFIX = "login:";
	private final String prefix;
	
	public DefaultLoginService(){
		this(new MemoryDataTemplete());
		logger.info("Using memory {}", getTemporaryCache());
	}

	public DefaultLoginService(TemporaryCache temporaryCache) {
		this(temporaryCache, 7 * 24 * 60 * 60);
	}

	public DefaultLoginService(TemporaryCache temporaryCache, int exp) {
		this(temporaryCache, exp, DEFAULT_PREFIX);
	}

	public DefaultLoginService(TemporaryCache temporaryCache, int exp, String prefix) {
		super(temporaryCache, exp);
		this.prefix = StringUtils.isEmpty(prefix) ? DEFAULT_PREFIX : prefix;
	}

	@Override
	protected String formatUid(T uid) {
		return prefix + uid;
	}
}
