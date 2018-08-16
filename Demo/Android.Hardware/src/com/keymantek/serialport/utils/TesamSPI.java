package com.keymantek.serialport.utils;

public class TesamSPI
{

	static
	{
		System.loadLibrary("TesamSPI");
	}
	private final String DEBUG_TAG = "ccy";

	protected native int deInit();

	protected native int init();

	protected native int recvData(byte[] data, int offset, int length);

	protected native int sendData(byte[] data, int offset, int length);

	protected native int setBits(int bits);

	protected native int setFreq(int freq);

	protected native int setIOControl(int diy);

	protected native int setMode(int mode);

}
