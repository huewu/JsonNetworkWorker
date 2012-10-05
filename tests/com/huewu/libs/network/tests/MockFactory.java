package com.huewu.libs.network.tests;

import java.net.URL;

import com.huewu.libs.network.JsonRequest;
import com.huewu.libs.network.JsonRequest.Method;

public class MockFactory {
	
	public static <T> JsonRequest<T> createMockJsonRequest(){
		JsonRequest<T> req = new JsonRequest<T>(Method.GET, "http://www.example.com"){
			
		};
		return req;
	}
	
	public static <T> JsonRequest<T> createMockJsonRequest(Method method, URL url, final String dataStr){
		JsonRequest<T> req = new JsonRequest<T>(method, url){
			@Override
			public byte[] getData() {
				return dataStr.getBytes();
			}
		};
		return req;
	}
	
	private MockFactory(){}

}//end of class
