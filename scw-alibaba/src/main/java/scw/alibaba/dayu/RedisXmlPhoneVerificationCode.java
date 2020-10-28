package scw.alibaba.dayu;

import java.lang.reflect.InvocationTargetException;

import scw.core.utils.XTime;
import scw.redis.Redis;
import scw.result.ResultFactory;

public final class RedisXmlPhoneVerificationCode extends AbstractXmlPhoneVerificationCode {
	private final Redis redis;
	private final long tempSuffix = System.currentTimeMillis();

	public RedisXmlPhoneVerificationCode(String xmlPath, Redis redis, ResultFactory resultFactory)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(xmlPath, resultFactory);
		this.redis = redis;
	}

	@Override
	public PhoneVerificationCode getCacheData(int configIndex, String phone) {
		StringBuilder sb = new StringBuilder();
		sb.append(phone);
		sb.append("&").append(configIndex);
		sb.append("&").append(tempSuffix);
		return (PhoneVerificationCode) redis.getObjectOperations().get(sb.toString());
	}

	@Override
	public void setCacheData(int configIndex, String phone, PhoneVerificationCode json) {
		if (json == null) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(phone);
		sb.append("&").append(configIndex);
		sb.append("&").append(tempSuffix);
		redis.getObjectOperations().setex(sb.toString(), (int) (XTime.ONE_DAY / 1000), json);
	}
}
