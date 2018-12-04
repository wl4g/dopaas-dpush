package io.transport.fn;

/**
 * Caller interface
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年4月11日
 * @since
 */
public abstract interface Callback<P, R> {

	R call(P p);

}
