package com.xm.rpc.server.nio;

import java.util.concurrent.ConcurrentHashMap;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClientChannelPool {
	public static ConcurrentHashMap<String, Channel> clientPoolMap = new ConcurrentHashMap<String, Channel>();
	public static Channel getChannel(String serverAddress){
		Channel channel = null;
		if(clientPoolMap.containsKey(serverAddress)){
			channel = clientPoolMap.get(serverAddress);
		}else{
			EventLoopGroup group = new NioEventLoopGroup();
	        try {
	            Bootstrap b = new Bootstrap();
	            b.group(group)
	             .channel(NioSocketChannel.class)
	             .option(ChannelOption.TCP_NODELAY, true)
	             .handler(new ChannelInitializer<SocketChannel>() {
	                 @Override
	                 public void initChannel(SocketChannel channel) throws Exception {
	                	 channel.pipeline()
							.addLast(new RpcEncoder(RpcRequest.class))
							.addLast(new RpcDecoder(RpcResponse.class))
							.addLast(new NettyClientHandler());
	                 }
	             });
	            String[] ipport = serverAddress.split(":");
	            String host = ipport[0];
	            int port = Integer.parseInt(ipport[1]);
	            // Start the client.
	            ChannelFuture f = b.connect(host, port).sync();
	            channel = f.channel();
	            clientPoolMap.put(serverAddress, channel);
	        } catch(Exception e){
	        	e.printStackTrace();
	        }
		}
		return channel;
	}
}
