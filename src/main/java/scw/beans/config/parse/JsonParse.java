package scw.beans.config.parse;

import java.io.File;

import scw.beans.BeanFactory;
import scw.beans.config.ConfigParse;
import scw.core.io.FileUtils;
import scw.core.json.JSONArray;
import scw.core.json.JSONObject;
import scw.core.json.JSONUtils;
import scw.core.reflect.FieldDefinition;
import scw.core.utils.ConfigUtils;

/**
 * 将内容解析为json
 * 
 * @author shuchaowen
 *
 */
public final class JsonParse implements ConfigParse {

	public Object parse(BeanFactory beanFactory, FieldDefinition fieldDefinition, String filePath, String charset)
			throws Exception {
		File file = ConfigUtils.getFile(filePath);
		String content = FileUtils.readerFileContent(file, charset).toString();
		if (JSONObject.class.isAssignableFrom(fieldDefinition.getField().getType())) {
			return JSONUtils.parseObject(content);
		} else if (JSONArray.class.isAssignableFrom(fieldDefinition.getField().getType())) {
			return JSONUtils.parseArray(content);
		} else if (String.class.isAssignableFrom(fieldDefinition.getField().getType())) {
			return content;
		} else {
			return JSONUtils.parseObject(content, fieldDefinition.getField().getType());
		}
	}

}
