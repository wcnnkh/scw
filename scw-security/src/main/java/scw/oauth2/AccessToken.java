package scw.oauth2;

import java.io.Serializable;

import scw.security.Token;

public class AccessToken implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private final Token token;
	private final TokenType tokenType;
	private final Token refreshToken;
	private final String scope;
	private final String state;

	public AccessToken(AccessToken accessToken) {
		this.token = accessToken.token;
		this.tokenType = accessToken.tokenType;
		this.refreshToken = accessToken.refreshToken;
		this.scope = accessToken.scope;
		this.state = accessToken.state;
	}

	public AccessToken(Token accessToken, TokenType tokenType, Token refreshToken, String scope, String state) {
		this.token = accessToken;
		this.tokenType = tokenType;
		this.refreshToken = refreshToken;
		this.scope = scope;
		this.state = state;
	}

	public Token getToken() {
		return token;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public Token getRefreshToken() {
		return refreshToken;
	}

	public String getScope() {
		return scope;
	}

	public String getState() {
		return state;
	}

	@Override
	public AccessToken clone() {
		return new AccessToken(token.clone(), tokenType, refreshToken.clone(), scope, state);
	}

	@Override
	public String toString() {
		return "accessToken=[" + token + "], tokenType=[" + tokenType + "], refreshToken=[" + refreshToken
				+ "], scope=" + scope + ", state=" + state;
	}
}
