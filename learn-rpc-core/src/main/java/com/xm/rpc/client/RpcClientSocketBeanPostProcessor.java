package com.xm.rpc.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.UUID;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;
import com.xm.rpc.registry.ZKServiceDiscovery;
import com.xm.utils.JacksonSerializer;

public class RpcClientSocketBeanPostProcessor implements BeanPostProcessor {
	@Autowired
	private ZKServiceDiscovery zkServiceDiscovery; 
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
    	Field[] fields = o.getClass().getDeclaredFields();
    	for(Field field : fields){
    		//1、获取属性上的指定类型的注解    
    		Annotation annotation = field.getAnnotation(RpcClient.class);
    		if(annotation != null){
    			RpcClient rpcClient = (RpcClient)annotation;
    			Class<?> target = rpcClient.value();
    			try {
                    field.setAccessible(true);
                    Object obj = Proxy.newProxyInstance(target.getClassLoader(),
                            new Class[]{target},
                            new InvocationHandler() {
								@SuppressWarnings("resource")
								public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                 // request
									String className = method.getDeclaringClass().getSimpleName();
            						RpcRequest request = new RpcRequest();
            	                    request.setRequestId(UUID.randomUUID().toString());
            	                    request.setClassName(className);
            	                    request.setMethodName(method.getName());
            	                    request.setParameterTypes(method.getParameterTypes());
            	                    request.setParameters(args);
            	                    
            	                    RpcResponse rpcResponse;
            	                    Socket socket = null;
            	                    InputStream in = null;
            	                    OutputStream out = null;
									try {
										String serverAddress = getZkServiceDiscovery().discovery(className);
										System.out.println("serverAddress+++++++++++++++++++"+serverAddress);
										socket = new Socket("localhost",9090);
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
											rpcResponse = (RpcResponse)JacksonSerializer.deserialize(buffer, RpcResponse.class);
											return rpcResponse.getResult();
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
                            });
                    //利用反射为属性赋值
                    field.set(o, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                } 
    		}
    	}
        return o;
    }
	public ZKServiceDiscovery getZkServiceDiscovery() {
		return this.zkServiceDiscovery;
	}
	public void setZkServiceDiscovery(ZKServiceDiscovery zkServiceDiscovery) {
		this.zkServiceDiscovery = zkServiceDiscovery;
	}
}