package io.transport.core.exception;

import io.transport.common.utils.exception.TransportException;

/**
 * DDOS attacks are likely to be abnormal.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2017年10月12日
 * @since
 */
public class TransportDDOSAmbiguousException extends TransportException {

	private static final long serialVersionUID = -7449999088257602747L;

	public TransportDDOSAmbiguousException() {
		super();
	}

	public TransportDDOSAmbiguousException(String message) {
		super(message);
	}

	public TransportDDOSAmbiguousException(Throwable cause) {
		super(cause);
	}

	public TransportDDOSAmbiguousException(String message, Throwable cause) {
		super(message, cause);
	}
}
