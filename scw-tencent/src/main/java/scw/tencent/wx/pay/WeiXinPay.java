package scw.tencent.wx.pay;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import scw.aop.annotation.AopEnable;
import scw.core.Constants;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.http.HttpUtils;
import scw.http.MediaType;
import scw.io.Resource;
import scw.io.ResourceUtils;
import scw.json.JSONUtils;
import scw.json.JsonObject;
import scw.lang.NestedRuntimeException;
import scw.lang.NotSupportedException;
import scw.lang.ParameterException;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.mapper.MapperUtils;
import scw.net.MimeTypeUtils;
import scw.net.ssl.SSLContexts;
import scw.security.SignatureUtils;
import scw.tencent.wx.WeiXinException;
import scw.tencent.wx.WeiXinUtils;
import scw.util.RandomUtils;
import scw.xml.XMLUtils;

@AopEnable(false)
public class WeiXinPay {
	private static Logger logger = LoggerFactory.getLogger(WeiXinPay.class);

	private static final String weixin_unifiedorder_url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	private static final String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	private static final String CLOSEORDER_URL = "https://api.mch.weixin.qq.com/pay/closeorder";
	private static final String ORDER_QUERY = "https://api.mch.weixin.qq.com/pay/orderquery";
	private static final String SENDREDPACK = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
	private static final String SENDGROUPREDPACK = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendgroupredpack";
	private static final String GETHBINFO = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gethbinfo";

	private final String appId;
	private final String mch_id;
	private final String apiKey;
	private SignType signType;
	private String charsetName = Constants.UTF_8.name();
	private String notifyUrl;
	private String certTrustFile;
	private SSLSocketFactory sslSocketFactory;

	public WeiXinPay(String appId, String mch_id, String apiKey) {
		this(appId, mch_id, apiKey, null);
	}

	public WeiXinPay(String appId, String mch_id, String apiKey, String certTrustFile) {
		this.appId = appId;
		this.mch_id = mch_id;
		this.apiKey = apiKey;
		this.certTrustFile = certTrustFile;
		this.sslSocketFactory = initSSLSocketFactory(certTrustFile);
	}

	public final SignType getSignType() {
		return signType;
	}

