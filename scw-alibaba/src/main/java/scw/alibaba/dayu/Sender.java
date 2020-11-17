package scw.alibaba.dayu;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scw.aop.annotation.AopEnable;
import scw.core.Assert;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.core.utils.XTime;
import scw.http.HttpUtils;
import scw.http.MediaType;
import scw.json.JSONUtils;
import scw.json.JsonObject;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.result.DataResult;
import scw.result.ResultFactory;
import scw.security.SignatureUtils;

@AopEnable(false)
public class Sender {
	private static Logger logger = LoggerFactory.getLogger(Sender.class);
	
	private final String host;
	private final String appKey;
	private final String version;
	private final String format;
	private final String sign_method;
	private final String appSecret;
	private final ResultFactory resultFactory;

	public Sender(String appKey, String appSecret, ResultFactory resultFactory) {
		this("http://gw.api.taobao.com/router/rest", appKey, "2.0", "json", "md5", appSecret, resultFactory);
	}

	public Sender(String host, String appKey, String version, String format, String sign_method, String appSecret,
			ResultFactory resultFactory) {
		this.host = host;
		this.appKey = appKey;
		this.version = version;
		this.format = format;
		this.sign_method = sign_method;
		this.appSecret = appSecret;
		this.resultFactory = resultFactory;
	}

	public final DataResult<String> send(MessageModel messageModel, Map<String, String> parameterMap, String phone) {
		Assert.requiredArgument(StringUtils.isNotEmpty(phone), "phone");
		return send(messageModel, parameterMap, Arrays.asList(phone));
	}

	public DataResult<String> send(MessageModel messageModel, Map<String, String> parameterMap,
			Collection<String> phones) {
		Assert.requiredArgument(messageModel != null, "messageModel");
		Assert.requiredArgument(!CollectionUtils.isEmpty(phones), "phones");
		Map<String, String> map = new HashMap<String, String>();
		map.put("app_key", appKey);
		map.put("v", version);
		map.put("format", format);
		map.put("sign_method", sign_method);

		map.put("timestamp", XTime.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
		map.put("sms_free_sign_name", messageModel.getSms_free_sign_name());
		if (!CollectionUtils.isEmpty(parameterMap)) {
			map.put("sms_param", JSONUtils.toJSONString(parameterMap));
		}
		map.put("sms_template_code", messageModel.getSms_template_code());
		map.put("method", "alibaba.aliqin.fc.sms.num.send");
		map.put("sms_type", "normal");
		map.put("rec_num", StringUtils.collectionToCommaDelimitedString(phones));
		map.put("sign", getSign(map));
		JsonObject response = HttpUtils.getHttpClient()
				.post(JsonObject.class, host, map, MediaType.APPLICATION_FORM_URLENCODED).getBody();
		if(response.containsKey("alibaba_aliqin_fc_sms_num_send_response")){
			response = response.getJsonObject("alibaba_aliqin_fc_sms_num_send_response");
		}
		
		response = response.getJsonObject("result");
		if (response.containsKey("err_code") && response.getIntValue("err_code") == 0) {
			return resultFactory.success(response.toJsonString());
		}
		
		logger.error(response);
		JsonObject errorResponse = response.getJsonObject("error_response");
		String msg = errorResponse.getString("sub_msg");
		if (StringUtils.isEmpty(msg)) {
			msg = errorResponse.getString("msg");
		}
		return resultFactory.error(msg, response.toJsonString());
	}

	/**
	 * 签名
	 * 
	 * @param map
	 * @param secret
	 * @param sign_method
	 * @return
	 */
	protected String getSign(Map<String, String> map) {
		String[] keys = map.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		StringBuilder sb = new StringBuilder();
		boolean isMd5 = false;
		if ("md5".equals(sign_method)) {
			sb.append(appSecret);
			isMd5 = true;
		}

		for (String key : keys) {
			String value = map.get(key);
			if (StringUtils.isEmpty(key, value)) {
				continue;
			}
			// sb.append(key).append(Http.encode(value, "utf-8"));
			sb.append(key).append(value);
		}

		String bytes = null;
		if (isMd5) {
			sb.append(appSecret);
			bytes = SignatureUtils.md5(sb.toString(), "UTF-8");
		} else {
			bytes = SignatureUtils.hmacMD5(sb.toString(), appSecret, "UTF-8");
		}

		return bytes.toUpperCase();
	}
}
