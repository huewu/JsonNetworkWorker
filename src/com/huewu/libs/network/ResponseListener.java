package com.huewu.libs.network;

/**
 * @author huewu.yang
 * JsonNetwork request is responded. 
 */
public interface ResponseListener {
	
	void onRequsetReady( JsonRequest<?> req );
	
	void onRequestRetrying( JsonRequest<?> req );
	
	void onRequestResponse( JsonRequest<?> req );
	
	void onRequestFinished( JsonRequest<?> req );
	
	void onRequestFailed( JsonRequest<?> req );

}// end of interface
