/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scw.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of {@link MultiValueMap} that wraps a
 * {@link LinkedHashMap}, storing multiple values in a {@link LinkedList}.
 *
 * <p>
 * This Map implementation is generally not thread-safe. It is primarily
 * designed for data structures exposed from request objects, for use in a
 * single thread only.
 *
 */
public class LinkedMultiValueMap<K, V> extends AbstractMultiValueMap<K, V> {

	private static final long serialVersionUID = 3801124242820219131L;

	private final Map<K, List<V>> targetMap;

	/**
	 * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}.
	 */
	public LinkedMultiValueMap() {
		this.targetMap = new LinkedHashMap<K, List<V>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<K, List<V>> eldest) {
				return overrideRemoveEldestEntry(eldest);
			}
		};
	}

	/**
	 * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap} with
	 * the given initial capacity.
	 * 
	 * @param initialCapacity
	 *            the initial capacity
	 */
	public LinkedMultiValueMap(int initialCapacity) {
		this.targetMap = new LinkedHashMap<K, List<V>>(initialCapacity) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<K, List<V>> eldest) {
				return overrideRemoveEldestEntry(eldest);
			}
		};
	}

	public LinkedMultiValueMap(int initialCapacity, float loadFactor) {
		this.targetMap = new LinkedHashMap<K, List<V>>(initialCapacity, loadFactor) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<K, List<V>> eldest) {
				return overrideRemoveEldestEntry(eldest);
			}
		};
	}

	public LinkedMultiValueMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		this.targetMap = new LinkedHashMap<K, List<V>>(initialCapacity, loadFactor, accessOrder) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<K, List<V>> eldest) {
				return overrideRemoveEldestEntry(eldest);
			}
		};
	}

	/**
	 * 重写LinkedHashMap的此方法
	 * 
	 * @param eldest
	 * @return
	 */
	protected boolean overrideRemoveEldestEntry(java.util.Map.Entry<K, List<V>> eldest) {
		return false;
	}

	/**
	 * Copy constructor: Create a new LinkedMultiValueMap with the same mappings
	 * as the specified Map.
	 * 
	 * @param otherMap
	 *            the Map whose mappings are to be placed in this Map
	 */
	public LinkedMultiValueMap(Map<K, List<V>> otherMap) {
		this.targetMap = new LinkedHashMap<K, List<V>>(otherMap);
	}

	@Override
	protected Map<K, List<V>> getTargetMap() {
		return targetMap;
	}
}
