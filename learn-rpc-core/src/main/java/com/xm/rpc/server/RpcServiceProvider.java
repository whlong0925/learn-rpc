package com.xm.rpc.server;

import java.util.HashMap;
import java.util.Map;

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
import com.xm.rpc.server.nio.NettyServer;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
@ConfigurationProperties(prefix = "rpc.server")
public class RpcServiceProvider implements ApplicationContextAware, InitializingBean {
	public static final Logger logger = LoggerFactory.getLogger(RpcServiceProvider.class);
	private static Map<String, Object> rpcServiceMap = new HashMap<String, Object>();
	private int port = 9090;
	private ApplicationContext applicationContext;
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		//获取上下文中@RpcService注解
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
		ZKServiceRegistry zkServiceRegistry = this.applicationContext.getBean(ZKServiceRegistry.class);
		//注册zookeeper
		zkServiceRegistry.register(this.port, rpcServiceMap.keySet());
		//启动服务 BIO方式
//		new BIOServer(this.port).startServer();
		//启动服务 NIO方式
		new NettyServer(this.port).start();
	}


	public static RpcResponse invokeServerService(RpcRequest request, Object serviceBean) {
		System.out.println("----------invokeServerService-------------"+request.getRequestId());
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
