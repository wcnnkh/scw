package scw.zookeeper.registry;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import scw.event.BasicEventDispatcher;
import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.event.support.DefaultBasicEventDispatcher;
import scw.event.support.EventType;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.registry.ServiceRegistryInstance;
import scw.registry.ServiceRegistry;
import scw.registry.ServiceRegistryEvent;
import scw.util.XUtils;
import scw.zookeeper.ZooKeeperUtils;

public class ZooKeeperServiceRegistry implements ServiceRegistry, Watcher {
	private static Logger logger = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);
	private BasicEventDispatcher<ServiceRegistryEvent> eventDispatcher = new DefaultBasicEventDispatcher<ServiceRegistryEvent>(true);
	private final ZooKeeper zooKeeper;
	private final String parentPath;

	public ZooKeeperServiceRegistry(ZooKeeper zooKeeper) {
		this(zooKeeper, ZooKeeperUtils.PATH_PREFIX);
	}

	public ZooKeeperServiceRegistry(ZooKeeper zooKeeper, String parentPath) {
		this.zooKeeper = zooKeeper;
		this.parentPath = ZooKeeperUtils.cleanPath(parentPath);
		zooKeeper.register(this);
	}

	private String toName(String path) {
		String name = path;
		if (name.startsWith("/")) {
			name = name.substring(1);
		}

		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}

		return name;
	}

	@Override
	public void process(WatchedEvent event) {
		if (logger.isTraceEnabled()) {
			logger.trace(event);
		}

		String eventPath = event.getPath();
		if (eventPath == null || !eventPath.startsWith(parentPath)) {
			return;
		}

		String suffixPath = eventPath.substring(parentPath.length());
		int index = suffixPath.lastIndexOf(ZooKeeperUtils.PATH_PREFIX);
		if (index == -1) {
			return;
		}

		String name = suffixPath.substring(0, index - 1);
		name = toName(name);
		ZooKeeperServiceRegistryInstance instanceInfo = new ZooKeeperServiceRegistryInstance(name, eventPath, zooKeeper);
		ServiceRegistryEvent serviceRegistryEvent;
		switch (event.getType()) {
		case NodeDeleted:
			serviceRegistryEvent = new ServiceRegistryEvent(EventType.DELETE, instanceInfo);
			break;
		case NodeCreated:
			serviceRegistryEvent = new ServiceRegistryEvent(EventType.CREATE, instanceInfo);
			break;
		default:
			serviceRegistryEvent = new ServiceRegistryEvent(EventType.UPDATE, instanceInfo);
			break;
		}

		if (logger.isDebugEnabled()) {
			logger.debug(serviceRegistryEvent);
		}

		eventDispatcher.publishEvent(serviceRegistryEvent);
	}

	@Override
	public EventRegistration registerListener(EventListener<ServiceRegistryEvent> eventListener) {
		return eventDispatcher.registerListener(eventListener);
	}

	@Override
	public List<ServiceRegistryInstance> getInstances(String name) {
		String parent = ZooKeeperUtils.cleanPath(parentPath, name);
		List<String> paths = ZooKeeperUtils.getChildren(zooKeeper, parent);
		List<ServiceRegistryInstance> serviceRegistryInstances = new ArrayList<ServiceRegistryInstance>();
		for (String path : paths) {
			serviceRegistryInstances.add(new ZooKeeperServiceRegistryInstance(name, path, zooKeeper));
		}
		return serviceRegistryInstances;
	}

	@Override
	public ServiceRegistryInstance create(String name) {
		String rootPath = ZooKeeperUtils.cleanPath(parentPath, name);
		ZooKeeperUtils.createNotExist(zooKeeper, rootPath, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		String path = ZooKeeperUtils.cleanPath(rootPath, XUtils.getUUID());
		ZooKeeperUtils.createNotExist(zooKeeper, path, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		return new ZooKeeperServiceRegistryInstance(name, path, zooKeeper);
	}
}
