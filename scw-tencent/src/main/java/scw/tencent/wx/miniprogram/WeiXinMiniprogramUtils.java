package scw.tencent.wx.miniprogram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import scw.compatible.CompatibleUtils;
import scw.core.Constants;
import scw.json.JSONUtils;
import scw.json.JsonObject;
import scw.tencent.wx.WeiXinUtils;
import scw.util.Base64;

public final class WeiXinMiniprogramUtils {
	private WeiXinMiniprogramUtils() {
	};

	public static AddTemplateResponse addTemplate(String access_token, String id, Collection<Integer> keyword_id_list) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("keyword_id_list", keyword_id_list);
		JsonObject json = WeiXinUtils
				.doPost("https://api.weixin.qq.com/cgi-bin/wxopen/template/add?access_token=" + access_token, map);
		return new AddTemplateResponse(json);
	}

	/**
	 * 创建被分享动态消息的 activity_id
	 * 
	 * @author shuchaowen
	 *
	 */
	public static CreateActivityIdResponse createActivityId(String access_token) {
		JsonObject json = WeiXinUtils.doPost(
				"https://api.weixin.qq.com/cgi-bin/message/wxopen/activityid/create?access_token=" + access_token,
				null);
		return new CreateActivityIdResponse(json);
	}

	public static BaseResponse deleteTemplate(String access_token, String template_id) {
		Map<String, String> map = new HashMap<String, String>(2, 1);
		map.put("template_id", template_id);
		JsonObject json = WeiXinUtils
				.doPost("https://api.weixin.qq.com/cgi-bin/wxopen/template/del?access_token=" + access_token, map);
		return new BaseResponse(json);
	}

	public static GetTemplateLibraryByIdResponse getTemplateLibraryById(String access_token, String id) {
		Map<String, String> map = new HashMap<String, String>(2, 1);
		map.put("id", id);
		JsonObject json = WeiXinUtils.doPost(
				"https://api.weixin.qq.com/cgi-bin/wxopen/template/library/get?access_token=" + access_token, map);
		return new GetTemplateLibraryByIdResponse(json);
	}

	public static GetTemplateLibraryListResponse getTemplateLibraryList(String access_token, int offset, int count) {
		Map<String, Object> map = new HashMap<String, Object>(2, 1);
		map.put("offset", offset);
		map.put("count", count);
		JsonObject json = WeiXinUtils.doPost(
				"https://api.weixin.qq.com/cgi-bin/wxopen/template/library/list?access_token=" + access_token, map);
		return new GetTemplateLibraryListResponse(json);
	}

	/**
	 * 
	 * @param access_token
	 * @param offset
	 *            用于分页，表示从offset开始。从 0 开始计数。
	 * @param count
	 *            用于分页，表示拉取count条记录。最大为 20。最后一页的list长度可能小于请求的count。
	 */
	public static GetTemplateListResponse getTemplateList(String access_token, int offset, int count) {
		Map<String, Object> map = new HashMap<String, Object>(2, 1);
		map.put("offset", offset);
		map.put("count", count);
		JsonObject json = WeiXinUtils
				.doPost("https://api.weixin.qq.com/cgi-bin/wxopen/template/list?access_token=" + access_token, map);
		return new GetTemplateListResponse(json);
	}

	/**
	 * 发送模板消息
	 * 
	 * @param access_token
	 * @param touser
	 *            接收者（用户）的 openid
	 * @param weappTemplateMsg
	 *            属性 类型 必填 说明 template_id string 是 所需下发的模板消息的id page string 否
	 *            点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
	 *            form_id string 是 表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的
	 *            prepay_id data string 否 模板内容，不填则下发空模板 emphasis_keyword string
	 *            否 模板需要放大的关键词，不填则默认无放大
	 */
	public static BaseResponse sendTemplateMessage(String access_token, String touser,
			WeappTemplateMsg weappTemplateMsg) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("touser", touser);
		map.put("template_id", weappTemplateMsg.getTemplate_id());
		map.put("page", weappTemplateMsg.getPage());
		map.put("form_id", weappTemplateMsg.getForm_id());
		map.put("data", weappTemplateMsg.getData());
		map.put("emphasis_keyword", weappTemplateMsg.getEmphasis_keyword());
		JsonObject json = WeiXinUtils.doPost(
				"https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=" + access_token, map);
		return new BaseResponse(json);
	}

	/**
	 * @param access_token
	 * @param activity_id
	 *            动态消息的 ID，通过 createActivityId 接口获取
	 * @param target_state
	 *            动态消息修改后的状态（具体含义见后文）
	 * @param parameter_list
	 *            动态消息对应的模板信息
	 */
	public static BaseResponse setUpdatableMsg(String access_token, String activity_id, TargetState target_state,
			EnumMap<TemplateParameterName, String> parameter_list) {
		Map<String, Object> map = new HashMap<String, Object>(4, 1);
		map.put("activity_id", activity_id);
		map.put("target_state", target_state.getState());

		Map<String, Object> template_info = new HashMap<String, Object>(2);
		List<Object> list = new ArrayList<Object>();
		for (Entry<TemplateParameterName, String> entry : parameter_list.entrySet()) {
			Map<String, Object> json = new HashMap<String, Object>(2, 1);
			json.put("name", entry.getKey().name());
			json.put("value", entry.getValue());
			list.add(json);
		}
		template_info.put("parameter_list", list);
		map.put("template_info", template_info);
		JsonObject json = WeiXinUtils.doPost(
				"https://api.weixin.qq.com/cgi-bin/message/wxopen/updatablemsg/send?access_token=" + access_token, map);
		return new BaseResponse(json);
	}

	public static Session code2session(String appId, String appSecret, String js_code) {
		StringBuilder sb = new StringBuilder("https://api.weixin.qq.com/sns/jscode2session");
		sb.append("?appid=").append(appId);
		sb.append("&secret=").append(appSecret);
		sb.append("&js_code=").append(js_code);
		sb.append("&grant_type=authorization_code");
		JsonObject json = WeiXinUtils.doGet(sb.toString());
		return new Session(json);
	}

	public static PhoneNumber decrypt(String encryptedData, String key, String iv) {
		byte[] dataBytes = Base64.decode(encryptedData);
		byte[] keyBytes = Base64.decode(key);
		byte[] ivKeys = Base64.decode(iv);
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(ivKeys));
			byte[] data = cipher.doFinal(dataBytes);
			String content = CompatibleUtils.getStringOperations().createString(data, Constants.UTF_8_NAME).trim();
			return JSONUtils.getJsonSupport().parseObject(content, PhoneNumber.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
