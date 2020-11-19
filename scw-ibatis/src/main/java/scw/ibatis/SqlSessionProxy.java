package scw.ibatis;

import org.apache.ibatis.session.SqlSession;

public interface SqlSessionProxy extends SqlSession{
	SqlSession getTargetSqlSession();
}
