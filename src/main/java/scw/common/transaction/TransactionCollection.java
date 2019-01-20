package scw.common.transaction;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public final class TransactionCollection extends LinkedList<Transaction>
		implements Transaction {
	private static final long serialVersionUID = 1L;
	private int beginTag = 0;
	private int processTag = 0;

	public TransactionCollection() {
		super();
	}

	public TransactionCollection(Collection<? extends Transaction> transactions) {
		super(transactions);
	}

	public void clear() {
		super.clear();
		beginTag = 0;
		processTag = 0;
	}

	public void begin() throws Exception {
		Iterator<Transaction> iterator = iterator();
		for (; iterator.hasNext(); beginTag++) {
			Transaction transaction = iterator.next();
			if (transaction != null) {
				transaction.begin();
			}
		}
	}

	public void process() throws Exception {
		Iterator<Transaction> iterator = iterator();
		for (; iterator.hasNext(); processTag++) {
			Transaction transaction = iterator.next();
			if (transaction != null) {
				transaction.process();
			}
		}
	}

	public void end() throws Exception {
		Iterator<Transaction> iterator = iterator();
		for (; beginTag >= 0 && iterator.hasNext(); beginTag--) {
			Transaction transaction = iterator.next();
			if (transaction != null) {
				try {
					transaction.end();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void rollback() throws Exception {
		Iterator<Transaction> iterator = iterator();
		for (; processTag >= 0 && iterator.hasNext(); processTag--) {
			Transaction transaction = iterator.next();
			if (transaction != null) {
				try {
					transaction.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
