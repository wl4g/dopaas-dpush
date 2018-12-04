/*
 * Copyright (c) 2008, Harald Walker (bitwalker.nl) 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 * 
 * * Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * 
 * * Neither the name of bitwalker nor the names of its
 * contributors may be used to endorse or promote products
 * derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.transport.common.utils.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Use UserAgentUtils-1.2.4.jar(nl.bitwalker.useragentutils)<br/>
 * 
 * HTTP访问客户端类型工具Kit
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年1月4日
 * @since
 */
class UserAgentKit {

	/**
	 * Enum constants for internet applications like web-application and rich
	 * internet application.
	 * 
	 * @author harald
	 * 
	 */

	public static enum Application {

		HOTMAIL(Manufacturer.MICROSOFT, 1, "Windows Live Hotmail", new String[] { "mail.live.com", "hotmail.msn" },
				ApplicationType.WEBMAIL), GMAIL(Manufacturer.GOOGLE, 5, "Gmail", new String[] { "mail.google.com" },
						ApplicationType.WEBMAIL), YAHOO_MAIL(Manufacturer.YAHOO, 10, "Yahoo Mail",
								new String[] { "mail.yahoo.com" },
								ApplicationType.WEBMAIL), COMPUSERVE(Manufacturer.COMPUSERVE, 20, "Compuserve",
										new String[] { "csmail.compuserve.com" },
										ApplicationType.WEBMAIL), AOL_WEBMAIL(Manufacturer.AOL, 30, "AOL webmail",
												new String[] { "webmail.aol.com" }, ApplicationType.WEBMAIL),
		/**
		 * MobileMe webmail client by Apple. Previously known as .mac.
		 */
		MOBILEME(Manufacturer.APPLE, 40, "MobileMe", new String[] { "www.me.com" }, ApplicationType.WEBMAIL),
		/**
		 * Mail.com Mail.com provides consumers with web-based e-mail services
		 */
		MAIL_COM(Manufacturer.MMC, 50, "Mail.com", new String[] { ".mail.com" }, ApplicationType.WEBMAIL),
		/**
		 * Popular open source webmail client. Often installed by providers or
		 * privately.
		 */
		HORDE(Manufacturer.OTHER, 50, "horde", new String[] { "horde" }, ApplicationType.WEBMAIL), OTHER_WEBMAIL(Manufacturer.OTHER, 60, "Other webmail client", new String[] { "webmail", "webemail" }, ApplicationType.WEBMAIL), UNKNOWN(Manufacturer.OTHER, 0, "Unknown", new String[0], ApplicationType.UNKNOWN);

		private final short id;
		private final String name;
		private final String[] aliases;
		private final ApplicationType applicationType;
		private final Manufacturer manufacturer;

		private Application(Manufacturer manufacturer, int versionId, String name, String[] aliases,
				ApplicationType applicationType) {
			this.id = (short) ((manufacturer.getId() << 8) + (byte) versionId);
			this.name = name;
			this.aliases = aliases;
			this.applicationType = applicationType;
			this.manufacturer = manufacturer;
		}

		public short getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		/**
		 * @return the applicationType
		 */
		public ApplicationType getApplicationType() {
			return applicationType;
		}

		/**
		 * @return the manufacturer
		 */
		public Manufacturer getManufacturer() {
			return manufacturer;
		}

		/*
		 * Checks if the given referrer string matches to the application. Only
		 * checks for one specific application.
		 */
		public boolean isInReferrerString(String referrerString) {
			for (String alias : aliases) {
				if (referrerString.toLowerCase().indexOf(alias.toLowerCase()) != -1)
					return true;
			}
			return false;
		}

		/*
		 * Iterates over all Application to compare the signature with the
		 * referrer string. If no match can be found Application.UNKNOWN will be
		 * returned.
		 */
		public static Application parseReferrerString(String referrerString) {
			// skip the empty and "-" referrer
			if (referrerString != null && referrerString.length() > 1) {
				for (Application applicationInList : Application.values()) {
					if (applicationInList.isInReferrerString(referrerString))
						return applicationInList;
				}
			}
			return Application.UNKNOWN;
		}

		/**
		 * Returns the enum constant of this type with the specified id. Throws
		 * IllegalArgumentException if the value does not exist.
		 * 
		 * @param id
		 * @return
		 */
		public static Application valueOf(short id) {
			for (Application application : Application.values()) {
				if (application.getId() == id)
					return application;
			}

			// same behavior as standard valueOf(string) method
			throw new IllegalArgumentException("No enum const for id " + id);
		}

	}

	/**
	 * Enum constants classifying the different types of applications which are
	 * common in referrer strings
	 * 
	 * @author harald
	 *
	 */
	public static enum ApplicationType {

		/**
		 * Webmail service like Windows Live Hotmail and Gmail.
		 */
		WEBMAIL("Webmail client"), UNKNOWN("unknown");

		private String name;

		private ApplicationType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	/**
	 * Enum constants for most common browsers, including e-mail clients and
	 * bots.
	 * 
	 * @author harald
	 * 
	 */
	public static enum Browser {

		OPERA(Manufacturer.OPERA, null, 1, "Opera", new String[] { "Opera" }, null, BrowserType.WEB_BROWSER,
				RenderingEngine.PRESTO, "Opera\\/(([\\d]+)\\.([\\w]+))"), // before
																			// MSIE
		OPERA_MINI(Manufacturer.OPERA, Browser.OPERA, 20, "Opera Mini", new String[] { "Opera Mini" }, null,
				BrowserType.MOBILE_BROWSER, RenderingEngine.PRESTO, null), // Opera
																			// for
																			// mobile
																			// devices
		/**
		 * For some strange reason Opera uses 9.80 in the user-agent string.
		 */
		OPERA10(Manufacturer.OPERA, Browser.OPERA, 10, "Opera 10", new String[] { "Opera/9.8" }, null, BrowserType.WEB_BROWSER, RenderingEngine.PRESTO, "Version\\/(([\\d]+)\\.([\\w]+))"), OPERA9(Manufacturer.OPERA, Browser.OPERA, 5, "Opera 9", new String[] { "Opera/9" }, null, BrowserType.WEB_BROWSER, RenderingEngine.PRESTO, null), KONQUEROR(Manufacturer.OTHER, null, 1, "Konqueror", new String[] { "Konqueror" }, null, BrowserType.WEB_BROWSER, RenderingEngine.KHTML, "Konqueror\\/(([0-9]+)\\.?([\\w]+)?(-[\\w]+)?)"),

