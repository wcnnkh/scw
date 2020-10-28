package scw.mvc.parameter;

import scw.aop.annotation.AopEnable;
import scw.beans.annotation.AutoImpl;
import scw.core.parameter.ParameterDescriptor;
import scw.json.JSONSupport;
import scw.mvc.HttpChannel;

@AutoImpl({DefaultRequestBodyParse.class})
@AopEnable(false)
public interface RequestBodyParse {
	Object requestBodyParse(HttpChannel httpChannel, JSONSupport jsonParseSupport, ParameterDescriptor parameterDescriptor) throws Exception;
}
