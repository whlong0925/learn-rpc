package com.xm.rpc.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;
import com.xm.rpc.registry.ZKServiceDiscovery;
import com.xm.rpc.server.nio.NettyClientProxy;
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
								public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                 // request
									String className = method.getDeclaringClass().getSimpleName();
            						RpcRequest request = new RpcRequest();
            	                    request.setRequestId(UUID.randomUUID().toString());
            	                    request.setClassName(className);
            	                    request.setMethodName(method.getName());
            	                    request.setParameterTypes(method.getParameterTypes());
            	                    request.setParameters(args);
            	                    //BIO方式
            	                    String serverAddress = getZkServiceDiscovery().discovery(request.getClassName());
//            	                    RpcResponse rpcResponse = BIOClient.send(request,serverAddress);
            	                    NettyClientProxy nettyProxy = new NettyClientProxy(request,serverAddress);
            	                    System.out.println("+++++++++11111++++++++++"+request.getRequestId());
            	                    RpcResponse rpcResponse = nettyProxy.send();
            	                    System.out.println("+++++++++22222++++++++++"+request.getRequestId());
            	                    return rpcResponse.getResult();
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