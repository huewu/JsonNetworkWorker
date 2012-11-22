package com.huewu.libs.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.huewu.libs.network.RequestEvents.RequestFailedEvent;
import com.huewu.libs.network.RequestEvents.RequestFinishedEvent;
import com.huewu.libs.network.RequestEvents.RequestReadyEvent;
import com.huewu.libs.network.RequestEvents.RequestResponsedEvent;
import com.huewu.libs.network.RequestEvents.RequestRetryingEvent;
import com.integralblue.httpresponsecache.HttpResponseCache;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

/**
 * Json Network Worker.
 * @author huewu.yang
 *
 */
public class JsonNetworkWorker {

	//Worker Thread pool / event managing thread.

	private static final String TAG = "NetworkWokrer";

	protected static final int REQUEST_READY = 101;
	protected static final int REQUEST_WORKING = 102;
	protected static final int REQUEST_FINISHED = 103;

	protected boolean mCacheInstalled = false; //every request is sent after this flag is set true.
	protected boolean mUseEmptySSLVerifier = false;
	protected ScheduledExecutorService mWorkerPool = null; 

	//for debuggable.
	protected JsonRequest<?> mLastRequest;	//for debuggable purpose only.
	protected Bus mEventBus = null;	//for debuggable purpose only.

	private HostnameVerifier mHostnameVerifier;

	/**
	 * constructor.
	 * @param ctx
	 */
	public JsonNetworkWorker( Context ctx ) {
		init( ctx, 3 );
	}

	public JsonNetworkWorker( Context ctx, int num_of_threads ){
		init( ctx, num_of_threads );
	}

	private void init( Context ctx, int num_of_threads ){
		mWorkerPool = Executors.newScheduledThreadPool(num_of_threads);		
		mEventBus = new Bus(ThreadEnforcer.ANY);
		mEventBus.register(this);
	}

