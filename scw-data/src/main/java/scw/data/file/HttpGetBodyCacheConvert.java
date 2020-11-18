package scw.data.file;

import scw.core.Converter;
import scw.http.HttpUtils;

public class HttpGetBodyCacheConvert implements
		Converter<String, String> {

	public String convert(String url) {
		return HttpUtils.getHttpClient().get(String.class, url).getBody();
	}
}
