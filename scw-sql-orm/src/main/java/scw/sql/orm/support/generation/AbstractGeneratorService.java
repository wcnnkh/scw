package scw.sql.orm.support.generation;

import scw.data.generator.SequenceId;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.mapper.MapperUtils;
import scw.sql.orm.Column;
import scw.sql.orm.ORMException;
import scw.sql.orm.OrmUtils;
import scw.sql.orm.enums.OperationType;
import scw.sql.orm.support.generation.annotation.CreateTime;
import scw.sql.orm.support.generation.annotation.Generator;
import scw.util.Accept;
import scw.util.XUtils;

public abstract class AbstractGeneratorService implements GeneratorService {
	protected Logger logger = LoggerUtils.getLogger(getClass());
	private final TemporaryVariable temporaryVariable = new TemporaryVariable();

	public TemporaryVariable getTemporaryVariable() {
		return temporaryVariable;
	}

	public void process(GeneratorContext generatorContext) throws ORMException {
		if (generatorContext.getOperationType() == OperationType.SAVE
				|| (generatorContext.getOperationType() == OperationType.SAVE_OR_UPDATE && !MapperUtils
						.isExistValue(generatorContext.getColumn().getField(), generatorContext.getBean()))) {
			scw.sql.orm.support.generation.annotation.SequenceId sequenceId = generatorContext.getColumn()
					.getAnnotatedElement().getAnnotation(scw.sql.orm.support.generation.annotation.SequenceId.class);
			if (sequenceId != null) {
				generatorContext.getColumn().set(generatorContext.getBean(), getSequenceId(generatorContext).getId());
				return;
			}

			CreateTime createTime = generatorContext.getColumn().getAnnotatedElement().getAnnotation(CreateTime.class);
			if (createTime != null) {
				generatorContext.getColumn().set(generatorContext.getBean(), getCreateTime(generatorContext));
				return;
			}

			scw.sql.orm.support.generation.annotation.UUID uuid = generatorContext.getColumn().getAnnotatedElement()
					.getAnnotation(scw.sql.orm.support.generation.annotation.UUID.class);
			if (uuid != null) {
				generatorContext.getColumn().set(generatorContext.getBean(), getUUID(generatorContext));
				return;
			}

			Generator generator = generatorContext.getColumn().getAnnotatedElement().getAnnotation(Generator.class);
			if (generator != null) {
				// 如果是String走uuid流程
				if (String.class == generatorContext.getColumn().getField().getSetter().getType()) {
					generatorContext.getColumn().set(generatorContext.getBean(), getUUID(generatorContext));
					return;
				}

				if (Number.class.isAssignableFrom(generatorContext.getColumn().getField().getSetter().getType())
						|| generatorContext.getColumn().getField().getSetter().getType().isPrimitive()) {
					generatorContext.getColumn().set(generatorContext.getBean(), generateNumber(generatorContext));
					return;
				}
			}
		}
	}

	public final SequenceId getSequenceId(GeneratorContext generatorContext) {
		SequenceId sId = (SequenceId) generatorContext.getAttribute(SequenceId.class);
		if (sId == null) {
			sId = generateSequeueId(generatorContext);
			getTemporaryVariable().setSequeueId(generatorContext, sId);
			getTemporaryVariable().setCreateTime(generatorContext, sId.getTimestamp());
		}
		return sId;
	}

	public final long getCreateTime(GeneratorContext generatorContext) {
		SequenceId sequenceId = getTemporaryVariable().getSequenceId(generatorContext);
		long t;
		if (sequenceId != null) {
			t = sequenceId.getTimestamp();
		} else {
			t = System.currentTimeMillis();
			getTemporaryVariable().setCreateTime(generatorContext, t);
		}
		return t;
	}

	public final long getUpdateTime(GeneratorContext generatorContext) {
		return getCreateTime(generatorContext);
	}

	public String getUUID(GeneratorContext generatorContext) {
		return XUtils.getUUID();
	}

	public abstract SequenceId generateSequeueId(GeneratorContext generatorContext);

	public abstract Number generateNumber(GeneratorContext generatorContext);

	static class TemporaryVariable {

		public SequenceId getSequenceId(GeneratorContext generatorContext) {
			return (SequenceId) generatorContext.getAttribute(SequenceId.class);
		}

		public void setSequeueId(GeneratorContext generatorContext, SequenceId sequenceId) {
			generatorContext.setAttribute(SequenceId.class, sequenceId);
		}

		public Long getCreateTime(GeneratorContext generatorContext) {
			Long createTime = (Long) generatorContext.getAttribute(CreateTime.class);
			if (createTime == null) {
				Column createTimeColumn = OrmUtils.getObjectRelationalMapping()
						.getColumns(generatorContext.getBean().getClass()).find(new Accept<Column>() {

							public boolean accept(Column e) {
								return e.getAnnotatedElement().getAnnotation(CreateTime.class) != null;
							}
						});

				if (createTimeColumn != null) {
					createTime = (Long) createTimeColumn.getField().getGetter().get(generatorContext.getBean());
					if (createTime != null && createTime.longValue() != 0) {
						setCreateTime(generatorContext, createTime);
						return createTime;
					}
				}
			}
			return createTime;
		}

		public void setCreateTime(GeneratorContext generatorContext, long createTime) {
			generatorContext.setAttribute(CreateTime.class, createTime);
		}
	}
}
