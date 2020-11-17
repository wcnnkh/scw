package scw.feign.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FeignClient {
	/**
	 * 默认不指明host，会去从配置文件中去查找feign.host
	 * @return
	 */
	public String host() default "";
}
