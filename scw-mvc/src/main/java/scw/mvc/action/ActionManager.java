package scw.mvc.action;

import java.lang.reflect.Method;
import java.util.Collection;

import scw.aop.annotation.AopEnable;
import scw.event.BasicEventRegistry;
import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.event.ObjectEvent;
import scw.http.server.ServerHttpRequest;

@AopEnable(false)
public interface ActionManager extends BasicEventRegistry<ObjectEvent<Action>> {
	Action getAction(Method method);

	Collection<Action> getActions();

	Action getAction(ServerHttpRequest request);

	void register(Action action);

	/**
	 * 监听新的action注册
	 */
	EventRegistration registerListener(EventListener<ObjectEvent<Action>> eventListener);
}
