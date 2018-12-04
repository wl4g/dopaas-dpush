package io.transport.persistent;

import java.util.List;

import io.transport.common.bean.PushMessageBean;
import io.transport.common.bean.PushMessageBean.RowKey;

/**
 * Persistent data Service
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月12日
 * @since
 */
public interface PersistentService {

	/**
	 * Persistent archive push message
	 * 
	 * @param beans
	 *            TransportMessage object.
	 * @return If it is successful, it will return to true, otherwise it will be
	 *         false.
	 */
	void batchSavePushMessage(List<PushMessageBean> beans);

	/**
	 * Update archive push message received ack.
	 * 
	 * @param msg
	 *            msgId.
	 * @return If it is successful, it will return to true, otherwise it will be
	 *         false.
	 */
	void batchUpdateRecAckMessage(List<Integer> msgIds);

	/**
	 * Getting rowKey based on msgId matching.
	 * 
	 * @param msgId
	 * @return
	 */
	String findRowkey(int msgId);

	/**
	 * Get temporary rowKey from cache(Redis)
	 * 
	 * @param msgId
	 * @param clean
	 * @return
	 */
	RowKey getTmpRowkey(int msgId, boolean clean);

	/**
	 * Save temporary rowKey to cache(Redis).
	 * 
	 * @param msgId
	 * @param fromDeviceId
	 * @param toDeviceId
	 * @param toGroupId
	 */
	void saveTmpRowkey(int msgId, String fromDeviceId, String toDeviceId, String toGroupId);

}
