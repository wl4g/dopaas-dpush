package io.transport.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import io.transport.common.bean.PushMessageBean;
import io.transport.common.bean.PushMessageBean.RowKey;
import io.transport.common.cache.JedisService;
import io.transport.common.utils.exception.TransportException;
import io.transport.core.config.Configuration;
import io.transport.core.registry.ChannelRegistry;
import io.transport.core.registry.Client;
import io.transport.data.dao.PushMessageDao;
import io.transport.persistent.PersistentService;

/**
 * 推送消息持久化Service
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月13日
 * @since
 */
@Service
public class PushMessagePersistentService implements PersistentService {
	final private static Logger logger = LoggerFactory.getLogger(PushMessagePersistentService.class);

	@Resource
	private Configuration conf;
	@Resource
	private JedisService jedisService;
	@Resource
	private PushMessageDao pushMessageDao;
	@Resource
	private ChannelRegistry registry;

	@Override
	public void batchSavePushMessage(List<PushMessageBean> beans) {
		if (beans == null || beans.isEmpty())
			return;
		// 1.1 Batch save or update message.
		try {
			this.pushMessageDao.upsert(beans);
		} catch (Exception e) {
			logger.error("Persistent error.", e);
			throw e;
		}
	}

	@Override
	public void batchUpdateRecAckMessage(List<Integer> msgIds) {
		if (msgIds == null || msgIds.isEmpty())
			return;

		// 1.1 Parameters set.
		List<PushMessageBean> beans = new ArrayList<>();
		for (Integer msgId : msgIds) {
			if (logger.isInfoEnabled())
				logger.info("Update ack. msgId={}", msgId);

			// 1.1 Get rowKey by msgId.
			RowKey rowkey = this.getTmpRowkey(msgId, true);
			if (rowkey != null) {
				// 1.2 Setting up the update message rowKey.
				beans.add(new PushMessageBean(rowkey, System.currentTimeMillis()));
			} else
				throw new TransportException("Get rowkey from cache(redis) by msgId is null.");
		}

		// 1.2 Batch save or update message ack.
		try {
			this.pushMessageDao.upsert(beans);
		} catch (Exception e) {
			logger.error("Update ack error.", e);
		}
	}

	@Override
	public String findRowkey(int msgId) {
		return this.pushMessageDao.getRowByPrefixMsgId(String.valueOf(msgId)).toString();
	}

	@Override
	public RowKey getTmpRowkey(int msgId, boolean clean) {
		String rkk = this.conf.getCtlMsgIdRowKeyPkey() + msgId;
		RowKey rowKey = JSON.parseObject(this.jedisService.get(rkk), RowKey.class);
		if (clean) {
			// Delete rowKey by msgId.
			this.jedisService.del(rkk);
		}
		return rowKey;
	}

	@Override
	public void saveTmpRowkey(int msgId, String fromDeviceId, String toDeviceId, String toGroupId) {
		// 1.1 Get appId info.
		String appId = null;
		Client src = this.registry.getClient(fromDeviceId);
		if (src != null)
			appId = src.getAppId();
		else
			throw new TransportException("fromDeviceId=" + fromDeviceId + " corresponding client is null.");

		// 1.2 Check whether or not the same appId subordinate equipment.
		this.checkSameAppId(appId, fromDeviceId, toDeviceId, toGroupId);

		// Get the key of the rowKey cache.
		RowKey rowkey = new RowKey().setAppId(String.valueOf(appId)).setFromDeviceId(fromDeviceId)
				.setToDeviceId(toDeviceId).setToGroupId(toGroupId).setSentTime(System.currentTimeMillis());

		// Set msgId -> rowKey mapping.
		String rkk = this.conf.getCtlMsgIdRowKeyPkey() + msgId;
		this.jedisService.set(rkk, rowkey.toString(), conf.getCtlMsgIdRowKeyExpire());
	}

	/**
	 * Check whether or not the same appId subordinate equipment.
	 * 
	 * @param fromAppId
	 * @param fromDeviceId
	 * @param toDeviceId
	 * @param toGroupId
	 */
	private void checkSameAppId(String fromAppId, String fromDeviceId, String toDeviceId, String toGroupId) {
		// 1.1 Check toDeviceId appId.
		if (!StringUtils.isEmpty(toDeviceId)) {
			Client dst = this.registry.getClient(toDeviceId);
			if (dst != null && !StringUtils.equals(dst.getAppId(), fromAppId))
				throw new TransportException("Non-appId direct push is not allowed. fromDeviceId=" + fromDeviceId
						+ ", fromAppId=" + fromAppId + ", toDeviceId=" + toDeviceId + ", toAppId=" + dst.getAppId());
		}
		// 1.1 Check toGroupId appId.
		if (!StringUtils.isEmpty(toGroupId)) {
			Set<String> deviceIds = this.registry.getAllDeviceIds(toGroupId);
			if (deviceIds != null) {
				for (String deviceId : deviceIds) {
					if (!StringUtils.isEmpty(deviceId)) {
						Client dst = this.registry.getClient(deviceId);
						if (dst != null && !StringUtils.equals(dst.getAppId(), fromAppId))
							throw new TransportException("Non-appId direct push is not allowed. fromDeviceId="
									+ fromDeviceId + ", fromAppId=" + fromAppId + ", toDeviceId=" + toDeviceId
									+ ", toAppId=" + dst.getAppId());
					}
				}
			}
		}
	}

}
