package com.bjw.ComAssistant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

import com.bjw.bean.ComBean;

/**
 *串口辅助工具类
 */
public abstract class SerialHelper{
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private SendThread mSendThread;
	private String sPort="/dev/s3c2410_serial0";
	private int iBaudRate=9600;
	private int Data=8;
	private int Stop=1;
	private int Parity='E';
	private boolean _isOpen=false;
	private byte[] _bLoopData=new byte[]{0x30};
	private int iDelay=500;
	//----------------------------------------------------
	public SerialHelper(String sPort,int iBaudRate,int Data,int Stop,int Parity){
		this.sPort = sPort;
		this.iBaudRate=iBaudRate;
		this.Data=Data;
		this.Stop=Stop;
		this.Parity=Parity;
	}
	public SerialHelper(){
		this("/dev/s3c2410_serial0",9600,8,1,'E');
	}
	public SerialHelper(String sPort){
		this(sPort,9600,8,1,'E');
	}
	public SerialHelper(String sPort,String sBaudRate){
		this(sPort,Integer.parseInt(sBaudRate),8,1,'E');
	}
	public SerialHelper(String sPort,String sBaudRate,String sDate,String sStop,String sParity){
		this(sPort,Integer.parseInt(sBaudRate),Integer.parseInt(sDate),Integer.parseInt(sStop),Integer.parseInt(sParity));
	}
	//----------------------------------------------------
	public void open() throws SecurityException, IOException,InvalidParameterException{
		mSerialPort =  new SerialPort(new File(sPort), iBaudRate, Data, Stop, Parity);
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		mReadThread = new ReadThread();
		mReadThread.start();
		mSendThread = new SendThread();
		mSendThread.setSuspendFlag();
		mSendThread.start();
		_isOpen=true;
	}
	//----------------------------------------------------
	public void close(){
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		_isOpen=false;
	}
	//----------------------------------------------------
	public void send(byte[] bOutArray){
		try
		{
			mOutputStream.write(bOutArray);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	//----------------------------------------------------
	public void sendHex(String sHex){
		byte[] bOutArray = MyFunc.HexToByteArr(sHex);
		send(bOutArray);		
	}
	//----------------------------------------------------
	public void sendTxt(String sTxt){
		byte[] bOutArray =sTxt.getBytes();
		send(bOutArray);		
	}
	//----------------------------------------------------
	private class ReadThread extends Thread {		
		int size;
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				try
				{	
					byte[] buffer=new byte[8192];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					if (size > 0){
						ComBean ComRecData = new ComBean(sPort,buffer,size);
						onDataReceived(ComRecData);
					}
					try
					{
						Thread.sleep(50);//延时50ms
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				} catch (Throwable e)
				{
					e.printStackTrace();
					return;
				}
			}
		}
	}
	//----------------------------------------------------
	private class SendThread extends Thread{
		public boolean suspendFlag = true;// 控制线程的执行
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				synchronized (this)
				{
					while (suspendFlag)
					{
						try
						{
							wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				send(getbLoopData());
				try
				{
					Thread.sleep(iDelay);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		//线程暂停
		public void setSuspendFlag() {
		this.suspendFlag = true;
		}
		
		//唤醒线程
		public synchronized void setResume() {
		this.suspendFlag = false;
		notify();
		}
	}
	//----------------------------------------------------
	public int getData() {
		return Data;
	}

	public boolean setData(int iData)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			Data = iData;
			return true;
		}
	}
	public boolean setData(String sData){
		int iData = Integer.parseInt(sData);
		return setData(iData);
	}
	public int getStop() {
		return Stop;
	}
	public boolean setStop(int iStop)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			Stop = iStop;
			return true;
		}
	}
	public boolean setStop(String sStop){
		int iStop = Integer.parseInt(sStop);
		return setStop(iStop);
	}
	
	public int getParity() {
		return Parity;
	}
	public boolean setParity(int iParity)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			Parity = iParity;
			return true;
		}
	}
	public boolean setParity(String sParity){
		int iParity = 'N';
		if(sParity.equals("N")){
			iParity = 'N';
		}else if(sParity.equals("O")){
			iParity = 'O';
		}else if(sParity.equals("E")){
			iParity = 'E';
		}
		return setParity(iParity);
	}
	
	public int getBaudRate()
	{
		return iBaudRate;
	}
	public boolean setBaudRate(int iBaud)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			iBaudRate = iBaud;
			return true;
		}
	}
	public boolean setBaudRate(String sBaud)
	{
		int iBaud = Integer.parseInt(sBaud);
		return setBaudRate(iBaud);
	}
	//----------------------------------------------------
	public String getPort()
	{
		return sPort;
	}
	public boolean setPort(String sPort)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			this.sPort = sPort;
			return true;
		}
	}
	//----------------------------------------------------
	public boolean isOpen()
	{
		return _isOpen;
	}
	//----------------------------------------------------
	public byte[] getbLoopData()
	{
		return _bLoopData;
	}
	//----------------------------------------------------
	public void setbLoopData(byte[] bLoopData)
	{
		this._bLoopData = bLoopData;
	}
	//----------------------------------------------------
	public void setTxtLoopData(String sTxt){
		this._bLoopData = sTxt.getBytes();
	}
	//----------------------------------------------------
	public void setHexLoopData(String sHex){
		this._bLoopData = MyFunc.HexToByteArr(sHex);
	}
	//----------------------------------------------------
	public int getiDelay()
	{
		return iDelay;
	}
	//----------------------------------------------------
	public void setiDelay(int iDelay)
	{
		this.iDelay = iDelay;
	}
	//----------------------------------------------------
	public void startSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setResume();
		}
	}
	//----------------------------------------------------
	public void stopSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setSuspendFlag();
		}
	}
	//----------------------------------------------------
	protected abstract void onDataReceived(ComBean ComRecData);
}