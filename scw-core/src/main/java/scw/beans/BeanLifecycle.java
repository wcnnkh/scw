package scw.beans;

import java.util.concurrent.atomic.AtomicBoolean;

public class BeanLifecycle implements Init, Destroy {
	private final AtomicBoolean initializing = new AtomicBoolean(false);
	private final AtomicBoolean initialized = new AtomicBoolean(false);

	public void init() throws Throwable {
		if (isInitialized() || isInitializing()) {
			throw new BeansException("Already initialized");
		}

		if (initializing.compareAndSet(false, true)) {
			try {
				beforeInit();
				afterInit();
			} finally {
				try {
					initComplete();
				} catch (Exception e) {
					initialized.set(true);
				}
			}
		} else {
			throw new BeansException("Already initialized");
		}
	}

	public void destroy() throws Throwable {
		if (!isInitializing() && !isInitialized()) {
			throw new BeansException("Not yet initialized");
		}

		if (initializing.compareAndSet(true, false)) {
			try {
				beforeDestroy();
				afterDestroy();
			} finally {
				try {
					destroyComplete();
				} finally {
					initialized.set(false);
				}
			}
		} else {
			throw new BeansException("Not yet initialized");
		}
	}

	/**
	 * 是否已经初始化完毕
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return initialized.get();
	}

	public boolean isInitializing() {
		return initializing.get();
	}

	public void beforeInit() throws Throwable {
	}

	public void afterInit() throws Throwable {
	}

	public void initComplete() throws Throwable {
	}

	public void beforeDestroy() throws Throwable {
	}

	public void afterDestroy() throws Throwable {
	}

	public void destroyComplete() throws Throwable {
	}
}
