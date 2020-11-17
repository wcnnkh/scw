package scw.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import scw.core.Converter;
import scw.core.StringFormat;
import scw.core.instance.NoArgsInstanceFactory;
import scw.core.utils.StringUtils;
import scw.io.IOUtils;
import scw.io.Resource;
import scw.io.ResourceUtils;
import scw.lang.NotFoundException;
import scw.mapper.Field;
import scw.mapper.Fields;
import scw.mapper.FilterFeature;
import scw.mapper.MapperUtils;
import scw.util.KeyValuePair;
import scw.util.ToMap;
import scw.util.XUtils;
import scw.value.ValueUtils;
import scw.value.property.PropertyFactory;

public final class XMLUtils {
	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
	public static final NodeList EMPTY_NODE_LIST = new NodeList() {

		public Node item(int index) {
			return null;
		}

		public int getLength() {
			return 0;
		}
	};

	static {
		DOCUMENT_BUILDER_FACTORY.setIgnoringElementContentWhitespace(true);
		DOCUMENT_BUILDER_FACTORY.setIgnoringComments(true);
		DOCUMENT_BUILDER_FACTORY.setCoalescing(true);
		DOCUMENT_BUILDER_FACTORY.setExpandEntityReferences(false);
	}

	private XMLUtils() {
	}

