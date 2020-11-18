package scw.sql.orm.support.generation;

import scw.sql.orm.ORMException;


public interface GeneratorService {
	void process(GeneratorContext generatorContext) throws ORMException;
}
