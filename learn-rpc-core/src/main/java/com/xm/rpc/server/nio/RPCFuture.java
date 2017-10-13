package com.xm.rpc.server.nio;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.xm.rpc.RpcRequest;
import com.xm.rpc.RpcResponse;

public class RPCFuture{

    private RpcRequest request;
    private RpcResponse response;
    private volatile boolean isDone = false;
    private Object lock = new Object();
    public RPCFuture(RpcRequest request) {
        this.request = request;
    }


    public RpcResponse get(long timeout, TimeUnit unit) throws Exception {
    	if (!this.isDone) {
    		synchronized (this.lock) {
    			try {
    				long time = unit.toMillis(timeout);
					this.lock.wait(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw e;
				}
    		}
				
		}
		
		if (!this.isDone) {
			System.out.println("======"+this.request.getRequestId());
			throw new TimeoutException(MessageFormat.format(">>>>>>>>>>>> xxl-rpc, netty request timeout at:{0}, request:{1}", System.currentTimeMillis(), this.request.toString()));
		}
		return this.response;
    }

    public void setRpcResponse(RpcResponse response) {
        this.response = response;
        synchronized (this.lock) {
        	this.isDone = true;
        	this.lock.notifyAll();
		}
        
    }
}