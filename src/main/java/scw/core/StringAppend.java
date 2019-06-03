package scw.core;

import java.io.IOException;
import java.io.Serializable;

/**
 * 字符串拼接
 * 
 * @author shuchaowen
 *
 */
public final class StringAppend implements CharSequence, Appendable, Serializable {
	private static final long serialVersionUID = 1L;
	private char[] chars;
	private int count;

	public StringAppend() {
		this(16);
	}

	public StringAppend(int initCapacity) {
		this.chars = new char[initCapacity];
	}

	public StringAppend(String text) {
		this.chars = text.toCharArray();
		this.count = chars.length;
	}

	public StringAppend appendNull() {
		expandCapacity(count + 4);
		chars[count++] = 'n';
		chars[count++] = 'u';
		chars[count++] = 'l';
		chars[count++] = 'l';
		return this;
	}

	public StringAppend append(String text) {
		if (text == null) {
			return appendNull();
		}

		int len = text.length();
		if (len == 0) {
			return this;
		}

		expandCapacity(count + len);
		text.getChars(0, len, chars, count);
		count += len;
		return this;
	}

	public StringAppend append(Object value) {
		if (value == null) {
			return appendNull();
		}

		if (value instanceof String) {
			return append((String) value);
		} else if (value instanceof CharSequence) {
			return append((CharSequence) value);
		} else if (value instanceof StringBuilder) {
			StringBuilder sb = (StringBuilder) value;
			int len = sb.length();
			if (len == 0) {
				return this;
			}

			sb.getChars(0, len, chars, count);
			count += len;
			return this;
		} else if (value instanceof StringBuffer) {
			StringBuffer sb = (StringBuffer) value;
			int len = sb.length();
			if (len == 0) {
				return this;
			}

			sb.getChars(0, len, chars, count);
			count += len;
			return this;
		} else {
			return append(value.toString());
		}
	}

	public StringAppend append(String text, int start, int end) {
		if (text == null) {
			return appendNull();
		}

		if ((start < 0) || (start > end) || (end > text.length()))
			throw new IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + text.length());

		int len = end - start;
		expandCapacity(count + len);
		text.getChars(start, end, chars, count);
		count += len;
		return this;
	}

	private void expandCapacity(int minimumCapacity) {
		if (minimumCapacity > chars.length) {
			changeCapacity(minimumCapacity);
		}
	}

	public char[] toCharArray() {
		return chars.clone();
	}

	private void changeCapacity(int capacity) {
		if (capacity == chars.length) {
			return;
		}

		char[] newChars = new char[capacity];
		System.arraycopy(chars, 0, newChars, 0, chars.length);
		this.chars = newChars;
	}

	public void trimToSize() {
		changeCapacity(count);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		if (srcBegin < 0)
			throw new StringIndexOutOfBoundsException(srcBegin);
		if ((srcEnd < 0) || (srcEnd > count))
			throw new StringIndexOutOfBoundsException(srcEnd);
		if (srcBegin > srcEnd)
			throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
		System.arraycopy(chars, srcBegin, dst, dstBegin, srcEnd - srcBegin);
	}

	/**
	 * 清空
	 * 
	 */
	public void clear() {
		count = 0;
	}

	public StringAppend append(CharSequence sequence, int start, int end) {
		if (sequence == null) {
			return appendNull();
		}

		if ((start < 0) || (start > end) || (end > sequence.length()))
			throw new IndexOutOfBoundsException(
					"start " + start + ", end " + end + ", s.length() " + sequence.length());
		int len = end - start;
		expandCapacity(count + len);
		for (int i = start, j = count; i < end; i++, j++)
			chars[j] = sequence.charAt(i);
		count += len;
		return this;
	}

	public int capacity() {
		return chars.length;
	}

	public int length() {
		return count;
	}

	@Override
	public String toString() {
		return new String(chars, 0, count);
	}

	public CharSequence subSequence(int start, int end) {
		if (start < 0)
			throw new StringIndexOutOfBoundsException(start);
		if (end > count)
			throw new StringIndexOutOfBoundsException(end);
		if (start > end)
			throw new StringIndexOutOfBoundsException(end - start);
		return new String(chars, start, end - start);
	}

	public char charAt(int index) {
		if (index > count) {
			throw new IndexOutOfBoundsException(index + "");
		}

		return chars[index];
	}

	public StringAppend append(CharSequence csq) {
		if (csq == null) {
			return appendNull();
		}

		int len = csq.length();
		if (len == 0) {
			return this;
		}

		expandCapacity(count + len);
		for (int i = 0; i < csq.length(); i++, count++) {
			chars[count] = csq.charAt(i);
		}
		return this;
	}

	public Appendable append(char c) throws IOException {
		chars[count++] = c;
		return this;
	}
}
