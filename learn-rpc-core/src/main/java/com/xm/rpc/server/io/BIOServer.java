package com.xm.rpc.server.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;
import com.xm.rpc.server.RpcServiceProvider;
import com.xm.utils.JacksonSerializer;

public class BIOServer {
	private final int threadPoolSize = 10;
	private ExecutorService serverHandlerThreads = Executors.newFixedThreadPool(this.threadPoolSize);
	private volatile static boolean running = false;
	private int port;
	
	public BIOServer(int port) {
		this.port = port;
	}

	public void startServer(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {

				try {
					ServerSocket serverSocket = new ServerSocket(port);
					running = true;
					while (running) {
						final Socket socket = serverSocket.accept();
						serverHandlerThreads.execute(new Runnable() {
							public void run() {
								InputStream in = null;
								OutputStream out = null;
								try {
									in = socket.getInputStream();
									int count = 0;
									while (count == 0) {
										count = in.available();
									}
									byte[] temp = new byte[count];
									in.read(temp);
									if (temp.length > 0) {
										RpcRequest request = (RpcRequest)JacksonSerializer.deserialize(temp,RpcRequest.class);
										RpcResponse response = RpcServiceProvider.invokeServerService(request, null);
										out = socket.getOutputStream();
										out.write(JacksonSerializer.serialize(response));
										out.flush();
									}
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									try {
										if (in != null) {
											in.close();
										}
										if (out != null) {
											out.close();
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		
	}
}