	public void setSignType(SignType signType) {
		this.signType = signType;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	private SSLSocketFactory initSSLSocketFactory(String certTrustFile) {
		if (StringUtils.isEmpty(certTrustFile)) {
			return null;
		}

		Resource resource = ResourceUtils.getResourceOperations().getResource(certTrustFile);
		if (!resource.exists()) {
			return null;
		}

		InputStream is = ResourceUtils.getInputStream(resource);
		char[] password = mch_id.toCharArray();
		try {
			return SSLContexts.custom().loadKeyMaterial(is, password, password).build().getSocketFactory();
		} catch (Exception e) {
			throw new NestedRuntimeException(certTrustFile, e);
		}
	}

	public String getAppId() {
		return appId;
	}

	public String getMch_id() {
		return mch_id;
	}

	public String getApiKey() {
		return apiKey;
	}

	public final String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	/**
	 * 检查签名
	 * 
	 * @param params
	 * @return
	 */
	public boolean checkSign(Map<String, String> params) {
		Map<String, String> cloneParams = new HashMap<String, String>(params);
		String sign = cloneParams.get("sign");
		if (sign == null) {
			return false;
		}

		cloneParams.remove("sign");
		StringBuilder checkStr = SignatureUtils.formatSortParams(cloneParams);
		checkStr.append("&key=").append(apiKey);

		String mySign = toSign(getSignType(cloneParams), checkStr.toString());
		boolean b = sign.equals(mySign);
		if (!b) {
			logger.error("签名检验失败：{}------>{}", JSONUtils.toJSONString(params), mySign);
		}
		return b;
	}

	private SignType getSignType(Map<String, ?> params) {
		SignType signTypeToUse = signType;
		if (params.containsKey("sign_type")) {
			signTypeToUse = SignType.valueOf(params.get("sign_type").toString());
		}
		return signTypeToUse;
	}

	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	/**
	 * 向微信支付服务器发送请求
	 * 
	 * @param url
	 * @param parameterMap
	 * @param isCertTrustFile
	 *            请求中是否包含证书
	 * @return
	 */
	public WeiXinPayResponse invoke(String url, Map<String, ?> parameterMap, boolean isCertTrustFile) {
		if (isCertTrustFile && StringUtils.isEmpty(certTrustFile)) {
			throw new ParameterException("未配置API证书目录");
		}

		Map<String, Object> params = new HashMap<String, Object>();
		if (!CollectionUtils.isEmpty(parameterMap)) {
			params.putAll(parameterMap);
		}

		if (!params.containsKey("nonce_str")) {
			params.put("nonce_str", RandomUtils.getRandomStr(10));
		}

		if (!params.containsKey("wxappid") && !params.containsKey("appid")) {
			params.put("appid", appId);
		}

		if (!params.containsKey("mch_id")) {
			params.put("mch_id", mch_id);
		}

		SignType signTypeToUse = getSignType(params);
		if (signTypeToUse != null) {
			params.put("sign_type", signType.getValue());
		}

		String[] keys = params.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		StringBuilder sb = new StringBuilder();
		Document document = XMLUtils.newDocumentBuilder().newDocument();
		Element element = document.createElement("xml");
		for (int i = 0; i < keys.length; i++) {
			String k = keys[i];
			if (k == null) {
				continue;
			}

			Object v = params.get(k);
			if (v == null) {
				continue;
			}

			String value = v.toString();
			Element c = document.createElement(k);
			c.setTextContent(value);
			element.appendChild(c);
			if (sb.length() > 0) {
				sb.append("&");
			}

			sb.append(k).append("=").append(value);
		}
		sb.append("&key=").append(apiKey);
		logger.debug("微信支付签名字符串：{}", sb);
		String sign = toSign(signTypeToUse, sb.toString());
		Element c = document.createElement("sign");
		c.setTextContent(sign);
		element.appendChild(c);
		final String content = XMLUtils.toString(element);

		logger.debug("微信支付请求xml内容:{}", content);

		String res = HttpUtils.getHttpClient().post(String.class, url, sslSocketFactory, content,
				new MediaType(MimeTypeUtils.APPLICATION_XML, charsetName)).getBody();
		if (res == null) {
			throw new RuntimeException("请求：" + url + "失败");
		}

		logger.debug("请求：{}，返回{}", url, res);

		Map<String, String> map = XMLUtils.xmlToMap(res);
		JsonObject jsonObject = JSONUtils.parseObject(JSONUtils.toJSONString(map));
		return new WeiXinPayResponse(jsonObject);
	}

	protected String toSign(SignType signType, String str) {
		if (signType == null || signType == SignType.MD5) {
			return SignatureUtils.md5(str, charsetName).toUpperCase();
		}
		throw new NotSupportedException("不支持的签名方式:" + signType);
	}

	/**
	 * 统一下单接口
	 * 
	 * @param request
	 * @return
	 */
	public UnifiedorderResponse getUnifiedorder(UnifiedorderRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("device_info", request.getDevice_info());
		if (request.getBody().length() >= 128) {
			map.put("body", request.getBody().substring(0, 120) + "...");
		} else {
			map.put("body", request.getBody());
		}

		map.put("detail", request.getDetail());
		map.put("attach", request.getAttach());
		map.put("out_trade_no", request.getOut_trade_no());
		map.put("fee_type", request.getFee_type());
		map.put("total_fee", request.getTotal_fee() + "");
		map.put("spbill_create_ip", request.getSpbill_create_ip());
		map.put("time_start", request.getTime_start());
		map.put("time_expire", request.getTime_expire());
		map.put("goods_tag", request.getGoods_tag());
		map.put("notify_url", StringUtils.isEmpty(request.getNotify_url()) ? getNotifyUrl() : request.getNotify_url());
		map.put("trade_type", request.getTrade_type());
		map.put("product_id", request.getProduct_id());
		map.put("limit_pay", request.getLimit_pay());
		map.put("openid", request.getOpenid());
		WeiXinPayResponse response = invoke(weixin_unifiedorder_url, map, false);
		return new UnifiedorderResponse(response);
	}

	public Unifiedorder payment(UnifiedorderRequest request) {
		UnifiedorderResponse response = getUnifiedorder(request);
		return payment(response);
	}

	public Unifiedorder payment(UnifiedorderResponse response) {
		if (!response.isReturnSuccess()) {
			throw new WeiXinException(response.getReturnMsg());
		}

		if (!response.isResultSuccess()) {
			throw new WeiXinException(response.getResultErrCodeDes());
		}

		long timestamp = System.currentTimeMillis() / 1000;
		String prepay_id = response.getPrepayId();
		Unifiedorder unifiedorder = new Unifiedorder();
		unifiedorder.setTimestamp(timestamp);
		unifiedorder.setNonce_str(response.getNonceStr());
		if (response.getTradeType() == TradeType.JSAPI || response.getTradeType() == TradeType.MWEB) {
			unifiedorder.setPaySign(WeiXinUtils.getBrandWCPayRequestSign(appId, apiKey,
					String.valueOf(unifiedorder.getTimestamp()), unifiedorder.getNonce_str(), prepay_id));
		} else {
			unifiedorder.setPaySign(WeiXinUtils.getAppPayRequestSign(appId, mch_id, apiKey, unifiedorder.getTimestamp(),
					unifiedorder.getNonce_str(), prepay_id));
		}
		unifiedorder.setPrepay_id(prepay_id);
		return unifiedorder;
	}

	/**
	 * 退款
	 * 
	 * @param request
	 * @return
	 */
	public WeiXinPayResponse refund(RefundRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("transaction_id", request.getTransaction_id());
		map.put("out_trade_no", request.getOut_trade_no());
		map.put("out_refund_no", request.getOut_refund_no());
		map.put("total_fee", request.getTotal_fee() + "");
		map.put("refund_fee", request.getRefund_fee() + "");
		map.put("refund_fee_type", request.getRefund_fee_type());
		map.put("refund_desc", request.getRefund_desc());
		map.put("notify_url",
				StringUtils.isNotEmpty(request.getNotify_url()) ? getNotifyUrl() : request.getNotify_url());
		return invoke(REFUND_URL, map, true);
	}

	/**
	 * 关闭订单
	 * 
	 * @param out_trade_no
	 *            商户订单号
	 * @return
	 */
	public WeiXinPayResponse closeorder(String out_trade_no) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("out_trade_no", out_trade_no);
		return invoke(CLOSEORDER_URL, map, false);
	}

