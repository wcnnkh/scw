package scw.json;

public interface JSONParseSupport {
	String toJSONString(Object obj);
	
	JSONArray parseArray(String text);

	JSONObject parseObject(String text);

	<T> T parseObject(String text, Class<T> type);
}