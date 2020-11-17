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

/**
 * Contract to register and deregister instances with a Service Registry.
 * 
 * @author shuchaowen
 *
 */
public interface ServiceRegistry<T extends ServiceInstance> {

	/**
	 * Registers the registration. A registration typically has information
	 * about an instance, such as its hostname and port.
	 * 
	 * @param registration
	 *            instance meta data
	 */
	void register(T instance);

	/**
	 * Deregisters the registration.
	 * 
	 * @param registration
	 *            instance meta data
	 */
	void deregister(T instance);

}
