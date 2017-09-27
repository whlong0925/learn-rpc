package com.xm.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xm.rpc.client.RpcClient;
@RestController
public class TestController {
	
	@RpcClient(value=BookService.class)
	BookService bookService;
	
	@RequestMapping("/home")
	String home() throws Exception {
		System.out.println("+++++++++++++++++++++++++++++");
		Object book = this.bookService.getBook(2);
		System.out.println(book);
		return "Hello World!";
	}
}
