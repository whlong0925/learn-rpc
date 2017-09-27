package com.xm.rpc.registry;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;


@ConfigurationProperties("zk.server")
public class ZKServiceDiscovery {
	private static Logger logger = LoggerFactory.getLogger(ZKServiceDiscovery.class);
	private String address;
	private CuratorFramework client;
	private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);

	private CuratorFramework getInstance(){
		if (this.client==null) {
			try {
				if (INSTANCE_INIT_LOCK.tryLock(2, TimeUnit.SECONDS)) {
					try {
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
				        this.client = CuratorFrameworkFactory.builder()
				                .connectString(this.address)
				                .sessionTimeoutMs(5000)
				                .connectionTimeoutMs(5000)
				                .retryPolicy(retryPolicy)
				                .build();
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

	public String discovery(String serviceName) throws Exception{
		List<String> servers = getInstance().getChildren().forPath("/xmrpc-service/"+serviceName);
		String data = null;
		if(!CollectionUtils.isEmpty(servers)){
			String key = servers.get(ThreadLocalRandom.current().nextInt(servers.size()));
			data = new String(getInstance().getData().forPath("/xmrpc-service/"+serviceName+"/"+key),StandardCharsets.UTF_8);
		}else{
			logger.error("zk discovery is null");
		}
		return data;
	}


	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	
}