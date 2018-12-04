package io.transport.common.utils.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport exception process tools.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年5月15日
 * @since
 */
public class Exceptions {
	final private static Logger LOG = LoggerFactory.getLogger(Exceptions.class);

	/**
	 * Print error message.
	 * 
	 * @param logger
	 * @param t
	 */
	public static void defaultPrintErr(Logger logger, Throwable t) {
		try {
			if (t instanceof TransportException)
				logger.error(ExceptionUtils.getRootCauseMessage(t));
			else
				logger.error("Processing error.", t);
		} catch (Throwable tt) {
			LOG.error("Processing with abnormal abnormality.", tt);
		}
	}

}
