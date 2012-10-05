package com.huewu.libs.network;

import com.squareup.otto.Subscribe;

public interface ResponseListener<T> {
	
	@Subscribe
	void onRequsetReady( RequestEvents.RequestReadyEvent ev );
	
	@Subscribe
	void onRequestRetry( RequestEvents.RequestRetryingEvent ev );
	
	@Subscribe
	void onRequestResponse( RequestEvents.RequestResponsedEvent ev );
	
	@Subscribe
	void onRequestFinished( RequestEvents.RequestFinishedEvent ev );
	
	@Subscribe
	void onRequestFailed( RequestEvents.RequestFailedEvent ev );

}// end of class
