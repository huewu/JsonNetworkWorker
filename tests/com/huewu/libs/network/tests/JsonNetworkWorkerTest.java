package com.huewu.libs.network.tests;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;

import com.huewu.libs.network.DebuggableJsonNetworkWorker;
import com.huewu.libs.network.JsonNetworkWorker;
import com.huewu.libs.network.JsonRequest;
import com.huewu.libs.network.RequestEvents;
import com.huewu.libs.network.JsonRequest.Method;
import com.huewu.libs.network.RequestEvents.RequestResponsedEvent;
import com.huewu.libs.network.RequestEvents.RequestFailedEvent;
import com.huewu.libs.network.RequestEvents.RequestFinishedEvent;
import com.huewu.libs.network.RequestEvents.RequestReadyEvent;
import com.huewu.libs.network.RequestEvents.RequestRetryingEvent;
import com.huewu.libs.network.tests.runner.*;
import com.squareup.otto.Subscribe;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;

@RunWith(JsonNetworkWorkerTestRunner.class)
public class JsonNetworkWorkerTest {
	
	private DebuggableJsonNetworkWorker mWorker = null;
	
	@Before
	public void init(){
		mWorker = new DebuggableJsonNetworkWorker(Robolectric.application.getApplicationContext());
	}
	
	@Test
	public void testSendRequest(){
		
		JsonRequest<String> req = MockFactory.<String>createMockJsonRequest();

		EventListener listener = new EventListener();
		mWorker.getEventBus().register(listener);
		mWorker.sendRequest(req);
		
		listener.waitEvent(5000);
		
		//I should receive event. BeforeRequest
		assertNotNull(listener.readyEvent);
	}
	
	@Test
	public void testSendRequestWhenRetryingRequired(){
		
		//make request that fails always at the first time. 
		
		JsonRequest<String> req = new JsonRequest<String>(Method.GET, "http://www.example.com"){
			
			@Override
			public int getTimeout() {
				if( retryCount == 0 )
					return 1;
				else
					return super.getTimeout();
			}
			
		};

		EventListener listener = new EventListener();
		mWorker.getEventBus().register(listener);
		mWorker.sendRequest(req);
		
		listener.waitEvent(1000 * 30);
		
		//I should receive event. BeforeRequest
		assertNotNull(listener.readyEvent);
		assertNotNull(listener.retryEvent);
	}
	
	@Test
	public void testSendRequestWhenFailed(){
		
		JsonRequest<String> req = MockFactory.<String>createMockJsonRequest();

		EventListener listener = new EventListener();
		mWorker.getEventBus().register(listener);
		mWorker.sendRequest(req);
		
		listener.waitEvent(5000);
		
		//I should receive event. BeforeRequest
		assertNotNull(listener.readyEvent);
	}
	
	@Test
	public void testEnableCache(){
	}
	
	private class EventListener{
		
		private Object mWaitObj = new Object();
		
		private RequestReadyEvent readyEvent;
		private RequestRetryingEvent retryEvent;
		private RequestFinishedEvent finishEvent;
		private RequestFailedEvent errorEvent;
		
		public void waitEvent(int timeoutMs) {
			synchronized (mWaitObj) {
				try {
					mWaitObj.wait(timeoutMs);
				} catch (InterruptedException e) {
				}
			}
		}
		
		@Subscribe
		public void onRecv(RequestReadyEvent ev){
			readyEvent = ev;
		}
		
		@Subscribe
		public void onRecv(RequestRetryingEvent ev){
			retryEvent = ev;
		}
		
		@Subscribe
		public void onRecv(RequestFinishedEvent ev){
			finishEvent = ev;
			synchronized (mWaitObj) {
				mWaitObj.notifyAll();
			}
		}

		@Subscribe
		public void onRecv(RequestFailedEvent ev){
			errorEvent = ev;
			synchronized (mWaitObj) {
				mWaitObj.notifyAll();
			}
		}

		@Subscribe
		public void onRecv(RequestResponsedEvent ev){
			
		}
		
	}

}//end of class
