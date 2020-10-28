package scw.timer;

import scw.aop.annotation.AopEnable;
import scw.beans.annotation.AutoImpl;
import scw.locks.Lock;
import scw.timer.db.DBTaskLockFactory;
import scw.timer.support.CASTaskLockFactory;
import scw.timer.support.EmptyTaskLockFactory;

@AutoImpl({ CASTaskLockFactory.class, DBTaskLockFactory.class, EmptyTaskLockFactory.class})
@AopEnable(false)
public interface TaskLockFactory {
	Lock getLock(TaskConfig taskConfig, long executionTime);
}
