/*
 * Copyright 2002-2015 the original author or authors.
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

package scw.http.client;

import java.io.IOException;
import java.net.URI;

import scw.http.HttpHeaders;
import scw.http.HttpMethod;
import scw.io.StreamUtils;

/**
 * Simple implementation of {@link ClientHttpRequest} that wraps another
 * request.
 *
 * @author Arjen Poutsma
 */
final class BufferingClientHttpRequestWrapper extends AbstractBufferingClientHttpRequest {
 
	private final ClientHttpRequest request;

	BufferingClientHttpRequestWrapper(ClientHttpRequest request) {
		this.request = request;
	}

	public HttpMethod getMethod() {
		return this.request.getMethod();
	}

	public URI getURI() {
		return this.request.getURI();
	}

	@Override
	protected ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput) throws IOException {
		this.request.getHeaders().putAll(headers);
		StreamUtils.copy(bufferedOutput, this.request.getBody());
		ClientHttpResponse response = this.request.execute();
		return new BufferingClientHttpResponseWrapper(response);
	}

}
