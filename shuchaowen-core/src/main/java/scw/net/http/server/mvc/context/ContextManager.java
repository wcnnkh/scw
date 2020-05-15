package scw.net.http.server.mvc.context;

import scw.context.Context;
import scw.context.Propagation;
import scw.context.ThreadLocalDefaultContextManager;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.net.http.server.mvc.HttpChannel;
import scw.net.http.server.mvc.action.Action;
import scw.net.http.server.mvc.action.ActionFilter;
import scw.net.http.server.mvc.action.ActionService;

public class ContextManager extends ThreadLocalDefaultContextManager {
	private static Logger logger = LoggerUtils.getLogger(ContextManager.class);
	private static ContextManager instance = new ContextManager();

	private ContextManager() {
		super();
	};

	public static ContextManager getInstance() {
		return instance;
	}

	public static void bindAction(Context context, Action action) {
		if (context.getResource(Action.class) == null) {
			context.bindResource(Action.class, action);
		} else {
			logger.warn("context bind action is exist: {}", action.toString());
		}
	}

	public static Action getAction(Context context) {
		return (Action) context.getResource(Action.class);
	}

	public static void bindChannel(Context context, HttpChannel httpChannel) {
		if (context.getResource(HttpChannel.class) == null) {
			context.bindResource(HttpChannel.class, httpChannel);
		} else {
			logger.warn("context bind channel is exist: {}", httpChannel.toString());
		}
	}

	public static HttpChannel getChannel(Context context) {
		return (HttpChannel) context.getResource(HttpChannel.class);
	}

	public static Action getCurrentAction() {
		Context context = getInstance().getContext();
		return context == null ? null : getAction(context);
	}

	public static HttpChannel getCurrentChannel() {
		Context context = getInstance().getContext();
		return context == null ? null : getChannel(context);
	}

	public static Object doAction(HttpChannel httpChannel, Action action, ActionService actionService)
			throws Throwable {
		return getInstance().execute(Propagation.REQUIRES_NEW,
				new ActionContextExecute(httpChannel, action, actionService));
	}

	public static Object doFilter(HttpChannel httpChannel, Action action, ActionFilter actionFilter,
			ActionService service) throws Throwable {
		return getInstance().execute(Propagation.REQUIRES_NEW,
				new FilterContextExecute(httpChannel, action, actionFilter, service));
	}
}
