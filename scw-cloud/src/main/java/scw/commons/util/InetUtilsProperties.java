/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scw.commons.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import scw.aop.annotation.AopEnable;

/**
 * Properties for {@link InetUtils}.
 *
 * @author Spencer Gibb
 */
@AopEnable(false)
public class InetUtilsProperties {

	/**
	 * Prefix for the Inet Utils properties.
	 */
	public static final String PREFIX = "registry.inetutils";

	/**
	 * The default hostname. Used in case of errors.
	 */
	private String defaultHostname = "localhost";

	/**
	 * The default IP address. Used in case of errors.
	 */
	private String defaultIpAddress = "127.0.0.1";

	/**
	 * Timeout, in seconds, for calculating hostname.
	 */
	private int timeoutSeconds = 1;

	/**
	 * List of Java regular expressions for network interfaces that will be
	 * ignored.
	 */
	private List<String> ignoredInterfaces = new ArrayList<String>();

	/**
	 * Whether to use only interfaces with site local addresses. See
	 * {@link InetAddress#isSiteLocalAddress()} for more details.
	 */
	private boolean useOnlySiteLocalInterfaces = false;

	/**
	 * List of Java regular expressions for network addresses that will be
	 * preferred.
	 */
	private List<String> preferredNetworks = new ArrayList<String>();

	public String getDefaultHostname() {
		return this.defaultHostname;
	}

	public void setDefaultHostname(String defaultHostname) {
		this.defaultHostname = defaultHostname;
	}

	public String getDefaultIpAddress() {
		return this.defaultIpAddress;
	}

	public void setDefaultIpAddress(String defaultIpAddress) {
		this.defaultIpAddress = defaultIpAddress;
	}

	public int getTimeoutSeconds() {
		return this.timeoutSeconds;
	}

	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public List<String> getIgnoredInterfaces() {
		return this.ignoredInterfaces;
	}

	public void setIgnoredInterfaces(List<String> ignoredInterfaces) {
		this.ignoredInterfaces = ignoredInterfaces;
	}

	public boolean isUseOnlySiteLocalInterfaces() {
		return this.useOnlySiteLocalInterfaces;
	}

	public void setUseOnlySiteLocalInterfaces(boolean useOnlySiteLocalInterfaces) {
		this.useOnlySiteLocalInterfaces = useOnlySiteLocalInterfaces;
	}

	public List<String> getPreferredNetworks() {
		return this.preferredNetworks;
	}

	public void setPreferredNetworks(List<String> preferredNetworks) {
		this.preferredNetworks = preferredNetworks;
	}

}
