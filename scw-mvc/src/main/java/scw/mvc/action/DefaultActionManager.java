package scw.mvc.action;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import scw.beans.BeanFactory;
import scw.core.Constants;
import scw.core.annotation.Order;
import scw.core.instance.annotation.Configuration;
import scw.core.utils.StringUtils;
import scw.event.BasicEventDispatcher;
import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.event.ObjectEvent;
import scw.event.support.DefaultBasicEventDispatcher;
import scw.http.HttpMethod;
import scw.http.server.HttpControllerDescriptor;
import scw.http.server.ServerHttpRequest;
import scw.lang.AlreadyExistsException;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.mvc.MVCUtils;
import scw.mvc.annotation.Controller;
import scw.net.Restful;
import scw.net.Restful.RestfulMatchingResult;
import scw.util.ClassScanner;
import scw.value.Value;
import scw.value.property.PropertyFactory;
import scw.web.WebUtils;

@Configuration(order = Integer.MIN_VALUE)
public class DefaultActionManager implements ActionManager {
	protected final Logger logger = LoggerUtils.getLogger(getClass());
	private Map<Method, Action> actionMap = new LinkedHashMap<Method, Action>();
	private BasicEventDispatcher<ObjectEvent<Action>> eventDispatcher = new DefaultBasicEventDispatcher<ObjectEvent<Action>>(
			false);
	private final EnumMap<HttpMethod, Map<Restful, Action>> restfulActionMap = new EnumMap<HttpMethod, Map<Restful, Action>>(
			HttpMethod.class);
	private final Map<String, EnumMap<HttpMethod, Action>> pathActionMap = new HashMap<String, EnumMap<HttpMethod, Action>>();
	private final Map<String, EnumMap<HttpMethod, Map<String, Action>>> pathParameterActionMap = new HashMap<String, EnumMap<HttpMethod, Map<String, Action>>>();
	private String actionParameterName = "action";

	public DefaultActionManager() {
	}

	/**
	 * 优先使用此构造方法
	 * 
	 * @param beanFactory
	 * @param propertyFactory
	 */
	@Order
	public DefaultActionManager(BeanFactory beanFactory, PropertyFactory propertyFactory) {
		this(beanFactory, propertyFactory, MVCUtils.getScanAnnotationPackageName(propertyFactory));
	}

	public DefaultActionManager(BeanFactory beanFactory, PropertyFactory propertyFactory,
			String scanAnnotationPackageName) {
		for (Class<?> clz : ClassScanner.getInstance().getClasses(Constants.SYSTEM_PACKAGE_NAME,
				scanAnnotationPackageName)) {
			if (!isSupport(beanFactory, clz)) {
				continue;
			}
			for (Method method : clz.getDeclaredMethods()) {
				if (!isSupport(method)) {
					continue;
				}

				Action action = builder(beanFactory, propertyFactory, clz, method);
				if (action != null) {
					register(action);
				}
			}
		}
	}

	protected Action builder(BeanFactory beanFactory, PropertyFactory propertyFactory, Class<?> clazz, Method method) {
		if (isSupport(beanFactory, clazz) && isSupport(method)) {
			return new DefaultAction(beanFactory, propertyFactory, clazz, method);
		}
		return null;
	}

	protected boolean isSupport(BeanFactory beanFactory, Class<?> clazz) {
		Controller clzController = clazz.getAnnotation(Controller.class);
		if (clzController == null) {
			return false;
		}

		return beanFactory.isInstance(clazz);
	}

	protected boolean isSupport(Method method) {
		return method.getAnnotation(Controller.class) != null;
	}

	public final String getActionParameterName() {
		return actionParameterName;
	}

	public void setActionParameterName(String actionParameterName) {
		this.actionParameterName = actionParameterName;
	}

	public Action getAction(Method method) {
		return actionMap.get(method);
	}

	public Collection<Action> getActions() {
		return actionMap.values();
	}

	public Action getAction(ServerHttpRequest request) {
		Action action = getActionByPath(request);
		if (action != null) {
			return action;
		}

		action = getActionByParameter(request);
		if (action != null) {
			return action;
		}

		return getActionByRestful(request);
	}

	public Action getActionByRestful(ServerHttpRequest request) {
		Map<Restful, Action> map = restfulActionMap.get(request.getMethod());
		if (map == null) {
			return null;
		}

		String[] pathArr = StringUtils.split(request.getPath(), '/');
		for (Entry<Restful, Action> entry : map.entrySet()) {
			Restful restful = entry.getKey();
			RestfulMatchingResult result = restful.matching(pathArr);
			if (result.isSuccess()) {
				Restful.restfulParameterMapAware(request, result.getParameterMap());
				return entry.getValue();
			}
		}
		return null;
	}