		/**
		 * Outlook email client
		 */
		OUTLOOK(Manufacturer.MICROSOFT, null, 100, "Outlook", new String[] { "MSOffice" }, null, BrowserType.EMAIL_CLIENT, RenderingEngine.WORD, "MSOffice (([0-9]+))"), // before
																																											// IE7
		/**
		 * Microsoft Outlook 2007 identifies itself as MSIE7 but uses the html
		 * rendering engine of Word 2007. Example user agent: Mozilla/4.0
		 * (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727;
		 * .NET CLR 3.0.04506; .NET CLR 1.1.4322; MSOffice 12)
		 */
		OUTLOOK2007(Manufacturer.MICROSOFT, Browser.OUTLOOK, 107, "Outlook 2007", new String[] { "MSOffice 12" }, null, BrowserType.EMAIL_CLIENT, RenderingEngine.WORD, null), // before
																																												// IE7
		/**
		 * Outlook 2010 is still using the rendering engine of Word.
		 * http://www.fixoutlook.org
		 */
		OUTLOOK2010(Manufacturer.MICROSOFT, Browser.OUTLOOK, 108, "Outlook 2010", new String[] { "MSOffice 14" }, null, BrowserType.EMAIL_CLIENT, RenderingEngine.WORD, null), // before
																																												// IE7

