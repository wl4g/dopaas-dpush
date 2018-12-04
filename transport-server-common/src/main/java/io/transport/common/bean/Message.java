package io.transport.common.bean;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 采集开放平台接口输出基础参数Bean
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年3月9日
 * @since
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 2647155468624590650L;

	@JSONField(ordinal = 1)
	private String code = "0";
	@JSONField(ordinal = 2)
	private String msg = "ok";

	public Message() {
		super();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
