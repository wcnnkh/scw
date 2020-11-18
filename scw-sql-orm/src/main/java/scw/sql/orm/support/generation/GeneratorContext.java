package scw.sql.orm.support.generation;

import scw.sql.orm.Column;
import scw.sql.orm.EntityOperations;
import scw.sql.orm.enums.OperationType;
import scw.util.attribute.DefaultAttributes;

public final class GeneratorContext extends DefaultAttributes<Object, Object> {
	private Column column;
	private final EntityOperations entityOperations;
	private final OperationType operationType;
	private final Object bean;
	private final String tableName;// ORM 入参，并非实际表名

	public GeneratorContext(EntityOperations entityOperations, OperationType operationType, Object bean,
			String tableName) {
		this.entityOperations = entityOperations;
		this.operationType = operationType;
		this.bean = bean;
		this.tableName = tableName;
	}

	public final OperationType getOperationType() {
		return operationType;
	}

	public final Object getBean() {
		return bean;
	}

	public Column getColumn() {
		return column;
	}

	public void setColumn(Column column) {
		this.column = column;
	}

	public EntityOperations getEntityOperations() {
		return entityOperations;
	}

	/**
	 * 入参，并非实际表名
	 * 
	 * @return
	 */
	public final String getTableName() {
		return tableName;
	}
}
