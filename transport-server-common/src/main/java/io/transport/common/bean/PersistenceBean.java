package io.transport.common.bean;

import java.io.Serializable;

/**
 * Persistence interface bean.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年5月11日
 * @since
 */
public abstract interface PersistenceBean extends Serializable {

	byte[] getRowKey();

	void validation();

}
