package scw.security.session;

import scw.util.attribute.Attributes;

public interface Session extends Attributes<String, Object>{

	/**
	 * 创建时间
	 * @return
	 */
	long getCreationTime();

	String getId();

	/**
	 * 最后一次获取此session的时间
	 * @return
	 */
	long getLastAccessedTime();

	/**
	 * session过期时间 单位:秒
	 * @param maxInactiveInterval
	 */
	void setMaxInactiveInterval(int maxInactiveInterval);

	/**
	 * session过期时间
	 * @return
	 */
	int getMaxInactiveInterval();

	/**
	 * 使session失效
	 */
	void invalidate();

	/**
	 * 是否是新创建的session
	 * @return
	 */
	boolean isNew();
}
