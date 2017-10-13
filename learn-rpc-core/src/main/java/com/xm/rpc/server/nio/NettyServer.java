package com.xm.rpc.server.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
	private int port;
	
	public NettyServer(int port) {
		this.port = port;
	}

	public void start() throws Exception {
		new Thread(new Runnable() {
			@Override
			public void run() {
				EventLoopGroup bossGroup = new NioEventLoopGroup();
				EventLoopGroup workerGroup = new NioEventLoopGroup();
				try {
					ServerBootstrap bootstrap = new ServerBootstrap();
					bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
							.childHandler(new ChannelInitializer<SocketChannel>() {
								@Override
								public void initChannel(SocketChannel channel) throws Exception {
									channel.pipeline()
											.addLast(new RpcDecoder(RpcRequest.class))
											.addLast(new RpcEncoder(RpcResponse.class))
											.addLast(new NettyServerHandler());
								}
							})
							.option(ChannelOption.SO_TIMEOUT, 100)
							.option(ChannelOption.SO_BACKLOG, 128)
							.option(ChannelOption.TCP_NODELAY, true)
							.option(ChannelOption.SO_REUSEADDR, true)
							.childOption(ChannelOption.SO_KEEPALIVE, true);
					ChannelFuture future = bootstrap.bind(port).sync();
					logger.info("netty rpc server start success, port={}", port);
					future.channel().closeFuture().sync().channel();
				} catch (InterruptedException e) {
					logger.error("", e);
				} finally {
					workerGroup.shutdownGracefully();
					bossGroup.shutdownGracefully();
				}
			}
		}).start();

	}
}