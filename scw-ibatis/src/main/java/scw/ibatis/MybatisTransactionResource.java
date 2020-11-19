package scw.ibatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import scw.transaction.Savepoint;
import scw.transaction.TransactionException;
import scw.transaction.TransactionResource;

public class MybatisTransactionResource implements TransactionResource {
	private final SqlSessionFactory sqlSessionFactory;
	private final boolean isActive;
	private SqlSession sqlSession;

	public MybatisTransactionResource(SqlSessionFactory sqlSessionFactory,
			boolean isActive) {
		this.sqlSessionFactory = sqlSessionFactory;
		this.isActive = isActive;
	}

	public SqlSession getSqlSession() {
		if (sqlSession == null) {
			sqlSession = MybatisUtils.proxySqlSession(sqlSessionFactory
					.openSession(!isActive));
		}
		return sqlSession;
	}

	public void commit() throws Throwable {
		if(sqlSession != null){
			sqlSession.commit();
		}
	}

	public void rollback() {
		if(sqlSession != null){
			sqlSession.rollback();
		}
	}

	public void completion() {
		if(sqlSession != null){
			MybatisUtils.closeSqlSessionProxy(sqlSession);
		}
	}

	public Savepoint createSavepoint() throws TransactionException {
		return null;
	}

}
