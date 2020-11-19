package scw.ibatis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.ibatis.session.SqlSession;

import scw.core.utils.ArrayUtils;

public class SqlSessionProxyInvocationHandler implements InvocationHandler{
	private final SqlSession sqlSession;
	
	public SqlSessionProxyInvocationHandler(SqlSession sqlSession){
		this.sqlSession = sqlSession;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if(ArrayUtils.isEmpty(args)){
			if(method.equals("close")){
				return null;
			}else if(method.equals("getTargetSqlSession")){
				return sqlSession;
			}
		}
		
		return method.invoke(sqlSession, args);
	}
}
