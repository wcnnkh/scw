package scw.mvc.support;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import scw.lang.AlreadyExistsException;
import scw.mvc.MVCUtils;
import scw.mvc.http.HttpChannel;
import scw.net.http.Method;

public final class HttpParameterActionFactory extends HttpActionFactory {
	private final Map<String, EnumMap<Method, Map<String, HttpAction>>> actionMap = new HashMap<String, EnumMap<Method, Map<String, HttpAction>>>();
	private String key;

	public HttpParameterActionFactory(String key) {
		this.key = key;
	}

	@Override
	public HttpAction getAction(HttpChannel httpChannel) {
		if (key == null) {
			return null;
		}

		Map<Method, Map<String, HttpAction>> map = actionMap.get(httpChannel.getRequest().getControllerPath());
		if (map == null) {
			return null;
		}

		Map<String, HttpAction> methodMap = map.get(httpChannel.getRequest().getMethod());
		if (methodMap == null) {
			return null;
		}

		String action = httpChannel.getString(key);
		if (action == null) {
			return null;
		}
		return methodMap.get(action);
	}

	@Override
	public void scanning(HttpAction action, HttpControllerConfig controllerConfig) {
		EnumMap<Method, Map<String, HttpAction>> clzMap = actionMap.get(controllerConfig.getClassController());
		if (clzMap == null) {
			clzMap = new EnumMap<Method, Map<String, HttpAction>>(Method.class);
		}

		Map<String, HttpAction> map = clzMap.get(controllerConfig.getHttpMethod());
		if (map == null) {
			map = new HashMap<String, HttpAction>();
		}

		if (map.containsKey(controllerConfig.getMethodController())) {
			throw new AlreadyExistsException(
					MVCUtils.getExistActionErrMsg(action, map.get(controllerConfig.getMethodController())));
		}

		map.put(controllerConfig.getMethodController(), action);
		clzMap.put(controllerConfig.getHttpMethod(), map);
		actionMap.put(controllerConfig.getClassController(), clzMap);
	}
}
