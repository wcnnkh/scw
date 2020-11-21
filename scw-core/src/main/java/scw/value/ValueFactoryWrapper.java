package scw.value;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

public class ValueFactoryWrapper<K> implements ValueFactory<K> {
	private final ValueFactory<K> target;

	public ValueFactoryWrapper(ValueFactory<K> target) {
		this.target = target;
	}

	public Value getValue(K key) {
		return target.getValue(key);
	}

	public Byte getByte(K key) {
		return target.getByte(key);
	}

	public byte getByteValue(K key) {
		return target.getByteValue(key);
	}

	public Short getShort(K key) {
		return target.getShort(key);
	}

	public short getShortValue(K key) {
		return target.getShortValue(key);
	}

	public Integer getInteger(K key) {
		return target.getInteger(key);
	}

	public int getIntValue(K key) {
		return target.getIntValue(key);
	}

	public Long getLong(K key) {
		return target.getLong(key);
	}

	public long getLongValue(K key) {
		return target.getLongValue(key);
	}

	public Boolean getBoolean(K key) {
		return target.getBoolean(key);
	}

	public boolean getBooleanValue(K key) {
		return target.getBooleanValue(key);
	}

	public Float getFloat(K key) {
		return target.getFloat(key);
	}

	public float getFloatValue(K key) {
		return target.getFloatValue(key);
	}

	public Double getDouble(K key) {
		return target.getDouble(key);
	}

	public double getDoubleValue(K key) {
		return target.getDoubleValue(key);
	}

	public char getChar(K key) {
		return target.getChar(key);
	}

	public Character getCharacter(K key) {
		return target.getCharacter(key);
	}

	public String getString(K key) {
		return target.getString(key);
	}

	public BigInteger getBigInteger(K key) {
		return target.getBigInteger(key);
	}

	public BigDecimal getBigDecimal(K key) {
		return target.getBigDecimal(key);
	}

	public Number getNumber(K key) {
		return target.getNumber(key);
	}

	public Class<?> getClass(K key) {
		return target.getClass(key);
	}

	public Enum<?> getEnum(K key, Class<?> enumType) {
		return target.getEnum(key, enumType);
	}

	public <T> T getObject(K key, Class<? extends T> type) {
		return target.getObject(key, type);
	}

	public Object getObject(K key, Type type) {
		return target.getObject(key, type);
	}

	public Object getValue(K key, Type type, Object defaultValue) {
		return target.getValue(key, type, defaultValue);
	}

	public <T> T getValue(K key, Class<? extends T> type, T defaultValue) {
		return target.getValue(key, type, defaultValue);
	}

	@Override
	public int hashCode() {
		return target.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		return target.equals(obj);
	}

	@Override
	public String toString() {
		return target.toString();
	}
}