	/**
	 * 两个参数选择其中一个
	 * 
	 * @param transactionId
	 *            微信的订单号，优先使用
	 * @param outTradeNo
	 *            商户系统内部的订单号，当没提供transaction_id时需要传这个。
	 * @return
	 */
	public OrderQueryResponse orderQuery(String transactionId, String outTradeNo) {
		Map<String, String> map = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(transactionId)) {
			map.put("out_trade_no", outTradeNo);
		}

		if (StringUtils.isNotEmpty(outTradeNo)) {
			map.put("transaction_id", transactionId);
		}
		WeiXinPayResponse response = invoke(ORDER_QUERY, map, false);
		return new OrderQueryResponse(response);
	}

	/**
	 * 现金红包发放后会以公众号消息的形式触达用户，不同情况下触达消息的形式会有差别，相关规则如下：<br/>
	 * 1.已关注公众号的用户，使用“防伪消息”触达；<br/>
	 * 2.未关注公众号的用户，使用“模板消息”触达。
	 * 
	 * {@link https://pay.weixin.qq.com/wiki/doc/api/tools/cash_coupon.php?chapter=13_4&index=3}
	 * 
	 * @param request
	 * @return
	 */
	public SendredpackResponse sendredpack(SendredpackRequest request) {
		Map<String, Object> parameter = MapperUtils.getMapper().getFieldValueMap(request);
		WeiXinPayResponse response = invoke(SENDREDPACK, parameter, true);
		return new SendredpackResponse(response);
	}

	/**
	 * 裂变红包：一次可以发放一组红包。首先领取的用户为种子用户，种子用户领取一组红包当中的一个，并可以通过社交分享将剩下的红包给其他用户。裂变红包充分利用了人际传播的优势。
	 * {@link https://pay.weixin.qq.com/wiki/doc/api/tools/cash_coupon.php?chapter=13_5&index=4}
	 * 
	 * @param request
	 * @return
	 */
	public SendredpackResponse sendgroupredpack(SendgroupredpackRequest request) {
		Map<String, Object> parameter = MapperUtils.getMapper().getFieldValueMap(request);
		WeiXinPayResponse response = invoke(SENDGROUPREDPACK, parameter, true);
		return new SendredpackResponse(response);
	}

	public GethbinfoResponse gethbinfo(GethbinfoRequest request) {
		Map<String, Object> parameter = MapperUtils.getMapper().getFieldValueMap(request);
		WeiXinPayResponse response = invoke(GETHBINFO, parameter, true);
		return new GethbinfoResponse(response);
	}
}