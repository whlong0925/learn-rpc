package com.xm.rpc.server.nio;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NettyClientProxy {
	private Channel channel;
	private  RpcRequest request;
	public static ConcurrentHashMap<String, RPCFuture> rpcFutureResult = new ConcurrentHashMap<>();
	public NettyClientProxy(RpcRequest request,String serverAddress) throws Exception{
		this.request = request;
		this.channel = NettyClientChannelPool.getChannel(serverAddress);
	}

	public RpcResponse send() throws Exception {
		 final CountDownLatch latch = new CountDownLatch(1);
	        RPCFuture rpcFuture = new RPCFuture(this.request);
	        rpcFutureResult.put(this.request.getRequestId(), rpcFuture);
	        this.channel.writeAndFlush(this.request).addListener(new ChannelFutureListener() {
	            @Override
	            public void operationComplete(ChannelFuture future) {
	                latch.countDown();
	            }
	        });
	        try {
	            latch.await();
	        } catch (InterruptedException e) {
	            e.getMessage();
	        }
	        return rpcFuture.get(2,TimeUnit.SECONDS);
    }
	
}
