package scw.io.support;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import scw.core.GlobalPropertyFactory;
import scw.core.utils.StringUtils;
import scw.io.serialzer.JavaSerializer;
import scw.io.serialzer.NoTypeSpecifiedSerializer;
import scw.io.support.LocalLogger.Record;
import scw.logger.Logger;
import scw.logger.LoggerUtils;
import scw.util.XUtils;
import scw.value.property.SystemPropertyFactory;

public final class SystemLocalLogger<T> {
	private static final String DIRCETORY = GlobalPropertyFactory.getInstance().getValue("scw.system.local.directory",
			String.class, SystemPropertyFactory.getInstance().getUserHome());
	private static Logger logger = LoggerUtils.getLogger(SystemLocalLogger.class);
	private final LocalLogger localLogger;
	private final NoTypeSpecifiedSerializer serializer;

	public SystemLocalLogger(String name) {
		this(name, JavaSerializer.INSTANCE);
	}

	public SystemLocalLogger(String name, NoTypeSpecifiedSerializer serializer) {
		this.localLogger = new LocalLogger(StringUtils
				.cleanPath(DIRCETORY + "/" + GlobalPropertyFactory.getInstance().getSystemLocalId() + "/" + name));
		this.serializer = serializer;
	}

	public LocalLogger getLocalLogger() {
		return localLogger;
	}

	public Record<T> create(T data) {
		return create(XUtils.getUUID(), data);
	}

	public Record<T> create(String id, T data) {
		try {
			localLogger.create(id, serializer.serialize(data));
		} catch (IOException e) {
			throw new RuntimeException("create record id:" + id, e);
		}
		return new Record<T>(id, data);
	}

	public Enumeration<Record<T>> enumeration() {
		return new SystemEnumeration();
	}

	private final class SystemEnumeration implements Enumeration<Record<T>> {
		private Enumeration<Record<byte[]>> enumeration = localLogger.enumeration();
		private boolean next;
		private Record<T> record;

		private Record<T> to(Record<byte[]> record) {
			try {
				T data = serializer.deserialize(record.getData());
				return new Record<T>(record.getId(), data);
			} catch (ClassNotFoundException e) {
				logger.error(e, "deserialize for key: {}", record.getId());
			}
			return null;
		}

		public boolean hasMoreElements() {
			if (next) {
				return true;
			}

			while (enumeration.hasMoreElements()) {
				Record<T> record = to(enumeration.nextElement());
				if (record != null) {
					this.record = record;
					this.next = true;
					return true;
				}
			}
			return false;
		}

		public Record<T> nextElement() {
			if (next) {
				next = false;
				return record;
			}

			while (enumeration.hasMoreElements()) {
				Record<T> record = to(enumeration.nextElement());
				if (record != null) {
					return record;
				}
			}

			throw new NoSuchElementException();
		}
	}
}
