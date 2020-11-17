package scw.dubbo;

import java.util.ArrayList;
import java.util.List;

import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.w3c.dom.NodeList;

import scw.application.Application;
import scw.application.ApplicationInitialization;
import scw.beans.BeanFactory;
import scw.beans.xml.XmlBeanFactory;
import scw.core.instance.annotation.Configuration;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.logger.SplitLineAppend;

/**
 * 暴露dubbo服务
 * @author shuchaowen
 *
 */
@Configuration(order = Integer.MIN_VALUE)
public class XmlDubboServiceExport implements ApplicationInitialization {
	private static Logger logger = LoggerFactory.getLogger(XmlDubboServiceExport.class);

	@SuppressWarnings("rawtypes")
	@Override
	public void init(Application application) throws Throwable {
		BeanFactory beanFactory = application.getBeanFactory();
		if (beanFactory instanceof XmlBeanFactory) {
			NodeList nodeList = ((XmlBeanFactory) beanFactory).getNodeList();
			if (nodeList == null) {
				return;
			}

			List<ProtocolConfig> protocolConfigs = XmlDubboUtils
					.parseProtocolConfigList(application.getPropertyFactory(), nodeList, null);
			if (!protocolConfigs.isEmpty()) {
				startLog();
			}

			List<ServiceConfig> serviceConfigs = XmlDubboUtils.parseServiceConfigList(application.getPropertyFactory(),
					nodeList, null, beanFactory);
			if (!serviceConfigs.isEmpty()) {
				if (protocolConfigs.isEmpty()) {
					startLog();
				}

				for (ServiceConfig<?> config : serviceConfigs) {
					List<ProtocolConfig> protocolConfigsToUse = new ArrayList<ProtocolConfig>(protocolConfigs);
					if (config.getProtocols() != null) {
						protocolConfigsToUse.addAll(config.getProtocols());
					}
					config.setProtocols(protocolConfigsToUse);
					config.export();
				}
			}

			if (!protocolConfigs.isEmpty() || !protocolConfigs.isEmpty()) {
				endLog();
			}
		}
	}

	private void startLog() {
		logger.info(new SplitLineAppend("Start to register Dubbo service"));
	}

	private void endLog() {
		logger.info(new SplitLineAppend("Dubbo service registration completed"));
	}
}
