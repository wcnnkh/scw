package scw.transaction;

import scw.context.Propagation;

/**
 * 事务的定义
 * @author shuchaowen
 *
 */
public interface TransactionDefinition {
	
	Propagation getPropagation();

	Isolation getIsolation();

	int getTimeout();

	boolean isReadOnly();
}