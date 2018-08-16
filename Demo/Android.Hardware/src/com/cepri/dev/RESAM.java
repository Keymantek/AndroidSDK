package com.cepri.dev;

public class RESAM {
	private static RESAM instance;
	
	private RESAM() {
	}
	
	public static RESAM getInstance(){
		if (instance==null) {
			instance=new RESAM();
		}
		return instance;
	}
	
	static{
		System.loadLibrary("cepri");
	}
	
	static public native int init();

	static public native int deInit();

	static public native int clearSendCache();

	static public native int clearRevCache();

	static public native int config(int baudrate, int databits, int parity, int stopbits);

	static public native int sendData(byte[] data, int offset, int length);

	static public native int recvData(byte[] data, int offset);

	static public native int setTimeOut(int direction, int timeout);
	
	public byte ReadFile() {
		return 0;
	}
	
	public byte[] ReadFile(int index) {
		return null;
	}
}