package scw.beans;

import scw.lang.Ignore;

@Ignore
public interface Destroy {
	/**
	 * 销毁时执行
	 * @throws Throwable
	 */
	void destroy() throws Throwable;
}