	public Action getActionByParameter(ServerHttpRequest request) {
		if (StringUtils.isEmpty(actionParameterName)) {
			return null;
		}

		Map<HttpMethod, Map<String, Action>> map = pathParameterActionMap.get(request.getPath());
		if (map == null) {
			return null;
		}

		Map<String, Action> methodMap = map.get(request.getMethod());
		if (methodMap == null) {
			return null;
		}

		Value action = WebUtils.getParameter(request, actionParameterName);
		if (action == null || action.isEmpty()) {
			return null;
		}
		return methodMap.get(action.getAsString());
	}

	public Action getActionByPath(ServerHttpRequest request) {
		Map<HttpMethod, Action> map = pathActionMap.get(request.getPath());
		if (map == null) {
			return null;
		}

		return map.get(request.getMethod());
	}

	public void register(Action action) {
		if (logger.isTraceEnabled()) {
			logger.trace("register action: {}", action);
		}

		if (action instanceof AbstractAction) {
			((AbstractAction) action).optimization();
		}

		actionMap.put(action.getMethod(), action);
		eventDispatcher.publishEvent(new ObjectEvent<Action>(action));

		for (HttpControllerDescriptor descriptor : action.getHttpControllerDescriptors()) {
			if (!descriptor.getRestful().isRestful()) {
				continue;
			}

			Map<Restful, Action> map = restfulActionMap.get(descriptor.getMethod());
			if (map == null) {
				map = new HashMap<Restful, Action>();
			}

			if (map.containsKey(descriptor.getRestful())) {
				throw new AlreadyExistsException(
						MVCUtils.getExistActionErrMsg(action, map.get(descriptor.getRestful())));
			}

			map.put(descriptor.getRestful(), action);
			restfulActionMap.put(descriptor.getMethod(), map);
		}

		for (HttpControllerDescriptor descriptor : action.getHttpControllerDescriptors()) {
			if (descriptor.getRestful().isRestful()) {
				continue;
			}

			EnumMap<HttpMethod, Action> map = pathActionMap.get(descriptor.getPath());
			if (map == null) {
				map = new EnumMap<HttpMethod, Action>(HttpMethod.class);
			}

			if (map.containsKey(descriptor.getMethod())) {
				throw new AlreadyExistsException(
						MVCUtils.getExistActionErrMsg(action, map.get(descriptor.getMethod())));
			}
			map.put(descriptor.getMethod(), action);
			pathActionMap.put(descriptor.getPath(), map);
		}

		for (String classController : toControllerSet(action.getSourceClassHttpControllerDescriptors())) {
			for (String methodController : toControllerSet(action.getMethodHttpControllerDescriptors())) {
				for (HttpControllerDescriptor descriptor : action.getHttpControllerDescriptors()) {
					register(descriptor.getMethod(), classController, methodController, action);
				}
			}
		}
	}

	private void register(HttpMethod httpMethod, String classController, String methodController, Action action) {
		EnumMap<HttpMethod, Map<String, Action>> clzMap = pathParameterActionMap.get(classController);
		if (clzMap == null) {
			clzMap = new EnumMap<HttpMethod, Map<String, Action>>(HttpMethod.class);
		}

		Map<String, Action> map = clzMap.get(httpMethod);
		if (map == null) {
			map = new HashMap<String, Action>();
		}

		if (map.containsKey(methodController)) {
			throw new AlreadyExistsException(MVCUtils.getExistActionErrMsg(action, map.get(methodController)));
		}

		map.put(methodController, action);
		clzMap.put(httpMethod, map);
		pathParameterActionMap.put(classController, clzMap);
	}

	private Set<String> toControllerSet(Collection<HttpControllerDescriptor> httpHttpControllerDescriptors) {
		HashSet<String> actions = new HashSet<String>();
		for (HttpControllerDescriptor methodControllerDescriptor : httpHttpControllerDescriptors) {
			if (methodControllerDescriptor.getRestful().isRestful()) {
				continue;
			}

			actions.add(methodControllerDescriptor.getPath());
		}
		return actions;
	}

	public EventRegistration registerListener(EventListener<ObjectEvent<Action>> eventListener) {
		return eventDispatcher.registerListener(eventListener);
	}

}
