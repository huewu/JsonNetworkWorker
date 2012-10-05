package com.huewu.libs.network;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public abstract class JsonRequest<T> {

	public final static int PORT = 80;	
	public final static int RETRY_COUNT = 5;

	protected URL url = null;
	protected Method method = Method.GET;
	protected HashMap<String, String> headers = new HashMap<String, String>();
	
	protected ArrayList<T> response = new ArrayList<T>(5);
	protected Exception exception = null;
	protected byte[] data = null;
	protected boolean useSecure = false;
	protected int retryCount = 0;
	protected int maxRetryCount = RETRY_COUNT;
	protected int status = 0;
	
	private ResponseDecoder<T> mDecoder;
	private ResponseListener mRespListener;

	private boolean mForceUseCache = false;

	public enum Method
	{
		GET, POST, PUT, DELETE
	}

	public JsonRequest( Method method, URL url ){
		this.method = method;
		this.url = url;
	}

	public JsonRequest(Method method, String urlStr) {
		this.method = method;
		try {
			this.url = new URL(urlStr);
		} catch (MalformedURLException e) {
			this.url = null;
		}
	}

	public URL getURL() {
		return url;
	}

	public Method getMethod() {
		return method;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setDecoder( ResponseDecoder<T> decoder ){
		mDecoder = decoder;
	}
	
	public void setResponseListener( ResponseListener listener ){
		mRespListener = listener;
	}
	
	public ResponseDecoder<T> getDecoder(){
		return mDecoder;
	}
	
	public ResponseListener getResponseListener(){
		return mRespListener;
	}
	
	/**
	 * return read-only unmodifiable map.
	 * 
	 * @return
	 */
	public HashMap<String, String> getHeaders() {
		return (HashMap<String, String>) Collections.unmodifiableMap(headers);
	}

	public boolean useForceCache() {
		return mForceUseCache;
	}

	public void setForceCache(boolean flag) {
		mForceUseCache = flag;
	}

	public int getResponseCode(){
		return status;
	}
	
	public void setResponseCode( int code ){
		status = code;
	}

	public void addResponse( Object obj ){
		response.add((T) obj);
	}

	public ArrayList<T> getResponse() {
		return response;
	}

	public boolean isCanceled() {
		return false;
	}

	public int getTimeout() {
		return 5000;	//default timeout value is 5sec.
	}
}// end of class
