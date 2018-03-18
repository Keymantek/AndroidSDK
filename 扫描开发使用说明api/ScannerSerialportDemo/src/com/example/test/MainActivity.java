package com.example.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cepri.dev.Scanner;
import com.mediatek.engineermode.io.EmGpio;

public class MainActivity extends Activity {	

	int type;
	TextView tv_recv;
	Scanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
		initData();
    }

	private void initData() {
		scanner = Scanner.getInstance();
	}

	private void initView() {		
		tv_recv = (TextView) findViewById(R.id.tv_recv);
	}	

	public void Scanner(View v) {
		//扫描
		scanner.powerOn();
		scanner.init();
		tv_recv.setText("");
		byte[] recv = new byte[100];
		new Thread() {
			public void run() {
				int len;
				byte[] data = new byte[100];
				len = scanner.decode(50, data, 0);
				if (len == -1) {
					Message msg = new Message();
					msg.obj = "未获取到数据";
					handler.sendMessage(msg);
					return;
				}
				String temp = null;
				try {
					temp = new String(data, 0, len, "UTF-8").substring(0, len - 2);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				Message msg = new Message();
				msg.obj = temp;
				handler.sendMessage(msg);
			};
		}.start();
	}

	public void close(View v) {
		scanner.powerOff();
		byte[] recv = new byte[100];	
		scanner.deInit();		
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		EmGpio.setGpioOutput(19);
		EmGpio.setGpioDataLow(19);
		EmGpio.gpioUnInit();
		byte[] recv = new byte[100];	
		scanner.deInit();
	}
	/**
	 * 把16进制字符串转换成字节数组
	 * 
	 * @param //hexString
	 * @return byte[]
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(Character.toUpperCase(c));
		return b;
	}

	/**
	 * byte数组转换成十六进制字符串
	 * 
	 * @param bArray
	 *            要转换的byte数组
	 * @param size
	 *            数组的大小
	 * @return
	 */
	public static final String bytesToHexString(byte[] bArray, int size) {
		StringBuffer sb = new StringBuffer(size);
		String sTemp;
		for (int i = 0; i < size; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
			sb.append(" ");
		}
		return sb.toString();
	}	

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(msg.obj.toString().equals("未获取到数据")){
				tv_recv.setText(tv_recv.getText().toString() + msg.obj.toString());
			}else{
				MediaPlayer mp = new MediaPlayer();
		        try
		        {
		            mp.setDataSource("/sdcard/5383.mp3");
		            mp.prepare();
		            mp.start();
		            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(40);
		        } catch (IOException e)
		        {
		            e.printStackTrace();
		        }
				tv_recv.setText(tv_recv.getText().toString() + msg.obj.toString());
			}
		};
	};
}