	public static DocumentBuilder newDocumentBuilder() {
		try {
			return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Transformer newTransformer() {
		try {
			return TRANSFORMER_FACTORY.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Document parse(InputStream is) throws IOException {
		DocumentBuilder documentBuilder = newDocumentBuilder();
		try {
			return documentBuilder.parse(is);
		} catch (SAXException e) {
			throw new XmlException(e);
		}
	}

	public static Document parse(InputSource is) {
		DocumentBuilder documentBuilder = newDocumentBuilder();
		try {
			return documentBuilder.parse(is);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document parseForURI(String uri) {
		DocumentBuilder documentBuilder = newDocumentBuilder();
		try {
			return documentBuilder.parse(uri);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document parse(InputStream is, String systemId) {
		DocumentBuilder documentBuilder = newDocumentBuilder();
		try {
			return documentBuilder.parse(is, systemId);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document parse(String text) {
		return parse(new InputSource(new StringReader(text)));
	}

	public static Document getDocument(String path) throws NotFoundException {
		return getDocument(ResourceUtils.getResourceOperations().getResource(path));
	}

	public static Document getDocument(Resource resource) {
		if (!resource.exists()) {
			throw new NotFoundException(resource.getDescription());
		}

		InputStream inputStream = null;
		try {
			inputStream = resource.getInputStream();
			return parse(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(resource.getDescription(), e);
		} finally {
			IOUtils.close(inputStream);
		}
	}

	public static Element getRootElement(String xmlPath) {
		Document document = getDocument(xmlPath);
		return document.getDocumentElement();
	}

	private static MyNodeList getIncludeNodeList(HashSet<String> includeHashSet, Node includeNode) {
		String file = getNodeAttributeValueOrNodeContent(includeNode, "file");
		if (StringUtils.isEmpty(file)) {
			return new MyNodeList();
		}

		if (includeHashSet.contains(file)) {
			throw new RuntimeException(file + "存在循环引用，请检查include地址");
		}

		includeHashSet.add(file);
		Document document = XMLUtils.getDocument(file);
		Node root = document.getDocumentElement();
		if (root == null) {
			return new MyNodeList();
		}

		NodeList nodeList = root.getChildNodes();
		MyNodeList list = new MyNodeList();
		for (int i = 0, size = nodeList.getLength(); i < size; i++) {
			Node n = nodeList.item(i);
			if (n == null) {
				continue;
			}

			list.add(n);
		}
		return list;
	}

	private static MyNodeList converIncludeNodeList(NodeList nodeList, HashSet<String> includeHashSet) {
		MyNodeList list = new MyNodeList();
		if (nodeList != null) {
			for (int i = 0, size = nodeList.getLength(); i < size; i++) {
				Node n = nodeList.item(i);
				if (n == null) {
					continue;
				}

				if (n.getNodeName().equalsIgnoreCase("include")) {
					MyNodeList n2 = getIncludeNodeList(includeHashSet, n);
					list.addAll(converIncludeNodeList(n2, includeHashSet));
				} else {
					list.add(n);
				}
			}
		}
		return list;
	}

	public static NodeList getChildNodes(Node node, boolean include) {
		if (node == null) {
			return null;
		}

		return include ? converIncludeNodeList(node.getChildNodes(), new HashSet<String>()) : node.getChildNodes();
	}

	/**
	 * 将xml文档解析为map
	 * 
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> toRecursionMap(Node node) {
		return toMap(node, new Converter<Node, KeyValuePair<String, Object>>() {

			public KeyValuePair<String, Object> convert(Node n) {
				NodeList nodeList = n.getChildNodes();
				Object v;
				if (nodeList == null || nodeList.getLength() == 0) {
					v = n.getTextContent();
				} else {
					List<Object> list = toList(n, new Converter<Node, Object>() {

						public Object convert(Node k) {
							return toRecursionMap(k);
						}
					});

					v = list == null ? n.getTextContent() : list;
				}
				return new KeyValuePair<String, Object>(n.getNodeName(), v);
			}
		});
	}

	public static List<Object> toList(Node node, Converter<Node, Object> nodeConvert) {
		if (ignoreNode(node)) {
			return null;
		}

		NodeList nodeList = node.getChildNodes();
		if (nodeList == null) {
			return null;
		}

		int len = nodeList.getLength();
		if (len == 0) {
			return null;
		}

		List<Object> list = new ArrayList<Object>(len);
		for (int i = 0; i < len; i++) {
			Node n = nodeList.item(i);
			if (ignoreNode(n)) {
				continue;
			}

			Object v = nodeConvert.convert(n);
			if (v == null) {
				continue;
			}

			list.add(v);
		}

		return list.isEmpty() ? null : list;
	}

	public static Map<String, Object> toMap(Node node, Converter<Node, KeyValuePair<String, Object>> nodeParse) {
		if (ignoreNode(node)) {
			return null;
		}

		NodeList nodeList = node.getChildNodes();
		if (nodeList == null) {
			return null;
		}

		int len = nodeList.getLength();
		if (len == 0) {
			return null;
		}

		Map<String, Object> map = new LinkedHashMap<String, Object>(len);
		for (int i = 0; i < len; i++) {
			Node n = nodeList.item(i);
			if (ignoreNode(node)) {
				continue;
			}

			KeyValuePair<String, Object> keyValuePair = nodeParse.convert(n);
			if (keyValuePair != null) {
				map.put(keyValuePair.getKey(), keyValuePair.getValue());
			}
		}

		return map.isEmpty() ? null : map;
	}

	public static String toString(Node node) {
		Transformer transformer;
		try {
			transformer = TRANSFORMER_FACTORY.newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
			return null;
		}

		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource domSource = new DOMSource(node);
		String content = null;
		try {
			transformer.transform(domSource, result);
			content = sw.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		} finally {
			IOUtils.close(sw);
		}
		return content;
	}

	public static LinkedHashMap<String, String> xmlToMap(Node node) {
		NodeList nodeList = node.getChildNodes();
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (ignoreNode(n)) {
				continue;
			}

			map.put(n.getNodeName(), n.getTextContent());
		}
		return map;
	}

	public static LinkedHashMap<String, String> xmlToMap(String text) {
		Document document = parse(text);
		return xmlToMap(document.getDocumentElement());
	}

	@SuppressWarnings("rawtypes")
	private static void appendElement(Document document, Element parent, String name, Object value) {
		if (value == null) {
			return;
		}

		if (value instanceof Map) {
			appendElement(document, parent, (Map) value);
		} else if (value instanceof Collection) {
			for (Object item : (Collection) value) {
				appendElement(document, parent, name, item);
			}
		} else if (value instanceof ToMap) {
			appendElement(document, parent, XUtils.toMap(value, false));
		} else if (value.getClass().isArray()) {
			for (int i = 0, len = Array.getLength(value); i < len; i++) {
				appendElement(document, parent, name, Array.get(value, i));
			}
		} else {
			Element element = document.createElement(name);
			element.setTextContent(value.toString());
			parent.appendChild(element);
		}
	}

	public static void appendElement(Document document, Element parent, Map<?, ?> map) {
		for (Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey() == null) {
				continue;
			}

			Object value = entry.getValue();
			if (value == null) {
				continue;
			}

			appendElement(document, parent, entry.getKey().toString(), value);
		}
	}

	public static Element createElement(Document document, String name, Map<?, ?> map) {
		Element parent = document.createElement(name);
		appendElement(document, parent, map);
		return parent;
	}

	public static String toXml(String name, Map<?, ?> map) {
		return toString(createElement(newDocumentBuilder().newDocument(), name, map));
	}

	public static String getNodeAttributeValue(Node node, String name) {
		return getNodeAttributeValue(node, name, null);
	}

	/**
	 * @param node
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getNodeAttributeValue(Type basicType, Node node, String name, T defaultValue) {
		String value = getNodeAttributeValue(node, name);
		if (value == null) {
			return defaultValue;
		} else {
			return (T) ValueUtils.parse(value, basicType);
		}
	}

	public static String getNodeAttributeValue(Node node, String name, String defaultValue) {
		if (node == null) {
			return null;
		}

		NamedNodeMap namedNodeMap = node.getAttributes();
		if (namedNodeMap == null) {
			return null;
		}

		Node n = namedNodeMap.getNamedItem(name);
		return n == null ? defaultValue : n.getNodeValue();
	}

	public static String getNodeAttributeValueOrNodeContent(Node node, String name) {
		NamedNodeMap namedNodeMap = node.getAttributes();
		if (namedNodeMap == null) {
			return null;
		}

		Node n = namedNodeMap.getNamedItem(name);
		return n == null ? node.getTextContent() : n.getNodeValue();
	}

	public static String getRequireNodeAttributeValue(Node node, String name) {
		String value = getNodeAttributeValue(node, name);
		if (StringUtils.isEmpty(value)) {
			throw new NotFoundException("not found attribute [" + name + "], " + toString(node));
		}
		return value;
	}

	public static void requireAttribute(Node node, String... name) {
		for (String n : name) {
			if (StringUtils.isEmpty(XMLUtils.getNodeAttributeValue(node, n))) {
				throw new NotFoundException("not found attribute [" + n + "], " + toString(node));
			}
		}
	}

	public static Boolean getBooleanValueAndParent(Node node, String name, Boolean defaultValue) {
		Node parent = node.getParentNode();
		return getBooleanValue(node, name,
				parent == null ? defaultValue : getBooleanValueAndParent(parent, name, defaultValue));
	}

	public static Boolean getBooleanValue(Node node, String name, Boolean defaultValue) {
		String value = getNodeAttributeValue(node, name);
		return StringUtils.parseBoolean(value, defaultValue);
	}

	public static <T> T getBean(NoArgsInstanceFactory instanceFactory, Node node, Class<T> type) {
		T t = null;
		Fields fields = MapperUtils.getMapper().getFields(type, FilterFeature.SUPPORT_SETTER,
				FilterFeature.SETTER_IGNORE_STATIC);
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (ignoreNode(n)) {
				continue;
			}

			Field field = fields.find(n.getNodeName(), null);
			if (field == null) {
				continue;
			}

			String value = n.getTextContent();
			if (value == null) {
				continue;
			}

			if (t == null) {
				t = instanceFactory.getInstance(type);
			}

			MapperUtils.setStringValue(field, t, value);
		}
		return t;
	}

	public static <T> List<T> getBeanList(NoArgsInstanceFactory instanceFactory, Node rootNode, Class<T> type) {
		if (rootNode == null) {
			return null;
		}

		NodeList nodeList = rootNode.getChildNodes();
		if (nodeList == null) {
			return null;
		}

		List<T> list = new ArrayList<T>(nodeList.getLength());
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (ignoreNode(node)) {
				continue;
			}

			T t = getBean(instanceFactory, node, type);
			if (t == null) {
				continue;
			}
			list.add(t);
		}
		return list;
	}

	public static boolean ignoreNode(Node node) {
		return node == null || StringUtils.isEmpty(node.getNodeName()) || "#text".equals(node.getNodeName());
	}

	public static Map<String, Node> attributeAsMap(Node node) {
		if (node == null) {
			return null;
		}

		NamedNodeMap map = node.getAttributes();
		if (map == null) {
			return null;
		}

		int size = map.getLength();
		Map<String, Node> properties = new HashMap<String, Node>(size, 1);
		for (int i = 0; i < size; i++) {
			Node n = map.item(i);
			properties.put(n.getNodeName(), n);
		}
		return properties;
	}

	public static String formatNodeValue(final PropertyFactory propertyFactory, Node node, String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}

		if (!getBooleanValue(node, "replace", true)) {
			return value;
		}

		String replacePrefix = getNodeAttributeValue(node, "replace-prefix");
		String replaceSuffix = getNodeAttributeValue(node, "replace-suffix");
		replacePrefix = StringUtils.isEmpty(replacePrefix) ? "{" : replacePrefix;
		replaceSuffix = StringUtils.isEmpty(replaceSuffix) ? "}" : replaceSuffix;
		return StringFormat.format(value, replacePrefix, replaceSuffix, propertyFactory);
	}

	public static String getNodeAttributeValue(PropertyFactory propertyFactory, Node node, String name) {
		String value = getNodeAttributeValue(node, name);
		if (value == null || value.length() == 0) {
			return value;
		}

		return formatNodeValue(propertyFactory, node, value);
	}

	public static String getNodeAttributeValueOrNodeContent(PropertyFactory propertyFactory, Node node, String name) {
		String value = getNodeAttributeValueOrNodeContent(node, name);
		if (StringUtils.isEmpty(value)) {
			return null;
		}

		return formatNodeValue(propertyFactory, node, value);
	}

	public static String getRequireNodeAttributeValueOrNodeContent(PropertyFactory propertyFactory, Node node,
			String name) {
		String value = getNodeAttributeValueOrNodeContent(node, name);
		if (StringUtils.isEmpty(value)) {
			throw new NotFoundException("not found attribute " + name);
		}
		return formatNodeValue(propertyFactory, node, value);
	}

	public static String getRequireNodeAttributeValue(PropertyFactory propertyFactory, Node node, String name) {
		String value = getNodeAttributeValue(propertyFactory, node, name);
		if (StringUtils.isEmpty(value)) {
			throw new NotFoundException("not found attribute " + name);
		}
		return value;
	}
}

final class MyNodeList extends ArrayList<Node> implements NodeList {
	private static final long serialVersionUID = 1L;

	public Node item(int index) {
		return get(index);
	}

	public int getLength() {
		return size();
	}

}
