package scw.tencent.wx.miniprogram;

import java.util.Collection;
import java.util.EnumMap;

import scw.tencent.wx.TokenFactory;

/**
 * 微信小程序实现
 * 
 * @author shuchaowen
 *
 */
public class WeixinMiniprogram {
	private final TokenFactory tokenFactory;

	public WeixinMiniprogram(TokenFactory tokenFactory) {
		this.tokenFactory = tokenFactory;
	}

	public final String getAppId() {
		return tokenFactory.getAppId();
	}

	public final String getAppSecret() {
		return tokenFactory.getAppSecret();
	}

	public final String getAccessToken() {
		return tokenFactory.getAccessToken().getToken().getToken();
	}

	public Session code2session(String js_code) {
		return WeiXinMiniprogramUtils.code2session(getAppId(), getAppSecret(), js_code);
	}

	/**
	 * 组合模板并添加至帐号下的个人模板库
	 * 
	 * @param access_token
	 *            接口调用凭证
	 * @param id
	 *            模板标题id，可通过接口获取，也可登录小程序后台查看获取
	 * @param keyword_id_list
	 *            开发者自行组合好的模板关键词列表，关键词顺序可以自由搭配（例如[3,5,4]或[4,5,3]），最多支持10个关键词组合
	 */
	public AddTemplateResponse addTemplate(String id, Collection<Integer> keyword_id_list) {
		return WeiXinMiniprogramUtils.addTemplate(getAccessToken(), id, keyword_id_list);
	}

	/**
	 * 创建被分享动态消息的 activity_id
	 * 
	 * @author shuchaowen
	 *
	 */
	public CreateActivityIdResponse createActivityId() {
		return WeiXinMiniprogramUtils.createActivityId(getAccessToken());
	}

	public BaseResponse deleteTemplate(String template_id) {
		return WeiXinMiniprogramUtils.deleteTemplate(getAccessToken(), template_id);
	}

	public GetTemplateLibraryByIdResponse getTemplateLibraryById(String id) {
		return WeiXinMiniprogramUtils.getTemplateLibraryById(getAccessToken(), id);
	}

	public GetTemplateLibraryListResponse getTemplateLibraryList(int offset, int count) {
		return WeiXinMiniprogramUtils.getTemplateLibraryList(getAccessToken(), offset, count);
	}

	/**
	 * 
	 * @param offset
	 *            用于分页，表示从offset开始。从 0 开始计数。
	 * @param count
	 *            用于分页，表示拉取count条记录。最大为 20。最后一页的list长度可能小于请求的count。
	 */
	public GetTemplateListResponse getTemplateList(String access_token, int offset, int count) {
		return WeiXinMiniprogramUtils.getTemplateList(getAccessToken(), offset, count);
	}

	/**
	 * 发送模板消息
	 * 
	 * @param touser
	 *            接收者（用户）的 openid
	 * @param weappTemplateMsg
	 *            属性 类型 必填 说明 template_id string 是 所需下发的模板消息的id page string 否
	 *            点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
	 *            form_id string 是 表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的
	 *            prepay_id data string 否 模板内容，不填则下发空模板 emphasis_keyword string
	 *            否 模板需要放大的关键词，不填则默认无放大
	 */
	public BaseResponse sendTemplateMessage(String touser, WeappTemplateMsg weappTemplateMsg) {
		return WeiXinMiniprogramUtils.sendTemplateMessage(getAccessToken(), touser, weappTemplateMsg);
	}

	/**
	 * @param activity_id
	 *            动态消息的 ID，通过 createActivityId 接口获取
	 * @param target_state
	 *            动态消息修改后的状态（具体含义见后文）
	 * @param parameter_list
	 *            动态消息对应的模板信息
	 */
	public BaseResponse setUpdatableMsg(String activity_id, TargetState target_state,
			EnumMap<TemplateParameterName, String> parameter_list) {
		return WeiXinMiniprogramUtils.setUpdatableMsg(getAccessToken(), activity_id, target_state, parameter_list);
	}
}
