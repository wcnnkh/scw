package scw.hibernate;

import org.hibernate.Session;

public interface SessionProxy extends Session{
	Session getTargetSession();
}
