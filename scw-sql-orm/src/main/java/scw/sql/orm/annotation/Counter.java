package scw.sql.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此字段一更新方式为field=field+?
 * 规定数值更新的范围
 * 默认的范围是0到int的最大值
 * 只能在非主键中使用
 * @author shuchaowen
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Counter {
	public double min() default 0;
	
	public double max() default Integer.MAX_VALUE;
}
