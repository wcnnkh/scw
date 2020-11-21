/*
 * Copyright 2002-2016 the original author or authors.
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

package scw.http.client.accessor;

import java.io.IOException;
import java.net.URI;

import scw.core.Assert;
import scw.http.HttpMethod;
import scw.http.client.ClientHttpRequest;
import scw.http.client.ClientHttpRequestFactory;
import scw.http.client.SimpleClientHttpRequestFactory;
import scw.logger.Logger;
import scw.logger.LoggerFactory;

public abstract class HttpAccessor {

	/** Logger available to subclasses */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

	/**
	 * Set the request factory that this accessor uses for obtaining client
	 * request handles.
	 * <p>
	 * The default is a {@link SimpleClientHttpRequestFactory} based on the
	 * JDK's own HTTP libraries ({@link java.net.HttpURLConnection}).
	 * <p>
	 * <b>Note that the standard JDK HTTP library does not support the HTTP
	 * PATCH method. Configure the Apache HttpComponents or OkHttp request
	 * factory to enable PATCH.</b>
	 * 
	 * @see #createRequest(URI, HttpMethod)
	 * @see org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory
	 * @see org.springframework.http.client.OkHttp3ClientHttpRequestFactory
	 */
	public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
		Assert.notNull(requestFactory, "ClientHttpRequestFactory must not be null");
		this.requestFactory = requestFactory;
	}

	/**
	 * Return the request factory that this accessor uses for obtaining client
	 * request handles.
	 */
	public ClientHttpRequestFactory getRequestFactory() {
		return this.requestFactory;
	}

	protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
		ClientHttpRequest request = getRequestFactory().createRequest(url, method);
		if (logger.isDebugEnabled()) {
			logger.debug("Created " + method.name() + " request for \"" + url + "\"");
		}
		return request;
	}

}
