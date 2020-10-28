package scw.redis.enums;

public enum EXPX {
	/**
	 * EX second ：设置键的过期时间为 second 秒
	 */
	EX, 
	/**
	 * PX millisecond ：设置键的过期时间为 millisecond 毫秒
	 */
	PX;
}
