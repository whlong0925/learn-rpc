package com.xm.rpc.autoconfig;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.xm.rpc.client.RpcClientSocketBeanPostProcessor;
import com.xm.rpc.registry.ZKServiceDiscovery;
import com.xm.rpc.registry.ZKServiceRegistry;
import com.xm.rpc.server.RpcServiceProvider;

public class XMRpcConfigRegistrar implements ImportBeanDefinitionRegistrar {
	  @Override
	  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        
        BeanDefinitionBuilder rpcServiceProviderbeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(RpcServiceProvider.class);
        registry.registerBeanDefinition(RpcServiceProvider.class.getName(), rpcServiceProviderbeanDefinitionBuilder.getBeanDefinition());
        
        BeanDefinitionBuilder rpcClientSocketBeanPostProcessorbeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(RpcClientSocketBeanPostProcessor.class);
        registry.registerBeanDefinition(RpcClientSocketBeanPostProcessor.class.getName(), rpcClientSocketBeanPostProcessorbeanDefinitionBuilder.getBeanDefinition());
        
        BeanDefinitionBuilder zkServiceRegistryBeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(ZKServiceRegistry.class);
        registry.registerBeanDefinition(ZKServiceRegistry.class.getName(), zkServiceRegistryBeanDefinitionBuilder.getBeanDefinition());
        
        BeanDefinitionBuilder zkServiceDiscoveryBeanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(ZKServiceDiscovery.class);
        registry.registerBeanDefinition(ZKServiceDiscovery.class.getName(), zkServiceDiscoveryBeanDefinitionBuilder.getBeanDefinition());
        
	  }
}
