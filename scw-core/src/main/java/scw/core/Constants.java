package scw.core;

import java.nio.charset.Charset;

import org.objectweb.asm.Opcodes;

import scw.core.utils.StringUtils;

public class Constants {
	public static final String ISO_8859_1_NAME = "ISO-8859-1";
	
	public static final Charset ISO_8859_1 = Charset.forName(ISO_8859_1_NAME);

	public static final String UTF_8_NAME = "UTF-8";
	
	public static final Charset UTF_8 = Charset.forName(UTF_8_NAME);
	
	public static final String US_ASCII_NAME = "US-ASCII";
	
	public static final Charset US_ASCII = Charset.forName(US_ASCII_NAME);

	/**
	 * 系统包名
	 */
	public static final String SYSTEM_PACKAGE_NAME = StringUtils.split(Constants.class.getPackage().getName(), '.')[0];

	/**
	 * 默认的字符集
	 */
	public static final String DEFAULT_CHARSET_NAME = GlobalPropertyFactory.getInstance()
			.getValue("constants.default.charsetName", String.class, UTF_8_NAME);

	public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

	/**
	 * The ASM version used internally throughout the framework.<br/>
	 * 默认的asm版本
	 * 
	 * @see Opcodes#ASM4
	 */
	public static final int ASM_VERSION = StringUtils
			.parseInt(GlobalPropertyFactory.getInstance().getString("constants.asm.version"), Opcodes.ASM7);

}
