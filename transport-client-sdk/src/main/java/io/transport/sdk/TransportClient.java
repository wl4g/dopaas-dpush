package io.transport.sdk;

import io.transport.sdk.exception.TransportException;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Future;

import com.alibaba.fastjson.JSON;

import io.netty.util.internal.PlatformDependent;
import io.transport.sdk.exception.TransportAuthenticationException;
import io.transport.sdk.exception.TransportInitializeException;
import io.transport.sdk.protocol.message.DeviceInfo.DeviceType;
import io.transport.sdk.protocol.message.Message;
import io.transport.sdk.protocol.message.internal.ConnectMessage;
import io.transport.sdk.protocol.message.internal.DeviceRegistMessage;
import io.transport.sdk.protocol.message.internal.TransportMessage;
import io.transport.sdk.utils.ByteBufs;
import io.transport.sdk.utils.SubmitTaskExecutors;

/**
 * Transport client program.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月19日
 * @since
 */
final public class TransportClient {
	private Configuration configuration;
	private TransportConnector connector;
	private volatile boolean authState = false;

	/**
	 * Building Transport applications
	 * 
	 * @param config
	 *            configuration information
	 * @return
	 * @throws TransportException
	 */
	public TransportClient(Configuration config) throws TransportException {
		if (config == null)
			throw new IllegalArgumentException("'config' is null.");

		this.configuration = config;
		this.configuration.validation();
	}

	/**
	 * Get an instance of Configuration
	 * 
	 * @return
	 */
	public Configuration getConfig() {
		return this.configuration;
	}

	/**
	 * Start and connect to the Transport server
	 * 
	 * @return
	 * @throws TransportException
	 */
	public synchronized TransportClient join() throws TransportException {
		// 1.1 可能是重连，暂停等待reconnectDelay秒，控制重连频率.
		try {
			this.wait(this.configuration.getReconnectDelay() * 1000);
		} catch (InterruptedException e) {
			throw new TransportException(e);
		}

		if (this.connector == null)
			this.connector = new TransportConnector(this);
		// 1.2 Connect channel server.
		this.connector.configure().connect(true); // 连接成功后发起登录
		return this;
	}

	/**
	 * Destroy the Transport instance
	 * 
	 * @throws TransportException
	 */
	public synchronized void destroy() throws TransportException {
		TransportConnector cli = this.getChannel(false);
		if (cli != null) {
			try {
				cli.close();
			} catch (IOException e) {
				throw new TransportException(e);
			}
			this.connector = null;
		}
	}

	/**
	 * Update authentication status
	 * 
	 * @param authState
	 */
	public synchronized void setAuthState(boolean authState) {
		this.authState = authState;
	}

	/**
	 * Unicast sends messages (specified point to point one-to-one)
	 * 
	 * @param toDeviceId
	 *            Connected device ID
	 * @param classifier
	 *            message classifier
	 * @param payload
	 *            Load message
	 * @throws TransportException
	 */
	public Future<Message> unicast(String toDeviceId, String classifier, String payload) throws TransportException {
		if (ByteBufs.isEmpty(toDeviceId) || ByteBufs.isEmpty(payload))
			throw new TransportException("'toDeviceId/payload' is not allowed to be empty.");

		String fromDeviceId = this.configuration.getDeviceId();
		TransportMessage msg = new TransportMessage(fromDeviceId, toDeviceId, payload);
		msg.setClassifier(classifier);
		return this.execute(msg, true);
	}

	/**
	 * Multicast sends messages (specified point to many one-to-many)
	 * 
	 * @param toGroupId
	 *            Connected device group ID
	 * @param classifier
	 *            message classifier
	 * @param payload
	 *            Load message
	 * @return
	 * @throws TransportException
	 */
	public Future<Message> groupcast(String toGroupId, String classifier, String payload) throws TransportException {
		if (ByteBufs.isEmpty(toGroupId) || ByteBufs.isEmpty(payload))
			throw new TransportException("'toGroupId/payload' is not allowed to be empty.");

		String fromDeviceId = this.configuration.getDeviceId();
		TransportMessage msg = new TransportMessage(fromDeviceId, null, toGroupId, payload);
		msg.setClassifier(classifier);
		return this.execute(msg, true);
	}

