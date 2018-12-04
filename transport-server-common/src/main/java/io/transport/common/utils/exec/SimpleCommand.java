package io.transport.common.utils.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System shell execution.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2018年5月23日
 * @since
 */
public class SimpleCommand {
	final private static Logger logger = LoggerFactory.getLogger(SimpleCommand.class);

	public static String exec(String cmd) {
		return exec(cmd, 10);
	}

	/**
	 * Execution shell simple command
	 * 
	 * @param cmd
	 * @param timeout
	 * @return
	 */
	public static String exec(String cmd, int timeout) {
		InputStream in = null;
		InputStreamReader inr = null;
		BufferedReader read = null;
		try {
			Process pro = Runtime.getRuntime().exec(cmd);
			pro.waitFor(timeout, TimeUnit.SECONDS);
			in = pro.getInputStream();
			inr = new InputStreamReader(in);
			read = new BufferedReader(inr);
			return read.readLine();
		} catch (Exception e) {
			logger.error("执行shell失败. cmd=" + cmd, e);
		} finally {
			try {
				if (read != null)
					read.close();
			} catch (IOException e) {
				logger.error("", e);
			}
			try {
				if (inr != null)
					inr.close();
			} catch (IOException e) {
				logger.error("", e);
			}
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(exec("cat /proc/sys/fs/file-max", 10));
		System.out.println(exec("lsof -i:2552 | wc -l", 10));
	}

}
