package scw.value;

import java.math.BigDecimal;
import java.math.BigInteger;

import scw.core.utils.ClassUtils;
import scw.core.utils.ObjectUtils;
import scw.core.utils.StringUtils;

public abstract class AbstractStringValue extends SupportDefaultValue {
	public AbstractStringValue(Value defaultValue) {
		super(defaultValue);
	}

	public int getNumberRadix() {
		return 10;
	}

	protected String formatNumberText(String value) {
		return StringUtils.formatNumberText(value);
	}

	protected boolean isNumeric(String value) {
		return StringUtils.isNumeric(value);
	}

	public Byte getAsByte() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsByte();
		}
		return Byte.parseByte(v, getNumberRadix());
	}

	public byte getAsByteValue() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsByteValue();
		}

		return Byte.parseByte(v, getNumberRadix());
	}

	public Short getAsShort() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsShort();
		}
		return Short.parseShort(v, getNumberRadix());
	}

	public short getAsShortValue() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsShortValue();
		}
		return Short.parseShort(v, getNumberRadix());
	}

	public Integer getAsInteger() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsInteger();
		}
		return Integer.parseInt(v, getNumberRadix());
	}

	public int getAsIntValue() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsIntValue();
		}
		return Integer.parseInt(v, getNumberRadix());
	}

	public Long getAsLong() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsLong();
		}
		return Long.parseLong(v, getNumberRadix());
	}

	public long getAsLongValue() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsLongValue();
		}
		return Long.parseLong(v, getNumberRadix());
	}

	protected boolean parseBooleanValue(String value) {
		return StringUtils.parseBoolean(value);
	}

	public Boolean getAsBoolean() {
		String v = getAsString();
		if (!StringUtils.hasText(v)) {
			return getDefaultValue().getAsBoolean();
		}
		return parseBooleanValue(v);
	}

	public boolean getAsBooleanValue() {
		String v = getAsString();
		if (!StringUtils.hasText(v)) {
			return getDefaultValue().getAsBooleanValue();
		}
		return parseBooleanValue(v);
	}

	public Float getAsFloat() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsFloat();
		}
		return Float.parseFloat(v);
	}

	public float getAsFloatValue() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsFloatValue();
		}
		return Float.parseFloat(v);
	}

	public Double getAsDouble() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsDouble();
		}
		return Double.parseDouble(v);
	}

	public double getAsDoubleValue() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v) || !isNumeric(v)) {
			return getDefaultValue().getAsDoubleValue();
		}
		return Double.parseDouble(v);
	}

	public char getAsChar() {
		String v = getAsString();
		if (StringUtils.isEmpty(v)) {
			return getDefaultValue().getAsChar();
		}
		return v.charAt(0);
	}

	public Character getAsCharacter() {
		String v = getAsString();
		if (StringUtils.isEmpty(v)) {
			return getDefaultValue().getAsCharacter();
		}

		return v.charAt(0);
	}

	public BigInteger getAsBigInteger() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v)) {
			return getDefaultValue().getAsBigInteger();
		}

		try {
			return new BigInteger(v, getNumberRadix());
		} catch (NumberFormatException e) {
			return getDefaultValue().getAsBigInteger();
		}
	}

	public BigDecimal getAsBigDecimal() {
		String v = formatNumberText(getAsString());
		if (StringUtils.isEmpty(v)) {
			return getDefaultValue().getAsBigDecimal();
		}

		try {
			return new BigDecimal(v);
		} catch (NumberFormatException e) {
			return getDefaultValue().getAsBigDecimal();
		}
	}

	public Class<?> getAsClass() {
		String v = getAsString();
		if (StringUtils.isEmpty(v)) {
			return getDefaultValue().getAsClass();
		}

		try {
			return ClassUtils.forName(v);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Enum<?> getAsEnum(Class<?> enumType) {
		String v = getAsString();
		if (StringUtils.isEmpty(v)) {
			return getDefaultValue().getAsEnum(enumType);
		}
		return Enum.valueOf((Class<? extends Enum>) enumType, v);
	}

	public Number getAsNumber() {
		return getAsBigDecimal();
	}

	@Override
	public int hashCode() {
		String value = getAsString();
		return value == null ? super.hashCode() : value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof Value) {
			return ObjectUtils.nullSafeEquals(getAsString(), ((Value) obj).getAsString());
		}

		return super.equals(obj);
	}

	public boolean isNumber() {
		String value = getAsString();
		value = formatNumberText(value);
		if (StringUtils.isNotEmpty(value)) {
			return false;
		}

		if (isNumeric(value)) {
			return true;
		}

		if (value.lastIndexOf('e') != -1 || value.lastIndexOf('E') != -1) {
			try {
				new BigDecimal(value);
				return true;
			} catch (NumberFormatException e) {
			}
		}
		return false;
	}

	public boolean isEmpty() {
		return StringUtils.isEmpty(getAsString());
	}
}
