package scw.application.crontab;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import scw.beans.BeanFactory;
import scw.beans.MethodProxyInvoker;
import scw.core.CrontabTask;
import scw.core.aop.Invoker;
import scw.core.exception.AlreadyExistsException;
import scw.core.logger.Logger;
import scw.core.logger.LoggerFactory;
import scw.core.utils.AnnotationUtils;
import scw.core.utils.ArrayUtils;
import scw.core.utils.ClassUtils;

public final class CrontabAnnotationUtils {
	private static Logger logger = LoggerFactory.getLogger(CrontabAnnotationUtils.class);

	private CrontabAnnotationUtils() {
	};

	public static void crontabService(Collection<Class<?>> classList, BeanFactory beanFactory, String[] filters) {
		HashSet<String> taskNameSet = new HashSet<String>();
		for (Class<?> clz : classList) {
			for (Method method : AnnotationUtils.getAnnoationMethods(clz, true, true, Crontab.class)) {
				Crontab c = method.getAnnotation(Crontab.class);
				if (taskNameSet.contains(c.name())) {
					throw new AlreadyExistsException("任务：" + c.name() + "已经存在");
				}
				scw.core.Crontab crontab = beanFactory.get(scw.core.Crontab.class);

				boolean invokeTime = !ArrayUtils.isEmpty(method.getParameterTypes())
						&& method.getParameterTypes().length == 1
						&& ClassUtils.isLongType(method.getParameterTypes()[1]);
				CrontabRunnable crontabRun = new CrontabRunnable(c.name(), beanFactory.get(c.factory()),
						new MethodProxyInvoker(beanFactory, clz, method, filters), invokeTime);
				crontab.crontab(c.dayOfWeek(), c.month(), c.dayOfMonth(), c.hour(), c.minute(), crontabRun);
				logger.info("添加计划任务：{},dayOfWeek={},month={},dayOfMonth={},hour={},minute={}", c.name(), c.dayOfWeek(),
						c.month(), c.dayOfMonth(), c.hour(), c.minute());
			}
		}
	}
}

final class CrontabRunnable implements CrontabTask {
	private static Logger logger = LoggerFactory.getLogger(Crontab.class);
	private final String name;
	private final Invoker invoker;
	private final CrontabContextFactory crontabContextFactory;
	private final boolean invokeTime;

	public CrontabRunnable(String name, CrontabContextFactory crontabContextFactory, Invoker invoker,
			boolean invokeTime) {
		this.name = name;
		this.crontabContextFactory = crontabContextFactory;
		this.invoker = invoker;
		this.invokeTime = invokeTime;
	}

	public void run(long executionTime) {
		CrontabContext context = crontabContextFactory.getContext(name, executionTime);
		if (!context.begin()) {
			context.completet();
			return;
		}

		logger.info("开始执行{}", name);
		try {
			if (invokeTime) {
				invoker.invoke(executionTime);
			} else {
				invoker.invoke();
			}
			logger.info("执行{}成功", name);
			context.end();
		} catch (Throwable e) {
			logger.error(e, "执行{}异常", name);
			context.error(e);
		} finally {
			context.completet();
		}
	}
}