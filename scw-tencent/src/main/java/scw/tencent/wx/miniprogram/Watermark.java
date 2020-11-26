package scw.tencent.wx.miniprogram;

import java.io.Serializable;

public class Watermark implements Serializable {
	private static final long serialVersionUID = 1L;
	private String appid;
	private long timestamp;

	public String getAppid() {
		return appid;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
