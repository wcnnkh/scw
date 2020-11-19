package scw.hibernate;

import java.lang.reflect.Proxy;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import scw.transaction.Transaction;
import scw.transaction.TransactionManager;

public final class HibernateUtils {
	private HibernateUtils(){};
	
	public static Session proxySession(Session session) {
		if (session == null) {
			return null;
		}

		if (session instanceof SessionProxy) {
			return session;
		}

		return (Session) Proxy.newProxyInstance(
				SessionProxy.class.getClassLoader(),
				new Class<?>[] { SessionProxy.class },
				new SessionProxyInvocationHandler(session));
	}

	public static void closeProxySession(Session session) {
		if (session == null) {
			return;
		}

		if (session instanceof SessionProxy) {
			((SessionProxy) session).getTargetSession().close();
			return;
		}

		session.close();
	}

	public static Session getTransactionSession(SessionFactory sessionFactory) {
		Transaction transaction = TransactionManager.getCurrentTransaction();
		if (transaction == null) {
			return sessionFactory.openSession();
		}

		HibernateTransactionResource resource = (HibernateTransactionResource) transaction
				.getResource(sessionFactory);
		if (resource == null) {
			resource = new HibernateTransactionResource(sessionFactory,
					transaction.isActive());
			transaction.bindResource(sessionFactory, resource);
		}
		return resource.getSession();
	}
}
