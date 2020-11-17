package scw.beans.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import scw.core.utils.StringUtils;
import scw.http.HttpUtils;
import scw.value.property.PropertyFactory;
import scw.xml.XMLUtils;

public class XmlPropertyFactory extends PropertyFactory {

	public XmlPropertyFactory(PropertyFactory propertyFactory, NodeList nodeList) {
		super(true, false);
		if (nodeList == null) {
			return;
		}

		if (nodeList == null || nodeList.getLength() == 0) {
			return;
		}

		for (int i = 0, size = nodeList.getLength(); i < size; i++) {
			Node node = nodeList.item(i);
			if (node == null) {
				continue;
			}

			if (!"properties".equalsIgnoreCase(node.getNodeName())) {
				continue;
			}

			load(propertyFactory, null, null, node);
		}
	}

	private void load(PropertyFactory propertyFactory, String prefix, String charsetName, Node node) {
		String prefixToUse = XMLUtils.getNodeAttributeValue(node, "prefix");
		if (StringUtils.isEmpty(prefixToUse)) {
			prefixToUse = prefix;
		} else {
			prefixToUse = StringUtils.isEmpty(prefix) ? prefixToUse : (prefix + prefixToUse);
		}

		String charsetNameToUse = XMLUtils.getNodeAttributeValue(node, "charsetName");
		if (StringUtils.isEmpty(charsetNameToUse)) {
			charsetNameToUse = charsetName;
		} else {
			charsetNameToUse = StringUtils.isEmpty(charsetName) ? charsetNameToUse : charsetName;
		}

		String file = XMLUtils.getNodeAttributeValue(node, "file");
		if (!StringUtils.isEmpty(file)) {
			loadProperties(prefixToUse, file, charsetNameToUse)
					.registerListener();
		}

		String name = XMLUtils.getNodeAttributeValue(node, "name");
		if (StringUtils.isNotEmpty(name)) {
			name = StringUtils.isEmpty(prefixToUse) ? name : (prefixToUse + name);

			String url = getURL(node);
			if (StringUtils.isNotEmpty(url)) {
				String value = HttpUtils.getHttpClient().get(String.class, url).getBody();
				put(name, value);
			}

			String value = XMLUtils.getNodeAttributeValueOrNodeContent(propertyFactory, node, "value");
			if (StringUtils.isNotEmpty(value)) {
				put(name, value, true);
			}
		}

		NodeList nodeList = node.getChildNodes();
		if (nodeList != null) {
			for (int i = 0, size = nodeList.getLength(); i < size; i++) {
				Node n = nodeList.item(i);
				if (n == null) {
					continue;
				}

				if (!"property".equalsIgnoreCase(n.getNodeName())) {
					continue;
				}

				load(propertyFactory, prefixToUse, charsetNameToUse, n);
			}
		}
	}

	private static String getURL(Node node) {
		return XMLUtils.getNodeAttributeValue(node, "url");
	}
}
