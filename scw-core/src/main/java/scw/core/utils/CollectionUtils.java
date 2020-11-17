/*
 * Copyright 2002-2012 the original author or authors.
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

package scw.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import scw.core.Assert;
import scw.core.Callable;
import scw.util.MultiValueMap;
import scw.util.MultiValueMapWrapper;

public abstract class CollectionUtils {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final MultiValueMap EMPTY_MULTI_VALUE_MAP = new MultiValueMapWrapper(Collections.emptyMap());

	@SuppressWarnings("unchecked")
	public static <K, V> MultiValueMap<K, V> emptyMultiValueMap() {
		return EMPTY_MULTI_VALUE_MAP;
	}

	/**
	 * Return {@code true} if the supplied Collection is {@code null} or empty.
	 * Otherwise, return {@code false}.
	 * 
	 * @param collection
	 *            the Collection to check
	 * @return whether the given Collection is empty
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Collection collection) {
		return (collection == null || collection.isEmpty());
	}

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Iterable iterable) {
		if (iterable == null) {
			return true;
		}

		if (iterable instanceof Collection) {
			return ((Collection) iterable).isEmpty();
		}

		return !iterable.iterator().hasNext();
	}

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Collection... collections) {
		for (Collection collection : collections) {
			if (collection == null || collection.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return {@code true} if the supplied Map is {@code null} or empty.
	 * Otherwise, return {@code false}.
	 * 
	 * @param map
	 *            the Map to check
	 * @return whether the given Map is empty
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Map map) {
		return (map == null || map.isEmpty());
	}

	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Map... map) {
		for (Map m : map) {
			if (m == null || m.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert the supplied array into a List. A primitive array gets converted
	 * into a List of the appropriate wrapper type.
	 * <p>
	 * A {@code null} source value will be converted to an empty List.
	 * 
	 * @param source
	 *            the (potentially primitive) array
	 * @return the converted List result
	 * @see ObjectUtils#toObjectArray(Object)
	 */
	@SuppressWarnings("rawtypes")
	public static List arrayToList(Object source) {
		return Arrays.asList(ObjectUtils.toObjectArray(source));
	}

	/**
	 * Merge the given array into the given Collection.
	 * 
	 * @param array
	 *            the array to merge (may be {@code null})
	 * @param collection
	 *            the target Collection to merge the array into
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void mergeArrayIntoCollection(Object array, Collection collection) {
		if (collection == null) {
			throw new IllegalArgumentException("Collection must not be null");
		}
		Object[] arr = ObjectUtils.toObjectArray(array);
		for (Object elem : arr) {
			collection.add(elem);
		}
	}

	/**
	 * Merge the given Properties instance into the given Map, copying all
	 * properties (key-value pairs) over.
	 * <p>
	 * Uses {@code Properties.propertyNames()} to even catch default properties
	 * linked into the original Properties instance.
	 * 
	 * @param props
	 *            the Properties instance to merge (may be {@code null})
	 * @param map
	 *            the target Map to merge the properties into
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void mergePropertiesIntoMap(Properties props, Map map) {
		if (map == null) {
			throw new IllegalArgumentException("Map must not be null");
		}
		if (props != null) {
			for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				Object value = props.getProperty(key);
				if (value == null) {
					// Potentially a non-String value...
					value = props.get(key);
				}
				map.put(key, value);
			}
		}
	}

	/**
	 * Check whether the given Iterator contains the given element.
	 * 
	 * @param iterator
	 *            the Iterator to check
	 * @param element
	 *            the element to look for
	 * @return {@code true} if found, {@code false} else
	 */
	@SuppressWarnings("rawtypes")
	public static boolean contains(Iterator iterator, Object element) {
		if (iterator != null) {
			while (iterator.hasNext()) {
				Object candidate = iterator.next();
				if (ObjectUtils.nullSafeEquals(candidate, element)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check whether the given Enumeration contains the given element.
	 * 
	 * @param enumeration
	 *            the Enumeration to check
	 * @param element
	 *            the element to look for
	 * @return {@code true} if found, {@code false} else
	 */
	@SuppressWarnings("rawtypes")
	public static boolean contains(Enumeration enumeration, Object element) {
		if (enumeration != null) {
			while (enumeration.hasMoreElements()) {
				Object candidate = enumeration.nextElement();
				if (ObjectUtils.nullSafeEquals(candidate, element)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check whether the given Collection contains the given element instance.
	 * <p>
	 * Enforces the given instance to be present, rather than returning
	 * {@code true} for an equal element as well.
	 * 
	 * @param collection
	 *            the Collection to check
	 * @param element
	 *            the element to look for
	 * @return {@code true} if found, {@code false} else
	 */
	@SuppressWarnings("rawtypes")
	public static boolean containsInstance(Collection collection, Object element) {
		if (collection != null) {
			for (Object candidate : collection) {
				if (candidate == element) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return {@code true} if any element in '{@code candidates}' is contained
	 * in '{@code source}'; otherwise returns {@code false}.
	 * 
	 * @param source
	 *            the source Collection
	 * @param candidates
	 *            the candidates to search for
	 * @return whether any of the candidates has been found
	 */
	@SuppressWarnings("rawtypes")
	public static boolean containsAny(Collection source, Collection candidates) {
		if (isEmpty(source) || isEmpty(candidates)) {
			return false;
		}
		for (Object candidate : candidates) {
			if (source.contains(candidate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the first element in '{@code candidates}' that is contained in '
	 * {@code source}'. If no element in '{@code candidates}' is present in '
	 * {@code source}' returns {@code null}. Iteration order is
	 * {@link Collection} implementation specific.
	 * 
	 * @param source
	 *            the source Collection
	 * @param candidates
	 *            the candidates to search for
	 * @return the first present object, or {@code null} if not found
	 */
	@SuppressWarnings("rawtypes")
	public static Object findFirstMatch(Collection source, Collection candidates) {
		if (isEmpty(source) || isEmpty(candidates)) {
			return null;
		}
		for (Object candidate : candidates) {
			if (source.contains(candidate)) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Find a single value of the given type in the given Collection.
	 * 
	 * @param collection
	 *            the Collection to search
	 * @param type
	 *            the type to look for
	 * @return a value of the given type found if there is a clear match, or
	 *         {@code null} if none or more than one such value found
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findValueOfType(Collection<?> collection, Class<T> type) {
		if (isEmpty(collection)) {
			return null;
		}
		T value = null;
		for (Object element : collection) {
			if (type == null || type.isInstance(element)) {
				if (value != null) {
					// More than one value found... no clear single value.
					return null;
				}
				value = (T) element;
			}
		}
		return value;
	}

	/**
	 * Find a single value of one of the given types in the given Collection:
	 * searching the Collection for a value of the first type, then searching
	 * for a value of the second type, etc.
	 * 
	 * @param collection
	 *            the collection to search
	 * @param types
	 *            the types to look for, in prioritized order
	 * @return a value of one of the given types found if there is a clear
	 *         match, or {@code null} if none or more than one such value found
	 */
	public static Object findValueOfType(Collection<?> collection, Class<?>[] types) {
		if (isEmpty(collection) || ObjectUtils.isEmpty(types)) {
			return null;
		}
		for (Class<?> type : types) {
			Object value = findValueOfType(collection, type);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Determine whether the given Collection only contains a single unique
	 * object.
	 * 
	 * @param collection
	 *            the Collection to check
	 * @return {@code true} if the collection contains a single reference or
	 *         multiple references to the same instance, {@code false} else
	 */
	@SuppressWarnings("rawtypes")
	public static boolean hasUniqueObject(Collection collection) {
		if (isEmpty(collection)) {
			return false;
		}
		boolean hasCandidate = false;
		Object candidate = null;
		for (Object elem : collection) {
			if (!hasCandidate) {
				hasCandidate = true;
				candidate = elem;
			} else if (candidate != elem) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find the common element type of the given Collection, if any.
	 * 
	 * @param collection
	 *            the Collection to check
	 * @return the common element type, or {@code null} if no clear common type
	 *         has been found (or the collection was empty)
	 */
	@SuppressWarnings("rawtypes")
	public static Class<?> findCommonElementType(Collection collection) {
		if (isEmpty(collection)) {
			return null;
		}
		Class<?> candidate = null;
		for (Object val : collection) {
			if (val != null) {
				if (candidate == null) {
					candidate = val.getClass();
				} else if (candidate != val.getClass()) {
					return null;
				}
			}
		}
		return candidate;
	}

	/**
	 * Marshal the elements from the given enumeration into an array of the
	 * given type. Enumeration elements must be assignable to the type of the
	 * given array. The array returned will be a different instance than the
	 * array given.
	 */
	public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
		ArrayList<A> elements = new ArrayList<A>();
		while (enumeration.hasMoreElements()) {
			elements.add(enumeration.nextElement());
		}
		return elements.toArray(array);
	}

	/**
	 * Adapt an enumeration to an iterator.
	 * 
	 * @param enumeration
	 *            the enumeration
	 * @return the iterator
	 */
	public static <E> Iterator<E> toIterator(Enumeration<? extends E> enumeration) {
		return new EnumerationIterator<E>(enumeration);
	}

	public static <E> Enumeration<E> toEnumeration(final Iterator<? extends E> iterator) {
		return new Enumeration<E>() {

			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			public E nextElement() {
				return iterator.next();
			}
		};
	}

	/**
	 * Adapts a {@code Map<K, List<V>>} to an {@code MultiValueMap<K,V>}.
	 *
	 * @param map
	 *            the map
	 * @return the multi-value map
	 */
	public static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, List<V>> map) {
		return new MultiValueMapWrapper<K, V>(map);

	}

	/**
	 * Returns an unmodifiable view of the specified multi-value map.
	 *
	 * @param map
	 *            the map for which an unmodifiable view is to be returned.
	 * @return an unmodifiable view of the specified multi-value map.
	 */
	public static <K, V> MultiValueMap<K, V> unmodifiableMultiValueMap(MultiValueMap<? extends K, ? extends V> map) {
		Assert.notNull(map, "'map' must not be null");
		Map<K, List<V>> result = new LinkedHashMap<K, List<V>>(map.size());
		for (Map.Entry<? extends K, ? extends List<? extends V>> entry : map.entrySet()) {
			List<V> values = Collections.unmodifiableList(entry.getValue());
			result.put(entry.getKey(), values);
		}
		Map<K, List<V>> unmodifiableMap = Collections.unmodifiableMap(result);
		return toMultiValueMap(unmodifiableMap);
	}

	/**
	 * 颠倒一个集合的排列
	 * 
	 * @param collection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<E> reversal(Collection<E> collection) {
		if (collection == null || collection.isEmpty()) {
			return Collections.emptyList();
		}

		Object[] values = collection.toArray();
		values = ArrayUtils.reversal(values);
		return Arrays.asList((E[]) values);
	}

	/**
	 * Iterator wrapping an Enumeration.
	 */
	private static class EnumerationIterator<E> extends scw.util.AbstractIterator<E> {

		private Enumeration<? extends E> enumeration;

		public EnumerationIterator(Enumeration<? extends E> enumeration) {
			this.enumeration = enumeration;
		}

		public boolean hasNext() {
			return this.enumeration.hasMoreElements();
		}

		public E next() {
			return this.enumeration.nextElement();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Set<T> asSet(T... values) {
		if (ArrayUtils.isEmpty(values)) {
			return Collections.EMPTY_SET;
		}

		return asSet(Arrays.asList(values));
	}

	@SuppressWarnings("unchecked")
	public static <E> Set<E> asSet(Collection<E> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return Collections.EMPTY_SET;
		}

		return Collections.unmodifiableSet(new LinkedHashSet<E>(collection));
	}

	public static <K, V, M extends Map<K, V>> void put(M map, K key, V value, Callable<? extends M> callback) {
		if (map == null) {
			map = callback.call();
		}

		map.put(key, value);
	}

	public static <K, V> Callable<HashMap<K, V>> hashMapCallable(final int initialCapacity) {
		return new Callable<HashMap<K, V>>() {

			public HashMap<K, V> call() {
				return new HashMap<K, V>(initialCapacity);
			}
		};
	};

	public static <E> Callable<ArrayList<E>> arrayListCallable(final int initialCapacity) {
		return new Callable<ArrayList<E>>() {

			public ArrayList<E> call() {
				return new ArrayList<E>(initialCapacity);
			}
		};
	}

	public static <T> T first(Collection<T> values) {
		if (isEmpty(values)) {
			return null;
		}
		if (values instanceof List) {
			List<T> list = (List<T>) values;
			return list.get(0);
		} else {
			return values.iterator().next();
		}
	}
}
