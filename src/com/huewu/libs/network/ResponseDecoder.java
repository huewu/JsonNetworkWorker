package com.huewu.libs.network;

import com.google.gson.stream.JsonReader;

public interface ResponseDecoder<T> {
	
	T decode( JsonReader reader ); 
	
}//end of interface
