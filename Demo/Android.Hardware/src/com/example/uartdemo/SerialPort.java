/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.example.uartdemo;

import java.io.FileDescriptor;

/**
 * P44 SerialPort类是JNI类，负责程序与硬件的通信
 * 
 * @author li
 *
 */
public final class SerialPort
{
	// 加载本地.so类库
	static
	{
		System.loadLibrary("devapi");
		System.loadLibrary("SerialPortP44");
	}

	/**
	 * JNI关闭串口操作
	 * 
	 * @param port
	 *            端口
	 */
	public native static void close(int port);

	/**
	 * JNI打开串口操作
	 * 
	 * @param port
	 *            端口
	 * @param baudrate
	 *            波特率
	 * @return 串口句柄
	 */
	public native static FileDescriptor open(int port, int baudrate);

	/**
	 * VDD 3.3v开关
	 */
	public native static void power3v3off();

	/**
	 * VDD 3.3v开关
	 */
	public native static void power3v3on();

	/**
	 * 设置GPIO高电平
	 * 
	 * @param pin
	 *            端口
	 */
	public native static void setGpioHigh(int pin);

	/**
	 * 设置GPIO低电平
	 * 
	 * @param pin
	 *            端口
	 */
	public native static void setGpioLow(int pin);

	private SerialPort()
	{

	}

}