package scw.data;

import java.util.HashMap;
import java.util.Map;

import scw.transaction.Transaction;
import scw.transaction.TransactionManager;

/**
 * 事务缓存
 * 
 * @author shuchaowen
 *
 */
@SuppressWarnings("unchecked")
public final class TransactionContextCache extends AbstractMapCache implements Cache {
	private final Object name;
	
	public TransactionContextCache(Object name){
		this.name = name;
	};
	
	public void clear(){
		Map<String, Object> map = getMap();
		if(map != null){
			map.clear();
		}
	}
	
	public Map<String, Object> getMap() {
		Transaction transaction = TransactionManager.getCurrentTransaction();
		if (transaction == null) {
			return null;
		}

		return (Map<String, Object>) transaction.getResource(name);
	}

	@Override
	protected Map<String, Object> createMap() {
		Transaction transaction = TransactionManager.getCurrentTransaction();
		if (transaction == null) {
			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>(8);
		transaction.bindResource(name, map);
		return map;
	}
}
