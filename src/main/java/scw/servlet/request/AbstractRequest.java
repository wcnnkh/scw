package scw.servlet.request;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.core.utils.StringParseUtils;
import scw.core.utils.StringUtils;
import scw.servlet.Request;
import scw.servlet.beans.RequestBeanFactory;
import scw.servlet.service.RestService;

public class AbstractRequest extends Request {
	private static final Charset GET_DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
	private final boolean cookieValue;

	public AbstractRequest(RequestBeanFactory requestBeanFactory, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, boolean isDebug, boolean cookieValue) throws IOException {
		super(requestBeanFactory, httpServletRequest, httpServletResponse, isDebug);
		this.cookieValue = cookieValue;
	}

	public String getRequireValue(String key) {
		String v = getValue(key);
		if (isNull(v)) {
			throw new NullPointerException("require '" + key + "'");
		}
		return v;
	}

	@SuppressWarnings("unchecked")
	public String getValue(String key, boolean cookie) {
		String v = getParameter(key);
		if (v == null) {
			Map<String, String> restParameterMap = (Map<String, String>) getAttribute(
					RestService.RESTURL_PATH_PARAMETER);
			if (restParameterMap != null) {
				v = restParameterMap.get(key);
			}
		}

		if (v == null && cookie) {
			Cookie c = getCookie(key, false);
			if (c != null) {
				v = c.getValue();
			}
		}

		if (isNull(v)) {
			return null;
		}

		if ("GET".equals(getMethod())) {
			v = decodeGETParameter(v);
		}
		return v;
	}

	public String getValue(String key) {
		return getValue(key, isCookieValue());
	}

	public boolean isCookieValue() {
		return cookieValue;
	}

	public String decodeGETParameter(String value) {
		if (StringUtils.containsChinese(value)) {
			return value;
		}
		return new String(value.getBytes(GET_DEFAULT_CHARSET), Charset.forName(getCharacterEncoding()));
	}

	public String getString(String key) {
		return getValue(key);
	}

	public Byte getByte(String key) {
		String v = getValue(key);
		return StringUtils.isNull(v) ? null : Byte.valueOf(v);
	}

	public byte getByteValue(String key) {
		String v = getRequireValue(key);
		return Byte.parseByte(v);
	}

	public Short getShort(String key) {
		String v = getValue(key);
		if (isNull(v)) {
			return null;
		}

		return Short.valueOf(StringParseUtils.formatNumberText(v));
	}

	public short getShortValue(String key) {
		String v = getRequireValue(key);

		return Short.parseShort(StringParseUtils.formatNumberText(v));
	}

	public Integer getInteger(String key) {
		String str = getValue(key);
		return isNull(str) ? null : Integer.parseInt(StringParseUtils.formatNumberText(str));
	}

	public int getIntValue(String key) {
		String v = getRequireValue(key);

		return Integer.parseInt(StringParseUtils.formatNumberText(v));
	}

	public Long getLong(String key) {
		String v = getValue(key);
		return isNull(v) ? null : Long.valueOf(StringParseUtils.formatNumberText(v));
	}

	public long getLongValue(String key) {
		String v = getRequireValue(key);

		return Long.parseLong(StringParseUtils.formatNumberText(v));
	}

	public Boolean getBoolean(String key) {
		String v = getValue(key);
		if (StringUtils.isEmpty(v)) {
			return null;
		}

		return StringParseUtils.parseBoolean(v);
	}

	public boolean getBooleanValue(String key) {
		String v = getRequireValue(key);
		return StringParseUtils.parseBoolean(v);
	}

	public Float getFloat(String key) {
		String v = getValue(key);
		return isNull(v) ? null : Float.valueOf(StringParseUtils.formatNumberText(v));
	}

	public float getFloatValue(String key) {
		String v = getRequireValue(key);
		return Float.parseFloat(StringParseUtils.formatNumberText(v));
	}

	public Double getDouble(String key) {
		String v = getValue(key);
		return isNull(v) ? null : Double.valueOf(StringParseUtils.formatNumberText(v));
	}

	public double getDoubleValue(String key) {
		String v = getRequireValue(key);
		return Double.parseDouble(StringParseUtils.formatNumberText(v));
	}

	@Override
	public char getChar(String key) {
		String v = getRequireValue(key);
		return v.charAt(0);
	}

	@Override
	public Character getCharacter(String key) {
		String v = getValue(key);
		return isNull(v) ? null : v.charAt(0);
	}

	public boolean isNull(String value) {
		return StringUtils.isEmpty(value);
	}
}