package scw.sql.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import scw.sql.orm.enums.CasType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	public String name() default "";

	public String type() default "";

	public int length() default 0;

	/**
	 * 默认是可以为空的，但是基本数据类型(值类型)、索引、主键列是不能为空的
	 * 
	 * @return
	 */
	public boolean nullAble() default true;

	/**
	 * 是否建立唯一索引
	 * 
	 * @return
	 */
	public boolean unique() default false;

	public String comment() default "";

	/**
	 * 指定此字段以指定类型的cas方式更新,默认不参与 cas，在主键上设置无效
	 * @return
	 */
	public CasType casType() default CasType.NOTHING;
	
	public String charsetName() default "";
}
