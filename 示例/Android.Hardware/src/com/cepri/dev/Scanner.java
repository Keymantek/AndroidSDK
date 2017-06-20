package com.cepri.dev;

public class Scanner {
	private static Scanner instance;
	
	private Scanner() {
	}
	
	public static Scanner getInstance(){
		if (instance==null) {
			instance=new Scanner();
		}
		return instance;
	}
	
	static{
		System.loadLibrary("cepri");
	}
	
	static public native int init();

	static public native int deInit();

	static public native int decode(int timeout, byte[] code, int offset);
}
