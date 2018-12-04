package io.transport.core.exception;

import io.transport.common.utils.exception.TransportException;

/**
 * Authentication abnormity
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2017年10月12日
 * @since
 */
public class TransportAuthenticationException extends TransportException {

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
