/**
 * Copyright (c) 2014 ~ , wangl.sir Individual Inc. All rights reserved. It was made by wangl.sir Auto Build Generated file. Contact us 983708408@qq.com.
 */
/**
 * Copyright (c) 2014 ~ , wangl.sir Individual Inc. All rights reserved. It was made by wangl.sir Auto Build Generated file. Contact us 983708408@qq.com.
 */
package io.transport.common.utils.web;

import javax.servlet.http.HttpServletRequest;

import io.transport.common.utils.web.UserAgentKit.Browser;
import io.transport.common.utils.web.UserAgentKit.DeviceType;
import io.transport.common.utils.web.UserAgentKit.UserAgent;

/**
 * 用户代理字符串识别工具
 * 
 * @author ThinkGem
 * @version 2014-6-13
 */
public class UserAgents {

	/**
	 * 获取用户代理对象
	 * 
	 * @param request
	 * @return
	 */
	public static UserAgent getUserAgent(String userAgent) {
		return UserAgent.parseUserAgentString(userAgent);
	}

	/**
	 * 获取用户代理对象
	 * 
	 * @param request
	 * @return
	 */
	public static UserAgent getUserAgent(HttpServletRequest request) {
		return getUserAgent(request.getHeader("User-Agent"));
	}

	/**
	 * 获取设备类型
	 * 
	 * @param request
	 * @return
	 */
	public static DeviceType getDeviceType(String userAgent) {
		return getUserAgent(userAgent).getOperatingSystem().getDeviceType();
	}

	/**
	 * 获取设备类型
	 * 
	 * @param request
	 * @return
	 */
	public static DeviceType getDeviceType(HttpServletRequest request) {
		return getUserAgent(request).getOperatingSystem().getDeviceType();
	}

	/**
	 * 是否是PC
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isComputer(String userAgent) {
		return DeviceType.COMPUTER.equals(getDeviceType(userAgent));
	}

	/**
	 * 是否是PC
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isComputer(HttpServletRequest request) {
		return DeviceType.COMPUTER.equals(getDeviceType(request));
	}

	/**
	 * 是否是手机
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isMobile(HttpServletRequest request) {
		return DeviceType.MOBILE.equals(getDeviceType(request));
	}

	/**
	 * 是否是平板
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isTablet(HttpServletRequest request) {
		return DeviceType.TABLET.equals(getDeviceType(request));
	}

	/**
	 * 是否是手机和平板
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isMobileOrTablet(HttpServletRequest request) {
		DeviceType deviceType = getDeviceType(request);
		return DeviceType.MOBILE.equals(deviceType) || DeviceType.TABLET.equals(deviceType);
	}

	/**
	 * 获取浏览类型
	 * 
	 * @param request
	 * @return
	 */
	public static Browser getBrowser(HttpServletRequest request) {
		return getUserAgent(request).getBrowser();
	}

	/**
	 * 是否IE版本是否小于等于IE8
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isLteIE8(HttpServletRequest request) {
		Browser browser = getBrowser(request);
		return Browser.IE5.equals(browser) || Browser.IE6.equals(browser) || Browser.IE7.equals(browser)
				|| Browser.IE8.equals(browser);
	}

}
