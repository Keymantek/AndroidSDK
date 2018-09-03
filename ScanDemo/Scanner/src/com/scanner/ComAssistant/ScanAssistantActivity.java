
package com.scanner.ComAssistant;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;


import com.mediatek.engineermode.io.EmGpio;
import com.scanner.ScanAssistant.R;
import com.scanner.bean.AssistBean;
import com.scanner.bean.ComBean;
import com.scanner.bean.DataRecord;
import com.scanner.bean.until;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import keymantek.android.hardware.GPIO;

public class ScanAssistantActivity extends Activity {
	EditText editTextRecDisp,editTextCOMA;
	//EditText editTextTimeCOMA;
	Button ButtonClear;


	SerialControl ComA;
	DispQueueThread DispQueue;//刷新显示线程
	AssistBean AssistData;//用于界面数据序列化和反序列化
	private static byte[] cacheC,cacheB,cacheA;//用户暂存数据的A,B
	int iRecLines=0;//接收区行数
	int screenWidth;//屏幕宽度
	boolean isOnline;//activity是否处于前台
	String model;//机型
	String carrier;
	String getprop;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         model = android.os.Build.MODEL;//机型
        carrier = android.os.Build.MANUFACTURER;//厂商
        Process process = null;
        try {

            process = Runtime.getRuntime().exec("getprop ro.product.device");
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        BufferedReader input = new BufferedReader(ir);

        StringBuffer buffer = new StringBuffer();
        String line = " ";
        try {
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        getprop = buffer.toString();
        
        Toast.makeText(this, model+"   "+carrier, Toast.LENGTH_SHORT).show();
        isOnline = true;
       
        
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        Button button1 = (Button) findViewById(R.id.Button1);
        ButtonClear=(Button)findViewById(R.id.ButtonClear);
        button1.getLayoutParams().width=(int) (screenWidth*0.7);
        ButtonClear.getLayoutParams().width=(int) (screenWidth*0.7);
        
        setIO();
   	 
        //扫描按钮
		 button1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v)
			{	
				 if(model.equals("MST-4")){		//掌机置电
						boolean sRet = EmGpio.setGpioDataHigh(19);
						boolean tRet = EmGpio.setGpioDataHigh(80);
						boolean zRet = EmGpio.setGpioDataHigh(78);
						boolean bRet = EmGpio.setGpioDataHigh(94);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //设置一个延时500毫秒的操作，94的下降沿触发扫描
						boolean lRet = EmGpio.setGpioDataLow(94);
						//Long.setText("High:" + String.valueOf(bRet));
				     }
				 else if(model.equals("MST-701")){		//平板置电
					 if (("esky8735_tb_l1").equals(getprop)) {
							GPIO.setGpio(96, 1);
						 	GPIO.setGpio(95, 1);
						 	GPIO.setGpio(128, 1);
						 	GPIO.setGpio(78, 1);
						 	GPIO.setGpio(125, 1);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} //设置一个延时500毫秒的操作，94的下降沿触发扫描
						 	GPIO.setGpio(125, 0); 
					 }else{
						 boolean sRet = EmGpio.setGpioDataHigh(19);
						
							boolean tRet = EmGpio.setGpioDataHigh(80);
							boolean zRet = EmGpio.setGpioDataHigh(78);
							boolean bRet = EmGpio.setGpioDataHigh(94);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} //设置一个延时500毫秒的操作，94的下降沿触发扫描
							boolean lRet = EmGpio.setGpioDataLow(94);
					 }
					 
						//Long.setText("High:" + String.valueOf(bRet));
				     } 
				
