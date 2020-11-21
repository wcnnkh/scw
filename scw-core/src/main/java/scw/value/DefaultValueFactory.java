package scw.value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import scw.core.utils.CollectionUtils;

public class DefaultValueFactory<K> extends AbstractValueFactory<K> implements ValueFactory<K> {
	private volatile List<BaseValueFactory<K>> baseValueFactories;

	public Value getValue(K key) {
		if (baseValueFactories == null) {
			return null;
		}

		for (BaseValueFactory<K> baseValueFactory : baseValueFactories) {
			Value value = baseValueFactory.getValue(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public Collection<BaseValueFactory<K>> getBaseValueFactories() {
		if (baseValueFactories == null) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(baseValueFactories);
	}

	public void addLastBaseValueFactory(BaseValueFactory<K> baseValueFactory) {
		synchronized (this) {
			if (baseValueFactories == null) {
				baseValueFactories = new ArrayList<BaseValueFactory<K>>();
			}

			baseValueFactories.add(baseValueFactory);
		}
	}

	public void addFirstBaseValueFactoryBefore(BaseValueFactory<K> baseValueFactory) {
		synchronized (this) {
			if (baseValueFactories == null) {
				baseValueFactories = new ArrayList<BaseValueFactory<K>>();
			}

			baseValueFactories.add(0, baseValueFactory);
		}
	}

	public void addLastBaseValueFactory(List<BaseValueFactory<K>> baseValueFactories) {
		if (CollectionUtils.isEmpty(baseValueFactories)) {
			return;
		}

		for (BaseValueFactory<K> factory : baseValueFactories) {
			addLastBaseValueFactory(factory);
		}
	}
}
