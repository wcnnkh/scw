package scw.security;

import java.io.Serializable;
import java.util.Date;

import scw.core.utils.StringUtils;

/**
 * 一个令牌的定义
 * 
 * @author shuchaowen
 *
 */
public class Token implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private final String token;
	private final int expiresIn;
	private final long createTime;
	private final boolean isNew;

	public Token(String token, int expiresIn) {
		this(token, expiresIn, System.currentTimeMillis(), true);
	}

	public Token(String token, int expiresIn, long createTime, boolean isNew) {
		this.token = token;
		this.expiresIn = expiresIn;
		this.createTime = createTime;
		this.isNew = isNew;
	}

	public String getToken() {
		return token;
	}

	/**
	 * 过期时间(秒)
	 * 
	 * @return
	 */
	public int getExpiresIn() {
		return Math.max(0, expiresIn);
	}

	/**
	 * 创建时间
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return Math.max(0, createTime);
	}

	/**
	 * 是否是新创建的
	 * 
	 * @return
	 */
	public boolean isNew() {
		return isNew;
	}
	
	public boolean isExpired(){
		return isExpired(0);
	}
	

	/**
	 * 是否已过期,如果expiresIn或createTime小于等于0那么始终返回false
	 * @param ahead 提前多久过期(秒)
	 * @return
	 */
	public boolean isExpired(int ahead) {
		if (expiresIn <= 0 || createTime <= 0) {
			return false;
		}

		return (System.currentTimeMillis() - createTime) > ((expiresIn - ahead) * 1000);
	}

	/**
	 * 克隆会将isNew设置为false
	 */
	@Override
	public Token clone() {
		return new Token(token, expiresIn, createTime, false);
	}

	@Override
	public String toString() {
		return "token=" + token + ", expiresIn=" + expiresIn + ", createTime=" + new Date(createTime) + ", isNew="
				+ isNew;
	}

	@Override
	public int hashCode() {
		return token.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof Token) {
			return StringUtils.equals(token, ((Token) obj).token);
		}

		return false;
	}
}
