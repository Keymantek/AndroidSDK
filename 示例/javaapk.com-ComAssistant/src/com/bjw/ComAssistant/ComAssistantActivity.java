package com.bjw.ComAssistant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.bjw.bean.AssistBean;
import com.bjw.bean.ComBean;
import com.bjw.bean.DataRecord;
import com.bjw.bean.until;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android_serialport_api.SerialPortFinder;
import keymantek.android.deviceinfo.DeviceInfo;
import keymantek.android.serialport.OnDataReceivedListener;
import keymantek.android.serialport.Parity;
import keymantek.android.serialport.SerialPort;
import keymantek.android.serialport.StopBits;

public class ComAssistantActivity extends Activity implements OnDataReceivedListener
{
	// ----------------------------------------------------刷新显示线程
	private class DispQueueThread extends Thread
	{
		private Queue<ComBean> QueueList = new LinkedList<ComBean>();

		public synchronized void AddQueue(ComBean ComData)
		{
			QueueList.add(ComData);
		}

		@Override
		public void run()
		{
			super.run();
			while (!isInterrupted())
			{
				final ComBean ComData;
				while ((ComData = QueueList.poll()) != null)
				{
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							DispRecData(ComData);
						}
					});
					try
					{
						Thread.sleep(100);// 显示性能高的话，可以把此数值调小。
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	/*
	// ----------------------------------------------------串口控制类
	private class SerialControl extends SerialHelper
	{

		// public SerialControl(String sPort, String sBaudRate){
		// super(sPort, sBaudRate);
		// }
		public SerialControl()
		{
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData)
		{
			// 数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
			// 直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
			// 用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
			// 最终效果差不多-_-，线程定时刷新稍好一些。
			DispQueue.AddQueue(ComRecData);// 线程定时刷新显示(推荐)

			runOnUiThread(new Runnable()// 直接刷新显示
			{
				@Override
				public void run()
				{
					DispRecData(ComRecData);
				}
			});
		}
	}
	*/

	// ----------------------------------------------------清除按钮、发送按钮
	class ButtonClickEvent implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (v == ButtonClear)
			{
				iRecLines = 0;
				editTextLines.setText("");
				editTextRecDisp.setText("");
			}
			else if (v == ButtonSendCOMA)
			{
				// sendPortData(ComA, editTextCOMA.getText().toString());
				sendPortData(mySerialport, editTextCOMA.getText().toString());
			}
			else if (v == btn_scan)
			{
				String code = mylScanne();
				editTextRecDisp.setText(code);
			}
		}
	}

