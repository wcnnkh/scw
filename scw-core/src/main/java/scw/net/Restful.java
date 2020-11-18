package scw.net;

import java.io.Serializable;

import scw.core.Assert;
import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;
import scw.util.DefaultStringMatcher;
import scw.util.LinkedMultiValueMap;
import scw.util.MultiValueMap;
import scw.util.XUtils;

/**
 * REST全称是Representational State Transfer，中文意思是表述（编者注：通常译为表征）性状态转移。 <br/>
 * 它首次出现在2000年Roy Fielding的博士论文中，Roy Fielding是HTTP规范的主要编写者之一。 <br/>
 * 他在论文中提到：
 * "我这篇文章的写作目的，就是想在符合架构原理的前提下，理解和评估以网络为基础的应用软件的架构设计，得到一个功能强、性能好、适宜通信的架构。REST指的是一组架构约束条件和原则。"
 * <br/>
 * 如果一个架构符合REST的约束条件和原则，我们就称它为RESTful架构。 <br/>
 * REST本身并没有创造新的技术、组件或服务，而隐藏在RESTful背后的理念就是使用Web的现有特征和能力， 更好地使用现有Web标准中的一些准则和约束。
 * <br/>
 * 虽然REST本身受Web技术的影响很深， 但是理论上REST架构风格并不是绑定在HTTP上，只不过目前HTTP是唯一与REST相关的实例。
 * 
 * @author shuchaowen
 *
 */
public final class Restful {
	private static final char PATH_SPLIT = '/';
	private final RestfulPath[] paths;
	private final String sourcePath;

	public Restful(String sourcePath) {
		Assert.requiredArgument(sourcePath != null, "sourcePath");
		this.sourcePath = sourcePath;
		String[] paths = StringUtils.split(sourcePath, PATH_SPLIT);
		this.paths = new RestfulPath[paths.length];
		for (int i = 0; i < paths.length; i++) {
			this.paths[i] = new RestfulPath(paths[i]);
		}
	}

	public RestfulPath[] getPaths() {
		return paths.clone();
	}

	public boolean isRestful() {
		for (RestfulPath path : paths) {
			if (path.isWildcard() || path.isPlaceholder()) {
				return true;
			}
		}
		return false;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	@Override
	public int hashCode() {
		int code = 0;
		for (RestfulPath path : paths) {
			code += path.hashCode();
		}
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof Restful) {
			Restful restful = (Restful) obj;
			if (restful.paths.length != paths.length) {
				return false;
			}

			for (int i = 0; i < paths.length; i++) {
				if (!paths[i].equals(restful.paths[i])) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	public RestfulMatchingResult matching(String[] paths) {
		if (paths.length != this.paths.length) {
			return RestfulMatchingResult.ERROR;
		}

		MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<String, String>(paths.length);
		for (int i = 0; i < paths.length; i++) {
			String value = paths[i];
			RestfulPath path = this.paths[i];
			if (!path.match(value)) {
				return RestfulMatchingResult.ERROR;
			}

			if (path.isPlaceholder()) {
				parameterMap.add(path.getPath(), value);
			}
		}
		return new RestfulMatchingResult(parameterMap);
	}

	public RestfulMatchingResult matching(String requestPath) {
		return matching(StringUtils.split(requestPath, PATH_SPLIT));
	}

	@Override
	public String toString() {
		return sourcePath;
	}

	public static boolean restfulParameterMapAware(Object instance, MultiValueMap<String, String> parameterMap) {
		if (instance instanceof RestfulParameterMapAware) {
			RestfulParameterMapAware aware = XUtils.getTarget(instance, RestfulParameterMapAware.class);
			if(aware != null){
				aware.setRestfulParameterMap(parameterMap);
				return true;
			}
		}
		return false;
	}

	public static final class RestfulMatchingResult implements Serializable {
		private static final long serialVersionUID = 1L;
		public static final RestfulMatchingResult ERROR = new RestfulMatchingResult();
		private final boolean success;
		private final MultiValueMap<String, String> parameterMap;

		private RestfulMatchingResult() {
			this.success = false;
			this.parameterMap = CollectionUtils.emptyMultiValueMap();
		}

		private RestfulMatchingResult(MultiValueMap<String, String> parameterMap) {
			this.success = true;
			this.parameterMap = parameterMap;
		}

		public boolean isSuccess() {
			return success;
		}

		public MultiValueMap<String, String> getParameterMap() {
			return parameterMap;
		}
	}

	public static final class RestfulPath {
		private static final String PLACEHOLDER_BEGIN = "{";
		private static final String PLACEHOLDER_END = "}";

		private final String path;
		private final boolean wildcard;
		private final boolean placeholder;// 是否是点位符
		private final String targetPath;

		public RestfulPath(String path) {
			Assert.requiredArgument(path != null, "path");
			this.targetPath = path;
			this.placeholder = path.startsWith(PLACEHOLDER_BEGIN) && path.endsWith(PLACEHOLDER_END);
			this.wildcard = DefaultStringMatcher.getInstance().isPattern(path);
			this.path = placeholder
					? path.substring(PLACEHOLDER_BEGIN.length(), path.length() - PLACEHOLDER_END.length()) : path;
		}

		public String getPath() {
			return path;
		}

		public String getTargetPath() {
			return targetPath;
		}

		public boolean isWildcard() {
			return wildcard;
		}

		public boolean isPlaceholder() {
			return placeholder;
		}

		public boolean match(String path) {
			if (isWildcard()) {
				return DefaultStringMatcher.getInstance().match(this.path, path);
			} else if (isPlaceholder()) {
				return true;
			} else {
				return this.path.equals(path);
			}
		}

		@Override
		public String toString() {
			return targetPath;
		}

		@Override
		public int hashCode() {
			if (isWildcard() || isPlaceholder()) {
				return 0;
			}

			return path.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}

			if (obj == this) {
				return true;
			}

			if (obj instanceof RestfulPath) {
				if (isWildcard() || isPlaceholder()) {
					return true;
				}

				return path.equals(((RestfulPath) obj).path);
			}
			return false;
		}
	}
}
