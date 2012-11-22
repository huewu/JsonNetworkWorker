package com.huewu.libs.network;

import java.util.ArrayList;

public class RequestEvents {
	
	public static abstract class RequestEvent {
		private JsonRequest<?> mReq = null;
			
		public RequestEvent(JsonRequest<?> req) {
			mReq = req;
		}

		public JsonRequest<?> getRequest() {
			return mReq;
		}
	}
	
	public static class RequestReadyEvent extends RequestEvent {
		
		public RequestReadyEvent(JsonRequest<?> req) {
			super(req);
		}
	}
	
	public static class RequestRetryingEvent extends RequestEvent {
		
		public RequestRetryingEvent(JsonRequest<?> req) {
			super(req);
			req.retryCount++;
		}
	}
	
	public static class RequestResponsedEvent extends RequestEvent{

		public RequestResponsedEvent(JsonRequest<?> req) {
			super(req);
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getResponseObject(){
			ArrayList<?> respList = getRequest().getResponse();
			if( respList == null || respList.size() == 0 )
				return null;
			return (T) respList.get(respList.size() - 1);
		}
	}
	
	public static class RequestFinishedEvent extends RequestEvent{

		public RequestFinishedEvent(JsonRequest<?> req) {
			super(req);
		}
	}
	
	public static class RequestFailedEvent extends RequestEvent{

		public RequestFailedEvent(JsonRequest<?> req, Exception exception) {
			super(req);
			req.exception = exception;
			req.retryCount = req.maxRetryCount;
		}

		public Exception getException() {
			return getRequest().exception;
		}
	}
	
	private RequestEvents(){}

}//end of class
