package com.huewu.libs.network;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import com.huewu.libs.network.JsonNetworkWorker.RequestHandler;
import com.huewu.libs.network.RequestEvents.RequestFailedEvent;
import com.huewu.libs.network.RequestEvents.RequestFinishedEvent;
import com.huewu.libs.network.RequestEvents.RequestReadyEvent;
import com.huewu.libs.network.RequestEvents.RequestResponsedEvent;
import com.huewu.libs.network.RequestEvents.RequestRetryingEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import android.content.Context;

/**
 * Json Network Worekr
 * @author huewu.yang
 *
 */
public class DebuggableJsonNetworkWorker extends JsonNetworkWorker {

	public DebuggableJsonNetworkWorker(Context ctx) {
		super(ctx);
	}
	
	//provide a way to check handled request & response.
	
	public JsonRequest<?> getLastRequest(){
		return mLastRequest;
	}
	
	public Bus getEventBus(){
		return mEventBus;
	}
	
	@Subscribe
	public void handleReadyRequest( RequestReadyEvent event ){
		super.handleReadyRequest(event);
	}
	
	@Subscribe
	public void handleRetryingRequest( RequestRetryingEvent event ){
		super.handleRetryingRequest(event);
	}

	@Subscribe
	public void handleFinishedRequest( RequestFinishedEvent event ){
		super.handleFinishedRequest(event);
	}
	
	@Subscribe
	public void handleFailedRequest( RequestFailedEvent event ){
		super.handleFailedRequest(event);
	}
	
	@Subscribe
	public void handleResponsedRequest( RequestResponsedEvent event ){
		super.handleResponsedRequest(event);
	}	
	
}// end of class
