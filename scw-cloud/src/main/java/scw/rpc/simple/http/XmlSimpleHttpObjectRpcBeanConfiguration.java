package scw.rpc.simple.http;

import java.util.Arrays;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import scw.aop.MethodInterceptor;
import scw.beans.AbstractBeanConfiguration;
import scw.beans.BeanDefinition;
import scw.beans.BeanFactory;
import scw.beans.builder.ProxyBeanDefinition;
import scw.beans.xml.XmlBeanFactory;
import scw.beans.xml.XmlBeanUtils;
import scw.core.annotation.AnnotationUtils;
import scw.core.instance.annotation.Configuration;
import scw.core.utils.ClassUtils;
import scw.core.utils.StringUtils;
import scw.io.serialzer.Serializer;
import scw.io.serialzer.SerializerUtils;
import scw.util.ClassScanner;
import scw.value.property.PropertyFactory;
import scw.xml.XMLUtils;

@Configuration(order = Integer.MIN_VALUE)
public final class XmlSimpleHttpObjectRpcBeanConfiguration extends AbstractBeanConfiguration {
	private static final String TAG_NAME = "http:reference";

	public void init(BeanFactory beanFactory, PropertyFactory propertyFactory) throws Exception {
		if (beanFactory instanceof XmlBeanFactory) {
			NodeList rootNodeList = ((XmlBeanFactory) beanFactory).getNodeList();
			for (int i = 0; i < rootNodeList.getLength(); i++) {
				Node node = rootNodeList.item(i);
				if (node == null) {
					continue;
				}

				if (!TAG_NAME.equals(node.getNodeName())) {
					continue;
				}

				String sign = XMLUtils.getNodeAttributeValue(propertyFactory, node, "sign");
				String packageName = XmlBeanUtils.getPackageName(propertyFactory, node);
				String serializer = XMLUtils.getNodeAttributeValue(propertyFactory, node, "serializer");
				String address = XmlBeanUtils.getAddress(propertyFactory, node);
				boolean responseThrowable = StringUtils
						.parseBoolean(XMLUtils.getNodeAttributeValue(propertyFactory, node, "throwable"), true);

				Serializer ser = StringUtils.isEmpty(serializer) ? SerializerUtils.DEFAULT_SERIALIZER
						: (Serializer) beanFactory.getInstance(serializer);
				if (!StringUtils.isEmpty(packageName)) {
					for (Class<?> clz : ClassScanner.getInstance().getClasses(packageName)) {
						if (!clz.isInterface() || AnnotationUtils.isIgnore(clz)) {
							continue;
						}

						MethodInterceptor filter = new SimpleHttpObjectRpcMethodInterceptor(ser, sign,
								responseThrowable, address);
						BeanDefinition beanBuilder = new ProxyBeanDefinition(beanFactory, propertyFactory, clz,
								Arrays.asList(filter));
						beanDefinitions.add(beanBuilder);
					}
				}

				NodeList nodeList = node.getChildNodes();
				for (int a = 0; a < nodeList.getLength(); a++) {
					Node n = nodeList.item(a);
					if (n == null) {
						continue;
					}

					String className = XMLUtils.getNodeAttributeValue(propertyFactory, node, "interface");
					if (StringUtils.isEmpty(className)) {
						continue;
					}

					Class<?> clz = ClassUtils.forName(className);
					String mySign = XMLUtils.getNodeAttributeValue(propertyFactory, node, "sign");
					if (StringUtils.isEmpty(mySign)) {
						mySign = sign;
					}

					String myAddress = XmlBeanUtils.getAddress(propertyFactory, node);
					if (StringUtils.isEmpty(myAddress)) {
						myAddress = address;
					}

					MethodInterceptor filter = new SimpleHttpObjectRpcMethodInterceptor(ser, mySign, responseThrowable,
							myAddress);
					BeanDefinition beanBuilder = new ProxyBeanDefinition(beanFactory, propertyFactory, clz,
							Arrays.asList(filter));
					beanDefinitions.add(beanBuilder);
				}
			}
		}
	}
}
