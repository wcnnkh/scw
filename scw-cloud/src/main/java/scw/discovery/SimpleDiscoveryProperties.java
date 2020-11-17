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

package scw.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Properties to hold the details of a
 * {@link org.springframework.cloud.client.discovery.DiscoveryClient} service instances
 * for a given service. It also holds the user-configurable order that will be used to
 * establish the precedence of this client in the list of clients used by
 * {@link org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient}.
 *
 * @author Biju Kunjummen
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @author Charu Covindane
 */

public class SimpleDiscoveryProperties {

	private Map<String, List<DefaultServiceInstance>> instances = new HashMap<String, List<DefaultServiceInstance>>();

	public Map<String, List<DefaultServiceInstance>> getInstances() {
		return this.instances;
	}

	public void setInstances(Map<String, List<DefaultServiceInstance>> instances) {
		this.instances = instances;
	}
}
