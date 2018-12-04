package io.transport.data.config;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Hbase tables定义
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2018年4月21日
 * @since
 */
public enum HTableType {

	/**
	 * 推送消息记录PushMessageBean表
	 */
	T_PUSH_MSG("tb_push_message");

	final public static String T_SCHEM_DEFAULT = "transporter";
	final public static byte[] FAMILY_INFO = Bytes.toBytes("info");
	private String tableSchem = T_SCHEM_DEFAULT;
	private String tableName;

	private HTableType(String tableName) {
		this.tableName = tableName;
	}

	private HTableType(String tableSchem, String tableName) {
		this.tableSchem = tableSchem;
		this.tableName = tableName;
	}

	public String getTableSchem() {
		return tableSchem;
	}

	public String getTableName() {
		return this.getTableSchem() + "." + tableName;
	}

}
