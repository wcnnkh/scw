package scw.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import scw.http.HttpMethod;
import scw.mvc.action.ActionInterceptor;
import scw.mvc.action.DefaultAction;
import scw.util.PropertyPlaceholderHelper;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Controller {
	/**
	 * 行为的值，作用视请求分发器决定
	 * 在类上的注解支持使用${name:value}
	 * @see DefaultAction
	 * @see PropertyPlaceholderHelper
	 * @return
	 */
	public String value() default "";

	/**
	 * 请求方法类型
	 * 只在http中有用
	 * @return
	 */
	public HttpMethod[] methods() default {};

	public Class<? extends ActionInterceptor>[] interceptors() default {};
}
