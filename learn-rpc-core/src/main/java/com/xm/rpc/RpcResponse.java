package com.xm.rpc;

import java.io.Serializable;

public class RpcResponse implements Serializable{
	private static final long serialVersionUID = -1512376730186508027L;
	private String requestId;
    private Throwable error;
    private Object result;

   
	public String getRequestId() {
		return this.requestId;
	}


	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}


	public Throwable getError() {
		return this.error;
	}


	public void setError(Throwable error) {
		this.error = error;
	}


	public Object getResult() {
		return this.result;
	}


	public void setResult(Object result) {
		this.result = result;
	}


	@Override
	public String toString() {
		return "RpcResponse [requestId=" + requestId + ", error=" + error + ", result=" + result + "]";
	}
    
}