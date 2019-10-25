package scw.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import scw.mvc.wrapper.CompatibleResponseWrapperService;
import scw.mvc.wrapper.ResponseWrapperService;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseWrapper {
	public boolean value() default true;

	public Class<? extends ResponseWrapperService> service() default CompatibleResponseWrapperService.class;
}
