package scw.zookeeper;

import scw.application.Application;
import scw.application.ApplicationInitialization;
import scw.core.instance.annotation.Configuration;
import scw.io.ResourceUtils;

@Configuration(order = Integer.MAX_VALUE)
public final class AutoZooKeeperServerStart implements ApplicationInitialization {
	private static final String DEFAULT_ZOOKEEPER_CONFIG = "zookeeper.properties";

	public void init(Application application) throws Throwable {
		ZooKeeperServerStart start = null;
		if (ResourceUtils.getResourceOperations().isExist(DEFAULT_ZOOKEEPER_CONFIG)) {
			start = new ZooKeeperServerStart(
					ResourceUtils.getResourceOperations().getProperties(DEFAULT_ZOOKEEPER_CONFIG).getResource());
		} else {
			Integer port = application.getPropertyFactory().getInteger("zookeeper.port");
			if (port != null) {
				start = new ZooKeeperServerStart(port);
			}
		}

		if (start != null) {
			start.start();
		}
	}

}
