package com.scanner.bean;


public class DataRecord {
	
	private String serialPort; //����
	private int dataBits; //����λ 
	private int stop; //ֹͣ
	private int parity;//У��
	private int baudrate;//������
	private String sendData;//���͵�����
	private String txtOrHex;//�Ƿ�ΪTXT   true-txt  false--hex
	
	
	public String getTxtOrHex() {
		return txtOrHex;
	}
	public void setTxtOrHex(String txtOrHex) {
		this.txtOrHex = txtOrHex;
	}
	public String getSendData() {
		return sendData;
	}
	public void setSendData(String sendData) {
		this.sendData = sendData;
	}
	public String getSerialPort() {
		return serialPort;
	}
	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}
	public int getDataBits() {
		return dataBits;
	}
	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}
	public int getStop() {
		return stop;
	}
	public void setStop(int stop) {
		this.stop = stop;
	}
	public int getParity() {
		return parity;
	}
	public void setParity(int parity) {
		this.parity = parity;
	}
	public int getBaudrate() {
		return baudrate;
	}
	public void setBaudrate(int baudrate) {
		this.baudrate = baudrate;
	}
	@Override
	public String toString() {
		return "DataRecord [serialPort=" + serialPort + ", dataBits="
				+ dataBits + ", stop=" + stop + ", parity=" + parity
				+ ", baudrate=" + baudrate + ", sendData=" + sendData
				+ ", txtOrHex=" + txtOrHex + "]";
	}
	
}
