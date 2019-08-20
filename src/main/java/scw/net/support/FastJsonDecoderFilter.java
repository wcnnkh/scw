package scw.net.support;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import scw.core.Constants;

public class FastJsonDecoderFilter extends AbstractTextDecoderFilter {
	public FastJsonDecoderFilter() {
		this(Constants.DEFAULT_CHARSET_NAME);
	}

	public FastJsonDecoderFilter(String charsetName) {
		super(charsetName);
	}

	@Override
	protected boolean isVerifyType(Type type) {
		return true;
	}

	@Override
	protected Object textDecoder(String contentType, String text, Type type) {
		return JSON.parseObject(text, type, Feature.SupportNonPublicField);
	}
}
