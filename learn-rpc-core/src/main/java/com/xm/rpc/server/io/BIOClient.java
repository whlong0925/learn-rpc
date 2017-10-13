package com.xm.rpc.server.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;
import com.xm.utils.JacksonSerializer;
public class BIOClient {
	//目前是调用一次连接一次 后续可以把连接缓存起来
	public static RpcResponse send(RpcRequest request,String serverAddress) throws IOException{
		Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
		try {
			String[] ipport = serverAddress.split(":");
			socket = new Socket(ipport[0],Integer.parseInt(ipport[1]));
			out = socket.getOutputStream();
			out.write(JacksonSerializer.serialize(request));
			out.flush();
           //等待服务端返回数据
            in = socket.getInputStream();
			int count = 0;//in.available();  
			while (count == 0) {  
			    count = in.available();  
			}
			byte[] buffer = new byte[count];
			in.read(buffer);
			if(buffer.length > 0){
				RpcResponse rpcResponse = (RpcResponse)JacksonSerializer.deserialize(buffer, RpcResponse.class);
				return rpcResponse;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (in!=null) {
				in.close();
			}
			if (out!=null) {
				out.close();
			}
			if (socket!=null) { 
				socket.close();
			}
		}
        return null;
	}
}
