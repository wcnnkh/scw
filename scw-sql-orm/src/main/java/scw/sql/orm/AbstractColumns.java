package scw.sql.orm;

import java.util.LinkedHashSet;
import java.util.Set;

import scw.util.Accept;

public abstract class AbstractColumns implements Columns {

	public Set<Column> toSet() {
		LinkedHashSet<Column> columns = new LinkedHashSet<Column>();
		for (Column column : this) {
			if (columns.contains(column)) {
				continue;
			}

			columns.add(column);
		}
		return columns;
	}

	public Set<Column> getColumns() {
		LinkedHashSet<Column> columns = new LinkedHashSet<Column>();
		for (Column column : this) {
			if (column.isEntity() || columns.contains(column)) {
				continue;
			}

			columns.add(column);
		}
		return columns;
	}

	public Set<Column> getPrimaryKeys() {
		LinkedHashSet<Column> columns = new LinkedHashSet<Column>();
		for (Column column : this) {
			if (!column.isPrimaryKey() || column.isEntity() || columns.contains(column)) {
				continue;
			}

			columns.add(column);
		}
		return columns;
	}

	public Set<Column> getNotPrimaryKeys() {
		LinkedHashSet<Column> columns = new LinkedHashSet<Column>();
		for (Column column : this) {
			if (column.isPrimaryKey() || column.isEntity() || columns.contains(column)) {
				continue;
			}

			columns.add(column);
		}
		return columns;
	}

	public Column find(String name) {
		for (Column column : this) {
			if (column.isEntity()) {
				continue;
			}

			if (column.getName().equals(name) || column.getField().getGetter().getName().equalsIgnoreCase(name)
					|| column.getField().getSetter().getName().equalsIgnoreCase(name)) {
				return column;
			}
		}
		return null;
	}

	public Column find(Accept<Column> accept) {
		for (Column column : this) {
			if (accept == null || accept.accept(column)) {
				return column;
			}
		}
		return null;
	}
}
