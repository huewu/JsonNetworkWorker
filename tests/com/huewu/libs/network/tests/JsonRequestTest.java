package com.huewu.libs.network.tests;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.huewu.libs.network.JsonRequest;
import com.huewu.libs.network.JsonRequest.Method;
import com.huewu.libs.network.tests.runner.JsonNetworkWorkerTestRunner;

@RunWith(JsonNetworkWorkerTestRunner.class)
public class JsonRequestTest {

	private JsonRequest<Object> mReq = null; 

	@Before
	public void init(){
		mReq = new JsonRequest<Object>(Method.GET, "http://www.example.com") {
		};
	}

	@Test
	public void testGetMethod(){
		assertEquals( Method.GET, mReq.getMethod() );
	}

	@Test
	public void testGetURL() throws MalformedURLException{
		assertEquals( new URL("http://www.example.com"), mReq.getURL() );
	}

	@Test
	public void testGetTimeout(){
		//default timeout is 5sec.
		assertEquals( 5000, mReq.getTimeout() );
	}

	@Test
	public void testFormDataWithNull(){
		mReq.putFormData("aaa", null);
		mReq.putFormData(null, "bbb");
		mReq.putFormData(null, null);

		byte[] data = mReq.getFormData();

		String shouldStr = "";
		assertArrayEquals(shouldStr.getBytes(), data);
	}

	@Test
	public void testFormData(){
		mReq.putFormData("aaa", "1234");
		mReq.putFormData("bbb", "3456");

		byte[] data = mReq.getFormData();

		String shouldStr = "aaa=1234&bbb=3456";
		assertArrayEquals(shouldStr.getBytes(), data);
	}

	@Test
	public void testResponseCode(){
		int respCode = -100;
		mReq.setResponseCode(respCode);
		assertEquals(respCode, mReq.getResponseCode());
	}

	@Test
	public void testResponse(){
		Object obj1 = new Object();
		Object obj2 = new Object();
		Object obj3 = new Object();

		mReq.addResponse(obj1);
		mReq.addResponse(obj2);
		mReq.addResponse(obj3);

		assertEquals(3, mReq.getResponse().size());
		assertEquals(obj1, mReq.getResponse().get(0));
		assertEquals(obj2, mReq.getResponse().get(1));
		assertEquals(obj3, mReq.getResponse().get(2));
	}

}//end of class
