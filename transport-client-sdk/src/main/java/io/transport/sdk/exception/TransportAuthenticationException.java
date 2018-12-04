package io.transport.sdk.exception;

/**
 * 传输系统异常
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2017年10月12日
 * @since
 */
public class TransportAuthenticationException extends RuntimeException {

	private static final long serialVersionUID = -7449999088257602747L;

	public TransportAuthenticationException() {
		super();
	}

	public TransportAuthenticationException(String message) {
		super(message);
	}

	public TransportAuthenticationException(Throwable cause) {
		super(cause);
	}

	public TransportAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}
