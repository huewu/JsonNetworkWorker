package com.huewu.libs.network;

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
	}
	
	private RequestEvents(){}

}//end of class
