package scw.ibatis;

import java.lang.reflect.Proxy;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import scw.transaction.Transaction;
import scw.transaction.TransactionManager;

public final class MybatisUtils {
	private MybatisUtils() {
	};

	public static SqlSession getTransactionSqlSession(
			SqlSessionFactory sqlSessionFactory) {
		Transaction transaction = TransactionManager.getCurrentTransaction();
		if (transaction == null) {
			return sqlSessionFactory.openSession(true);
		}

		MybatisTransactionResource resource = (MybatisTransactionResource) transaction
				.getResource(sqlSessionFactory);
		if (resource == null) {
			resource = new MybatisTransactionResource(sqlSessionFactory,
					transaction.isActive());
			transaction.bindResource(sqlSessionFactory, resource);
		}
		return resource.getSqlSession();
	}

	public static SqlSession proxySqlSession(SqlSession sqlSession) {
		if (sqlSession == null) {
			return null;
		}

		if (sqlSession instanceof SqlSessionProxy) {
			return sqlSession;
		}

		return (SqlSession) Proxy.newProxyInstance(
				SqlSessionProxy.class.getClassLoader(),
				new Class<?>[] { SqlSessionProxy.class },
				new SqlSessionProxyInvocationHandler(sqlSession));
	}

	public static void closeSqlSessionProxy(SqlSession sqlSession) {
		if (sqlSession == null) {
			return;
		}

		if (sqlSession instanceof SqlSessionProxy) {
			((SqlSessionProxy) sqlSession).getTargetSqlSession().close();
			return;
		}
		sqlSession.close();
	}
}
