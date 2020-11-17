package scw.tomcat;

import org.apache.catalina.Context;

import scw.application.MainApplication;

public interface TomcatContextConfiguration {
	void configuration(MainApplication application, Context context);
}
