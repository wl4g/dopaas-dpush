package io.transport.common.bean;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;

import io.transport.common.utils.exception.TransportException;

/**
 * Push message bean.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月16日
 * @since
 */
public class PushMessageBean implements PersistenceBean {
	private static final long serialVersionUID = -2445296301568918363L;

	private int msgId; // Transport message ID.
	private RowKey rowKey; // HBASE persistence rowKey

	private String classifier; // Push message type by application custom
	private String srcDevice; // Message source device
	private String dstDevice; // Message destination device
	private String payload;
	private Long recTime;// Receive the timestamp of the confirmation

	public PushMessageBean() {
		super();
	}

	public PushMessageBean(RowKey rowKey, Long recTime) {
		super();
		this.rowKey = rowKey;
		this.recTime = recTime;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	@Override
	public byte[] getRowKey() {
		this.rowKey.validation();
		return this.rowKey.asText().getBytes(Charsets.UTF_8);
	}

	public void setRowKey(RowKey rowKey) {
		this.rowKey = rowKey;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getRecDateFormat() {
		return DateFormatUtils.format(getRecTime(), "yyyyMMddHHmmssSSS", Locale.CANADA);
	}

	public Long getRecTime() {
		return recTime;
	}

	public void setRecTime(Long recTime) {
		this.recTime = recTime;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getSrcDevice() {
		return srcDevice;
	}

	public void setSrcDevice(String srcDevice) {
		this.srcDevice = srcDevice;
	}

	public String getDstDevice() {
		return dstDevice;
	}

	public void setDstDevice(String dstDevice) {
		this.dstDevice = dstDevice;
	}

	@Override
	public void validation() {
		if (this.rowKey == null)
			throw new TransportException("rowkey not allowed is null.");

		this.rowKey.validation();
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	/**
	 * Hbase table rowkey wrapper.
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0 2018年4月21日
	 * @since
	 */
	public static class RowKey implements PersistenceBean {
		final private static long serialVersionUID = 4885301963566655284L;

		private String appId;
		private String fromDeviceId;
		private String toDeviceId;
		private String toGroupId;
		private Long sentTime; // Push time stamp

		public RowKey() {
			super();
		}

		public RowKey(String appId, String fromDeviceId, String toDeviceId, String toGroupId, Long sentTime) {
			super();
			this.appId = appId;
			this.fromDeviceId = fromDeviceId;
			this.toDeviceId = toDeviceId;
			this.toGroupId = toGroupId;
			this.sentTime = sentTime;
		}

		public String getAppId() {
			return appId;
		}

		public RowKey setAppId(String appId) {
			this.appId = appId;
			return this;
		}

		public String getFromDeviceId() {
			return fromDeviceId;
		}

		public RowKey setFromDeviceId(String fromDeviceId) {
			this.fromDeviceId = fromDeviceId;
			return this;
		}

		public String getToDeviceId() {
			return toDeviceId;
		}

		public RowKey setToDeviceId(String toDeviceId) {
			this.toDeviceId = toDeviceId;
			return this;
		}

		public String getToGroupId() {
			return toGroupId;
		}

		public RowKey setToGroupId(String toGroupId) {
			this.toGroupId = toGroupId;
			return this;
		}

		public Long getSentTime() {
			return sentTime;
		}

		public String getSentDateFormat() {
			return DateFormatUtils.format(getSentTime(), "yyyyMMddHHmmssSSS", Locale.CANADA);
		}

		public RowKey setSentTime(Long sentTime) {
			this.sentTime = sentTime;
			return this;
		}

		public byte[] toBytes() {
			if (asText().length() <= 0)
				return null;
			return asText().getBytes(Charsets.UTF_8);
		}

		public String asText() {
			StringBuffer buf = new StringBuffer();
			buf.append(getAppId()).append(",").append(getFromDeviceId()).append(",").append(getToDeviceId()).append(",")
					.append(getToGroupId()).append(",").append(getSentDateFormat()).toString();
			return buf.toString();
		}

		@Override
		public String toString() {
			return JSON.toJSONString(this);
		}

		@JSONField(serialize = false, deserialize = false)
		@Override
		public byte[] getRowKey() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void validation() {
			if (this.getSentTime() == null || StringUtils.isBlank(this.getAppId())
					|| StringUtils.isBlank(this.getFromDeviceId())
					|| (StringUtils.isBlank(this.getToDeviceId()) && StringUtils.isBlank(this.getToGroupId()))) {
				throw new TransportException("appId/fromDeviceId/toDeviceId/toGroupId not allowed is null.");
			}
		}

	}

}
