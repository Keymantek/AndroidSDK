package com.cepri.dev;

public class LibInfo {
	private static LibInfo instance;
	
	private LibInfo() {
	}
	
	public static LibInfo getInstance(){
		if (instance==null) {
			instance=new LibInfo();
		}
		return instance;
	}
	
	static{
		System.loadLibrary("cepri");
	}
	
	static public native int getVersion();

	static public native int getCompany();
}
