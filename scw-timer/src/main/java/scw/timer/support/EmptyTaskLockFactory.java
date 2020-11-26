package scw.timer.support;

import scw.core.instance.annotation.Configuration;
import scw.locks.EmptyLock;
import scw.locks.Lock;
import scw.timer.TaskConfig;
import scw.timer.TaskLockFactory;

@Configuration(order=Integer.MIN_VALUE)
public final class EmptyTaskLockFactory implements TaskLockFactory {

	public Lock getLock(TaskConfig taskConfig, long executionTime) {
		return new EmptyLock(true);
	}

}
