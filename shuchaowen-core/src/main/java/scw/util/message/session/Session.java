package scw.util.message.session;

import java.io.Closeable;

import scw.util.message.Message;

public interface Session extends Closeable {
	String getId();

	boolean isOpen();

	void send(Message<?> message) throws Exception;
}