package io.transport.sdk.logger;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.transport.sdk.Configuration;

/**
 * 日志默认Repository
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月19日
 * @since
 */
public class DefaultLogger implements Logger {
	private static final long serialVersionUID = 7246287564612630746L;
	private Configuration config;

	public DefaultLogger(Configuration config) {
		this.config = config;
	}

	public void debug(String msg) {
		this.println(Level.DEBUG, msg, null);
	}

	public void debug(String msg, Throwable t) {
		this.println(Level.DEBUG, msg, t);
	}

	public void info(String msg) {
		this.println(Level.INFO, msg, null);
	}

	public void info(String msg, Throwable t) {
		this.println(Level.INFO, msg, t);
	}

	public void warn(String msg) {
		this.println(Level.WARN, msg, null);
	}

	public void warn(String msg, Throwable t) {
		this.println(Level.WARN, msg, t);
	}

	public void error(String msg) {
		this.println(Level.ERROR, msg, null);
	}

	public void error(String msg, Throwable t) {
		this.println(Level.ERROR, msg, t);
	}

	private void println(Level l, String msg, Throwable t) {
		if (!this.config.isLoggingEnable())
			return;

		StringBuffer buf = new StringBuffer(8129);
		buf.append("[");
		buf.append(l.name());
		buf.append("] [");
		buf.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		buf.append("] [");

		Thread th = Thread.currentThread();
		buf.append(th.getName());
		buf.append("-");
		buf.append(th.getId());
		buf.append("] [");

		StackTraceElement[] ste = th.getStackTrace();
		if (ste != null && ste.length > 2) {
			buf.append(ste[3].getClassName());
			buf.append(".");
			buf.append(ste[3].getMethodName());
			buf.append("(");
			buf.append(ste[3].getFileName());
			buf.append(":");
			buf.append(ste[3].getLineNumber());
			buf.append(")");
		} else {
			buf.append(DefaultLogger.class);
		}

		buf.append("] ");
		buf.append(msg);
		if (t != null) {
			buf.append("\n");
			Writer w = new StringWriter(8129);
			PrintWriter pw = new PrintWriter(w);
			t.printStackTrace(pw);
			buf.append(w.toString());
		}

		PrintStream out = null;
		if (l.getValue() <= this.config.getLevel().getValue()) {
			if (l.getValue() < Level.INFO.getValue()) {
				out = System.err;
			} else {
				out = System.out;
			}
			out.println(buf);
		}
	}

}
