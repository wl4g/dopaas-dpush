package io.transport.core.exception;

import io.transport.common.utils.exception.TransportException;

/**
 * Off-line exception.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2017年10月12日
 * @since
 */
public class TransportOfflineException extends TransportException {

	private static final long serialVersionUID = -7449999088257602747L;

	public TransportOfflineException() {
		super();
	}

	public TransportOfflineException(String message) {
		super(message);
	}

	public TransportOfflineException(Throwable cause) {
		super(cause);
	}

	public TransportOfflineException(String message, Throwable cause) {
		super(message, cause);
	}
}
