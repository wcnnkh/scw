/*
 * Copyright 2002-2017 the original author or authors.
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

package scw.servlet.http;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.core.Assert;
import scw.http.server.ServerHttpAsyncControl;
import scw.http.server.ServerHttpAsyncListener;
import scw.lang.NotSupportedException;

public class HttpServletAsyncControl implements ServerHttpAsyncControl, javax.servlet.AsyncListener {

	private static final long NO_TIMEOUT_VALUE = Long.MIN_VALUE;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private AsyncContext asyncContext;

	private AtomicBoolean asyncCompleted = new AtomicBoolean(false);
	private LinkedList<ServerHttpAsyncListener> serverHttpAsyncListeners = new LinkedList<ServerHttpAsyncListener>();

	public HttpServletAsyncControl(HttpServletRequest request, HttpServletResponse response) {
		Assert.notNull(request, "request is required");
		Assert.notNull(response, "response is required");

		if (!request.isAsyncSupported()) {
			throw new NotSupportedException("Async support must be enabled on a servlet and for all filters involved "
					+ "in async request processing. This is done in Java code using the Servlet API "
					+ "or by adding \"<async-supported>true</async-supported>\" to servlet and "
					+ "filter declarations in web.xml. Also you must use a Servlet 3.0+ container");
		}
		this.request = request;
		this.response = response;
	}

	public boolean isStarted() {
		return (this.asyncContext != null && request.isAsyncStarted());
	}

	public boolean isCompleted() {
		return this.asyncCompleted.get();
	}

	public void start() {
		start(NO_TIMEOUT_VALUE);
	}

	public void start(long timeout) {
		Assert.state(!isCompleted(), "Async processing has already completed");
		if (isStarted()) {
			return;
		}

		this.asyncContext = request.startAsync(request, response);
		this.asyncContext.addListener(this);
		if (timeout != NO_TIMEOUT_VALUE) {
			this.asyncContext.setTimeout(timeout);
		}
	}

	public void complete() {
		if (isStarted() && !isCompleted()) {
			this.asyncContext.complete();
		}
	}

	// ---------------------------------------------------------------------
	// Implementation of AsyncListener methods
	// ---------------------------------------------------------------------

	public void onComplete(AsyncEvent event) throws IOException {
		scw.http.server.ServerHttpAsyncEvent serverHttpAsyncEvent = new scw.http.server.ServerHttpAsyncEvent(this,
				event.getThrowable());
		for (ServerHttpAsyncListener listener : serverHttpAsyncListeners) {
			listener.onComplete(serverHttpAsyncEvent);
		}
		this.asyncContext = null;
		this.asyncCompleted.set(true);
	}

	public void onStartAsync(AsyncEvent event) throws IOException {
		scw.http.server.ServerHttpAsyncEvent serverHttpAsyncEvent = new scw.http.server.ServerHttpAsyncEvent(this,
				event.getThrowable());
		for (ServerHttpAsyncListener listener : serverHttpAsyncListeners) {
			listener.onStartAsync(serverHttpAsyncEvent);
		}
	}

	public void onError(AsyncEvent event) throws IOException {
		scw.http.server.ServerHttpAsyncEvent serverHttpAsyncEvent = new scw.http.server.ServerHttpAsyncEvent(this,
				event.getThrowable());
		for (ServerHttpAsyncListener listener : serverHttpAsyncListeners) {
			listener.onError(serverHttpAsyncEvent);
		}
	}

	public void onTimeout(AsyncEvent event) throws IOException {
		scw.http.server.ServerHttpAsyncEvent serverHttpAsyncEvent = new scw.http.server.ServerHttpAsyncEvent(this,
				event.getThrowable());
		for (ServerHttpAsyncListener listener : serverHttpAsyncListeners) {
			listener.onTimeout(serverHttpAsyncEvent);
		}
	}

	public void addListener(ServerHttpAsyncListener serverHttpAsyncListener) {
		serverHttpAsyncListeners.add(serverHttpAsyncListener);
	}

}
