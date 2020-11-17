package scw.beans.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import scw.beans.BeansException;
import scw.beans.ioc.IocProcessor;
import scw.core.utils.ClassUtils;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.io.Resource;
import scw.value.property.PropertyFactory;
import scw.xml.XMLUtils;

public final class XmlBeanUtils {
	private XmlBeanUtils() {
	};

	public static XmlBeanParameter parseBeanParameter(Node node)
			throws ClassNotFoundException {
		String name = XMLUtils.getNodeAttributeValue(node, "name");
		String ref = XMLUtils.getNodeAttributeValue(node, "ref");
		String value = XMLUtils.getNodeAttributeValue(node, "value");
		String type = XMLUtils.getNodeAttributeValue(node, "type");
		String property = XMLUtils.getNodeAttributeValue(node, "property");

		Class<?> typeClass = StringUtils.isEmpty(type) ? null : ClassUtils
				.forName(type);
		if (!StringUtils.isEmpty(ref)) {
			return new XmlBeanParameter(XmlParameterType.ref, typeClass, name,
					ref, node);
		} else if (!StringUtils.isEmpty(property)) {
			return new XmlBeanParameter(XmlParameterType.property, typeClass,
					name, property, node);
		} else {
			if (StringUtils.isEmpty(value)) {
				value = node.getNodeValue();
			}
			return new XmlBeanParameter(XmlParameterType.value, typeClass, name,
					value, node);
		}
	}

	public static Boolean isSingleton(Node node) {
		return XMLUtils.getBooleanValue(node, "singleton", null);
	}

	public static boolean getBooleanValue(PropertyFactory propertyFactory,
			Node node, String name, boolean defaultValue) {
		String value = XMLUtils.getNodeAttributeValue(propertyFactory, node,
				name);
		return StringUtils.isEmpty(value) ? defaultValue : Boolean
				.parseBoolean(value);
	}

	public static int getIntegerValue(PropertyFactory propertyFactory,
			Node node, String name, int defaultValue) {
		String value = XMLUtils.getNodeAttributeValue(propertyFactory, node,
				name);
		return StringUtils.isEmpty(value) ? defaultValue : Integer
				.parseInt(value);
	}

