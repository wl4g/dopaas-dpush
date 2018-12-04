package io.transport.common.utils.exception;

/**
 * Transport operations (business) exceptions.
 * 
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2017年10月12日
 * @since
 */
public class TransportException extends RuntimeException {

	private static final long serialVersionUID = -7449999088257602747L;

	public TransportException() {
		super();
	}

	public TransportException(String message) {
		super(message);
	}

	public TransportException(Throwable cause) {
		super(cause);
	}

	public TransportException(String message, Throwable cause) {
		super(message, cause);
	}
}
