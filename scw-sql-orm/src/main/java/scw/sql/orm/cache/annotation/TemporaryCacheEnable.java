package scw.sql.orm.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TemporaryCacheEnable {
	public boolean value() default true;

	public boolean keys() default false;

	public int exp() default 2;

	public TimeUnit expTimeUnit() default TimeUnit.DAYS;
}