	public static List<XmlBeanParameter> parseBeanParameterList(Node node)
			throws ClassNotFoundException {
		List<XmlBeanParameter> xmlBeanParameters = new ArrayList<XmlBeanParameter>();
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node nRoot = nodeList.item(i);
			if (nRoot.getNodeName().equals("parameter")) {
				if (nRoot.getAttributes() == null) {
					continue;
				}

				xmlBeanParameters.add(parseBeanParameter(nRoot));
			}
		}
		return xmlBeanParameters;
	}

	public static NodeList getRootNodeList(Resource resource) {
		Document document = XMLUtils.getDocument(resource);
		Node node = document.getDocumentElement();
		if (!"beans".equals(node.getNodeName())) {
			throw new BeansException("root tag name error ["
					+ node.getNodeName() + "]");
		}
		return XMLUtils.getChildNodes(node, true);
	}

	public static String getPackageName(PropertyFactory propertyFactory,
			Node node) {
		return XMLUtils.getNodeAttributeValue(propertyFactory, node, "package");
	}

	public static String getVersion(PropertyFactory propertyFactory, Node node) {
		return XMLUtils.getNodeAttributeValue(propertyFactory, node, "version");
	}

	public static String getAddress(PropertyFactory propertyFactory, Node node) {
		return XMLUtils.getRequireNodeAttributeValue(propertyFactory, node,
				"address");
	}

	public static String getRef(PropertyFactory propertyFactory, Node node) {
		return XMLUtils.getRequireNodeAttributeValue(propertyFactory, node,
				"ref");
	}

	public static String getCharsetName(PropertyFactory propertyFactory,
			Node node, String defaultValue) {
		String charsetName = XMLUtils.getNodeAttributeValue(propertyFactory,
				node, "charsetName");
		return StringUtils.isEmpty(charsetName) ? defaultValue : charsetName;
	}

	public static String getCharsetName(Node node, String defaultValue) {
		String charsetName = XMLUtils
				.getNodeAttributeValue(node, "charsetName");
		return StringUtils.isEmpty(charsetName) ? defaultValue : charsetName;
	}

	public static List<XmlMethodIocProcessor> getMethodIocProcessos(Class<?> clz,
			NodeList nodeList, String tagName) throws Exception {
		List<XmlMethodIocProcessor> list = new ArrayList<XmlMethodIocProcessor>();
		for (int a = 0; a < nodeList.getLength(); a++) {
			Node n = nodeList.item(a);
			if (tagName.equalsIgnoreCase(n.getNodeName())) {
				XmlMethodIocProcessor xmlBeanMethod = new XmlMethodIocProcessor(
						clz, n);
				list.add(xmlBeanMethod);
			}
		}
		return list;
	}

	public static List<XmlMethodIocProcessor> getInitMethodIocProcessors(
			Class<?> clz, NodeList nodeList) throws Exception {
		return XmlBeanUtils.getMethodIocProcessos(clz, nodeList, "init");
	}

	public static List<XmlMethodIocProcessor> getDestroyMethodIocProcessors(
			Class<?> clz, NodeList nodeList) throws Exception {
		return XmlBeanUtils.getMethodIocProcessos(clz, nodeList, "destroy");
	}

	public static XmlBeanParameter[] getConstructorParameters(NodeList nodeList)
			throws Exception {
		List<XmlBeanParameter> constructorParameterList = new ArrayList<XmlBeanParameter>();
		for (int a = 0; a < nodeList.getLength(); a++) {
			Node n = nodeList.item(a);
			if ("constructor".equalsIgnoreCase(n.getNodeName())) {// Constructor
				List<XmlBeanParameter> list = parseBeanParameterList(n);
				if (list != null) {
					constructorParameterList.addAll(list);
				}
			}
		}

		return CollectionUtils.isEmpty(constructorParameterList) ? null
				: constructorParameterList
						.toArray(new XmlBeanParameter[constructorParameterList
								.size()]);
	}

	public static Collection<IocProcessor> getBeanPropertiesIocProcessors(
			Class<?> targetClass, NodeList nodeList)
			throws ClassNotFoundException {
		List<IocProcessor> iocProcessors = new ArrayList<IocProcessor>();
		for (int a = 0; a < nodeList.getLength(); a++) {
			Node n = nodeList.item(a);
			if ("properties".equalsIgnoreCase(n.getNodeName())) {// Properties
				List<XmlBeanParameter> list = XmlBeanUtils
						.parseBeanParameterList(n);
				if (list != null) {
					for (XmlBeanParameter xmlBeanParameter : list) {
						iocProcessors.add(new XmlPropertiesIocProcessor(
								targetClass, xmlBeanParameter));
					}
				}
			}
		}
		return iocProcessors;
	}

	public static TimeUnit getTimeUnit(Node node) {
		String format = XMLUtils.getNodeAttributeValue(node, "unit");
		TimeUnit timeUnit = TimeUnit.MINUTES;
		if (StringUtils.isNotEmpty(format)) {
			timeUnit = TimeUnit.valueOf(format.toUpperCase());
		}
		return timeUnit;
	}

	public static String getClassName(Node node, boolean require) {
		return require ? XMLUtils.getRequireNodeAttributeValue(node, "class")
				: XMLUtils.getNodeAttributeValue(node, "class");
	}

	public static Class<?> getClass(Node node, boolean require)
			throws ClassNotFoundException {
		String className = getClassName(node, require);
		if (StringUtils.isEmpty(className)) {
			return null;
		}

		return ClassUtils.forName(className);
	}
}
