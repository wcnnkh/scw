package scw.beans.xml;

import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import scw.beans.BeanDefinition;
import scw.beans.DefaultBeanFactory;
import scw.core.utils.StringUtils;
import scw.io.Resource;
import scw.io.ResourceUtils;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.value.property.PropertyFactory;
import scw.xml.XMLUtils;

public class XmlBeanFactory extends DefaultBeanFactory {
	public static final String DEFAULT_CONFIG = "beans.xml";
	public static final String CONFIG_NAME = "contextConfigLocation";
	private static Logger logger = LoggerUtils.getLogger(XmlBeanFactory.class);
	private static final String TAG_NAME = "bean";
	private NodeList nodeList;
	private String xml;
	
	public XmlBeanFactory(PropertyFactory propertyFactory, String xml) {
		super(propertyFactory);
		this.xml = xml;
	}

	public NodeList getNodeList() {
		return nodeList == null ? XMLUtils.EMPTY_NODE_LIST : nodeList;
	}
	
	public String getXml() {
		return xml;
	}

	@Override
	public void beforeInit() throws Throwable {
		Resource resource = null;
		if(StringUtils.isNotEmpty(xml)){
			resource = ResourceUtils.getResourceOperations().getResource(xml);
		}
		
		if(resource == null || !resource.exists()){
			String config = getPropertyFactory().getString(CONFIG_NAME);
			if(StringUtils.isNotEmpty(config)){
				resource = ResourceUtils.getResourceOperations().getResource(config);
			}
		}
		
		if(resource == null || !resource.exists()){
			resource = ResourceUtils.getResourceOperations().getResource(DEFAULT_CONFIG);
		}
		
		if(resource != null && resource.exists()){
			this.nodeList = XmlBeanUtils.getRootNodeList(resource);
			logger.info("Use config {}", resource);
			getPropertyFactory().addLastBasePropertyFactory(new XmlPropertyFactory(getPropertyFactory(), nodeList));
			addXmlBeanNameMapping(nodeList);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nRoot = nodeList.item(i);
				if (TAG_NAME.equalsIgnoreCase(nRoot.getNodeName())) {
					BeanDefinition beanDefinition = new XmlBeanDefinition(this, getPropertyFactory(), nRoot);
					addBeanDefinition(beanDefinition, true);
				}
			}
		}
		super.beforeInit();
	}

	private void addXmlBeanNameMapping(NodeList nodeList) {
		if (nodeList == null) {
			return;
		}

		for (int i = 0, len = nodeList.getLength(); i < len; i++) {
			Node node = nodeList.item(i);
			if (node == null) {
				continue;
			}

			if ("mapping".equalsIgnoreCase(node.getNodeName())) {
				Collection<String> names = Arrays.asList(
						StringUtils.commonSplit(XMLUtils.getRequireNodeAttributeValue(getPropertyFactory(), node, "name")));
				String id = XMLUtils.getRequireNodeAttributeValueOrNodeContent(getPropertyFactory(), node, "id");
				addBeanNameMapping(names, id, false);
			}
		}
	}
}
