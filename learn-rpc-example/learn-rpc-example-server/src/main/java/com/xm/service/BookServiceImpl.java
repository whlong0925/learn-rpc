package com.xm.service;


import org.springframework.stereotype.Service;

import com.xm.rpc.server.RpcService;


@Service
@RpcService(value = BookService.class)
public class BookServiceImpl implements BookService{

	public Book getBook(int id) {
		System.out.println("---------------getbook----------------");
		if(id == 1){
			return new Book(id,"java",123);
		}
		return new Book(id,"python",234);
	}

}
