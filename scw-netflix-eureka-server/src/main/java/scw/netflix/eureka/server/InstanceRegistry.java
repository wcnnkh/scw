package scw.netflix.eureka.server;

import java.util.List;

import scw.aop.annotation.AopEnable;
import scw.application.ApplicationAware;
import scw.application.ApplicationEvent;
import scw.logger.Logger;
import scw.logger.LoggerFactory;
import scw.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import scw.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import scw.netflix.eureka.server.event.EurekaInstanceRenewedEvent;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.lease.Lease;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import com.netflix.eureka.resources.ServerCodecs;

@AopEnable(false)
public class InstanceRegistry extends PeerAwareInstanceRegistryImpl implements ApplicationAware {
	private static Logger logger = LoggerFactory.getLogger(InstanceRegistry.class);
	private scw.application.Application application;
	private int defaultOpenForTrafficCount;

	public InstanceRegistry(EurekaServerConfig serverConfig, EurekaClientConfig clientConfig, ServerCodecs serverCodecs,
			EurekaClient eurekaClient, int expectedNumberOfClientsSendingRenews, int defaultOpenForTrafficCount) {
		super(serverConfig, clientConfig, serverCodecs, eurekaClient);
		this.expectedNumberOfClientsSendingRenews = expectedNumberOfClientsSendingRenews;
		this.defaultOpenForTrafficCount = defaultOpenForTrafficCount;
	}
	
	/**
	 * If
	 * {@link PeerAwareInstanceRegistryImpl#openForTraffic(ApplicationInfoManager, int)}
	 * is called with a zero argument, it means that leases are not automatically
	 * cancelled if the instance hasn't sent any renewals recently. This happens for a
	 * standalone server. It seems like a bad default, so we set it to the smallest
	 * non-zero value we can, so that any instances that subsequently register can bump up
	 * the threshold.
	 */
	@Override
	public void openForTraffic(ApplicationInfoManager applicationInfoManager, int count) {
		super.openForTraffic(applicationInfoManager, count == 0 ? this.defaultOpenForTrafficCount : count);
	}

	@Override
	public void register(InstanceInfo info, int leaseDuration, boolean isReplication) {
		handleRegistration(info, leaseDuration, isReplication);
		super.register(info, leaseDuration, isReplication);
	}

	@Override
	public void register(final InstanceInfo info, final boolean isReplication) {
		handleRegistration(info, resolveInstanceLeaseDuration(info), isReplication);
		super.register(info, isReplication);
	}

	@Override
	public boolean cancel(String appName, String serverId, boolean isReplication) {
		handleCancelation(appName, serverId, isReplication);
		return super.cancel(appName, serverId, isReplication);
	}

	@Override
	public boolean renew(final String appName, final String serverId, boolean isReplication) {
		log("renew " + appName + " serverId " + serverId + ", isReplication {}" + isReplication);
		List<Application> applications = getSortedApplications();
		for (Application input : applications) {
			if (input.getName().equals(appName)) {
				InstanceInfo instance = null;
				for (InstanceInfo info : input.getInstances()) {
					if (info.getId().equals(serverId)) {
						instance = info;
						break;
					}
				}
				publishEvent(new EurekaInstanceRenewedEvent(this, appName, serverId, instance, isReplication));
				break;
			}
		}
		return super.renew(appName, serverId, isReplication);
	}

	@Override
	protected boolean internalCancel(String appName, String id, boolean isReplication) {
		handleCancelation(appName, id, isReplication);
		return super.internalCancel(appName, id, isReplication);
	}

	private void handleCancelation(String appName, String id, boolean isReplication) {
		log("cancel " + appName + ", serverId " + id + ", isReplication " + isReplication);
		publishEvent(new EurekaInstanceCanceledEvent(this, appName, id, isReplication));
	}

	private void handleRegistration(InstanceInfo info, int leaseDuration, boolean isReplication) {
		log("register " + info.getAppName() + ", vip " + info.getVIPAddress() + ", leaseDuration " + leaseDuration
				+ ", isReplication " + isReplication);
		publishEvent(new EurekaInstanceRegisteredEvent(this, info, leaseDuration, isReplication));
	}

	private void log(String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	private void publishEvent(ApplicationEvent applicationEvent) {
		if(application == null){
			return ;
		}
		this.application.publishEvent(applicationEvent);
	}

	private int resolveInstanceLeaseDuration(final InstanceInfo info) {
		int leaseDuration = Lease.DEFAULT_DURATION_IN_SECS;
		if (info.getLeaseInfo() != null && info.getLeaseInfo().getDurationInSecs() > 0) {
			leaseDuration = info.getLeaseInfo().getDurationInSecs();
		}
		return leaseDuration;
	}

	@Override
	public void setApplication(scw.application.Application application) {
		this.application = application;
	}

}
