package scw.db.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface Hibernate {
	SessionFactory getSessionFactory();
	
	Session getTransactionSession();
}
