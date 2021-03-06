package scw.rpc.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import scw.core.Constants;
import scw.http.HttpMethod;
import scw.http.MediaType;

@Target({ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpRemote {
	String value();
	
	HttpMethod method() default HttpMethod.GET;
	
	String contentType() default MediaType.APPLICATION_FORM_URLENCODED_VALUE;
	
	String charsetName() default Constants.UTF_8_NAME;
}
