package scw.sql.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	public String name() default "";

	public String type() default "";

	public int length() default 0;

	public boolean nullAble() default false;

	/**
	 * 是否建立唯一索引
	 * 
	 * @return
	 */
	public boolean unique() default false;

	public String comment() default "";
}