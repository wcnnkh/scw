package scw.sql.orm.support.generation;

import scw.aop.annotation.AopEnable;
import scw.sql.orm.ORMException;

@AopEnable(false)
public interface GeneratorService {
	void process(GeneratorContext generatorContext) throws ORMException;
}
