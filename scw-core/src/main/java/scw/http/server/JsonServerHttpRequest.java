package scw.http.server;

import java.io.BufferedReader;
import java.io.IOException;

import scw.io.IOUtils;
import scw.json.EmptyJsonElement;
import scw.json.JSONSupport;
import scw.json.JSONUtils;
import scw.json.JsonArray;
import scw.json.JsonElement;
import scw.json.JsonObject;
import scw.logger.Logger;
import scw.logger.LoggerUtils;

/**
 * 一个json请求
 * 
 * @author shuchaowen
 *
 */
public class JsonServerHttpRequest extends CachingServerHttpRequest {
	private static Logger logger = LoggerUtils.getLogger(JsonServerHttpRequest.class);
	private JSONSupport jsonSupport;

	public JsonServerHttpRequest(ServerHttpRequest targetRequest) throws IOException {
		super(targetRequest);
	}

	public JSONSupport getJsonSupport() {
		return jsonSupport == null ? JSONUtils.getJsonSupport() : jsonSupport;
	}

	public void setJsonSupport(JSONSupport jsonSupport) {
		this.jsonSupport = jsonSupport;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getTarget(Class<T> targetType) {
		T target = super.getTarget(targetType);
		if(target == null){
			Object json = getJson();
			if(targetType.isInstance(json)){
				return (T) json;
			}
		}
		return null;
	}

	private Object json;
	public Object getJson() {
		if(json == null){
			BufferedReader reader;
			String text = null;
			try {
				reader = getReader();
				text = IOUtils.read(reader, -1);
			} catch (OutOfMemoryError e) {
				logger.error(e, toString());
			} catch (IOException e) {
				logger.error(e, toString());
			}
			
			if (text == null) {
				return EmptyJsonElement.INSTANCE;
			}
			
			JsonElement jsonElement = getJsonSupport().parseJson(text);
			if(jsonElement.isJsonArray()){
				json = jsonElement.getAsJsonArray();
			}else if(jsonElement.isJsonObject()){
				json = jsonElement.getAsJsonObject();
			}else{
				json = jsonElement;
			}
		}
		return json;
	}

	/**
	 * @return 如果不是一个JsonObject, 那么返回空
	 */
	public JsonObject getJsonObject(){
		Object json = getJson();
		if(json instanceof JsonObject){
			return (JsonObject) json;
		}
		return null;
	}
	
	/**
	 * @return 如果不是一个JsonArray, 那么返回空
	 */
	public JsonArray getJsonArray(){
		Object json = getJson();
		if(json instanceof JsonArray){
			return (JsonArray) json;
		}
		return null;
	}
	
	/**
	 * @return 如果不是一个JsonElement, 那么返回空
	 */
	public JsonElement getJsonElement(){
		Object json = getJson();
		if(json instanceof JsonElement){
			return (JsonElement) json;
		}
		return null;
	}
}
