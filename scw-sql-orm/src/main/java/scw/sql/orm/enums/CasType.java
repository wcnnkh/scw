package scw.sql.orm.enums;

public enum CasType {
	/**
	 * 不参与 cas更新
	 */
	NOTHING,
	/**
	 * 只有当存在更新时才会使用此字段的cas
	 */
	DEFAULT,
	/**
	 * 只要调用更新都会使用当前的值或更新前的值为cas
	 */
	UPDATE,
	/**
	 * 只要调用更新都会使用cas，和update的区别是他会插入一个新值，值在原基础上加1，非数值类型时会出现异常
	 */
	AUTO_INCREMENT,
	;
}
