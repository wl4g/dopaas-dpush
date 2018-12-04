package io.transport.sdk.exception;

/**
 * 传输系统异常
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2017年10月12日
 * @since
 */
public class TransportInitializeException extends RuntimeException {

	private static final long serialVersionUID = -7449999088257602747L;

	public TransportInitializeException() {
		super();
	}

	public TransportInitializeException(String message) {
		super(message);
	}

	public TransportInitializeException(Throwable cause) {
		super(cause);
	}

	public TransportInitializeException(String message, Throwable cause) {
		super(message, cause);
	}
}
