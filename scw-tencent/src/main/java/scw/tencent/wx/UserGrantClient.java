package scw.tencent.wx;

import scw.oauth2.AccessToken;
import scw.oauth2.client.AuthorizationCodeClient;

public class UserGrantClient implements AuthorizationCodeClient {
	private final Configuration configuration;

	public UserGrantClient(Configuration configuration) {
		this.configuration = configuration;
	}

	public UserAccessToken refreshAccessToken(AccessToken accessToken, String scope) {
		return WeiXinUtils.refreshWebUserAccesstoken(configuration.getAppId(),
				accessToken.getRefreshToken().getToken());
	}

	public String getClientId() {
		return configuration.getAppId();
	}

	public UserAccessToken getAccessToken(String code, String redirect_uri) {
		return WeiXinUtils.getUserAccesstoken(configuration.getAppId(), configuration.getAppSecret(), code);
	}

	public Userinfo getUserinfo(String code, String redirect_uri) {
		return getUserinfo(code, redirect_uri, "zh_CN");
	}

	public Userinfo getUserinfo(String code, String redirect_uri, String lang) {
		UserAccessToken userAccessToken = getAccessToken(code, redirect_uri);
		return WeiXinUtils.getUserinfo(userAccessToken.getOpenid(), userAccessToken.getToken().getToken(), lang);
	}
}
