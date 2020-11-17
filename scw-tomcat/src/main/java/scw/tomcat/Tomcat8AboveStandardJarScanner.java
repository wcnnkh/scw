package scw.tomcat;

import javax.servlet.ServletContext;

import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.scan.StandardJarScanner;

import scw.aop.annotation.AopEnable;
import scw.value.property.PropertyFactory;

/**
 * tomcat8以上
 * 
 * @author shuchaowen
 *
 */
@AopEnable(false)
public class Tomcat8AboveStandardJarScanner extends StandardJarScanner {
	private boolean scanTdl;

	public Tomcat8AboveStandardJarScanner(PropertyFactory propertyFactory) {
		this.scanTdl = TomcatUtils.tomcatScanTld(propertyFactory);
		setScanManifest(false);
	}

	@Override
	public void scan(JarScanType scanType, ServletContext context, JarScannerCallback callback) {
		if (!scanTdl && scanType == JarScanType.TLD) {
			return;
		}
		super.scan(scanType, context, callback);
	}
}
