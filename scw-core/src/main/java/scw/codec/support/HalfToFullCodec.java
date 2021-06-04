package scw.codec.support;

import scw.codec.Codec;
import scw.codec.DecodeException;
import scw.codec.EncodeException;

/**
 * 半角和全角的相互转换
 * 
 * @author shuchaowen
 *
 */
public class HalfToFullCodec implements Codec<String, String> {
	public static final HalfToFullCodec DEFAULT = new HalfToFullCodec();

	@Override
	public String encode(String source) throws EncodeException {
		char c[] = source.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);

			}
		}
		return new String(c);
	}

	@Override
	public String decode(String source) throws DecodeException {
		char c[] = source.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '\u3000') {
				c[i] = ' ';
			} else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
				c[i] = (char) (c[i] - 65248);

			}
		}
		return new String(c);
	}

}
