package com.xm.service;

import org.springframework.stereotype.Service;

import com.xm.rpc.server.RpcService;

@RpcService(UserService.class)
@Service
public class UserServiceImpl implements UserService {

	@Override
	public void findUser(int id) {

	}

}
