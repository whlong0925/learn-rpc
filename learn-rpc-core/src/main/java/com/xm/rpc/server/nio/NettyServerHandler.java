package com.xm.rpc.server.nio;
import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;
import com.xm.rpc.server.RpcServiceProvider;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理服务端 channel.
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { 
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
		System.out.println("++++++++++++++server收到请求++++++++++++++++"+request);
		// invoke
        RpcResponse response = RpcServiceProvider.invokeServerService(request, null);
        ctx.writeAndFlush(response);
		
	}
}