package com.xm.rpc.registry;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zk.server")
public class ZKServiceRegistry {
	private String address;
	private CuratorFramework client;
	private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);

	private CuratorFramework getInstance(){
		if (this.client==null) {
			try {
				if (INSTANCE_INIT_LOCK.tryLock(2, TimeUnit.SECONDS)) {
					try {
						this.client = CuratorFrameworkFactory.newClient(this.address, 15 * 1000, 5000,new ExponentialBackoffRetry(1000, 3));
						this.client.start();
					} finally {
						INSTANCE_INIT_LOCK.unlock();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (this.client == null) {
			throw new NullPointerException(">>>>>>>>>>> learn-rpc, zookeeper connect fail.");
		}
		return this.client;
	}
	

	/**
	 * 注册格式：/xmrpc-service/"+serviceName + "/"  节点值:serverAddress
	 * @param port
	 * @param serviceList
	 */
	public void register(int port,Set<String> serviceList) {
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			String serverAddress = hostName + ":" + port;
			for(String serviceName : serviceList){
				getInstance().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/xmrpc-service/" +serviceName + "/" + serverAddress, serverAddress.getBytes(StandardCharsets.UTF_8));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getAddress() {
		return this.address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}
