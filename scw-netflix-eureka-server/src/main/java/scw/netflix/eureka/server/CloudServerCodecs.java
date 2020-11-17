package scw.netflix.eureka.server;

import com.netflix.discovery.converters.wrappers.CodecWrapper;
import com.netflix.discovery.converters.wrappers.CodecWrappers;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.resources.DefaultServerCodecs;

public class CloudServerCodecs extends DefaultServerCodecs {

	/**
	 * A {@link CloudJacksonJson} instance.
	 */
	public static final CloudJacksonJson JACKSON_JSON = new CloudJacksonJson();

	private static CodecWrapper getFullJson(EurekaServerConfig serverConfig) {
		CodecWrapper codec = CodecWrappers.getCodec(serverConfig.getJsonCodecName());
		return codec == null ? CodecWrappers.getCodec(JACKSON_JSON.codecName()) : codec;
	}

	private static CodecWrapper getFullXml(EurekaServerConfig serverConfig) {
		CodecWrapper codec = CodecWrappers.getCodec(serverConfig.getXmlCodecName());
		return codec == null ? CodecWrappers.getCodec(CodecWrappers.XStreamXml.class) : codec;
	}

	public CloudServerCodecs(EurekaServerConfig serverConfig) {
		super(getFullJson(serverConfig), CodecWrappers.getCodec(CodecWrappers.JacksonJsonMini.class),
				getFullXml(serverConfig), CodecWrappers.getCodec(CodecWrappers.JacksonXmlMini.class));
	}
}
