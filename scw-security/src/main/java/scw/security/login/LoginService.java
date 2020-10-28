package scw.security.login;

import scw.aop.annotation.AopEnable;
import scw.beans.annotation.AutoImpl;

@AutoImpl(DefaultLoginService.class)
@AopEnable(false)
public interface LoginService<T> {
	UserToken<T> getUserToken(String token);

	UserToken<T> getUserTokenByUid(T uid);

	UserToken<T> login(T uid);

	boolean cancelLogin(String token);

	boolean cancelLoginByUid(T uid);
	
	boolean verification(String token, T uid);
}
