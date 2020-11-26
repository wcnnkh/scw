package scw.timer;

import scw.aop.annotation.AopEnable;
import scw.locks.Lock;

@AopEnable(false)
public interface TaskLockFactory {
	Lock getLock(TaskConfig taskConfig, long executionTime);
}
