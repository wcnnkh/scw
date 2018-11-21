package shuchaowen.cms.pojo;

import java.io.Serializable;

import shuchaowen.core.db.annoation.Column;
import shuchaowen.core.db.annoation.PrimaryKey;
import shuchaowen.core.db.annoation.Table;

@Table
public class ChannelAttr implements Serializable{
	private static final long serialVersionUID = 1L;
	@PrimaryKey
	private long channelId;
	@PrimaryKey
	private String attr;
	@Column(length=0, type="text", nullAble=true)
	private String value;
	private long modelId;

	public long getChannelId() {
		return channelId;
	}
	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}
	public String getAttr() {
		return attr;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public long getModelId() {
		return modelId;
	}
	public void setModelId(long modelId) {
		this.modelId = modelId;
	}
}
