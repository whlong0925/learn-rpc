package com.xm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import com.xm.rpc.autoconfig.EnableXMRpcConfig;

@SpringBootApplication
@Configuration
@EnableXMRpcConfig
public class XmRpcApplication{

	public static void main(String[] args) {
		SpringApplication.run(XmRpcApplication.class, args);
	}
}