	// ----------------------------------------------------自动发送
	class CheckBoxChangeEvent implements CheckBox.OnCheckedChangeListener
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView == checkBoxAutoCOMA)
			{
				if (!toggleButtonCOMA.isChecked() && isChecked)
				{
					buttonView.setChecked(false);
					return;
				}
				//SetLoopData(ComA, editTextCOMA.getText().toString());
				//SetAutoSend(ComA, isChecked);
				sendPortData(mySerialport, editTextCOMA.getText().toString());
			}
		}
	}

	// ----------------------------------------------------编辑框完成事件
	class EditorActionEvent implements EditText.OnEditorActionListener
	{
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
		{
			if (v == editTextCOMA)
			{
				setSendData(editTextCOMA);
			}
			return false;
		}
	}

	// ----------------------------------------------------编辑框焦点转移事件
	class FocusChangeEvent implements EditText.OnFocusChangeListener
	{
		@Override
		public void onFocusChange(View v, boolean hasFocus)
		{
			if (v == editTextCOMA)
			{
				setSendData(editTextCOMA);
			}
		}
	}

	// ----------------------------------------------------串口号或波特率变化时，关闭打开的串口
	class ItemSelectedEvent implements Spinner.OnItemSelectedListener
	{
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			if ((arg0 == SpinnerBaudRateCOMA) || (arg0 == SpinnerDate) || (arg0 == SpinnerStop)
					|| (arg0 == SpinnerParity))
			{
				// CloseComPort(ComA);
				if ((mySerialport != null) && mySerialport.getIsOpen())
				{
					mySerialport.Close();
				}

				checkBoxAutoCOMA.setChecked(false);
				toggleButtonCOMA.setChecked(false);
			}
			else if (arg0 == SpinnerCOMA)
			{
				if (SpinnerCOMA.getSelectedItem().toString().equals("Scan"))
				{

					if ((mySerialport != null) && mySerialport.getIsOpen())
					{
						mySerialport.Close();

					}
					dInfo.IR_Power(false);
					dInfo.RS232_Power(false);
					dInfo.RS485_Power(false);
					dInfo.Scan_Power(true);
					checkBoxAutoCOMA.setChecked(false);
					toggleButtonCOMA.setChecked(false);
					btn_scan.setEnabled(true);
				}
				else
				{
					btn_scan.setEnabled(false);
				}
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
		}

	}

	// ----------------------------------------------------Txt、Hex模式选择
	class radioButtonClickEvent implements RadioButton.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (v == radioButtonTxt)
			{
				KeyListener TxtkeyListener = new TextKeyListener(Capitalize.NONE, false);
				editTextCOMA.setKeyListener(TxtkeyListener);

				AssistData.setTxtMode(true);
			}
			else if (v == radioButtonHex)
			{
				KeyListener HexkeyListener = new NumberKeyListener()
				{
					@Override
					public int getInputType()
					{
						return InputType.TYPE_CLASS_TEXT;
					}

					@Override
					protected char[] getAcceptedChars()
					{
						return new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
								'f', 'A', 'B', 'C', 'D', 'E', 'F' };
					}
				};

				editTextCOMA.setKeyListener(HexkeyListener);

				AssistData.setTxtMode(false);
			}

			editTextCOMA.setText(dataRecord.getSendData().toString());
			setSendData(editTextCOMA);

		}
	}

	// ----------------------------------------------------打开关闭串口
	class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView == toggleButtonCOMA)
			{
				if (isChecked)
				{
					editTextCOMA.setText("68AAAAAAAAAAAA681300DF16");
					// ComA=new SerialControl("/dev/s3c2410_serial0", "9600");
					// ComA.setData(SpinnerDate.getSelectedItem().toString());

					// ComA.setStop(SpinnerStop.getSelectedItem().toString());
					// ComA.setParity(SpinnerParity.getSelectedItem().toString());
					// ComA.setPort(SpinnerCOMA.getSelectedItem().toString());
					// ComA.setBaudRate(SpinnerBaudRateCOMA.getSelectedItem().toString());
					//Log.d("test", "ComA=" + ComA.getBaudRate() + ComA.getData() + ComA.getStop() + ComA.getParity());

					// add by wt
					try
					{

						if (SpinnerCOMA.getSelectedItem().toString().equals("RS232"))
						{
							//dInfo.IR_Power(false);
							
							//dInfo.Scan_Power(false);
							//dInfo.RS232_Power(true);
							dInfo.RS485_Power(true);
							mySerialport.setPortName(dInfo.RS485PortName());
						}
						else if (SpinnerCOMA.getSelectedItem().toString().equals("Scan"))
						{
							Toast.makeText(ComAssistantActivity.this, "选择的是扫描无需打开串口 ", Toast.LENGTH_SHORT).show();
						}
						mySerialport.setDataBits(Integer.parseInt(SpinnerDate.getSelectedItem().toString()));
						if (SpinnerStop.getSelectedItem().toString().equals("1"))
						{
							mySerialport.setStopBits(StopBits.One);
						}
						else if (SpinnerStop.getSelectedItem().toString().equals("2"))
						{
							mySerialport.setStopBits(StopBits.Two);
						}
						if (SpinnerParity.getSelectedItem().toString().equals("N"))
						{
							mySerialport.setParity(Parity.None);
						}
						else if (SpinnerParity.getSelectedItem().toString().equals("O"))
						{
							mySerialport.setParity(Parity.Odd);
						}
						else if (SpinnerParity.getSelectedItem().toString().equals("E"))
						{
							mySerialport.setParity(Parity.Even);
						}

						mySerialport.setBaudRate(Integer.parseInt(SpinnerBaudRateCOMA.getSelectedItem().toString()));

					}
					catch (NumberFormatException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					try
					{
						File file = new File("sdcard/test.xml");
						if (!file.exists())
						{// 如果没有该文件
							file.createNewFile();
						}
						else
						{
							// file.delete();
							// file.createNewFile();
						}
					}
					catch (IOException e)
					{

					}
					// 序列化写入（本地保存状态）
					DataRecord dataRecordInfo = new DataRecord();
					dataRecordInfo.setSerialPort(SpinnerCOMA.getSelectedItem().toString());
					dataRecordInfo.setBaudrate(Integer.parseInt(SpinnerBaudRateCOMA.getSelectedItem().toString()));
					dataRecordInfo.setDataBits(Integer.parseInt(SpinnerDate.getSelectedItem().toString()));
					dataRecordInfo.setParity(until.setParity(SpinnerParity.getSelectedItem().toString()));
					dataRecordInfo.setStop(Integer.parseInt(SpinnerStop.getSelectedItem().toString()));
					dataRecordInfo.setSendData(editTextCOMA.getText().toString());
					try
					{
						until.serializer(dataRecordInfo);
						mySerialport.Open();
						dInfo.RS485_SendEnable(true);
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// OpenComPort(ComA);

				}
				else
				{
					if ((mySerialport != null) && mySerialport.getIsOpen())
					{
						mySerialport.Close();

					}
					// CloseComPort(ComA);
					checkBoxAutoCOMA.setChecked(false);
				}
			}
		}
	}

	private DataRecord dataRecord = new DataRecord();
	private DeviceInfo dInfo;
	private SerialPort mySerialport;
	private File path = Environment.getExternalStorageDirectory();
	AssistBean AssistData;// 用于界面数据序列化和反序列化
	Button ButtonClear, ButtonSendCOMA, btn_scan;
	CheckBox checkBoxAutoClear, checkBoxAutoCOMA;
	//SerialControl ComA;

	DispQueueThread DispQueue;// 刷新显示线程
	EditText editTextRecDisp, editTextLines, editTextCOMA;
	EditText editTextTimeCOMA;

	int iRecLines = 0;// 接收区行数
	SerialPortFinder mSerialPortFinder;// 串口设备搜索
	RadioButton radioButtonTxt, radioButtonHex;
	Spinner SpinnerBaudRateCOMA, SpinnerDate, SpinnerStop, SpinnerParity;
	Spinner SpinnerCOMA;
	ToggleButton toggleButtonCOMA;

	public String mylScanne()
	{

		if (!dInfo.HaveScan())
		{
			return "";
		}

		String code = "未扫描到数据";
		try
		{

			dInfo.Scan_Power(true);
			code = dInfo.Scan_Code();
			return code;

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			dInfo.ScanClose();
		}
		return code;

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		//CloseComPort(ComA);
		if (mySerialport != null && mySerialport.getIsOpen())
		{
			mySerialport.Close();
		}
		setContentView(R.layout.main);
		setControls();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// add by wt
		dInfo = DeviceInfo.CreateInstance();
		try
		{
			mySerialport = SerialPort.getInstance();
			mySerialport.OnDataReceived = this;

		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
		// 判断是否有记录文件，没有则创建
		File file = new File("sdcard/test.xml");
		if (!file.exists())
		{// 如果有该文件
			try
			{
				file.createNewFile();
				dataRecord.setSerialPort("/dev/ttyMT3");
				dataRecord.setBaudrate(9600);
				dataRecord.setDataBits(8);
				dataRecord.setParity('N');
				dataRecord.setStop(1);
				dataRecord.setSendData("68AAAAAAAAAAAA681300DF16");
				// dataRecord.setTxt(true)
				until.serializer(dataRecord);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				dataRecord = until.parser();
				Log.d("dataRecord", dataRecord.toString());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("dataRecord", "err=" + e.getMessage());
			}
		}
		//ComA = new SerialControl();
		DispQueue = new DispQueueThread();
		DispQueue.start();
		AssistData = getAssistData();
		setControls();
	}

	@Override
	public void OnDataReceived(Object data)
	{
		int cnt = 0;
		try
		{
			Thread.sleep(500);
			cnt = mySerialport.getBytesToRead();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final byte[] buffer = new byte[cnt];
		int size = 0;
		try
		{
			size = mySerialport.Read(buffer, 0, cnt);
			Log.i("wt", "" + size);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		if (size > 0)
		{
			runOnUiThread(new Runnable()// 直接刷新显示
			{
				@Override
				public void run()
				{
					editTextRecDisp.setText(MyFunc.Bytes2HexString(buffer));
				}
			});
		}

	}

	@Override
	public void onDestroy()
	{
		saveAssistData(AssistData);
		//CloseComPort(ComA);
		super.onDestroy();
		if (mySerialport != null && mySerialport.getIsOpen())
		{
			mySerialport.Close();
		}
	}

	/*
	// ----------------------------------------------------关闭串口
	private void CloseComPort(SerialHelper ComPort)
	{
		if (ComPort != null)
		{
			ComPort.stopSend();
			ComPort.close();
		}
	}
	*/

	// ----------------------------------------------------刷新界面数据
	private void DispAssistData(AssistBean AssistData)
	{
		editTextCOMA.setText(dataRecord.getSendData().toString());// AssistData.getSendA()

		setSendData(editTextCOMA);

		if (AssistData.isTxt())
		{
			radioButtonTxt.setChecked(true);
		}
		else
		{
			radioButtonHex.setChecked(true);
		}
		editTextTimeCOMA.setText(AssistData.sTimeA);

		//setDelayTime(editTextTimeCOMA);
		
	}

	// ----------------------------------------------------显示接收数据
	private void DispRecData(ComBean ComRecData)
	{
		StringBuilder sMsg = new StringBuilder();
		// sMsg.append(ComRecData.sRecTime);
		// sMsg.append("[");
		// sMsg.append(ComRecData.sComPort);
		// sMsg.append("]");
		if (radioButtonTxt.isChecked())
		{
			// sMsg.append("[Txt] ");
			sMsg.append(new String(ComRecData.bRec));
		}
		else if (radioButtonHex.isChecked())
		{
			// sMsg.append("[Hex] ");
			sMsg.append(MyFunc.Bytes2HexString(ComRecData.bRec));
			Log.d("test", "receData=" + sMsg);
		}
		// sMsg.append("\r\n");
		sMsg.append("\r");// sMsg.append(" ");
		editTextRecDisp.append(sMsg);
		iRecLines++;
		editTextLines.setText(String.valueOf(iRecLines));
		if ((iRecLines > 500) && (checkBoxAutoClear.isChecked()))// 达到500项自动清除
		{
			editTextRecDisp.setText("");
			editTextLines.setText("0");
			iRecLines = 0;
		}
	}

	// ----------------------------------------------------
	private AssistBean getAssistData()
	{
		SharedPreferences msharedPreferences = getSharedPreferences("ComAssistant", Context.MODE_PRIVATE);
		AssistBean AssistData = new AssistBean();
		try
		{
			String personBase64 = msharedPreferences.getString("AssistData", "");
			byte[] base64Bytes = Base64.decode(personBase64.getBytes(), 0);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			AssistData = (AssistBean) ois.readObject();
			return AssistData;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return AssistData;
	}

	/*
	// ----------------------------------------------------打开串口
	private void OpenComPort(SerialHelper ComPort)
	{
		try
		{
			ComPort.open();
		}
		catch (SecurityException e)
		{
			ShowMessage("打开串口失败:没有串口读/写权限!");
		}
		catch (IOException e)
		{
			ShowMessage("打开串口失败:未知错误!");
		}
		catch (InvalidParameterException e)
		{
			ShowMessage("打开串口失败:参数错误!");
		}
	}
	*/

	// ----------------------------------------------------保存、获取界面数据
	private void saveAssistData(AssistBean AssistData)
	{
		AssistData.sTimeA = editTextTimeCOMA.getText().toString();

		SharedPreferences msharedPreferences = getSharedPreferences("ComAssistant", Context.MODE_PRIVATE);
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(AssistData);
			String sBase64 = new String(Base64.encode(baos.toByteArray(), 0));
			SharedPreferences.Editor editor = msharedPreferences.edit();
			editor.putString("AssistData", sBase64);
			editor.commit();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	// ----------------------------------------------------串口发送
	private void sendPortData(SerialHelper ComPort, String sOut)
	{
		if ((ComPort != null) && ComPort.isOpen())
		{
			if (radioButtonTxt.isChecked())
			{
				ComPort.sendTxt(sOut);
			}
			else if (radioButtonHex.isChecked())
			{
				ComPort.sendHex(sOut);
			}
		}
	}
	*/

	// add by wt
	private void sendPortData(SerialPort serialport, String sOut)
	{
		if ((serialport != null) && serialport.getIsOpen())
		{
			try
			{
				dInfo.RS485_SendEnable(true);
				if (radioButtonTxt.isChecked())
				{
					serialport.Write(sOut.getBytes());
				}
				else if (radioButtonHex.isChecked())
				{
					serialport.Write(MyFunc.HexToByteArr(sOut));
				}
				Thread.sleep(100);
				dInfo.RS485_SendEnable(false);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/*
	// ----------------------------------------------------设置自动发送模式开关
	private void SetAutoSend(SerialHelper ComPort, boolean isAutoSend)
	{
		if (isAutoSend)
		{
			ComPort.startSend();
		}
		else
		{
			ComPort.stopSend();
		}
	}
	*/

	// ----------------------------------------------------
	private void setControls()
	{
		String appName = getString(R.string.app_name);
		try
		{
			PackageInfo pinfo = getPackageManager().getPackageInfo("com.bjw.ComAssistant",
					PackageManager.GET_CONFIGURATIONS);
			String versionName = pinfo.versionName;
			// String versionCode = String.valueOf(pinfo.versionCode);
			setTitle(appName + " V" + versionName);
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		SpinnerDate = (Spinner) findViewById(R.id.SpinnerData);
		SpinnerStop = (Spinner) findViewById(R.id.SpinnerStop);
		SpinnerParity = (Spinner) findViewById(R.id.SpinnerCheck);

		ArrayAdapter<CharSequence> adapterData = ArrayAdapter.createFromResource(this, R.array.data_name,
				android.R.layout.simple_spinner_item);
		adapterData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerDate.setAdapter(adapterData);
		int select = 0;
		for (int i = 0; i < adapterData.getCount(); i++)
		{
			if (Integer.toString(dataRecord.getDataBits()).equals(adapterData.getItem(i)))
			{
				select = i;
				break;
			}
		}
		SpinnerDate.setSelection(select);

		ArrayAdapter<CharSequence> adapterStop = ArrayAdapter.createFromResource(this, R.array.stop_name,
				android.R.layout.simple_spinner_item);
		adapterStop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerStop.setAdapter(adapterStop);
		int select1 = 0;
		for (int i = 0; i < adapterStop.getCount(); i++)
		{
			if (Integer.toString(dataRecord.getStop()).equals(adapterStop.getItem(i)))
			{
				select1 = i;
				break;
			}
		}
		SpinnerStop.setSelection(select1);

		ArrayAdapter<CharSequence> adapterParity = ArrayAdapter.createFromResource(this, R.array.parity_name,
				android.R.layout.simple_spinner_item);
		adapterParity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerParity.setAdapter(adapterParity);
		int select2 = 0;
		for (int i = 0; i < adapterParity.getCount(); i++)
		{
			if (Integer.toString(dataRecord.getParity())
					.equals(Integer.toString(until.setParity(adapterParity.getItem(i).toString()))))
			{
				select2 = i;
				break;
			}
		}
		SpinnerParity.setSelection(select2);

		editTextRecDisp = (EditText) findViewById(R.id.editTextRecDisp);
		editTextLines = (EditText) findViewById(R.id.editTextLines);
		editTextCOMA = (EditText) findViewById(R.id.editTextCOMA);
		editTextTimeCOMA = (EditText) findViewById(R.id.editTextTimeCOMA);

		checkBoxAutoClear = (CheckBox) findViewById(R.id.checkBoxAutoClear);
		checkBoxAutoCOMA = (CheckBox) findViewById(R.id.checkBoxAutoCOMA);
		ButtonClear = (Button) findViewById(R.id.ButtonClear);
		ButtonSendCOMA = (Button) findViewById(R.id.ButtonSendCOMA);
		btn_scan = (Button) findViewById(R.id.btn_scan);
		Log.i("wt", "" + (btn_scan == null));
		toggleButtonCOMA = (ToggleButton) findViewById(R.id.toggleButtonCOMA);
		SpinnerCOMA = (Spinner) findViewById(R.id.SpinnerCOMA);
		SpinnerBaudRateCOMA = (Spinner) findViewById(R.id.SpinnerBaudRateCOMA);
		radioButtonTxt = (RadioButton) findViewById(R.id.radioButtonTxt);
		radioButtonHex = (RadioButton) findViewById(R.id.radioButtonHex);
		editTextCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextTimeCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextCOMA.setOnFocusChangeListener(new FocusChangeEvent());
		editTextTimeCOMA.setOnFocusChangeListener(new FocusChangeEvent());
		radioButtonTxt.setOnClickListener(new radioButtonClickEvent());
		radioButtonHex.setOnClickListener(new radioButtonClickEvent());
		ButtonClear.setOnClickListener(new ButtonClickEvent());
		ButtonSendCOMA.setOnClickListener(new ButtonClickEvent());

		btn_scan.setOnClickListener(new ButtonClickEvent());

		toggleButtonCOMA.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());

		checkBoxAutoCOMA.setOnCheckedChangeListener(new CheckBoxChangeEvent());

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.baudrates_value,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerBaudRateCOMA.setAdapter(adapter);
		// SpinnerBaudRateCOMA.setSelection(12);
		int select3 = 0;
		for (int i = 0; i < adapter.getCount(); i++)
		{
			if (Integer.toString(dataRecord.getBaudrate()).equals(adapter.getItem(i)))
			{
				select3 = i;
				break;
			}
		}
		SpinnerBaudRateCOMA.setSelection(select3);

		mSerialPortFinder = new SerialPortFinder();
		String[] entryValues = mSerialPortFinder.getAllDevicesPath();
		List<String> allDevices = new ArrayList<String>();
		for (int i = 0; i < entryValues.length; i++)
		{
			allDevices.add(entryValues[i]);
		}
		// change by wt TODO
		ArrayAdapter<CharSequence> adapter_com = ArrayAdapter.createFromResource(this, R.array.port_name,
				android.R.layout.simple_spinner_item);
		adapter_com.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerCOMA.setAdapter(adapter_com);
		SpinnerCOMA.setSelection(0);
		Log.d("dataRecord", "SpinnerCOMA-entryValues[0]=" + entryValues[0]);
		// if (allDevices.size()>0)
		// {
		// int select4=0;
		// for(int i=0;i<entryValues.length;i++){
		// if(dataRecord.getSerialPort().contains(entryValues[i])){
		// select4=i;
		// break;
		// }
		// }
		// }

		SpinnerDate.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerStop.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerParity.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerBaudRateCOMA.setOnItemSelectedListener(new ItemSelectedEvent());

		DispAssistData(AssistData);
	}

	/*
	// ----------------------------------------------------设置自动发送延时
	private void setDelayTime(TextView v)
	{
		if (v == editTextTimeCOMA)
		{
			AssistData.sTimeA = v.getText().toString();
			SetiDelayTime(ComA, v.getText().toString());
		}
	}

	// ----------------------------------------------------设置自动发送延时
	private void SetiDelayTime(SerialHelper ComPort, String sTime)
	{
		ComPort.setiDelay(Integer.parseInt(sTime));
	}
	*/
	
	/*
	// ----------------------------------------------------设置自动发送数据
	private void SetLoopData(SerialHelper ComPort, String sLoopData)
	{
		if (radioButtonTxt.isChecked())
		{
			ComPort.setTxtLoopData(sLoopData);
		}
		else if (radioButtonHex.isChecked())
		{
			ComPort.setHexLoopData(sLoopData);
		}
	}
	*/

	// add by wt

	// add by wt
	// TODO scan

	
	// ----------------------------------------------------设置自动发送数据
	private void setSendData(TextView v)
	{
		if (v == editTextCOMA)
		{
			AssistData.setSendA(v.getText().toString());
			//SetLoopData(ComA, v.getText().toString());
			sendPortData(mySerialport, v.getText().toString());
		}
	}
	

	// ------------------------------------------显示消息
	private void ShowMessage(String sMsg)
	{
		Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
	}

}