				isOnline = true;
				openPort();
			}
		});
        ComA = new SerialControl(); 
        DispQueue = new DispQueueThread();
        DispQueue.start();
        setControls();   
        
        
    }
    
    //--开启引脚---/
    public void setIO(){
    	 if(model.equals("MST-4")){		//掌机置脚
    	   	    boolean bRet = EmGpio.gpioInit();
    	        EmGpio.setGpioOutput(19);
    	        EmGpio.setGpioOutput(94);
    	        EmGpio.setGpioOutput(80);
    	        EmGpio.setGpioOutput(78); 
    	   	  }
    	 else if(model.equals("MST-701")){		//平板置脚
    		 if (("esky8735_tb_l1").equals(getprop)) {
    			 GPIO.setGPIO(96, true);
   	   		  GPIO.setGPIO(95, true);
   	   		  GPIO.setGPIO(125, true);
   	   		  GPIO.setGPIO(128, true);
   	   		  GPIO.setGPIO(78, true); 
    		 }else{
    			  boolean bRet = EmGpio.gpioInit();
      	        EmGpio.setGpioOutput(19);
      	        EmGpio.setGpioOutput(94);
      	        EmGpio.setGpioOutput(80);
      	        EmGpio.setGpioOutput(78); 
    		 }
    	   		 
    	 }
    }
  
    
  //  ---------------------------------按键触发扫描
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_ALT_LEFT) {
			 if(model.equals("MST-4")){		//掌机置电
					boolean sRet = EmGpio.setGpioDataHigh(19);
					boolean tRet = EmGpio.setGpioDataHigh(80);
					boolean zRet = EmGpio.setGpioDataHigh(78);
					boolean bRet = EmGpio.setGpioDataHigh(94);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //设置一个延时500毫秒的操作，94的下降沿触发扫描
					boolean lRet = EmGpio.setGpioDataLow(94);
					//Long.setText("High:" + String.valueOf(bRet));
			     }
			 else if(model.equals("MST-701")){		//平板置电
				 	
				 	GPIO.setGpio(96, 1);
				 	GPIO.setGpio(95, 1);
				 	GPIO.setGpio(128, 1);
				 	GPIO.setGpio(78, 1);
				 	GPIO.setGpio(125, 1);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //设置一个延时500毫秒的操作，94的下降沿触发扫描
				 	GPIO.setGpio(125, 0);
				
				
			     } 
		isOnline = true;
		openPort();
			
			return true;
		} 
		else if (keyCode == KeyEvent.KEYCODE_BACK) {  
			ClearDataA();
			ClearDataB();
			ClearDataC();
			CloseComPort(ComA);
			isOnline = false;
			
            moveTaskToBack(false);  //按下back后activity不destroy，而是到后台运行
            return true;  
        }  
		return super.onKeyDown(keyCode, event);
	}
    
  
    @Override
    public void onDestroy(){
    	CloseComPort(ComA);
    	ClearDataA();
    	ClearDataB();
    	ClearDataC();//清除缓存数据
    	isOnline = false;
    	super.onDestroy();
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	CloseComPort(ComA);
        setContentView(R.layout.main);
        setControls();
        isOnline = false;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      CloseComPort(ComA);
      setContentView(R.layout.main);
      setControls();
      isOnline = false;
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	CloseComPort(ComA);
        setContentView(R.layout.main);
        setControls();
        isOnline = false;
    }
    
    
    //----------------------------------------------------
    private void setControls()
	{

        //主Ed
    	editTextRecDisp=(EditText)findViewById(R.id.editTextRecDisp);
    	//清除
		
    	ButtonClear.setOnClickListener(new ButtonClickEvent());
	}
   
    //----------------------------------------------------清除按钮、发送按钮
    class ButtonClickEvent implements View.OnClickListener {
		public void onClick(View v)
		{
			if (v == ButtonClear){
				iRecLines=0;
				editTextRecDisp.setText("");
			} 
		}
    }
    
    //----------------------------------------------------打开串口方法
    public void openPort(){
    	ComA.setData(8);
		ComA.setStop(1);
		
		ComA.setParity('N');
		if(model.equals("MST-4")){
			ComA.setPort("/dev/ttyMT2");
		}else if (model.equals("MST-701")) {
			 if (("esky8735_tb_l1").equals(getprop)) {
				 ComA.setPort("/dev/ttyMT3");
			 }else{
				 ComA.setPort("/dev/ttyMT2");
			 }
			
		}
		
		
		ComA.setBaudRate(9600);
		Log.d("test", "ComA="+ComA.getBaudRate()+ComA.getData()+ComA.getStop()+ComA.getParity());
		try{
			File file = new File("sdcard/scan.xml");
	        if (!file.exists()) {// 如果没有该文件
				file.createNewFile();
	        } else {
	        	file.delete();
	        	file.createNewFile();
	        }
		}catch(IOException e){
			
		}
		//序列化写入（本地保存状态）
		DataRecord dataRecordInfo=new DataRecord();
		dataRecordInfo.setParity('N');
		if(model.equals("MST-4")){
			dataRecordInfo.setSerialPort("/dev/ttyMT2");
		}else if (model.equals("MST-701")) {
			 if (("esky8735_tb_l1").equals(getprop)) {
				 dataRecordInfo.setSerialPort("/dev/ttyMT3");
			 }else{
				 dataRecordInfo.setSerialPort("/dev/ttyMT2");
			 }
			
		}
		
		dataRecordInfo.setBaudrate(9600); 
		dataRecordInfo.setDataBits(8);
		
		dataRecordInfo.setStop(1);
		try {
			until.serializer(dataRecordInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	OpenComPort(ComA);
    }
    
    //----------------------------------------------------串口控制类
    private class SerialControl extends SerialHelper{
		public SerialControl(){
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData)
		{
			//数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
			//直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
			//用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
			//最终效果差不多-_-，线程定时刷新稍好一些。
			if(isOnline){
				DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
			}
			
		/*	runOnUiThread(new Runnable()//直接刷新显示
			{
				public void run()
				{
					DispRecData(ComRecData);
				}
			});*/
		}
    }
    
    //----------------------------------------------------刷新显示线程
     private class DispQueueThread extends Thread{

    	 private Queue<ComBean> QueueList = new LinkedList<ComBean>(); //ComBean放入list
		@Override
		public void run() {
			
			super.run();
			Log.e("DispQueueThread", QueueList.size()+"");
			while(!isInterrupted()) {
				final ComBean ComData;
		        while((ComData=QueueList.poll())!=null)  //ComBean不为null时调用显示data方法
		        {
		        	runOnUiThread(new Runnable()
					{
						public void run()
						{
							DispRecData(ComData);
							
						}
					});
		        	try
					{
		        		Thread.sleep(5);//显示性能高的话，可以把此数值调小。
					} catch (Exception e)
					{
						e.printStackTrace();
					}
		        	break;
				}
			}
		}

		public synchronized void AddQueue(ComBean ComData){
			QueueList.add(ComData);
		}
	}


  
    

    
  

    //----------------------------------------------------显示接收数据
    private void DispRecData(ComBean ComRecData){
    	StringBuilder sMsg=new StringBuilder();  
    		if(ComRecData.bRec!=null&&!ComRecData.bRec.equals("")){
    			String chechS = MyFunc.ByteArrToHex(ComRecData.bRec);
    			String ss = new String(ComRecData.bRec);
    			Log.d("test", "receData="+chechS);
    			String dataInfo = "";
    			String aimID = "";
    			String bcType = "";
    			String isUrl = "";
    				if(deal1(ComRecData.bRec)!=null){
    					Log.d("d1", "这里是d1");
    					
    					dataInfo = new String(deal1(ComRecData.bRec));
    					bcType = chechS.substring(12, 14);
    					bcType = MyFunc.deBCType(bcType);
    					byte[] bb ={0x04,(byte)0xD0,0x04,0x00,(byte)0xFF,0x28};//如果返回的数据为完整的数据，那么发送ack码，向host说明已经收到数据了
    				//	ComA.send(bb);
    					VibratorUtil.Vibrate(this, 100);
    					 VibratorUtil.sound(this);
    				
    					sMsg.append("type:"+bcType+"\n"+"date:"+dataInfo+"\n");
    					}
    				else if(deal2(ComRecData.bRec)!=null){
    					Log.d("d2", "这里是d2");
    					
    					dataInfo = new String(deal2(ComRecData.bRec));
    					aimID = dataInfo.substring(0,3);
    					aimID = MyFunc.deAIMId(aimID);
    					dataInfo = dataInfo.substring(3);
    					if(dataInfo.length()>3)
    						{
    							isUrl = dataInfo.substring(0,4);
    						}
    							ClearDataB();
    					if(isUrl.equalsIgnoreCase("http")){
    						Intent intent = new Intent(this,WebActivity.class);
    						intent.putExtra("url", dataInfo);
    						
    						VibratorUtil.Vibrate(this, 100);
    						 VibratorUtil.sound(this);
    						startActivity(intent);
    					}else{
    						
    						VibratorUtil.Vibrate(this, 100);
    						 VibratorUtil.sound(this);
    						Log.d("d2", "这里是d2整栋");
    						sMsg.append("type:"+aimID+"\n"+"date:"+dataInfo+"\n");
    						}
    					}
    				 else if(deal3(ComRecData.bRec)!=null){
    					 Log.d("d3", "这里是d3");
    					 
    					 byte[] total = deal3(ComRecData.bRec);
    					 aimID = MyFunc.deAIMId((char)total[0]+""+(char)total[1]+""+(char)total[2]+"");
    					 int daLen = total.length-4;
    					 for(int i = 0;i<daLen;i++){
    						 dataInfo += (char)total[i+3] + "";
    					 }
    					 
    					 Log.d("data", dataInfo);
    					 VibratorUtil.Vibrate(this, 100);
    					 VibratorUtil.sound(this);
    					 sMsg.append("type:"+aimID+"\n"+"date:"+dataInfo+"\n");
    					sMsg.append(chechS+"\n" +ss+"\n");
    				 } else if(de2(ComRecData.bRec)!=null){
    					 Log.d("dde2", "这里是d3");
    					 
    					 byte[] total = de2(ComRecData.bRec);
    					 int dalen = total.length;
    					 
    					 for(int i = 0;i<dalen;i++){
    						 dataInfo += (char)total[i] + "";
    					 }
    					 Log.d("data", dataInfo);
    					 if(dataInfo.length()>3)
 						{
 							isUrl = dataInfo.substring(0,4);
 						}
    					   if(isUrl.equalsIgnoreCase("http")){
     						  Intent intent = new Intent(this,WebActivity.class);
     						  intent.putExtra("url", dataInfo);
     						
     						  VibratorUtil.Vibrate(this, 100);
     						 VibratorUtil.sound(this);
     						  startActivity(intent);
     					   }else{
     						  VibratorUtil.Vibrate(this, 100);
     						 VibratorUtil.sound(this);
     						  sMsg.append("date:"+dataInfo+"\n");
     						}
     					 }    				 
    		}	
    //	 sMsg.append(MyFunc.Bytes2HexString(ComRecData.bRec));
    		sMsg.append("\r\n");
        	sMsg.append("\r");
        	editTextRecDisp.append(sMsg);
        	iRecLines++;
			Log.d("test", "receData11="+sMsg);
		}
  


    
    //----------------------------------------------------关闭串口
    private void CloseComPort(SerialHelper ComPort){
    	if (ComPort!=null){
    		ComPort.stopSend();
    		ComPort.close();
		}
    }
    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort){
    	try
		{
			ComPort.open();
		} catch (SecurityException e) {
			ShowMessage("打开串口失败:没有串口读/写权限!");
		} catch (IOException e) {
			ShowMessage("打开串口失败:未知错误!");
		} catch (InvalidParameterException e) {
			ShowMessage("打开串口失败:参数错误!");
		}
    }
    //------------------------------------------显示消息
  	private void ShowMessage(String sMsg)
  	{
  		Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
  	}
  	
  	
  	
 	 private  void SaveDataC(byte[] cachedata) {   //保存暂存数据
 		cacheB = cachedata;
		}  
 	 
 	 private  void ClearDataC() {   //清空暂存数据
		   cacheC = null;
		   if(cacheC==null){
			   System.out.println("清空了C");
		   }
		}  
 	   
	private byte[] LoadDataC() {     //加载缓存信息
		    //指定操作的文件名称  
			  return cacheC;
		
		}
  	
  	 private  void SaveDataB(byte[] cachedata) {   //保存暂存数据
  		cacheB = cachedata;
		}  
  	 
  	 private  void ClearDataB() {   //清空暂存数据
		   cacheB = null;
		   if(cacheB==null){
			   System.out.println("清空了B");
		   }
		}  
  	   
	private byte[] LoadDataB() {     //加载缓存信息
		    //指定操作的文件名称  
			  return cacheB;
		
		}
	
	private  void SaveDataA(byte[] cachedata) {//保存暂存数据
  		cacheA = cachedata;
		}  
  	 
	private  void ClearDataA() {   //清空暂存数据
		   cacheA = null;
		   if(cacheA==null){
			   System.out.println("清空了A");
		   }
		}  
  	   
	private byte[] LoadDataA() {     //加载缓存信息
		    //指定操作的文件名称  
			  return cacheA;
		}
  	
  	 private byte[]  cs(byte[] total,int size){      //判断传入的字节数组是否满足规则
		 int length,opcode,msgsour,status,checksum,datasum = 0;//声明完整信息的变量
		 String opcodeN = "",msgsN = "",statusN = "";
		 length = MyFunc.ByteToInt(total[0]);
		 opcode = MyFunc.ByteToInt(total[1]);
		//判断OpCode
			if(opcode == 0xF3){	
				opcodeN="decode_data,解码得到的数据";
			}else{
				opcodeN="未知";
				System.out.println(opcode+" opcodeN"+opcodeN);
				return null;
			}
		 msgsour = MyFunc.ByteToInt(total[2]);
		 //判断MessageSource
			if(msgsour == 0x00){
				msgsN = "Decoder解码器";
			}else if(msgsour == 0x04){
				msgsN = "Host主机";
			}else{
				msgsN = "未知";
				System.out.println(msgsour+" msgsN"+msgsN);
				return null;
			}	
		 status = MyFunc.ByteToInt(total[3]);
		 //判断状态码
			if(status == 0x10){
				statusN = "随后的传输";
			}else if(status == 0x00){
				statusN = "第一次传输";
			}else{
				statusN = "未知";
				System.out.println(status+" statusN"+statusN);
				return null;
			}
		checksum = MyFunc.ByteToInt(total[size-2])*256+ MyFunc.ByteToInt(total[size-1]);//给各个位置赋值
		int datasize = length - 4;	//传入的size大于6，所以datasize不可能为负值
		byte [] realData = offsetByteArray(total, 4, datasize);	//取出真正的数据部分
		for(int j = 0;j<datasize;j++){
			datasum += MyFunc.ByteToInt(total[4+j]);
		}
		if (checksum == 0x10000-length-opcode-msgsour-status-datasum){//检验和与检验公式计算进行比对，正确符合则为完整数据，就返回完整的数据 
			return realData;
			}
		 return null;
	 }
	 
  	 private byte[] linkByte(byte[] a,byte [] b){  //拼接两个字节数组
  		 byte[] message;
		  byte[] Head= a;
		  byte[] Body= b; //a为头，b为尾
		  message=new byte[Head.length+Body.length];
		  for (int i = 0; i < message.length; i++) {
		   if(i<Head.length){
		    message[i]=Head[i];
		   }else{
		    message[i]=Body[i-Head.length];
		   }
		  }
		  return message;
  	 }
  	 
	 private  byte[] deal1(byte [] bs) { //为一维扫描封装的方法  
		byte [] cache;
		if(LoadDataA()!=null){    //如果存在缓存数据，就将缓存数据和本次获得的数据信息拼接到一起进行检验
			cache = LoadDataA();
			bs = linkByte(cache, bs);
		}
		int lenOfbs = 0;
		if(bs!=null){
			 lenOfbs = bs.length;	//传入数组的长度
		}
		int header; 
		if(lenOfbs>6){     		//如果传入字节数组长度不大于6，则不是一条完整的数据
			for(int i = 0;i<lenOfbs;i++){
				 header = MyFunc.ByteToInt(bs[i]);
				if(header>4&&header <= (lenOfbs-2-i)){//如果header不大于4或大于剩下字节长度减2，则他不能做当前字节数组的length位
					int totalsize = header + 2;  //满足这个条件后，完整数据的字节长度为length+2，这2位为校验和位的2位
					byte [] total = new byte[totalsize] ;
					for(int j = 0;j<header+2;j++){
						total[j] = bs[i+j];
					}                            // 将完整数据装入另一个字节数组
					byte [] realData = cs(total, totalsize);   //将字节数组传入cs方法进行校验。符合校验规则返回真实数据段，不符合返回空
					if(realData!=null){
						cache =offsetByteArray(bs, i+totalsize, lenOfbs-i-totalsize);
						if(cache!=null){
							SaveDataA(cache);
						}
						return  realData;
					}else{
						
						System.out.println("数据不能通过检验");
					}
				}
				else{
						   System.out.println("length不符合规则");
					}
			}
		}
		else{	
			System.out.println("字节数组长度不够，不是完整的数据");
			return null;
		 }
		SaveDataA(bs);
		return null;
		 
	}
	 private byte[] deal2(byte [] bs) { //为二维装的方法  
			byte [] cache;
			if(LoadDataB()!=null){    //如果存在缓存数据，就将缓存数据和本次获得的数据信息拼接到一起进行检验
				cache = LoadDataB();
				bs = linkByte(cache, bs);
			}
			byte[] realData = null;
			int lenOfbs = 0;
			int offset = 0;
			if(bs!=null){
				 lenOfbs = bs.length;	//传入数组的长度
			}
			String header; 
			if(lenOfbs>4){     		//如果传入字节数组长度不大于4，则不是一条完整的数据
				for(int i = 0;i<lenOfbs-4;i++){
					 header = MyFunc.Byte2Hex(bs[i])+MyFunc.Byte2Hex(bs[i+1]);
					if(header.equals("55AA")){//如果header不是55，则他不能做当前字节数组的头部
						byte[] testD = offsetByteArray(bs,i+2,lenOfbs-i-2);
						 offset = cs1(testD);
							if(offset!=-1){
								 realData = offsetByteArray(testD,0,offset);   // 将完整数据装入另一个字节数组
									if(realData!=null){
										cache = offsetByteArray(bs, offset+i+4, lenOfbs-(offset+4+i));
										if(cache!=null){
											SaveDataB(cache);
										}
										return  realData;  
								}	
							}                         
						}else{
							System.out.println("数据不能通过检验");
						}
					}
			}
			 SaveDataB(bs);
			return null;
			 
		}
	 
	 public   byte[] deal3(byte [] bs) { 
		 //为二维装的方法  
			byte [] cache;
		
			if(LoadDataC()!=null){    //如果存在缓存数据，就将缓存数据和本次获得的数据信息拼接到一起进行检验
				cache = LoadDataC();
				bs = linkByte(cache, bs);
			}
			
			String aimID = "";
			byte[] realData = null;
			int lenOfbs = 0;
			int offset = 0;
			
			if(bs!=null){
				 lenOfbs = bs.length;	//传入数组的长度
			}
			
			String header; 
			if(lenOfbs>4){     		//如果传入字节数组长度不大于4，则不是一条完整的数据
				for(int i = 0;i<lenOfbs-4;i++){
					 header =((char)bs[i])+""+((char)bs[i+1])+""+((char)bs[i+2])+"";
					 
					 aimID =MyFunc.deAIMId(header);
					 byte[] testD ={};
					if(!aimID.equals("未知类型")){//如果header不是aimID，则他不能做当前字节数组的头部
						Log.d("AIMID",aimID);
						testD = offsetByteArray(bs,i+3,lenOfbs-i-3);
						offset = cs2(testD);
							if(offset!=-1){
								 realData = offsetByteArray(testD,0,offset);
								 Log.d("test", MyFunc.ByteArrToHex(realData));
							}
						}                            // 将完整数据装入另一个字节数组
						
						if(realData!=null){
							cache =offsetByteArray(bs, i+offset+4, lenOfbs-i-offset-4);
							if(cache!=null){
								SaveDataC(cache);
							}
							
							return  bs;
						}else{
							System.out.println("数据不能通过检验");
						}
					}
			}
			SaveDataC(bs);
			return null;
			 
		} 
	 
	 public  byte[] de2(byte[] bs){
			byte [] cache;
			if(LoadDataB()!=null){    //如果存在缓存数据，就将缓存数据和本次获得的数据信息拼接到一起进行检验
				cache = LoadDataB();
				bs = linkByte(cache, bs);
			}
			
			byte[] realData = null;
			int lenOfbs = 0;
			if(bs!=null){
				 lenOfbs = bs.length;	//传入数组的长度
			}
			if(lenOfbs>2){
				for(int i =0; i<lenOfbs-1;i++){
				String header = MyFunc.Byte2Hex(bs[i])+MyFunc.Byte2Hex(bs[i+1]);
					if("0D0A".equals(header)){
						byte[] data = offsetByteArray(bs, 0, i);
						return data;
					}
				}
			}
			
			
			return null;
		}
	 
	private int cs1(byte[] bys){
		int size = bys.length;
			for(int i=0;i<size-1;i++){
				if((MyFunc.Byte2Hex(bys[i])).equals("AA")&&(MyFunc.Byte2Hex(bys[i+1])).equals("55")){
					return i;
					}
				}
				return -1;
		}
	
	 public static  int cs2(byte[] bys){
			int size = bys.length;
			
			for(int i=0;i<size;i++){
				if((((char)bys[i])+"").equals(""+((char)0x09))){
					return i;
				}
			}
			return -1;
		}
	 
	 

	 
	 private byte[] offsetByteArray(byte[] bys,int offset,int size){//偏移byte数组的方法，bys为要偏移的数组，offset为偏移量，size为偏移后想取数组的长度
		 if(size==0)return null;
		 byte [] realData = new byte[size];	//取出真正的数据部分
			for(int j = 0;j<size;j++){
				realData[j]=bys[offset+j];
			}
		 return realData;
	 }
}