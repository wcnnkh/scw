package scw.application;

import scw.beans.BeanFactory;
import scw.beans.BeanLifeCycleEvent;
import scw.beans.BeanLifeCycleEvent.Step;
import scw.beans.xml.XmlBeanFactory;
import scw.core.utils.StringUtils;
import scw.event.BasicEventDispatcher;
import scw.event.EventListener;
import scw.event.EventRegistration;
import scw.event.support.DefaultBasicEventDispatcher;
import scw.lang.AlreadyExistsException;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.logger.LoggerUtils;
import scw.logger.SplitLineAppend;
import scw.util.concurrent.CountLatch;
import scw.util.concurrent.SettableListenableFuture;
import scw.value.property.PropertyFactory;

public class CommonApplication extends XmlBeanFactory implements Application, EventListener<BeanLifeCycleEvent> {
	private volatile BasicEventDispatcher<ApplicationEvent> applicationEventDispathcer;
	private volatile Logger logger;
	private final SettableListenableFuture<Application> initializationListenableFuture = new SettableListenableFuture<Application>();
	private final CountLatch countLatch = new CountLatch(0);

	public CommonApplication() {
		this(DEFAULT_CONFIG);
	}

	public CommonApplication(String xml) {
		super(new PropertyFactory(true, true), StringUtils.isEmpty(xml) ? DEFAULT_CONFIG : xml);
		addInternalSingleton(Application.class, this);
		getBeanLifeCycleEventDispatcher().registerListener(this);
	}

	public Logger getLogger() {
		if (logger == null) {
			synchronized (this) {
				if (logger == null) {
					logger = LoggerUtils.getLogger(getClass());
				}
			}
		}
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void onEvent(BeanLifeCycleEvent event) {
		if (event.getStep() == Step.AFTER_INIT) {
			Object source = event.getSource();
			if (source == null) {
				return;
			}

			if (source instanceof ApplicationAware) {
				((ApplicationAware) source).setApplication(this);
			}
		}
	}

	protected void initDefaultApplicationEventDispathcer() {
		if (applicationEventDispathcer == null) {
			synchronized (this) {
				if (applicationEventDispathcer == null) {
					applicationEventDispathcer = new DefaultBasicEventDispatcher<ApplicationEvent>(true);
				}
			}
		}
	}

	public final BasicEventDispatcher<ApplicationEvent> getApplicationEventDispathcer() {
		return applicationEventDispathcer;
	}

	public void setApplicationEventDispathcer(BasicEventDispatcher<ApplicationEvent> applicationEventDispathcer) {
		if (applicationEventDispathcer != null) {
			throw new AlreadyExistsException("ApplicationEventDispathcer");
		}
		this.applicationEventDispathcer = applicationEventDispathcer;
	}

	@Override
	public final void init() {
		try {
			super.init();
			long count = countLatch.getCount();
			getLogger().info(new SplitLineAppend("Initialized(countLatch={})"), count);
			countLatch.await();
			initializationListenableFuture.set(this);
			if(count > 0){
				getLogger().info(new SplitLineAppend("Start up complete"));
			}
		} catch (Throwable e) {
			getLogger().error(e, "Initialization error");
			initializationListenableFuture.setException(e);
		}
	}

	public SettableListenableFuture<Application> getInitializationListenableFuture() {
		return initializationListenableFuture;
	}

	@Override
	public final void destroy() {
		try {
			super.destroy();
		} catch (Throwable e) {
			getLogger().error(e, "Destroy error");
		} finally {
			LoggerFactory.getILoggerFactory().destroy();
		}
	}

	@Override
	public void afterInit() throws Throwable {
		for (ApplicationInitialization initialization : ApplicationUtils.loadAllService(ApplicationInitialization.class,
				this)) {
			initialization.init(this);
		}
		super.afterInit();
	}

	@Override
	public void beforeDestroy() throws Throwable {
		getLogger().info(new SplitLineAppend("destroy"));
		super.beforeDestroy();
	}

	@Override
	public void destroyComplete() throws Throwable {
		super.destroyComplete();
	}

	public void publishEvent(ApplicationEvent event) {
		initDefaultApplicationEventDispathcer();
		applicationEventDispathcer.publishEvent(event);
	}

	public EventRegistration registerListener(EventListener<ApplicationEvent> eventListener) {
		initDefaultApplicationEventDispathcer();
		return applicationEventDispathcer.registerListener(eventListener);
	}

	public BeanFactory getBeanFactory() {
		return this;
	}

	public CountLatch getInitializationLatch() {
		return countLatch;
	}
}
