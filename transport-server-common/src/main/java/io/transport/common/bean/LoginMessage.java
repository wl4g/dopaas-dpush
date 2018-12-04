package io.transport.common.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Login message.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月7日
 * @since
 */
public class LoginMessage extends Message {
	private static final long serialVersionUID = -6660013588456173675L;

	private AuthenticationInfo authInfo = new AuthenticationInfo();

	public LoginMessage() {
		super();
	}

	public AuthenticationInfo getAuthInfo() {
		return authInfo;
	}

	public void setAuthInfo(AuthenticationInfo authInfo) {
		this.authInfo = authInfo;
	}

	/**
	 * Authentication info
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年5月24日
	 * @since
	 */
	public static class AuthenticationInfo {

		@JSONField(name = "token")
		private String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

	}

}
