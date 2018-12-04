package io.transport.data.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.TableCallback;

import io.transport.data.exception.DeployModeException;
import io.transport.mq.kafkaclient.config.KafkaConfiguration;

/**
 * HBase operation Abstract DAO.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年5月2日
 * @since
 */
@SuppressWarnings("deprecation")
public abstract class AbstractDao {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	protected KafkaConfiguration kafkaConf;
	/**
	 * When kafka consumer is not started, the HbaseTemplate object will not be
	 * created.
	 */
	@Autowired(required = false)
	protected HbaseTemplate hbaseTemplate;

	/**
	 * Getting the hbaseTemplate object.
	 * 
	 * @return
	 */
	protected HbaseTemplate getHbaseTemplate() {
		if (!this.kafkaConf.getConsumerConf().isStartup())
			throw new DeployModeException(
					"When Kafka consumer is not started, the HbaseTemplate object will not be created. In this case, HbaseTemplate is also not allowed to be used.");
		return this.hbaseTemplate;
	}

	/**
	 * Execute batch put submission.
	 * 
	 * @param tableName
	 * @param puts
	 * @return
	 */
	protected <T> T batchPut(String tableName, List<Put> puts) {
		return this.getHbaseTemplate().execute(tableName, new TableCallback<T>() {
			@Override
			public T doInTable(HTableInterface table) throws Throwable {
				table.put(puts);
				return null;
			}
		});
	}

}
