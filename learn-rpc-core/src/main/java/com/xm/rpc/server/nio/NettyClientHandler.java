package com.xm.rpc.server.nio;

import com.xm.rpc.RpcResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		String requestId = response.getRequestId();
		RPCFuture future = NettyClientProxy.rpcFutureResult.get(requestId);
		System.out.println("-----------NettyClientHandler---------------"+requestId);
		if (future != null) {
			NettyClientProxy.rpcFutureResult.remove(requestId);
			future.setRpcResponse(response);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}