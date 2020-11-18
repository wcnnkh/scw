package scw.net.message.multipart;

import scw.core.utils.StringUtils;
import scw.http.AbstractHttpInputMessage;
import scw.http.HttpHeaders;

public abstract class AbstractFileItem extends AbstractHttpInputMessage implements FileItem {
	private final HttpHeaders httpHeaders = new HttpHeaders();
	private final String fieldName;

	public AbstractFileItem(String fieldName) {
		this.fieldName = fieldName;
	}

	public HttpHeaders getHeaders() {
		return httpHeaders;
	}

	/**
	 * 获取文件名
	 * 
	 * @return
	 */
	public String getName() {
		return null;
	}

	public boolean isFormField() {
		return StringUtils.isEmpty(getName());
	}

	/**
	 * 字段名
	 * 
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}
}
