package scw.registry;

import java.util.List;

import scw.aop.annotation.AopEnable;
import scw.event.BasicEventRegister;

/**
 * 服务注册中心
 * @author shuchaowen
 *
 */
@AopEnable(false)
public interface ServiceRegistry extends BasicEventRegister<ServiceRegistryEvent> {
	/**
	 * 根据名称获取所有的实例
	 * 
	 * @param name
	 * @return
	 */
	List<ServiceRegistryInstance> getInstances(String name);

	/**
	 * 创建一个实例
	 * 
	 * @param name
	 * @return
	 */
	ServiceRegistryInstance create(String name);
}
