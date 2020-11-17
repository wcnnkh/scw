package scw.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import scw.core.instance.InstanceUtils;
import scw.core.utils.StringUtils;
import scw.http.HttpUtils;
import scw.http.multipart.MultipartMessageConverter;
import scw.net.message.Headers;
import scw.net.message.Message;
import scw.net.message.OutputMessage;
import scw.net.message.converter.ByteArrayMessageConverter;
import scw.net.message.converter.HttpFormMessageConveter;
import scw.net.message.converter.JsonMessageConverter;
import scw.net.message.converter.MessageConverter;
import scw.net.message.converter.MultiMessageConverter;
import scw.net.message.converter.ResourceMessageConverter;
import scw.net.message.converter.StringMessageConverter;
import scw.net.message.converter.XmlMessageConverter;
import scw.net.ssl.TrustAllManager;
import scw.util.Accept;

public final class InetUtils {
	private InetUtils() {
	};

	/**
	 * 本地ip
	 */
	private static final String[] LOCAL_IP = new String[] { "127.0.0.1", "0:0:0:0:0:0:0:1", "::1" };

	/**
	 * Regex of ip address.
	 */
	private static final String REGEX_IP = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
	private static final String INNER_IP_PATTERN = "((192\\.168|172\\.([1][6-9]|[2]\\d|3[01]))"
			+ "(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}|"
			+ "^(\\D)*10(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){3})";

	/**
	 * 一个信任所有的ssl socket factory <br/>
	 * 注意:在初始化失败后可能为空
	 */
	public static final SSLSocketFactory TRUSE_ALL_SSL_SOCKET_FACTORY;

	private static final MultiMessageConverter MESSAGE_CONVERTER = new MultiMessageConverter();

	static {
		// 创建一个信任所有的
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new TrustAllManager();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = null;
		try {
			sc = javax.net.ssl.SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		TRUSE_ALL_SSL_SOCKET_FACTORY = sc == null ? null : sc.getSocketFactory();

		MESSAGE_CONVERTER.add(new JsonMessageConverter());
		MESSAGE_CONVERTER.add(new StringMessageConverter());
		MESSAGE_CONVERTER.add(new ByteArrayMessageConverter());
		MESSAGE_CONVERTER.add(new XmlMessageConverter());
		MESSAGE_CONVERTER.add(new HttpFormMessageConveter());

		if (HttpUtils.isSupportMultiPart()) {
			MESSAGE_CONVERTER.add(new MultipartMessageConverter());
		}

		MESSAGE_CONVERTER.add(new ResourceMessageConverter());
		MESSAGE_CONVERTER.addAll(InstanceUtils.loadAllService(MessageConverter.class));
	}

	public static MessageConverter getMessageConverter() {
		return MESSAGE_CONVERTER;
	}

	public static List<InetSocketAddress> parseInetSocketAddressList(String address) {
		List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		String[] arr = StringUtils.commonSplit(address);
		for (String a : arr) {
			String[] vs = a.split(":");
			String h = vs[0];
			int port = 11211;
			if (vs.length == 2) {
				port = Integer.parseInt(vs[1]);
			}

			addresses.add(new InetSocketAddress(h, port));
		}
		return addresses;
	}

	public static URI toURI(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Failed to URI", e);
		}
	}

	public static void writeHeader(Message inputMessage, OutputMessage outputMessage) throws IOException {
		long len = outputMessage.getContentLength();
		if (len >= 0) {
			outputMessage.setContentLength(len);
		}

		MimeType mimeType = inputMessage.getContentType();
		if (mimeType != null) {
			outputMessage.setContentType(mimeType);
		}

		Headers headers = inputMessage.getHeaders();
		if (headers != null) {
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				for (String value : entry.getValue()) {
					outputMessage.getHeaders().add(entry.getKey(), value);
				}
			}
		}
	}

	public static boolean isLocalIP(String ip) {
		if (StringUtils.isEmpty(ip)) {
			return false;
		}

		for (String local : LOCAL_IP) {
			if (local.equals(ip)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return whether input matches regex of ip address.
	 *
	 * @param input
	 *            The input.
	 * @return {@code true}: yes<br>
	 *         {@code false}: no
	 */
	public static boolean isIP(final CharSequence ip) {
		return StringUtils.isNotEmpty(ip) && Pattern.matches(REGEX_IP, ip);
	}

	/**
	 * 判断是否是内网IP
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isInnerIP(String ip) {
		Pattern p = Pattern.compile(INNER_IP_PATTERN);
		Matcher matcher = p.matcher(ip);
		return matcher.find();
	}

	public static String getFilename(String url) {
		String urlToUse = url;
		int prefixIndex = urlToUse.indexOf("//");
		if (prefixIndex != -1) {
			urlToUse = url.substring(prefixIndex + 2);
		}

		int index = urlToUse.indexOf("/");
		if (index == -1 && prefixIndex != -1) {
			return null;
		}

		if (index != -1) {
			urlToUse = urlToUse.substring(index);
		}

		index = urlToUse.indexOf("#");
		if (index != -1) {
			urlToUse = url.substring(0, index);
		}

		index = urlToUse.indexOf("?");
		if (index != -1) {
			urlToUse = url.substring(0, index);
		}

		return StringUtils.getFilename(urlToUse);
	}

	/**
	 * 排除虚拟接口和没有启动运行的接口
	 */
	public static final Accept<NetworkInterface> LOCAL_IP_NETWORK_INTERFACE_ACCEPT = new Accept<NetworkInterface>() {
		public boolean accept(NetworkInterface networkInterface) {
			if (networkInterface.isVirtual()) {
				return false;
			}

			try {
				if (!networkInterface.isUp()) {
					return false;
				}
			} catch (SocketException e) {
				return false;
			}
			return true;
		};
	};

	public static final Accept<InetAddress> IPV4_INET_ADDRESS_ACCEPT = new Accept<InetAddress>() {
		public boolean accept(InetAddress inetAddress) {
			return inetAddress instanceof Inet4Address;
		};
	};

	public static Set<InetAddress> getLocalIpAddresses(boolean ipv4) {
		return getLocalIpAddresses(ipv4? IPV4_INET_ADDRESS_ACCEPT : null);
	}
	
	public static Set<InetAddress> getLocalIpAddresses(Accept<InetAddress> accept) {
		return getLocalIpAddresses(LOCAL_IP_NETWORK_INTERFACE_ACCEPT, accept);
	}

	public static Set<InetAddress> getLocalIpAddresses(Accept<NetworkInterface> networkInterfaceAccept,
			Accept<InetAddress> accept) {
		Enumeration<NetworkInterface> allNetInterfaces;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return Collections.emptySet();
		}

		Set<InetAddress> ips = new LinkedHashSet<InetAddress>(8);
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = allNetInterfaces.nextElement();
			if (netInterface == null
					|| (networkInterfaceAccept != null && !networkInterfaceAccept.accept(netInterface))) {
				continue;
			}

			Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				if (address == null || (accept != null && !accept.accept(address))) {
					continue;
				}

				if (isLocalIP(address.getHostAddress())) {
					continue;
				}

				ips.add(address);
			}
		}
		return ips;
	}

	public static boolean isUrl(String url) {
		if(StringUtils.isEmpty(url)){
			return false;
		}
		
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
}
