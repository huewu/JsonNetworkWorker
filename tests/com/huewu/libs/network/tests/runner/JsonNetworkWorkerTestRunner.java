package com.huewu.libs.network.tests.runner;
import java.io.File;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.RobolectricTestRunner;

public class JsonNetworkWorkerTestRunner extends RobolectricTestRunner {

	/**
	 * Call this constructor to specify the location of resources and AndroidManifest.xml.
	 * 
	 * @param testClass
	 * @throws InitializationError
	 */	
	public JsonNetworkWorkerTestRunner(@SuppressWarnings("rawtypes") Class testClass) throws InitializationError {
		super(testClass, new RobolectricConfig(new File("../AndroidManifest.xml"), new File("../res")));
	}
}