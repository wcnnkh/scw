package scw.tencent.wx;

import scw.aop.annotation.AopEnable;
import scw.oauth2.AccessToken;
import scw.security.Token;

@AopEnable(false)
public interface TokenFactory extends WXConstants{
	String getAppId();

	String getAppSecret();

	AccessToken getAccessToken();

	AccessToken getAccessToken(String type);

	Token getJsApiTicket();

	Token getTicket(String type);
}
