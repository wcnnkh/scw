package scw.sql.orm;

import java.util.Collection;
import java.util.Collections;

import scw.core.utils.CollectionUtils;

/**
 * 获取表变更
 * 
 * @author shuchaowen
 *
 */
public class TableChange {
	private Collection<String> deleteColumns;
	private Collection<Column> addColumns;

	@SuppressWarnings("unchecked")
	public TableChange(Collection<String> deleteColumns, Collection<Column> addColumns) {
		this.deleteColumns = CollectionUtils.isEmpty(deleteColumns) ? Collections.EMPTY_LIST
				: Collections.unmodifiableCollection(deleteColumns);
		this.addColumns = CollectionUtils.isEmpty(addColumns) ? Collections.EMPTY_LIST
				: Collections.unmodifiableCollection(addColumns);
	}

	/**
	 * 获取删除的字段
	 * 
	 * @return
	 */
	public Collection<String> getDeleteColumns() {
		return deleteColumns;
	}

	/**
	 * 获取添加的字段
	 * 
	 * @return
	 */
	public Collection<Column> getAddColumnss() {
		return addColumns;
	}
}
