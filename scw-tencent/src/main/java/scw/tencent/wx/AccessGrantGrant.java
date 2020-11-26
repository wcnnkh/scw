package scw.tencent.wx;

import scw.data.TemporaryCache;
import scw.oauth2.AccessToken;
import scw.oauth2.client.AbstractCredentialsClient;
import scw.security.Token;

public class AccessGrantGrant extends AbstractCredentialsClient {
	private final Configuration configuration;

	public AccessGrantGrant(TemporaryCache temporaryCache, Configuration configuration) {
		super(temporaryCache);
		this.configuration = configuration;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	protected String getTemporaryCacheKey(String scope) {
		return "wx:" + configuration.getAppId() + "&" + scope;
	}

	@Override
	public AccessToken getNewAccessToken(String scope) {
		return WeiXinUtils.getAccessToken(configuration.getAppId(), configuration.getAppSecret());
	}

	public Token getTicket(String scope, String type) {
		if (temporaryCache == null) {
			return WeiXinUtils.getTicket(getAccessToken(scope).getToken().getToken(), type);
		}

		String key = "wx_ticket:" + configuration.getAppId() + "&" + scope + "&" + type;
		Token token = temporaryCache.get(key);
		if (token == null || token.isExpired()) {
			token = WeiXinUtils.getTicket(getAccessToken(scope).getToken().getToken(), type);
			temporaryCache.set(key, token.getExpiresIn(), token.clone());
		}
		return token;
	}

	public Token getJsApiTicket(String scope) {
		return getTicket(scope, "jsapi");
	}
}
