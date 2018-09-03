package com.mediatek.engineermode.io;
public class EmGpio {
  
  public static native boolean gpioInit();
  public static native boolean gpioUnInit();
  public static native boolean setGpioOutput(int gpioIndex);
  public static native boolean setGpioDataHigh(int gpioIndex);
  public static native boolean setGpioDataLow(int gpioIndex);
  
  static{
    System.loadLibrary("em_gpio_jni");
  }
}