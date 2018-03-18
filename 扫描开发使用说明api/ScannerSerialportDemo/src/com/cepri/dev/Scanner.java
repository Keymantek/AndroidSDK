package com.cepri.dev;

import com.mediatek.engineermode.io.EmGpio;

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
	
	public boolean powerOn(){
		if(EmGpio.gpioInit()==false 
			||EmGpio.setGpioOutput(19)==false
			||EmGpio.setGpioDataHigh(19)==false
			||EmGpio.setGpioOutput(94)==false
			||EmGpio.setGpioDataHigh(94)==false
			||EmGpio.setGpioOutput(94)==false
			||EmGpio.setGpioDataLow(94)==false
			||EmGpio.setGpioOutput(80)==false
			||EmGpio.setGpioDataHigh(80)==false
			||EmGpio.setGpioOutput(78)==false
			||EmGpio.setGpioDataHigh(78)==false){
			return false;
		}else{
			return true;
		}
		
	}
	
	public boolean  powerOff(){
		if(EmGpio.setGpioOutput(19)==false 
			||EmGpio.setGpioDataLow(19)==false
			||EmGpio.gpioUnInit()==false){
			return false;
		}else{
			return true;
		}
	}
	
	static public native int init();

	static public native int deInit();

	static public native int decode(int timeout, byte[] code, int offset);
}
