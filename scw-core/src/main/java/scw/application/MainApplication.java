package scw.application;

import java.util.Map.Entry;

import scw.logger.LoggerUtils;
import scw.util.concurrent.ListenableFuture;

public class MainApplication extends CommonApplication implements Application, Runnable {
	private final Class<?> mainClass;
	private final MainArgs mainArgs;

	public MainApplication(Class<?> mainClass, String[] args) {
		this.mainClass = mainClass;
		this.mainArgs = new MainArgs(args);
		setClassLoader(mainClass.getClassLoader());
		ApplicationUtils.main(mainClass, mainArgs);
		for (Entry<String, String> entry : this.mainArgs.getParameterMap().entrySet()) {
			getPropertyFactory().put(entry.getKey(), entry.getValue());
		}

		setLogger(LoggerUtils.getLogger(mainClass));
		if (args != null) {
			getLogger().debug("args: {}", this.mainArgs);
		}
	}

	public Class<?> getMainClass() {
		return mainClass;
	}

	public MainArgs getMainArgs() {
		return mainArgs;
	}

	@Override
	public void beforeInit() throws Throwable {
		Thread shutdown = new Thread(new Runnable() {

			public void run() {
				if (MainApplication.this.isInitialized()) {
					MainApplication.this.destroy();
				}
			}
		}, mainClass.getSimpleName() + "-shutdown");
		Runtime.getRuntime().addShutdownHook(shutdown);
		super.beforeInit();
	}

	@Override
	public void afterInit() throws Throwable {
		if (getBeanFactory().isInstance(Main.class)) {
			getBeanFactory().getInstance(Main.class).main(this);
		}
		super.afterInit();
	}

	public void run() {
		init();
		while (true) {
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public static ListenableFuture<Application> run(Class<?> mainClass, String[] args) {
		MainApplication mainApplication = new MainApplication(mainClass, args);
		return ApplicationUtils.run(mainApplication);
	}

	public static final ListenableFuture<Application> run(Class<?> mainClass) {
		return run(mainClass, null);
	}
}
