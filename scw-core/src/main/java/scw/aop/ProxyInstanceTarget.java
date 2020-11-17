package scw.aop;

/**
 * 这是一个被代理的实例
 * @author shuchaowen
 *
 */
public interface ProxyInstanceTarget {
	static final Class<?>[] CLASSES = new Class<?>[]{ProxyInstanceTarget.class};
	static final String PROXY_TARGET_METHOD_NAME = "getProxyTarget";
	
	/**
	 * 获取被代理前的实例
	 * @return
	 */
	Object getProxyTarget();
}
