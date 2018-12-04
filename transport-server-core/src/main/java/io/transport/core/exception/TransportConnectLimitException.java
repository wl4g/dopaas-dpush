package io.transport.core.exception;

import io.transport.common.utils.exception.TransportException;

/**
 * Connection overrun exception.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2017年10月12日
 * @since
 */
public class TransportConnectLimitException extends TransportException {

	private static final long serialVersionUID = -7449999088257602747L;

	public TransportConnectLimitException() {
		super();
	}

	public TransportConnectLimitException(String message) {
		super(message);
	}

	public TransportConnectLimitException(Throwable cause) {
		super(cause);
	}

	public TransportConnectLimitException(String message, Throwable cause) {
		super(message, cause);
	}
}
