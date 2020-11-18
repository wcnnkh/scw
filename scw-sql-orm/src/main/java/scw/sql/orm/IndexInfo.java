package scw.sql.orm;

import java.io.Serializable;

import scw.sql.orm.enums.IndexMethod;
import scw.sql.orm.enums.IndexOrder;
import scw.sql.orm.enums.IndexType;

/**
 * 索引名和索引方法相同被视为同一组索引
 * @author shuchaowen
 *
 */
public class IndexInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Column column;
	private final String name;
	private final IndexType type;
	private final int length;
	private final IndexMethod method;
	private IndexOrder order;

	public IndexInfo(Column column, String name, IndexType type, int length,
			IndexMethod method, IndexOrder order) {
		this.column = column;
		this.name = name;
		this.method = method;
		this.type = type;
		this.length = length;
		this.order = order;
	}

	public Column getColumn() {
		return column;
	}

	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}

	public IndexOrder getOrder() {
		return order;
	}

	public void setOrder(IndexOrder order) {
		this.order = order;
	}

	public IndexType getType() {
		return type;
	}

	public IndexMethod getMethod() {
		return method;
	}

	@Override
	public final int hashCode() {
		return getName().hashCode() + getMethod().hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof IndexInfo) {
			IndexInfo indexInfo = (IndexInfo) obj;
			return indexInfo.getName().equals(getName())
					&& indexInfo.getMethod() == getMethod();
		}
		return false;
	}
}
