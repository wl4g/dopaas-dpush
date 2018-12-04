package io.transport.data.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import io.transport.common.bean.PushMessageBean;
import io.transport.data.config.HTableType;

/**
 * Push message DAO <br/>
 * Reference(spring-data-hadoop的bug)：http://www.th7.cn/db/mysql/201707/245164.shtml
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月13日
 * @since
 */
@Repository
public class PushMessageDao extends AbstractDao {

	/**
	 * execution updates or additions.
	 * 
	 * @param bean
	 * @return
	 */
	public void upsert(PushMessageBean bean) {
		this.upsert(Arrays.asList(new PushMessageBean[] { bean }));
	}

	/**
	 * Batch execution updates or additions.
	 * 
	 * @param rowKey
	 * @param statusAck
	 * @param timestampSent
	 * @param timestampRec
	 * @param payload
	 */
	public void upsert(List<PushMessageBean> beans) {
		String tableName = HTableType.T_PUSH_MSG.getTableName();
		// Assemble put batch list.
		List<Put> puts = new ArrayList<>();
		for (PushMessageBean bean : beans) {
			// Check required parameters.
			bean.validation();
			puts.add(this.wrapperPuts(bean));
		}

		// Execute batch put submission.
		super.batchPut(tableName, puts);
	}

	/**
	 * Assemble put object.
	 * 
	 * @param rowKey
	 * @param statusAck
	 * @param timestampSent
	 * @param timestampRec
	 * @param payload
	 * @return
	 */
	private Put wrapperPuts(PushMessageBean bean) {
		// Create Put.
		Put put = new Put(bean.getRowKey());

		// Push message type.
		if (bean.getClassifier() != null)
			put.addColumn(HTableType.FAMILY_INFO, Bytes.toBytes("classifier"),
					Bytes.toBytes(String.valueOf(bean.getClassifier())));

		// Source device type.
		if (bean.getSrcDevice() != null)
			put.addColumn(HTableType.FAMILY_INFO, Bytes.toBytes("srcDevice"),
					Bytes.toBytes(String.valueOf(bean.getSrcDevice())));

		// Dest device type.
		if (bean.getDstDevice() != null)
			put.addColumn(HTableType.FAMILY_INFO, Bytes.toBytes("dstDevice"),
					Bytes.toBytes(String.valueOf(bean.getDstDevice())));

		// Load message.
		if (bean.getPayload() != null)
			put.addColumn(HTableType.FAMILY_INFO, Bytes.toBytes("payload"), Bytes.toBytes(bean.getPayload()));

		// Receive the ACK packet time stamp.
		if (bean.getRecTime() != null)
			put.addColumn(HTableType.FAMILY_INFO, Bytes.toBytes("recDate"),
					Bytes.toBytes(String.valueOf(bean.getRecDateFormat())));

		return put;
	}

	/**
	 * Getting rowKey based on msgId matching.
	 * 
	 * @param msgId
	 * @return
	 */
	public String getRowByPrefixMsgId(String msgId) {
		String tableName = HTableType.T_PUSH_MSG.getTableName();

		Scan scan = new Scan();
		Filter filter = new RowFilter(CompareOp.EQUAL, new BinaryPrefixComparator(Bytes.toBytes(msgId)));
		scan.setFilter(filter);
		List<String> list = this.getHbaseTemplate().find(tableName, scan, new RowMapper<String>() {
			@Override
			public String mapRow(Result result, int rowNum) throws Exception {
				return Bytes.toString(result.getRow());
			}
		});
		return (list != null && !list.isEmpty()) ? list.get(0) : null;
	}

}