package scw.util.concurrent;

public interface Listenable<T> {
	/**
	 * Register the given {@code ListenableFutureCallback}.
	 * 
	 * @param callback
	 *            the callback to register
	 */
	void addCallback(ListenableFutureCallback<? super T> callback);

	/**
	 * Java 8 lambda-friendly alternative with success and failure callbacks.
	 * 
	 * @param successCallback
	 *            the success callback
	 * @param failureCallback
	 *            the failure callback
	 */
	void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback);
}
