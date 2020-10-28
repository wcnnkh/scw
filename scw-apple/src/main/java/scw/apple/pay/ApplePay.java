package scw.apple.pay;

import java.util.HashMap;
import java.util.Map;

import scw.aop.annotation.AopEnable;
import scw.core.utils.StringUtils;
import scw.http.HttpUtils;
import scw.http.MediaType;
import scw.json.JsonObject;
import scw.logger.Logger;
import scw.logger.LoggerFactory;

/**
 * {@link https://developer.apple.com/documentation/appstorereceipts/verifyreceipt}
 * 
 * @author shuchaowen
 *
 */
@AopEnable(false)
public class ApplePay {
	private static Logger logger = LoggerFactory.getLogger(ApplePay.class);
	static final String SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
	static final String DEV_URL = "https://buy.itunes.apple.com/verifyReceipt";

	private final String password;

	public ApplePay() {
		this(null);
	}

	/**
	 * 应用程序的共享机密（十六进制字符串）。仅对包含自动续订的收据使用此字段。
	 * 
	 * @param password
	 */
	public ApplePay(String password) {
		this.password = password;
	}

	public final String getPassword() {
		return password;
	}

	public VerifyReceiptResponse verifyReceipt(String host, VerifyReceiptRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("receipt-data", request.getReceiptData());
		if (!StringUtils.isEmpty(password)) {
			map.put("password", password);
		}

		Boolean excludeOldTransactions = request.getExcludeOldTransactions();
		if (excludeOldTransactions != null) {
			map.put("exclude-old-transactions", excludeOldTransactions);
		}

		JsonObject json = HttpUtils.getHttpClient().post(JsonObject.class, host, map, MediaType.APPLICATION_JSON)
				.getBody();
		logger.debug(json);
		VerifyReceiptResponse response = new VerifyReceiptResponse(json);
		if (response.isUseRetryable() && response.isRetryable()) {
			return verifyReceipt(host, request);
		}
		return response;
	}

	/**
	 * 检查凭据(自动检查是否是沙盒模式)
	 * 
	 * @param receiptData
	 *            前端给的支付凭据 base64
	 * @param password
	 *            可选
	 * @param excludeOldTransactions
	 *            可选
	 * @return
	 */
	public VerifyReceiptResponse verifyReceipt(VerifyReceiptRequest request) {
		VerifyReceiptResponse res = verifyReceipt(DEV_URL, request);
		if (res.isOperationModeError()) {// 这是一个测试环境下的订单或者收据无法通过身份验证。
			res = verifyReceipt(SANDBOX_URL, request);
		}
		return res;
	}
}
