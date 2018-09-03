package com.scanner.ComAssistant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import com.scanner.bean.ComBean;

import android_serialport_api.SerialPort;
import android.content.SharedPreferences;
import android.util.Log;

/**
 *���ڸ���������
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
		if (mReadThread != null){
			mReadThread.currentThread().interrupt();
		Log.e("interrupt", mReadThread.isInterrupted()+"");}
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
			Log.e("isInterrupted", mReadThread.isInterrupted()+"");
			while(!isInterrupted()) {
				try
				{	
					byte[] buffer=new byte[8192];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					Log.e("ssss", "ssss");
					if (size > 0){
						ComBean ComRecData = new ComBean(sPort,buffer,size);
						//ComRecData.bRec = deal(ComRecData.bRec);
						//sendHex("04D000F000FE2B");
						/*if(ComRecData.bRec!=null)		
						{
							byte[] bb ={0x04,(byte)0xD0,0x04,0x00,(byte)0xFF,0x28};//������ص�����Ϊ���������ݣ���ô����ack�룬��host˵���Ѿ��յ�������
							send(bb);
						}*/
						onDataReceived(ComRecData);
					}
				//	Log.e("buffer", MyFunc.ByteArrToHex(buffer));
					try
					{
						Thread.sleep(50);//��ʱ50ms
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
		public boolean suspendFlag = true;// �����̵߳�ִ��
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
				//send(getbLoopData());
				try
				{
					Thread.sleep(iDelay);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		//�߳���ͣ
		public void setSuspendFlag() {
		this.suspendFlag = true;
		}
		
		//�����߳�
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
	
	//----------------------------------------------------
	static public byte[]  cs(byte[] total,int size){      //�жϴ�����ֽ������Ƿ��������
		 int length,opcode,msgsour,status,checksum,datasum = 0;//����������Ϣ�ı���
		 String opcodeN = "",msgsN = "",statusN = "";
		 length = MyFunc.ByteToInt(total[0]);
		 opcode = MyFunc.ByteToInt(total[1]);
		//�ж�OpCode
			if(opcode == 0xF3){	
				opcodeN="decode_data,����õ�������";
			}else{
				opcodeN="δ֪";
				System.out.println(opcodeN);
				return null;
			}
		 msgsour = MyFunc.ByteToInt(total[2]);
		 //�ж�MessageSource
			if(msgsour == 0x00){
				msgsN = "Decoder������";
			}else if(msgsour == 0x04){
				msgsN = "Host����";
			}else{
				msgsN = "δ֪";
				System.out.println(msgsN);
				return null;
			}	
		 status = MyFunc.ByteToInt(total[3]);
		 //�ж�״̬��
			if(status == 0x10){
				statusN = "���Ĵ���";
			}else if(status == 0x00){
				statusN = "��һ�δ���";
			}else{
				statusN = "δ֪";
				System.out.println(statusN);
				return null;
			}
		checksum = MyFunc.ByteToInt(total[size-2])*256+ MyFunc.ByteToInt(total[size-1]);//������λ�ø�ֵ
		int datasize = length - 4;	//�����size����6������datasize������Ϊ��ֵ
		byte [] realData = new byte[datasize];	//ȡ�����������ݲ���
		for(int j = 0;j<datasize;j++){
			datasum += MyFunc.ByteToInt(total[4+j]);
			realData[j]=total[4+j];
			System.out.println(MyFunc.Byte2Hex(realData[j]));
		}
		if (checksum == 0x10000-length-opcode-msgsour-status-datasum){//���������鹫ʽ������бȶԣ���ȷ������Ϊ�������ݣ��ͷ������������� 
			return realData;
			}
			
		 return null;
	 }
	 
	 public static byte[] deal(byte [] bs) { //��װ�ķ���
		int lenOfbs = bs.length;	//��������ĳ���
		int header;   
		if(lenOfbs>6){     		//��������ֽ����鳤�Ȳ�����6������һ������������
			for(int i = 0;i<lenOfbs;i++){
				 header = MyFunc.ByteToInt(bs[i]);
				if(header>4&&header <= (lenOfbs-2-i)){//���header������4�����ʣ���ֽڳ��ȼ�2��������������ǰ�ֽ������lengthλ
					int totalsize = header + 2;  //��������������������ݵ��ֽڳ���Ϊlength+2����2λΪУ���λ��2λ
					byte [] total = new byte[totalsize] ;
					for(int j = 0;j<header+2;j++){
						total[j] = bs[i+j];
					}                            // ����������װ����һ���ֽ�����
					byte [] realData = cs(total, totalsize);   //���ֽ����鴫��cs��������У�顣����У����򷵻���ʵ���ݶΣ������Ϸ��ؿ�
					if(realData!=null){
						return  realData;
					}else{
						
						System.out.println("���ݲ���ͨ������");
					}
				}
				else{
						   System.out.println("length�����Ϲ���");
					}
			}
		}
		else{
			System.out.println("�ֽ����鳤�Ȳ�������������������");
		 }
		return null;
		 
	}
	 
	
}