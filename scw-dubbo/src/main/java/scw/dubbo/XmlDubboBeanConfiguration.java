package scw.dubbo;

import java.util.List;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.MetricsConfig;
import org.apache.dubbo.config.ModuleConfig;
import org.apache.dubbo.config.MonitorConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.SslConfig;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.w3c.dom.NodeList;

import scw.beans.AbstractBeanConfiguration;
import scw.beans.BeanFactory;
import scw.beans.xml.XmlBeanFactory;
import scw.core.instance.annotation.Configuration;
import scw.value.property.PropertyFactory;

@Configuration(order = Integer.MIN_VALUE)
public final class XmlDubboBeanConfiguration extends AbstractBeanConfiguration {

	public void init(BeanFactory beanFactory, PropertyFactory propertyFactory) throws Exception {
		if (beanFactory instanceof XmlBeanFactory) {
			NodeList nodeList = ((XmlBeanFactory) beanFactory).getNodeList();
			List<RegistryConfig> registryConfigs = XmlDubboUtils.parseRegistryConfigList(propertyFactory, nodeList,
					null);
			for (ApplicationConfig config : XmlDubboUtils.parseApplicationConfigList(propertyFactory, nodeList, null)) {
				config.setRegistries(registryConfigs);
				ApplicationModel.getConfigManager().setApplication(config);
			}

			for (SslConfig config : XmlDubboUtils.parseSslConfigList(propertyFactory, nodeList)) {
				ApplicationModel.getConfigManager().setSsl(config);
			}

			for (MetricsConfig config : XmlDubboUtils.parseMetricsConfigList(propertyFactory, nodeList)) {
				ApplicationModel.getConfigManager().setMetrics(config);
			}

			for (ModuleConfig config : XmlDubboUtils.parseModuleConfigList(propertyFactory, nodeList)) {
				ApplicationModel.getConfigManager().setModule(config);
			}

			for (MonitorConfig config : XmlDubboUtils.parseMonitorConfigList(propertyFactory, nodeList)) {
				ApplicationModel.getConfigManager().setMonitor(config);
			}

			for (ReferenceConfig<?> config : XmlDubboUtils.parseReferenceConfigList(propertyFactory, nodeList, null)) {
				DubboBeanDefinition xmlDubboBean = new DubboBeanDefinition(beanFactory, propertyFactory,
						config.getInterfaceClass(), config);
				beanDefinitions.add(xmlDubboBean);
			}
		}
	}
}
