package scw.redis.enums;

public enum NXXX {
	/**
	 * NX ：只在键不存在时，才对键进行设置操作
	 */
	NX, 
	/**
	 * XX ：只在键已经存在时，才对键进行设置操作
	 */
	XX;
}
