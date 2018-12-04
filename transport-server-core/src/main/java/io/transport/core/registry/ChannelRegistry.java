package io.transport.core.registry;

import java.util.Set;

import io.netty.channel.socket.SocketChannel;

/**
 * Client (terminal) socket connection channel registry
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2017年10月12日
 * @since
 */
public abstract interface ChannelRegistry {

	/**
	 * 依据groupId获取本地节点的所有client列表.
	 * 
	 * @param groupId
	 *            组ID(一般为userId)
	 * @return
	 */
	Set<Client> getLocalClients(String groupId);

	/**
	 * 依据deviceId获取本地节点client.
	 * 
	 * @param deviceId
	 *            等同于clientId
	 * @return
	 */
	Client getLocalClient(String deviceId);

	/**
	 * 更新Client.channel
	 * 
	 * @param clientId
	 * @param channel
	 */
	void setLocalChannel(String clientId, SocketChannel channel);

	/**
	 * 校验clientId是否存在.
	 * 
	 * @param clientId
	 * @return
	 */
	boolean localContains(String clientId);

	/**
	 * 当前本地节点的连接数大小
	 * 
	 * @param filter
	 *            local node.
	 * @return
	 */
	int localSize();

	/**
	 * 获取deviceId在集群中client信息，请注意：socketChannel字段将为空（因为可能socketChannel可能不在当前本地节点上）.
	 * 
	 * @param deviceId
	 *            等同于clientId
	 * @return
	 */
	Client getClient(String deviceId);

	/**
	 * 依据appId获取本地节点的所有groupId列表.
	 * 
	 * @param appId
	 *            appId
	 * @return
	 */
	Set<String> getAllGroupIds(String appId);

	/**
	 * 依据groupId获取本地节点的所有deviceId列表.
	 * 
	 * @param groupId
	 *            组ID(一般为userId)
	 * @return
	 */
	Set<String> getAllDeviceIds(String groupId);

	/**
	 * 更新保存Client
	 * 
	 * @param client
	 * @return
	 */
	boolean addRegistry(String appId, Client client);

	/**
	 * 清除Client
	 * 
	 * @param clientId
	 * @return
	 */
	boolean removeClient(String clientId);

}
