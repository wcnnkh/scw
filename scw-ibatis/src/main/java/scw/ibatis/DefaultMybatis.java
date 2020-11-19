package scw.ibatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import scw.core.instance.annotation.Configuration;

@Configuration(order = Integer.MIN_VALUE, value = Mybatis.class)
public class DefaultMybatis implements Mybatis {
	private final SqlSessionFactory sqlSessionFactory;

	public DefaultMybatis(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public SqlSession getTransactionSqlSession() {
		return MybatisUtils.getTransactionSqlSession(getSqlSessionFactory());
	}

}
