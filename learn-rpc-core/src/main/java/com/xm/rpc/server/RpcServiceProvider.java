package com.xm.rpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;
import com.xm.rpc.registry.ZKServiceRegistry;
import com.xm.utils.JacksonSerializer;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
@ConfigurationProperties(prefix = "rpc.server")
public class RpcServiceProvider implements ApplicationContextAware, InitializingBean {
	public static final Logger logger = LoggerFactory.getLogger(RpcServiceProvider.class);
	private static Map<String, Object> rpcServiceMap = new HashMap<String, Object>();
	private volatile static boolean running = false;
	private ExecutorService serverHandlerThreads;
	private final int threadPoolSize = 10;
	private int port = 9090;
	private static ServerSocket serverSocket;
	private ApplicationContext applicationContext;
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
		if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
			for (Object serviceBean : serviceBeanMap.values()) {
				String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getSimpleName();
				logger.info("interfaceName is {},servicebean is {}",interfaceName,serviceBean);
				rpcServiceMap.put(interfaceName, serviceBean);
			}
		}
	}

	public void afterPropertiesSet() throws Exception {
		this.serverHandlerThreads = Executors.newFixedThreadPool(this.threadPoolSize);
		ZKServiceRegistry zkServiceRegistry = this.applicationContext.getBean(ZKServiceRegistry.class);
		//注册zookeeper
		zkServiceRegistry.register(this.port, rpcServiceMap.keySet());
		//启动服务
		startServer();
	}

	@SuppressWarnings("resource")
	public void startServer() {
		try {
			serverSocket = new ServerSocket(this.port);
			running = true;
			while (running) {
				final Socket socket = serverSocket.accept();
				this.serverHandlerThreads.execute(new Runnable() {
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
								RpcResponse response = invokeServerService(request, null);
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

	public static RpcResponse invokeServerService(RpcRequest request, Object serviceBean) {
		if (serviceBean == null) {
			serviceBean = rpcServiceMap.get(request.getClassName());
		}
		if (serviceBean == null) {
			logger.error("request class name is {}",request.getClassName());
			throw new RuntimeException("no service");
		}
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());

		try {
			Class<?> serviceClass = serviceBean.getClass();
			String methodName = request.getMethodName();
			Class<?>[] parameterTypes = request.getParameterTypes();
			Object[] parameters = request.getParameters();

			FastClass serviceFastClass = FastClass.create(serviceClass);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
			Object result = serviceFastMethod.invoke(serviceBean, parameters);
			response.setResult(result);
		} catch (Throwable t) {
			t.printStackTrace();
			response.setError(t);
		}
		return response;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
