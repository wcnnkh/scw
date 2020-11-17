package scw.timer;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

import scw.aop.Invoker;
import scw.application.Application;
import scw.application.ApplicationInitialization;
import scw.beans.BeanFactory;
import scw.beans.BeanUtils;
import scw.core.annotation.AnnotationUtils;
import scw.core.instance.annotation.Configuration;
import scw.core.reflect.ReflectionUtils;
import scw.core.utils.ArrayUtils;
import scw.timer.annotation.Crontab;
import scw.timer.annotation.Schedule;
import scw.timer.support.SimpleCrontabConfig;
import scw.timer.support.SimpleTimerTaskConfig;
import scw.util.ClassScanner;
import scw.value.ValueFactory;

@Configuration(order = Integer.MIN_VALUE)
public final class TimerAnnotationScan implements ApplicationInitialization {

	public void init(Application application) throws Throwable {
		Timer timer = application.getBeanFactory().getInstance(Timer.class);
		for (Class<?> clz : ClassScanner.getInstance()
				.getClasses(getScanAnnotationPackageName(application.getPropertyFactory()))) {
			if (!ReflectionUtils.isPresent(clz)) {
				continue;
			}

			for (Method method : AnnotationUtils.getAnnoationMethods(clz, true, true, Schedule.class)) {
				Schedule schedule = method.getAnnotation(Schedule.class);
				schedule(application.getBeanFactory(), clz, method, timer, schedule);
			}

			for (Method method : AnnotationUtils.getAnnoationMethods(clz, true, true, Crontab.class)) {
				Crontab c = method.getAnnotation(Crontab.class);
				crontab(application.getBeanFactory(), clz, method, timer, c);
			}
		}
	}

	public String getScanAnnotationPackageName(ValueFactory<String> propertyFactory) {
		return propertyFactory.getValue("scw.scan.crontab.package", String.class,
				BeanUtils.getScanAnnotationPackageName(propertyFactory));
	}

	private Task getTask(BeanFactory beanFactory, Class<?> clz, Method method) {
		Class<?> parameterType = ArrayUtils.isEmpty(method.getParameterTypes()) ? null : method.getParameterTypes()[0];
		return new CrontabRunnable(beanFactory.getAop().getProxyMethod(beanFactory, clz, method), parameterType);
	}

	private void schedule(BeanFactory beanFactory, Class<?> clz, Method method, Timer timer, Schedule schedule) {
		Delayed delayed = beanFactory.isInstance(schedule.delay()) ? beanFactory.getInstance(schedule.delay()) : null;
		ScheduleTaskConfig config = new SimpleTimerTaskConfig(schedule.name(), getTask(beanFactory, clz, method),
				getTaskListener(beanFactory, schedule.listener()), delayed, schedule.period(), schedule.timeUnit());
		timer.schedule(config);
	}

	private TaskListener getTaskListener(BeanFactory beanFactory, Class<? extends TaskListener> taskListenerClazz) {
		return beanFactory.isInstance(taskListenerClazz) ? beanFactory.getInstance(taskListenerClazz) : null;
	}

	private void crontab(BeanFactory beanFactory, Class<?> clz, Method method, Timer timer, Crontab crontab) {
		timer.crontab(new SimpleCrontabConfig(crontab, getTask(beanFactory, clz, method),
				getTaskListener(beanFactory, crontab.listener())));
	}

	private static final class CrontabRunnable implements Task {
		private final Invoker invoker;
		private final Class<?> parameterType;

		public CrontabRunnable(Invoker invoker, Class<?> parameterType) {
			this.invoker = invoker;
			this.parameterType = parameterType;
		}

		public void run(long executionTime) throws Throwable {
			if (parameterType == null) {
				invoker.invoke();
			} else {
				Object value = executionTime;
				if (parameterType == Calendar.class) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(executionTime);
					value = calendar;
				} else if (parameterType == Date.class) {
					value = new Date(executionTime);
				}
				invoker.invoke(value);
			}
		}
	}
}
