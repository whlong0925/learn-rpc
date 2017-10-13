package com.xm.rpc.server.nio;

import com.xm.utils.JacksonSerializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<Object> {
 
    private Class<?> genericClass;
 
    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }
 
    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (this.genericClass.isInstance(in)) {
            byte[] data = JacksonSerializer.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}