	/**
	 * set empty ssl.
	 */
	public void initEmptySSL(){

		mUseEmptySSLVerifier = true;
		
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
		
		mHostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};		
	}	

	private boolean mUseCache = false;
	/**
	 * this api should be called before doing any request.
	 * it takes some time to install disk cache.
	 * @param flag
	 */
	public void setEnableCache( Context context, long cacheSize, boolean flag ){
		if(mUseCache == flag)
			return;	//do nothing.

		mUseCache = flag;
		if( mUseCache ){
			//init cache.
			final File cache_dir = context.getCacheDir();
			// set http cache.
			try {
				File httpCacheDir = new File(cache_dir, "http");
				HttpResponseCache.install(httpCacheDir, cacheSize);
			} catch (IOException e) {
				Log.d(TAG, "Fail to install a cache");
			}
		}
	}

	public void sendRequest(JsonRequest<?> req) {
		if (req == null)
			return;

		mEventBus.post(new RequestReadyEvent(req));
	}

	@Subscribe
	public void handleReadyRequest( RequestReadyEvent event ){

		JsonRequest<?> req = event.getRequest();
		mLastRequest = req;
		//Actual network job should be done in worker thread.
		mWorkerPool.execute(new RequestHandler(req));
		ResponseListener<?> listener = req.getResponseListener();
		if( listener != null )
			listener.onRequsetReady( req );
	}

	@Subscribe
	public void handleRetryingRequest( RequestRetryingEvent event ){

		JsonRequest<?> req = event.getRequest();

		long delay = event.getRequest().retryCount * 500;
		mWorkerPool.schedule(new RequestHandler(event.getRequest()), delay, TimeUnit.MILLISECONDS);

		ResponseListener<?> listener = req.getResponseListener();
		if( listener != null )
			listener.onRequestRetrying( req );
	}

	@Subscribe
	public void handleFinishedRequest( RequestFinishedEvent event ){
		JsonRequest<?> req = event.getRequest();
		ResponseListener<?> listener = req.getResponseListener();
		if( listener != null )
			listener.onRequestFinished( req );
	}

	@Subscribe
	public void handleFailedRequest( RequestFailedEvent event ){
		JsonRequest<?> req = event.getRequest();
		ResponseListener<?> listener = req.getResponseListener();
		if( listener != null )
			listener.onRequestFailed( req, event.getException() );
	}

	@SuppressWarnings("unchecked")
	@Subscribe
	public void handleResponsedRequest( RequestResponsedEvent event ){
		JsonRequest<?> req = event.getRequest();

		@SuppressWarnings("rawtypes")
		ResponseListener listener = req.getResponseListener();
		if( listener != null )
			listener.onRequestResponse( req, event.getResponseObject() );
	}

	protected class RequestHandler implements Runnable {

		private JsonRequest<?> mReq;
		public RequestHandler(JsonRequest<?> req) {
			mReq = req;
		}

		@TargetApi(14)
		@Override
		public void run() {
			// do http request.
			
			try {
				if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
					TrafficStats.setThreadStatsTag(0xF00D);

				URL url = new URL( mReq.getURL().toString() );
				HttpURLConnection conn = null;
				conn = (HttpURLConnection) url.openConnection();
				
				if( conn instanceof HttpsURLConnection && mUseEmptySSLVerifier)
					((HttpsURLConnection) conn).setHostnameVerifier(mHostnameVerifier);

				if( mReq.getFormData() != null )
					conn.setDoOutput(true);

				conn.setRequestMethod(mReq.getMethod().name());
				conn.setConnectTimeout(mReq.getTimeout());
				conn.setDefaultUseCaches(mUseCache);

				if( mReq.useForceCache() ){
					//in this case do not request network connection. just use locally stored cache.
					conn.setRequestProperty("Cache-Control", "only-if-cached");
				}

				for( Entry<String, String>  h : mReq.headers.entrySet() ){
					conn.setRequestProperty(h.getKey(), h.getValue());
				}

				OutputStream os;
				byte[] data = mReq.getFormData();
				if( data != null ){
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					os = conn.getOutputStream();
					os.write(data);
					os.close();
				}

				//convert stream to json. before that we should know the gson type.
				mReq.setResponseCode(conn.getResponseCode());
				InputStream is = getContent( conn );

				ResponseDecoder<?> decoder = mReq.getDecoder();
				if( decoder != null && is != null ) {

					InputStreamReader isr = new InputStreamReader(is);
					JsonReader reader = new JsonReader(isr);
					JsonToken token = reader.peek();
					switch( token ) {
					case BEGIN_OBJECT:
						handleObj( decoder, reader );
						break;
					case BEGIN_ARRAY:
						reader.beginArray();
						while( reader.hasNext() && mReq.isCanceled() == false )
							handleObj( decoder, reader );
						reader.endArray();
						break;
					default:
						//do nothing. (invalid json resp)
					}
					reader.close();
					isr.close();
				}
				if( is != null )
					is.close();
				conn.disconnect();
				mEventBus.post(new RequestEvents.RequestFinishedEvent(mReq));
			} catch (SocketException e) {
				handleRetry( e );
			} catch ( SocketTimeoutException e ) {
				handleRetry( e );
			} catch (Exception e) {
				Log.w(TAG, "UnknownException:" + mReq.getURL().toString() +":" + e);
				mEventBus.post(new RequestEvents.RequestFailedEvent(mReq, e));
			} finally {
				if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
				  TrafficStats.clearThreadStatsTag();
			}
		}//end of run.

		private void handleRetry( Exception e ) {
			if(mReq.retryCount <= mReq.maxRetryCount){
				Log.v(TAG, "Retry Request:" + mReq.getURL().toString() + ":" + e);
				mEventBus.post(new RequestEvents.RequestRetryingEvent(mReq));
			}else{
				Log.w(TAG, "Fail to Request:" + mReq.getURL().toString() + ":" + e);
				mEventBus.post(new RequestEvents.RequestFailedEvent(mReq, e));
			}
		}

		private void handleObj(ResponseDecoder<?> decoder, JsonReader reader) {
			Object obj = decoder.decode(reader);
			if(obj != null)
				mReq.addResponse( obj );

			mEventBus.post(new RequestEvents.RequestResponsedEvent(mReq));
		}
	}

	public InputStream getContent(HttpURLConnection conn) throws IOException {

		int code = conn.getResponseCode();
		Log.d(TAG, "Resp Code:" + code + ":" + conn.getRequestMethod() + ":" + conn.getURL() );

		switch(code)
		{
		case -1:
		case 400:	//invalid request.
		case 502:	//gateway timeout
		case 504:	//bad gateway
			return conn.getErrorStream();
		default:
			return conn.getInputStream();
		}
	}

	// shut down worker
	public void shutdown() {
		// if needed invoke callbacks.
		// remove all message.
		mWorkerPool.shutdown();
		mEventBus.unregister(this);
	}

}// end of class
