package scw.zookeeper;

import scw.beans.BeanFactory;
import scw.beans.BeanFactoryLifeCycle;
import scw.core.instance.annotation.Configuration;
import scw.io.ResourceUtils;
import scw.value.property.PropertyFactory;

@Configuration(order=Integer.MAX_VALUE)
public final class AutoZooKeeperServerStart implements BeanFactoryLifeCycle{
	private static final String DEFAULT_ZOOKEEPER_CONFIG = "zookeeper.properties";
	
	public void init(BeanFactory beanFactory, PropertyFactory propertyFactory)
			throws Exception {
		ZooKeeperServerStart start = null;
		if(ResourceUtils.getResourceOperations().isExist(DEFAULT_ZOOKEEPER_CONFIG)){
			start = new ZooKeeperServerStart(ResourceUtils.getResourceOperations().getProperties(DEFAULT_ZOOKEEPER_CONFIG).getResource());
		}else{
			Integer port = propertyFactory.getInteger("zookeeper.port");
			if(port != null){
				start = new ZooKeeperServerStart(port);
			}
		}
		
		if(start != null){
			start.start();
		}
	}

	public void destroy(BeanFactory beanFactory, PropertyFactory propertyFactory)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
