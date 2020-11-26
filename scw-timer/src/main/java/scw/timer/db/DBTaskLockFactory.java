package scw.timer.db;

import scw.core.instance.annotation.Configuration;
import scw.db.DB;
import scw.locks.Lock;
import scw.sql.SimpleSql;
import scw.sql.Sql;
import scw.timer.TaskConfig;
import scw.timer.TaskLockFactory;

@Configuration(order=Integer.MIN_VALUE + 1)
public class DBTaskLockFactory implements TaskLockFactory {
	private DB db;

	public DBTaskLockFactory(DB db) {
		this.db = db;
		db.createTable(TaskLockTable.class, false);
	}

	public final Lock getLock(TaskConfig taskConfig, long executionTime) {
		return new TableLock(db, createLockSql(taskConfig, executionTime));
	}

	protected Sql createLockSql(TaskConfig taskConfig, long executionTime) {
		return new SimpleSql(
				"insert into task_lock_table (taskId, lastTime) value (?, ?) on duplicate key update lastTime=if(lastTime<?, ?, lastTime)",
				taskConfig.getTaskId(), executionTime, executionTime, executionTime);
	}
}
