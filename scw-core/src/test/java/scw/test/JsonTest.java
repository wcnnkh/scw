package scw.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import scw.json.JSONUtils;
import scw.json.JsonArray;
import scw.json.JsonObject;
import scw.json.JsonObjectWrapper;

public class JsonTest {

	@Test
	public void test() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key1", "value1");
		map.put("key2", "asss");
		
		List<Object> list = new ArrayList<Object>();
		list.add(map);
		list.add(map);
		
		String content = JSONUtils.getJsonSupport().toJSONString(list);
		System.out.println(content);
		
		JsonArray jsonArray = JSONUtils.getJsonSupport().parseArray(content);
		List<TestJsonObjectWrapper> wrappers = jsonArray.convert(TestJsonObjectWrapper.class);
		System.out.println(wrappers);
	}
	
	public static class TestJsonObjectWrapper extends JsonObjectWrapper{

		public TestJsonObjectWrapper(JsonObject target) {
			super(target);
		}
		
	}
}