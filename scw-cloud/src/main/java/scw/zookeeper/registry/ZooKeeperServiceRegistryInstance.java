package scw.zookeeper.registry;

import org.apache.zookeeper.ZooKeeper;

import scw.io.serialzer.JavaSerializer;
import scw.registry.ServiceRegistryInstance;
import scw.registry.ServiceRegistryInstanceData;
import scw.registry.ServiceRegistryException;
import scw.zookeeper.ZooKeeperUtils;

public class ZooKeeperServiceRegistryInstance extends ServiceRegistryInstance {
	private ZooKeeper zooKeeper;
	private final String path;

	public ZooKeeperServiceRegistryInstance(String name, String path, ZooKeeper zooKeeper) {
		super(name);
		this.zooKeeper = zooKeeper;
		this.path = path;
		byte[] data = ZooKeeperUtils.getData(zooKeeper, path);
		if (data != null && data.length != 0) {
			ServiceRegistryInstanceData serviceRegistryInstanceData;
			try {
				serviceRegistryInstanceData = JavaSerializer.INSTANCE.deserialize(data);
				setData(serviceRegistryInstanceData);
			} catch (ClassNotFoundException e) {
				throw new ServiceRegistryException(e);
			}
		}
	}

	@Override
	public void close() throws ServiceRegistryException {
		ZooKeeperUtils.delete(zooKeeper, path, -1);
	}

	@Override
	public void flush() throws ServiceRegistryException {
		ServiceRegistryInstanceData serviceRegistryInstanceData = getData();
		byte[] data = serviceRegistryInstanceData == null ? null : JavaSerializer.INSTANCE.serialize(serviceRegistryInstanceData);
		ZooKeeperUtils.setData(zooKeeper, path, data);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public boolean equals(Object instance) {
		if (instance == null) {
			return false;
		}

		if (instance == this) {
			return true;
		}

		if (instance instanceof ZooKeeperServiceRegistryInstance) {
			return path.equals(((ZooKeeperServiceRegistryInstance) instance).path);
		}

		return false;
	}
}