		/**
		 * Family of Internet Explorer browsers
		 */
		IE(Manufacturer.MICROSOFT, null, 1, "Internet Explorer", new String[] { "MSIE" }, null, BrowserType.WEB_BROWSER, RenderingEngine.TRIDENT, "MSIE (([\\d]+)\\.([\\w]+))"), // before
																																													// Mozilla
		/**
		 * Since version 7 Outlook Express is identifying itself. By detecting
		 * Outlook Express we can not identify the Internet Explorer version
		 * which is probably used for the rendering. Obviously this product is
		 * now called Windows Live Mail Desktop or just Windows Live Mail.
		 */
		OUTLOOK_EXPRESS7(Manufacturer.MICROSOFT, Browser.IE, 110, "Windows Live Mail", new String[] { "Outlook-Express/7.0" }, null, BrowserType.EMAIL_CLIENT, RenderingEngine.TRIDENT, null), // before
																																																// IE7,
																																																// previously
																																																// known
																																																// as
																																																// Outlook
																																																// Express.
																																																// First
																																																// released
																																																// in
																																																// 2006,
																																																// offered
																																																// with
																																																// different
																																																// name
																																																// later
		/**
		 * Since 2007 the mobile edition of Internet Explorer identifies itself
		 * as IEMobile in the user-agent. If previous versions have to be
		 * detected, use the operating system information as well.
		 */
		IEMOBILE7(Manufacturer.MICROSOFT, Browser.IE, 121, "IE Mobile 7", new String[] { "IEMobile 7" }, null, BrowserType.MOBILE_BROWSER, RenderingEngine.TRIDENT, null), // before
																																											// MSIE
																																											// strings
		IEMOBILE6(Manufacturer.MICROSOFT, Browser.IE, 120, "IE Mobile 6", new String[] { "IEMobile 6" }, null,
				BrowserType.MOBILE_BROWSER, RenderingEngine.TRIDENT, null), // before
																			// MSIE
		IE9(Manufacturer.MICROSOFT, Browser.IE, 90, "Internet Explorer 9", new String[] { "MSIE 9" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.TRIDENT, null), // before
																			// MSIE
		IE8(Manufacturer.MICROSOFT, Browser.IE, 80, "Internet Explorer 8", new String[] { "MSIE 8" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.TRIDENT, null), // before
																			// MSIE
		IE7(Manufacturer.MICROSOFT, Browser.IE, 70, "Internet Explorer 7", new String[] { "MSIE 7" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.TRIDENT, null), // before
																			// MSIE
		IE6(Manufacturer.MICROSOFT, Browser.IE, 60, "Internet Explorer 6", new String[] { "MSIE 6" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.TRIDENT, null), // before
																			// MSIE
		IE5_5(Manufacturer.MICROSOFT, Browser.IE, 55, "Internet Explorer 5.5", new String[] { "MSIE 5.5" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.TRIDENT, null), // before
																			// MSIE
		IE5(Manufacturer.MICROSOFT, Browser.IE, 50, "Internet Explorer 5", new String[] { "MSIE 5" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.TRIDENT, null), // before
																			// MSIE

		/**
		 * Google Chrome browser
		 */
		CHROME(Manufacturer.GOOGLE, null, 1, "Chrome", new String[] { "Chrome" }, null, BrowserType.WEB_BROWSER, RenderingEngine.WEBKIT, "Chrome\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?(\\.[\\w]+)?)"), // before
																																																		// Mozilla
		CHROME10(Manufacturer.GOOGLE, Browser.CHROME, 15, "Chrome 10", new String[] { "Chrome/10" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.WEBKIT, null), // before
																		// Mozilla
		CHROME9(Manufacturer.GOOGLE, Browser.CHROME, 10, "Chrome 9", new String[] { "Chrome/9" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.WEBKIT, null), // before
																		// Mozilla
		CHROME8(Manufacturer.GOOGLE, Browser.CHROME, 5, "Chrome 8", new String[] { "Chrome/8" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.WEBKIT, null), // before
																		// Mozilla

		OMNIWEB(Manufacturer.OTHER, null, 2, "Omniweb", new String[] { "OmniWeb" }, null, BrowserType.WEB_BROWSER,
				RenderingEngine.WEBKIT, null), //

		SAFARI(Manufacturer.APPLE, null, 1, "Safari", new String[] { "Safari" }, null, BrowserType.WEB_BROWSER,
				RenderingEngine.WEBKIT, "Version\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?)"), // before
																							// AppleWebKit
		SAFARI5(Manufacturer.APPLE, Browser.SAFARI, 3, "Safari 5", new String[] { "Version/5" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.WEBKIT, null), // before
																		// AppleWebKit
		MOBILE_SAFARI(Manufacturer.APPLE, Browser.SAFARI, 2, "Mobile Safari",
				new String[] { "Mobile Safari", "Mobile/" }, null, BrowserType.MOBILE_BROWSER, RenderingEngine.WEBKIT,
				null), // before
						// Safari
		SAFARI4(Manufacturer.APPLE, Browser.SAFARI, 4, "Safari 4", new String[] { "Version/4" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.WEBKIT, null), // before
																		// AppleWebKit

		APPLE_MAIL(Manufacturer.APPLE, null, 50, "Apple Mail", new String[] { "AppleWebKit" }, null,
				BrowserType.EMAIL_CLIENT, RenderingEngine.WEBKIT, null), // Microsoft
																			// Entrourage/Outlook
																			// 2010
																			// also
																			// only
																			// identifies
																			// itself
																			// as
																			// AppleWebKit
		LOTUS_NOTES(Manufacturer.OTHER, null, 3, "Lotus Notes", new String[] { "Lotus-Notes" }, null,
				BrowserType.EMAIL_CLIENT, RenderingEngine.OTHER, "Lotus-Notes\\/(([\\d]+)\\.([\\w]+))"), // before
																											// Mozilla

		/*
		 * Thunderbird email client, based on the same Gecko engine Firefox is
		 * using.
		 */
		THUNDERBIRD(Manufacturer.MOZILLA, null, 110, "Thunderbird", new String[] { "Thunderbird" }, null,
				BrowserType.EMAIL_CLIENT, RenderingEngine.GECKO,
				"Thunderbird\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?(\\.[\\w]+)?)"), // using
																					// Gecko
																					// Engine
		THUNDERBIRD3(Manufacturer.MOZILLA, Browser.THUNDERBIRD, 130, "Thunderbird 3", new String[] { "Thunderbird/3" },
				null, BrowserType.EMAIL_CLIENT, RenderingEngine.GECKO, null), // using
																				// Gecko
																				// Engine
		THUNDERBIRD2(Manufacturer.MOZILLA, Browser.THUNDERBIRD, 120, "Thunderbird 2", new String[] { "Thunderbird/2" },
				null, BrowserType.EMAIL_CLIENT, RenderingEngine.GECKO, null), // using
																				// Gecko
																				// Engine

		CAMINO(Manufacturer.OTHER, null, 5, "Camino", new String[] { "Camino" }, null, BrowserType.WEB_BROWSER,
				RenderingEngine.GECKO, "Camino\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?)"), // using
																						// Gecko
																						// Engine
		CAMINO2(Manufacturer.OTHER, Browser.CAMINO, 17, "Camino 2", new String[] { "Camino/2" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.GECKO, null), // using
																		// Gecko
																		// Engine
		FLOCK(Manufacturer.OTHER, null, 4, "Flock", new String[] { "Flock" }, null, BrowserType.WEB_BROWSER,
				RenderingEngine.GECKO, "Flock\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?)"),

		FIREFOX(Manufacturer.MOZILLA, null, 10, "Firefox", new String[] { "Firefox" }, null, BrowserType.WEB_BROWSER,
				RenderingEngine.GECKO, "Firefox\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?(\\.[\\w]+)?)"), // using
																										// Gecko
																										// Engine
		FIREFOX3MOBILE(Manufacturer.MOZILLA, Browser.FIREFOX, 31, "Firefox 3 Mobile",
				new String[] { "Firefox/3.5 Maemo" }, null, BrowserType.MOBILE_BROWSER, RenderingEngine.GECKO, null), // using
																														// Gecko
																														// Engine
		FIREFOX4(Manufacturer.MOZILLA, Browser.FIREFOX, 40, "Firefox 4", new String[] { "Firefox/4" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.GECKO, null), // using
																		// Gecko
																		// Engine
		FIREFOX3(Manufacturer.MOZILLA, Browser.FIREFOX, 30, "Firefox 3", new String[] { "Firefox/3" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.GECKO, null), // using
																		// Gecko
																		// Engine
		FIREFOX2(Manufacturer.MOZILLA, Browser.FIREFOX, 20, "Firefox 2", new String[] { "Firefox/2" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.GECKO, null), // using
																		// Gecko
																		// Engine
		FIREFOX1_5(Manufacturer.MOZILLA, Browser.FIREFOX, 15, "Firefox 1.5", new String[] { "Firefox/1.5" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.GECKO, null), // using
																		// Gecko
																		// Engine

		SEAMONKEY(Manufacturer.OTHER, null, 15, "SeaMonkey", new String[] { "SeaMonkey" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.GECKO, "SeaMonkey\\/(([0-9]+)\\.?([\\w]+)?(\\.[\\w]+)?)"), // using
																													// Gecko
																													// Engine

		BOT(Manufacturer.OTHER,
				null, 12, "Robot/Spider", new String[] { "Googlebot", "bot", "spider", "crawler", "Feedfetcher",
						"Slurp", "Twiceler", "Nutch", "BecomeBot" },
				null, BrowserType.ROBOT, RenderingEngine.OTHER, null),

		MOZILLA(Manufacturer.MOZILLA, null, 1, "Mozilla", new String[] { "Mozilla", "Moozilla" }, null,
				BrowserType.WEB_BROWSER, RenderingEngine.OTHER, null), // rest
																		// of
																		// the
																		// mozilla
																		// browsers

		CFNETWORK(Manufacturer.OTHER, null, 6, "CFNetwork", new String[] { "CFNetwork" }, null, BrowserType.UNKNOWN,
				RenderingEngine.OTHER, null), // Mac OS X cocoa library

		EUDORA(Manufacturer.OTHER, null, 7, "Eudora", new String[] { "Eudora", "EUDORA" }, null,
				BrowserType.EMAIL_CLIENT, RenderingEngine.OTHER, null), // email
																		// client
																		// by
																		// Qualcomm

		POCOMAIL(Manufacturer.OTHER, null, 8, "PocoMail", new String[] { "PocoMail" }, null, BrowserType.EMAIL_CLIENT,
				RenderingEngine.OTHER, null),

		THEBAT(Manufacturer.OTHER, null, 9, "The Bat!", new String[] { "The Bat" }, null, BrowserType.EMAIL_CLIENT,
				RenderingEngine.OTHER, null), // Email Client

		NETFRONT(Manufacturer.OTHER, null, 10, "NetFront", new String[] { "NetFront" }, null,
				BrowserType.MOBILE_BROWSER, RenderingEngine.OTHER, null), // mobile
																			// device
																			// browser

		EVOLUTION(Manufacturer.OTHER, null, 11, "Evolution", new String[] { "CamelHttpStream" }, null,
				BrowserType.EMAIL_CLIENT, RenderingEngine.OTHER, null), // http://www.go-evolution.org/Camel.Stream

		LYNX(Manufacturer.OTHER, null, 13, "Lynx", new String[] { "Lynx" }, null, BrowserType.TEXT_BROWSER,
				RenderingEngine.OTHER, "Lynx\\/(([0-9]+)\\.([\\d]+)\\.?([\\w-+]+)?\\.?([\\w-+]+)?)"),

		DOWNLOAD(Manufacturer.OTHER, null, 16, "Downloading Tool", new String[] { "cURL", "wget" }, null,
				BrowserType.TEXT_BROWSER, RenderingEngine.OTHER, null),

		UNKNOWN(Manufacturer.OTHER, null, 14, "Unknown", new String[0], null, BrowserType.UNKNOWN,
				RenderingEngine.OTHER, null);

		private final short id;
		private final String name;
		private final String[] aliases;
		private final String[] excludeList; // don't match when these values are
											// in
											// the agent-string
		private final BrowserType browserType;
		private final Manufacturer manufacturer;
		private final RenderingEngine renderingEngine;
		private final Browser parent;
		private List<Browser> children;
		private Pattern versionRegEx;

		private Browser(Manufacturer manufacturer, Browser parent, int versionId, String name, String[] aliases,
				String[] exclude, BrowserType browserType, RenderingEngine renderingEngine, String versionRegexString) {
			this.id = (short) ((manufacturer.getId() << 8) + (byte) versionId);
			this.name = name;
			this.parent = parent;
			this.children = new ArrayList<Browser>();
			if (this.parent != null) {
				this.parent.children.add(this);
			}
			this.aliases = aliases;
			this.excludeList = exclude;
			this.browserType = browserType;
			this.manufacturer = manufacturer;
			this.renderingEngine = renderingEngine;
			if (versionRegexString != null) {
				this.versionRegEx = Pattern.compile(versionRegexString);
			}
		}

		public short getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		private Pattern getVersionRegEx() {
			if (this.versionRegEx == null) {
				if (this.getGroup() != this)
					return this.getGroup().getVersionRegEx();
				else
					return null;
			}
			return this.versionRegEx;
		}

		/**
		 * Detects the detailed version information of the browser. Depends on
		 * the userAgent to be available. Returns null if it can not detect the
		 * version information.
		 * 
		 * @return Version
		 */
		public Version getVersion(String userAgentString) {
			Pattern pattern = this.getVersionRegEx();
			if (userAgentString != null && pattern != null) {
				Matcher matcher = pattern.matcher(userAgentString);
				if (matcher.find()) {
					String fullVersionString = matcher.group(1);
					String majorVersion = matcher.group(2);
					String minorVersion = "0";
					if (matcher.groupCount() > 2) // usually but not always
													// there is
													// a minor version
						minorVersion = matcher.group(3);
					return new Version(fullVersionString, majorVersion, minorVersion);
				}
			}
			return null;
		}

		/**
		 * @return the browserType
		 */
		public BrowserType getBrowserType() {
			return browserType;
		}

		/**
		 * @return the manufacturer
		 */
		public Manufacturer getManufacturer() {
			return manufacturer;
		}

		/**
		 * @return the rendering engine
		 */
		public RenderingEngine getRenderingEngine() {
			return renderingEngine;
		}

		/**
		 * @return top level browser family
		 */
		public Browser getGroup() {
			if (this.parent != null) {
				return parent.getGroup();
			}
			return this;
		}

		/*
		 * Checks if the given user-agent string matches to the browser. Only
		 * checks for one specific browser.
		 */
		public boolean isInUserAgentString(String agentString) {
			for (String alias : aliases) {
				if (agentString.toLowerCase().indexOf(alias.toLowerCase()) != -1)
					return true;
			}
			return false;
		}

		/**
		 * Checks if the given user-agent does not contain one of the tokens
		 * which should not match. In most cases there are no excluding tokens,
		 * so the impact should be small.
		 * 
		 * @param agentString
		 * @return
		 */
		private boolean containsExcludeToken(String agentString) {
			if (excludeList != null) {
				for (String exclude : excludeList) {
					if (agentString.toLowerCase().indexOf(exclude.toLowerCase()) != -1)
						return true;
				}
			}
			return false;
		}

		private Browser checkUserAgent(String agentString) {
			if (this.isInUserAgentString(agentString)) {
				if (this.children.size() > 0) {
					for (Browser childBrowser : this.children) {
						Browser match = childBrowser.checkUserAgent(agentString);
						if (match != null) {
							return match;
						}
					}
				}
				// if children didn't match we continue checking the current to
				// prevent false positives
				if (!this.containsExcludeToken(agentString)) {
					return this;
				}

			}
			return null;
		}

		/**
		 * Iterates over all Browsers to compare the browser signature with the
		 * user agent string. If no match can be found Browser.UNKNOWN will be
		 * returned.
		 * 
		 * @param agentString
		 * @return Browser
		 */
		public static Browser parseUserAgentString(String agentString) {
			for (Browser browser : Browser.values()) {
				// only check top level objects
				if (browser.parent == null) {
					Browser match = browser.checkUserAgent(agentString);
					if (match != null) {
						return match; // either current operatingSystem or a
										// child
										// object
					}
				}
			}
			return Browser.UNKNOWN;
		}

		/**
		 * Returns the enum constant of this type with the specified id. Throws
		 * IllegalArgumentException if the value does not exist.
		 * 
		 * @param id
		 * @return
		 */
		public static Browser valueOf(short id) {
			for (Browser browser : Browser.values()) {
				if (browser.getId() == id)
					return browser;
			}

			// same behavior as standard valueOf(string) method
			throw new IllegalArgumentException("No enum const for id " + id);
		}

	}

	/**
	 * Enum constants classifying the different types of browsers which are
	 * common in user-agent strings
	 * 
	 * @author harald
	 *
	 */
	public static enum BrowserType {

		/**
		 * Standard web-browser
		 */
		WEB_BROWSER("Browser"),
		/**
		 * Special web-browser for mobile devices
		 */
		MOBILE_BROWSER("Browser (mobile)"),
		/**
		 * Text only browser like the good old Lynx
		 */
		TEXT_BROWSER("Browser (text only)"),
		/**
		 * Email client like Thunderbird
		 */
		EMAIL_CLIENT("Email Client"),
		/**
		 * Search robot, spider, crawler,...
		 */
		ROBOT("Robot"),
		/**
		 * Downloading tools
		 */
		TOOL("Downloading tool"), UNKNOWN("unknown");

		private String name;

		private BrowserType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	/**
	 * Enum contact for the type of used device
	 * 
	 * @author harald
	 *
	 */
	public static enum DeviceType {

		/**
		 * Standard desktop or laptop computer
		 */
		COMPUTER("Computer"),
		/**
		 * Mobile phone or similar small mobile device
		 */
		MOBILE("Mobile"),
		/**
		 * Small tablet type computer.
		 */
		TABLET("Tablet"),
		/**
		 * Game console like the Wii or Playstation.
		 */
		GAME_CONSOLE("Game console"),
		/**
		 * Digital media receiver like the Apple TV. No device detection
		 * implemented yet for this category. Please send provide user-agent
		 * strings if you have some.
		 */
		DMR("Digital media receiver"),
		/**
		 * Other or unknow type of device.
		 */
		UNKNOWN("Unknown");

		String name;

		private DeviceType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	/**
	 * Enum constants representing manufacturers of operating systems and client
	 * software. Manufacturer could be used for specific handling of browser
	 * requests.
	 * 
	 * @author harald
	 */
	public static enum Manufacturer {

		/**
		 * Unknow or rare manufacturer
		 */
		OTHER(1, "Other"),
		/**
		 * Microsoft Corporation
		 */
		MICROSOFT(2, "Microsoft Corporation"),
		/**
		 * Apple Inc.
		 */
		APPLE(3, "Apple Inc."),
		/**
		 * Sun Microsystems, Inc.
		 */
		SUN(4, "Sun Microsystems, Inc."),
		/**
		 * Symbian Ltd.
		 */
		SYMBIAN(5, "Symbian Ltd."),
		/**
		 * Nokia Corporation
		 */
		NOKIA(6, "Nokia Corporation"),
		/**
		 * Research In Motion Limited
		 */
		BLACKBERRY(7, "Research In Motion Limited"),
		/**
		 * Palm, Inc.
		 */
		PALM(8, "Palm, Inc. "),
		/**
		 * Sony Ericsson Mobile Communications AB
		 */
		SONY_ERICSSON(9, "Sony Ericsson Mobile Communications AB"),
		/**
		 * Sony Computer Entertainment, Inc.
		 */
		SONY(10, "Sony Computer Entertainment, Inc."),
		/**
		 * Nintendo
		 */
		NINTENDO(11, "Nintendo"),
		/**
		 * Opera Software ASA
		 */
		OPERA(12, "Opera Software ASA"),
		/**
		 * Mozilla Foundation
		 */
		MOZILLA(13, "Mozilla Foundation"),
		/**
		 * Google Inc.
		 */
		GOOGLE(15, "Google Inc."),
		/**
		 * CompuServe Interactive Services, Inc.
		 */
		COMPUSERVE(16, "CompuServe Interactive Services, Inc."),
		/**
		 * Yahoo Inc.
		 */
		YAHOO(17, "Yahoo Inc."),
		/**
		 * AOL LLC.
		 */
		AOL(18, "AOL LLC."),
		/**
		 * Mail.com Media Corporation
		 */
		MMC(19, "Mail.com Media Corporation");

		private final byte id;
		private final String name;

		private Manufacturer(int id, String name) {
			this.id = (byte) id;
			this.name = name;
		}

		/**
		 * @return the id
		 */
		public byte getId() {
			return id;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

	}

	/**
	 * Enum constants for most common operating systems.
	 * 
	 * @author harald
	 */
	public static enum OperatingSystem {

		// the order is important since the agent string is being compared with
		// the aliases
		/**
		 * Windows Mobile / Windows CE. Exact version unknown.
		 */
		WINDOWS(Manufacturer.MICROSOFT, null, 1, "Windows", new String[] { "Windows" }, new String[] { "Palm" }, DeviceType.COMPUTER, null), // catch
																																				// the
																																				// rest
																																				// of
																																				// older
																																				// Windows
																																				// systems
																																				// (95,
																																				// NT,...)
		WINDOWS_7(Manufacturer.MICROSOFT, OperatingSystem.WINDOWS, 21, "Windows 7", new String[] { "Windows NT 6.1" },
				null, DeviceType.COMPUTER, null), // before Win, yes, Windows 7
													// is called 6.1 LOL
		WINDOWS_VISTA(Manufacturer.MICROSOFT, OperatingSystem.WINDOWS, 20, "Windows Vista",
				new String[] { "Windows NT 6" }, null, DeviceType.COMPUTER, null), // before
																					// Win
		WINDOWS_2000(Manufacturer.MICROSOFT, OperatingSystem.WINDOWS, 15, "Windows 2000",
				new String[] { "Windows NT 5.0" }, null, DeviceType.COMPUTER, null), // before
																						// Win
		WINDOWS_XP(Manufacturer.MICROSOFT, OperatingSystem.WINDOWS, 10, "Windows XP", new String[] { "Windows NT 5" },
				null, DeviceType.COMPUTER, null), // before Win, 5.1 and 5.2 are
													// basically XP systems
		WINDOWS_MOBILE7(Manufacturer.MICROSOFT, OperatingSystem.WINDOWS, 51, "Windows Mobile 7",
				new String[] { "Windows Phone OS 7" }, null, DeviceType.MOBILE, null), // before
																						// Win
		WINDOWS_MOBILE(Manufacturer.MICROSOFT, OperatingSystem.WINDOWS, 50, "Windows Mobile",
				new String[] { "Windows CE" }, null, DeviceType.MOBILE, null), // before
																				// Win
		WINDOWS_98(Manufacturer.MICROSOFT, OperatingSystem.WINDOWS, 5, "Windows 98",
				new String[] { "Windows 98", "Win98" }, new String[] { "Palm" }, DeviceType.COMPUTER, null), // before
																												// Win

		ANDROID(Manufacturer.GOOGLE, null, 0, "Android", new String[] { "Android" }, null, DeviceType.MOBILE,
				null), ANDROID3_TABLET(Manufacturer.GOOGLE, OperatingSystem.ANDROID, 30, "Android 3.x Tablet",
						new String[] { "Android 3" }, null, DeviceType.TABLET, null), // as
																						// long
																						// as
																						// there
																						// are
																						// not
																						// Android
																						// 3.x
																						// phones
																						// this
																						// should
																						// be
																						// enough
		ANDROID2(Manufacturer.GOOGLE, OperatingSystem.ANDROID, 2, "Android 2.x", new String[] { "Android 2" }, null,
				DeviceType.MOBILE, null), ANDROID2_TABLET(Manufacturer.GOOGLE, OperatingSystem.ANDROID2, 20,
						"Android 2.x Tablet", new String[] { "GT-P1000", "SCH-I800" }, null, DeviceType.TABLET,
						null), ANDROID1(Manufacturer.GOOGLE, OperatingSystem.ANDROID, 1, "Android 1.x",
								new String[] { "Android 1" }, null, DeviceType.MOBILE, null),

		/**
		 * PalmOS, exact version unkown
		 */
		WEBOS(Manufacturer.PALM, null, 11, "WebOS", new String[] { "webOS" }, null, DeviceType.MOBILE, null), PALM(Manufacturer.PALM, null, 10, "PalmOS", new String[] { "Palm" }, null, DeviceType.MOBILE, null),

		/**
		 * iOS4, with the release of the iPhone 4, Apple renamed the OS to iOS.
		 */
		IOS(Manufacturer.APPLE, null, 2, "iOS", new String[] { "like Mac OS X" }, null, DeviceType.MOBILE, null), // before
																													// MAC_OS_X_IPHONE
																													// for
																													// all
																													// older
																													// versions
		iOS4_IPHONE(Manufacturer.APPLE, OperatingSystem.IOS, 41, "iOS 4 (iPhone)", new String[] { "iPhone OS 4" }, null,
				DeviceType.MOBILE, null), // before MAC_OS_X_IPHONE for all
											// older versions
		MAC_OS_X_IPAD(Manufacturer.APPLE, OperatingSystem.IOS, 50, "Mac OS X (iPad)", new String[] { "iPad" }, null,
				DeviceType.TABLET, null), // before Mac OS X
		MAC_OS_X_IPHONE(Manufacturer.APPLE, OperatingSystem.IOS, 40, "Mac OS X (iPhone)", new String[] { "iPhone" },
				null, DeviceType.MOBILE, null), // before Mac OS X
		MAC_OS_X_IPOD(Manufacturer.APPLE, OperatingSystem.IOS, 30, "Mac OS X (iPod)", new String[] { "iPod" }, null,
				DeviceType.MOBILE, null), // before Mac OS X

		MAC_OS_X(Manufacturer.APPLE, null, 10, "Mac OS X", new String[] { "Mac OS X", "CFNetwork" }, null,
				DeviceType.COMPUTER, null), // before Mac

		/**
		 * Older Mac OS systems before Mac OS X
		 */
		MAC_OS(Manufacturer.APPLE, null, 1, "Mac OS", new String[] { "Mac" }, null, DeviceType.COMPUTER, null), // older
																												// Mac
																												// OS
																												// systems

		/**
		 * Linux based Maemo software platform by Nokia. Used in the N900 phone.
		 * http://maemo.nokia.com/
		 */
		MAEMO(Manufacturer.NOKIA, null, 2, "Maemo", new String[] { "Maemo" }, null, DeviceType.MOBILE, null),

		/**
		 * Various Linux based operating systems.
		 */
		LINUX(Manufacturer.OTHER, null, 2, "Linux", new String[] { "Linux", "CamelHttpStream" }, null, DeviceType.COMPUTER, null), // CamelHttpStream
																																	// is
																																	// being
																																	// used
																																	// by
																																	// Evolution,
																																	// an
																																	// email
																																	// client
																																	// for
																																	// Linux

		/**
		 * Other Symbian OS versions
		 */
		SYMBIAN(Manufacturer.SYMBIAN, null, 1, "Symbian OS", new String[] { "Symbian", "Series60" }, null, DeviceType.MOBILE, null),
		/**
		 * Symbian OS 9.x versions. Being used by Nokia (N71, N73, N81, N82,
		 * N91, N92, N95, ...)
		 */
		SYMBIAN9(Manufacturer.SYMBIAN, OperatingSystem.SYMBIAN, 20, "Symbian OS 9.x", new String[] { "SymbianOS/9", "Series60/3" }, null, DeviceType.MOBILE, null),
		/**
		 * Symbian OS 8.x versions. Being used by Nokia (6630, 6680, 6681, 6682,
		 * N70, N72, N90).
		 */
		SYMBIAN8(Manufacturer.SYMBIAN, OperatingSystem.SYMBIAN, 15, "Symbian OS 8.x", new String[] { "SymbianOS/8", "Series60/2.6", "Series60/2.8" }, null, DeviceType.MOBILE, null),
		/**
		 * Symbian OS 7.x versions. Being used by Nokia (3230, 6260, 6600, 6620,
		 * 6670, 7610), Panasonic (X700, X800), Samsung (SGH-D720, SGH-D730) and
		 * Lenovo (P930).
		 */
		SYMBIAN7(Manufacturer.SYMBIAN, OperatingSystem.SYMBIAN, 10, "Symbian OS 7.x", new String[] { "SymbianOS/7" }, null, DeviceType.MOBILE, null),
		/**
		 * Symbian OS 6.x versions.
		 */
		SYMBIAN6(Manufacturer.SYMBIAN, OperatingSystem.SYMBIAN, 5, "Symbian OS 6.x", new String[] { "SymbianOS/6" }, null, DeviceType.MOBILE, null),
		/**
		 * Nokia's Series 40 operating system. Series 60 (S60) uses the Symbian
		 * OS.
		 */
		SERIES40(Manufacturer.NOKIA, null, 1, "Series 40", new String[] { "Nokia6300" }, null, DeviceType.MOBILE, null),
		/**
		 * Proprietary operating system used for many Sony Ericsson phones.
		 */
		SONY_ERICSSON(Manufacturer.SONY_ERICSSON, null, 1, "Sony Ericsson", new String[] { "SonyEricsson" }, null, DeviceType.MOBILE, null), // after
																																				// symbian,
																																				// some
																																				// SE
																																				// phones
																																				// use
																																				// symbian
		SUN_OS(Manufacturer.SUN, null, 1, "SunOS", new String[] { "SunOS" }, null, DeviceType.COMPUTER, null), PSP(
				Manufacturer.SONY, null, 1, "Sony Playstation", new String[] { "Playstation" }, null,
				DeviceType.GAME_CONSOLE, null),
		/**
		 * Nintendo Wii game console.
		 */
		WII(Manufacturer.NINTENDO, null, 1, "Nintendo Wii", new String[] { "Wii" }, null, DeviceType.GAME_CONSOLE, null),
		/**
		 * BlackBerryOS. The BlackBerryOS exists in different version. How
		 * relevant those versions are, is not clear.
		 */
		BLACKBERRY(Manufacturer.BLACKBERRY, null, 1, "BlackBerryOS", new String[] { "BlackBerry" }, null, DeviceType.MOBILE, null), BLACKBERRY6(Manufacturer.BLACKBERRY, OperatingSystem.BLACKBERRY, 6, "BlackBerry 6", new String[] { "Version/6" }, null, DeviceType.MOBILE, null),

		UNKNOWN(Manufacturer.OTHER, null, 1, "Unknown", new String[0], null, DeviceType.UNKNOWN, null);

		private final short id;
		private final String name;
		private final String[] aliases;
		private final String[] excludeList; // don't match when these values are
											// in the agent-string
		private final Manufacturer manufacturer;
		private final DeviceType deviceType;
		private final OperatingSystem parent;
		private List<OperatingSystem> children;
		@SuppressWarnings("unused")
		private Pattern versionRegEx;

		private OperatingSystem(Manufacturer manufacturer, OperatingSystem parent, int versionId, String name,
				String[] aliases, String[] exclude, DeviceType deviceType, String versionRegexString) {
			this.manufacturer = manufacturer;
			this.parent = parent;
			this.children = new ArrayList<OperatingSystem>();
			if (this.parent != null) {
				this.parent.children.add(this);
			}
			// combine manufacturer and version id to one unique id.
			this.id = (short) ((manufacturer.getId() << 8) + (byte) versionId);
			this.name = name;
			this.aliases = aliases;
			this.excludeList = exclude;
			this.deviceType = deviceType;
			if (versionRegexString != null) {
				this.versionRegEx = Pattern.compile(versionRegexString);
			}
		}

		public short getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		/*
		 * Shortcut to check of an operating system is a mobile device. Left in
		 * here for backwards compatibility.
		 */
		public boolean isMobileDevice() {
			return deviceType.equals(DeviceType.MOBILE);
		}

		public DeviceType getDeviceType() {
			return deviceType;
		}

		/*
		 * Gets the top level grouping operating system
		 */
		public OperatingSystem getGroup() {
			if (this.parent != null) {
				return parent.getGroup();
			}
			return this;
		}

		/**
		 * Returns the manufacturer of the operating system
		 * 
		 * @return the manufacturer
		 */
		public Manufacturer getManufacturer() {
			return manufacturer;
		}

		/**
		 * Checks if the given user-agent string matches to the operating
		 * system. Only checks for one specific operating system.
		 * 
		 * @param agentString
		 * @return boolean
		 */
		public boolean isInUserAgentString(String agentString) {
			for (String alias : aliases) {
				if (agentString.toLowerCase().indexOf(alias.toLowerCase()) != -1)
					return true;
			}
			return false;
		}

		/**
		 * Checks if the given user-agent does not contain one of the tokens
		 * which should not match. In most cases there are no excluding tokens,
		 * so the impact should be small.
		 * 
		 * @param agentString
		 * @return
		 */
		private boolean containsExcludeToken(String agentString) {
			if (excludeList != null) {
				for (String exclude : excludeList) {
					if (agentString.toLowerCase().indexOf(exclude.toLowerCase()) != -1)
						return true;
				}
			}
			return false;
		}

		private OperatingSystem checkUserAgent(String agentString) {
			if (this.isInUserAgentString(agentString)) {
				if (this.children.size() > 0) {
					for (OperatingSystem childOperatingSystem : this.children) {
						OperatingSystem match = childOperatingSystem.checkUserAgent(agentString);
						if (match != null) {
							return match;
						}
					}
				}
				// if children didn't match we continue checking the current to
				// prevent false positives
				if (!this.containsExcludeToken(agentString)) {
					return this;
				}

			}
			return null;
		}

		/**
		 * Parses user agent string and returns the best match. Returns
		 * OperatingSystem.UNKNOWN if there is no match.
		 * 
		 * @param agentString
		 * @return OperatingSystem
		 */
		public static OperatingSystem parseUserAgentString(String agentString) {
			for (OperatingSystem operatingSystem : OperatingSystem.values()) {
				// only check top level objects
				if (operatingSystem.parent == null) {
					OperatingSystem match = operatingSystem.checkUserAgent(agentString);
					if (match != null) {
						return match; // either current operatingSystem or a
										// child object
					}
				}
			}
			return OperatingSystem.UNKNOWN;
		}

		/**
		 * Returns the enum constant of this type with the specified id. Throws
		 * IllegalArgumentException if the value does not exist.
		 * 
		 * @param id
		 * @return
		 */
		public static OperatingSystem valueOf(short id) {
			for (OperatingSystem operatingSystem : OperatingSystem.values()) {
				if (operatingSystem.getId() == id)
					return operatingSystem;
			}

			// same behavior as standard valueOf(string) method
			throw new IllegalArgumentException("No enum const for id " + id);
		}

	}

	/**
	 * Enum constants classifying the different types of rendering engines which
	 * are being used by browsers.
	 * 
	 * @author harald
	 *
	 */
	public static enum RenderingEngine {

		/**
		 * Trident is the the Microsoft layout engine, mainly used by Internet
		 * Explorer.
		 */
		TRIDENT("Trident"),
		/**
		 * HTML parsing and rendering engine of Microsoft Office Word, used by
		 * some other products of the Office suite instead of Trident.
		 */
		WORD("Microsoft Office Word"),
		/**
		 * Open source and cross platform layout engine, used by Firefox and
		 * many other browsers.
		 */
		GECKO("Gecko"),
		/**
		 * Layout engine based on KHTML, used by Safari, Chrome and some other
		 * browsers.
		 */
		WEBKIT("WebKit"),
		/**
		 * Proprietary layout engine by Opera Software ASA
		 */
		PRESTO("Presto"),
		/**
		 * Original layout engine of the Mozilla browser and related products.
		 * Predecessor of Gecko.
		 */
		MOZILLA("Mozilla"),
		/**
		 * Layout engine of the KDE project
		 */
		KHTML("KHTML"),
		/**
		 * Other or unknown layout engine.
		 */
		OTHER("Other");

		String name;

		private RenderingEngine(String name) {
			this.name = name;
		}

	}

	/**
	 * Container class for user-agent information with operating system and
	 * browser details. Can decode user-agent strings. <br>
	 * <br>
	 * Resources:<br>
	 * <a href="http://www.useragentstring.com">User Agent String.Com</a><br>
	 * <a href="http://www.user-agents.org">List of User-Agents</a><br>
	 * <a href="http://www.zytrax.com/tech/web/browser_ids.htm">Browser ID
	 * (User-Agent) Strings</a><br>
	 * <a href="http://www.zytrax.com/tech/web/mobile_ids.html">Mobile Browser
	 * ID (User-Agent) Strings</a><br>
	 * <a href=
	 * "http://www.joergkrusesweb.de/internet/browser/user-agent.html">Browser-Kennungen</a><br>
	 * <a href="http://deviceatlas.com/devices">Device Atlas - Mobile Device
	 * Intelligence</a><br>
	 * <a href="http://mobileopera.com/reference/ua">Mobile Opera user-agent
	 * strings</a><br>
	 * <a href="http://en.wikipedia.org/wiki/S60_platform">S60 platform</a><br>
	 * <a href=
	 * "http://msdn.microsoft.com/en-us/library/ms537503.aspx">Understanding
	 * User-Agent Strings</a><br>
	 * <a href=
	 * "http://developer.sonyericsson.com/site/global/docstools/browsing/p_browsing.jsp">Sony
	 * Ericsson Web Docs & Tools</a><br>
	 * <a href=
	 * "http://developer.apple.com/internet/safari/faq.html#anchor2">What is the
	 * Safari user-agent string</a><br>
	 * <a href="http://www.pgts.com.au/pgtsj/pgtsj0208c.html">List of User Agent
	 * Strings</a><br>
	 * <a href=
	 * "http://blogs.msdn.com/iemobile/archive/2006/08/03/Detecting_IE_Mobile.aspx">Detecting
	 * Internet Explorer Mobile's User-Agent on the server</a>
	 * 
	 * @author harald
	 *
	 */
	public static class UserAgent {

		private OperatingSystem operatingSystem = OperatingSystem.UNKNOWN;
		private Browser browser = Browser.UNKNOWN;
		private int id;
		private String userAgentString;

		public UserAgent(OperatingSystem operatingSystem, Browser browser) {
			this.operatingSystem = operatingSystem;
			this.browser = browser;
			this.id = ((operatingSystem.getId() << 16) + browser.getId());
		}

		public UserAgent(String userAgentString) {
			Browser browser = Browser.parseUserAgentString(userAgentString);

			OperatingSystem operatingSystem = OperatingSystem.UNKNOWN;

			// BOTs don't have an interesting OS for us
			if (browser != Browser.BOT)
				operatingSystem = OperatingSystem.parseUserAgentString(userAgentString);

			this.operatingSystem = operatingSystem;
			this.browser = browser;
			this.id = ((operatingSystem.getId() << 16) + browser.getId());
			this.userAgentString = userAgentString;
		}

		/**
		 * @param userAgentString
		 * @return UserAgent
		 */
		public static UserAgent parseUserAgentString(String userAgentString) {
			return new UserAgent(userAgentString);
		}

		/**
		 * Detects the detailed version information of the browser. Depends on
		 * the userAgent to be available. Use it only after using
		 * UserAgent(String) or UserAgent.parseUserAgent(String). Returns null
		 * if it can not detect the version information.
		 * 
		 * @return Version
		 */
		public Version getBrowserVersion() {
			return this.browser.getVersion(this.userAgentString);
		}

		/**
		 * @return the system
		 */
		public OperatingSystem getOperatingSystem() {
			return operatingSystem;
		}

		/**
		 * @return the browser
		 */
		public Browser getBrowser() {
			return browser;
		}

		/**
		 * Returns an unique integer value of the operating system & browser
		 * combination
		 * 
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * Combined string representation of both enums
		 */
		public String toString() {
			return this.operatingSystem.toString() + "-" + this.browser.toString();
		}

		/**
		 * Returns UserAgent based on specified unique id
		 * 
		 * @param id
		 * @return
		 */
		public static UserAgent valueOf(int id) {
			OperatingSystem operatingSystem = OperatingSystem.valueOf((short) (id >> 16));
			Browser browser = Browser.valueOf((short) (id & 0x0FFFF));
			return new UserAgent(operatingSystem, browser);
		}

		/**
		 * Returns UserAgent based on combined string representation
		 * 
		 * @param name
		 * @return
		 */
		public static UserAgent valueOf(String name) {
			if (name == null)
				throw new NullPointerException("Name is null");

			String[] elements = name.split("-");

			if (elements.length == 2) {
				OperatingSystem operatingSystem = OperatingSystem.valueOf(elements[0]);
				Browser browser = Browser.valueOf(elements[1]);
				return new UserAgent(operatingSystem, browser);
			}

			throw new IllegalArgumentException("Invalid string for userAgent " + name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((browser == null) ? 0 : browser.hashCode());
			result = prime * result + id;
			result = prime * result + ((operatingSystem == null) ? 0 : operatingSystem.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final UserAgent other = (UserAgent) obj;
			if (browser == null) {
				if (other.browser != null)
					return false;
			} else if (!browser.equals(other.browser))
				return false;
			if (id != other.id)
				return false;
			if (operatingSystem == null) {
				if (other.operatingSystem != null)
					return false;
			} else if (!operatingSystem.equals(other.operatingSystem))
				return false;
			return true;
		}

	}

	/**
	 * Container for general version information. All version information is
	 * stored as String as sometimes version information includes alphabetical
	 * characters.
	 * 
	 * @author harald
	 */
	public static class Version {

		String version;
		String majorVersion;
		String minorVersion;

		public Version(String version, String majorVersion, String minorVersion) {
			super();
			this.version = version;
			this.majorVersion = majorVersion;
			this.minorVersion = minorVersion;
		}

		public String getVersion() {
			return version;
		}

		public String getMajorVersion() {
			return majorVersion;
		}

		public String getMinorVersion() {
			return minorVersion;
		}

		@Override
		public String toString() {
			return version;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((majorVersion == null) ? 0 : majorVersion.hashCode());
			result = prime * result + ((minorVersion == null) ? 0 : minorVersion.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Version other = (Version) obj;
			if (majorVersion == null) {
				if (other.majorVersion != null)
					return false;
			} else if (!majorVersion.equals(other.majorVersion))
				return false;
			if (minorVersion == null) {
				if (other.minorVersion != null)
					return false;
			} else if (!minorVersion.equals(other.minorVersion))
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}

	}

}