	/**
	 * For registration of WebSocket client connection authentication.
	 * 
	 * @param expiredSec
	 *            Expiration time (s)
	 * @param deviceIdTokens
	 *            DeviceId list
	 * @return
	 * @throws TransportException
	 */
	public Future<Message> registered(int expiredSec, String... deviceIdTokens) throws TransportException {
		if (!(deviceIdTokens != null && deviceIdTokens.length != 0))
			throw new TransportException("The 'deviceIdTokens' is not allowed to be empty.");
		if (expiredSec <= 0)
			throw new TransportException("'expiredSec' is invalid.");

		DeviceRegistMessage msg = new DeviceRegistMessage();
		msg.setAppId(this.configuration.getAppId());
		msg.setExpired(expiredSec);
		msg.setClientDeviceIds(Arrays.asList(deviceIdTokens));
		return this.execute(msg, true);
	}

	/**
	 * To get channel, the spin lock will be used.
	 * 
	 * @param loggedin
	 *            Screening for authenticated connections
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized TransportConnector getChannel(boolean loggedin) {
		if (this.connector == null)
			throw new TransportInitializeException("Uninitialized client.");

		if (loggedin) {
			int max = this.configuration.getLoginTimeout() * 100; // 60*100=6000
			int c = 0;
			while (c <= max) {
				++c;
				try {
					this.wait(10L);
				} catch (InterruptedException e) {
					throw new TransportException(e);
				}
				if (!this.isAuthState()) { // Unauthenticated
					if (c == max) { // 6000*10=60,000
						// 1.1 外抛未认证异常.
						throw new TransportAuthenticationException("Authentication timeout.");
						// 这种处理流程，会导致运行一段时间后线程会堵死？？？
						//
						// // 1.2 自动发起重认证流程
						// this.client.channel().close(); // 关闭channel
						// this.join(); // 重连、认证
					}
				} else
					break;
			}
		}
		return this.connector;
	}

	/**
	 * Getting the status of the current instance authentication
	 * 
	 * @return
	 */
	private boolean isAuthState() {
		return this.authState;
	}

	/**
	 * Login authentication connection
	 * 
	 * @return
	 * @throws TransportException
	 */
	synchronized Future<Message> login() throws TransportException {
		ConnectMessage msg = new ConnectMessage(this.configuration.getAppId(), this.configuration.getAppSecret());
		msg.getDeviceInfo().setGroupId(this.configuration.getGroupId());
		msg.getDeviceInfo().setDeviceId(this.configuration.getDeviceId());
		if (PlatformDependent.isAndroid())
			msg.getDeviceInfo().setDeviceType(DeviceType.ANDROID.name());
		else
			msg.getDeviceInfo().setDeviceType(DeviceType.PROVIDER.name());

		return this.execute(msg, false);
	}

	/**
	 * General method of execution
	 * 
	 * @param msg
	 *            Logical message
	 * @param loggedin
	 *            Screening for authenticated connections
	 * @return
	 * @throws TransportException
	 */
	private Future<Message> execute(final Message msg, final boolean loggedin) throws TransportException {
		return SubmitTaskExecutors.getLimitExecutor().submit(new Runnable() {

			@Override
			public void run() {
				try {
					configuration.getLoggerImpl().info("Transport sent: " + msg);
					getChannel(loggedin).channel().writeAndFlush(msg);
				} catch (Exception e) {
					configuration.getLoggerImpl().error("Write failed. " + JSON.toJSONString(msg), e);
				}
			}
		}, msg);
	}
}
