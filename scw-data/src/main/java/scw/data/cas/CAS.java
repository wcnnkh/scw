package scw.data.cas;

import java.io.Serializable;

public class CAS<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	private final long cas;
	private final T value;

	public CAS(long cas, T value) {
		this.cas = cas;
		this.value = value;
	}

	public long getCas() {
		return cas;
	}

	public T getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (getCas() ^ (getCas() >>> 32));
		result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		CAS<T> other = (CAS<T>) obj;
		if (getCas() != other.getCas()) {
			return false;
		}

		if (getValue() == null) {
			if (other.getValue() != null) {
				return false;
			}
		} else if (!getValue().equals(other.getValue())) {
			return false;
		}
		return true;
	}
}
