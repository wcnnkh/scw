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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import scw.core.utils.CollectionUtils;

public class SimpleDiscoveryClient<T extends ServiceInstance> implements DiscoveryClient, ServiceRegistry<T> {
	private final ConcurrentHashMap<String, Map<String, T>> instanceMap = new ConcurrentHashMap<String, Map<String, T>>();

	public List<ServiceInstance> getInstances(String name) {
		Map<String, T> serviceInstanceForService = instanceMap.get(name);
		if (CollectionUtils.isEmpty(serviceInstanceForService)) {
			return Collections.emptyList();
		}

		return new ArrayList<ServiceInstance>(serviceInstanceForService.values());
	}

	public List<String> getServices() {
		return new ArrayList<String>(instanceMap.keySet());
	}

	public void register(T instance) {
		Map<String, T> serviceInstanceForService = instanceMap.get(instance.getName());
		if (serviceInstanceForService == null) {
			serviceInstanceForService = new LinkedHashMap<String, T>();
			serviceInstanceForService.put(instance.getId(), instance);
			instanceMap.putIfAbsent(instance.getName(), serviceInstanceForService);
		} else {
			serviceInstanceForService.put(instance.getId(), instance);
			instanceMap.put(instance.getName(), serviceInstanceForService);
		}
	}

	public void deregister(T instance) {
		Map<String, T> serviceInstanceForService = instanceMap.get(instance.getName());
		if (serviceInstanceForService == null) {
			return;
		}

		serviceInstanceForService.remove(instance.getId());
	}
}
