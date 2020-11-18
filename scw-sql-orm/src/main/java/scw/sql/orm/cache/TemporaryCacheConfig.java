package scw.sql.orm.cache;

import scw.core.utils.XTime;
import scw.sql.orm.cache.annotation.TemporaryCacheEnable;

final class TemporaryCacheConfig {
	private final int exp;
	private final boolean keys;
	private final boolean enable;

	public TemporaryCacheConfig(int exp, boolean keys, boolean enable) {
		this.exp = exp;
		this.keys = keys;
		this.enable = enable;
	}

	public TemporaryCacheConfig(TemporaryCacheEnable temporaryCacheEnable) {
		this.exp = (int) Math.max(XTime.ONE_MINUTE / 1000,
				temporaryCacheEnable.expTimeUnit().toSeconds(temporaryCacheEnable.exp()));
		this.keys = temporaryCacheEnable.keys();
		this.enable = temporaryCacheEnable.value();
	}

	public int getExp() {
		return exp;
	}

	public boolean isKeys() {
		return keys;
	}

	public boolean isEnable() {
		return enable;
	}
}
