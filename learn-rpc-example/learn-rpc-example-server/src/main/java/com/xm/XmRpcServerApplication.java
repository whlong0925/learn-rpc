package com.xm;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Configuration;

import com.xm.rpc.autoconfig.EnableXMRpcConfig;

@SpringBootApplication
@Configuration
@EnableXMRpcConfig
public class XmRpcServerApplication implements EmbeddedServletContainerCustomizer{

	public static void main(String[] args) {
		SpringApplication.run(XmRpcServerApplication.class, args);
	}
	@Override  
    public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {  
        configurableEmbeddedServletContainer.setPort(7070);  
    } 
}