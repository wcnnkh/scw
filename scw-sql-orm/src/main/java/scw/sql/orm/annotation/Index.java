package scw.sql.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import scw.sql.orm.enums.IndexMethod;
import scw.sql.orm.enums.IndexOrder;
import scw.sql.orm.enums.IndexType;

/**
 * 索引
 * 
 * @author shuchaowen
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
	public String name() default "";

	public IndexType type() default IndexType.DEFAULT;

	/**
	 * 默认是全部长度
	 * 
	 * @return
	 */
	public int length() default -1;

	/**
	 * 默认由数据库自己选择
	 * 
	 * @return
	 */
	public IndexMethod method() default IndexMethod.DEFAULT;

	/**
	 * 索引的排序方式
	 * @return
	 */
	public IndexOrder order() default IndexOrder.DEFAULT;
}
