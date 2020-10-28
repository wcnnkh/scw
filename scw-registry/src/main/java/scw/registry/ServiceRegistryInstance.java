package scw.registry;

import java.io.Closeable;
import java.io.Flushable;

import scw.mapper.MapperUtils;

/**
 * 注册中心一个实例
 * 
 * @author shuchaowen
 *
 */
public abstract class ServiceRegistryInstance implements Closeable, Flushable {
	private final String name;
	private ServiceRegistryInstanceData data;

	public ServiceRegistryInstance(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ServiceRegistryInstanceData getData() {
		return data;
	}

	public ServiceRegistryInstanceData getOrCreateData() {
		if (this.data == null) {
			this.data = new ServiceRegistryInstanceData();
		}
		return this.data;
	}

	public void setData(ServiceRegistryInstanceData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return MapperUtils.getMapper().toString(this);
	}

	@Override
	public abstract int hashCode();

	/**
	 * 判断是否是同一实例
	 */
	@Override
	public abstract boolean equals(Object instance);

	public abstract void close() throws ServiceRegistryException;

	/**
	 * 将修改内容推送
	 */
	public abstract void flush() throws ServiceRegistryException;
}
