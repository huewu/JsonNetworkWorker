package com.huewu.libs.network;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class GenericDecoder<T> implements ResponseDecoder<T> {

	private Gson mGson = new Gson();
	private Class<T> mKlass = null;
	
	public GenericDecoder( Class<T> klass ){
		mKlass = klass;
	}

	@Override
	public T decode(JsonReader reader) {

		if( reader == null )
			return null;

		T target = mGson.fromJson(reader, mKlass);
		return target;
	}

}//end of class
