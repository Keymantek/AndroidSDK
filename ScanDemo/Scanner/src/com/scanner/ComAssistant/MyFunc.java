package com.scanner.ComAssistant;

/**
 *数据转换工具
 */
public class MyFunc {
	//-------------------------------------------------------
	// 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num)
	{
		return num & 0x1;
	}
    //-------------------------------------------------------
    static public int HexToInt(String inHex)//Hex字符串转int
    {
    	return Integer.parseInt(inHex, 16);
    }
    //-------------------------------------------------------
    static public byte HexToByte(String inHex)//Hex字符串转byte
    {
    	return (byte)Integer.parseInt(inHex,16);
    }
    //-------------------------------------------------------
    static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
    	return String.format("%02x", inByte).toUpperCase();	
    }
    //-------------------------------------------------------
    static public int ByteToInt(byte b){  //byte转为int
		 return (int)(b&0xff);
	 }
	 
    //-------------------------------------------------------
    public static String Bytes2HexString(byte[] b) {
	    String ret = "";
	    for (int i = 0; i < b.length; i++) {
	      String hex = Integer.toHexString(b[i] & 0xFF);
	      if (hex.length() == 1) {
	        hex = "0" + hex;
	      }
	      ret += hex.toUpperCase();
	    }
	    return ret;
	  }
	static public String ByteArrToHex(byte[] inBytArr)//字节数组转转hex字符串
	{
		StringBuilder strBuilder=new StringBuilder();
		int j=inBytArr.length;
		for (int i = 0; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		return strBuilder.toString(); 
	}
  //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inBytArr,int offset,int byteCount)//字节数组转转hex字符串，可选长度
	{
    	StringBuilder strBuilder=new StringBuilder();
		int j=byteCount;
		for (int i = offset; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	//转hex字符串转字节数组
    static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
	{
		int hexlen = inHex.length();
		String tt = "";
		for(int i = 0; i < hexlen; i++){
			char c = inHex.charAt(i);
			if(c==' '){
				tt+="0";
			}else {
				tt+=c;
			}
		}
		inHex = tt;
		byte[] result;
		if (isOdd(hexlen)==1)
		{//奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		}else {//偶数
			result = new byte[(hexlen/2)];
		}
	    int j=0;
		for (int i = 0; i < hexlen; i+=2)
		{
			result[j]=HexToByte(inHex.substring(i,i+2));
			j++;
		}
	    return result; 
	}
    static public String deBCType(String bcType){    //判断码型根据BarCode码
		 String codeType = "不支持的码型";
		 
			if(bcType.equals("01")){
				codeType = "Code 39";
			}else if(bcType.equals("02")){
				codeType = "Codebar";
			}else if(bcType.equals("03")){
				codeType = "Code 128";
			}else if(bcType.equals("04")){
				codeType = "Discrete 2 of 5";
			}else if(bcType.equals("05")){
				codeType = "IATA 2 of 5";
			}else if(bcType.equals("06")){
				codeType = "Interleaved 2 of 5";
			}else if(bcType.equals("07")){
				codeType = "Code 93";
			}else if(bcType.equals("08")){
				codeType = "UPC A";
			}else if(bcType.equals("09")){
				codeType = "UPC E0";
			}else if(bcType.equals("0A")){
				codeType = "EAN 8";
			}else if(bcType.equals("C0")){
				codeType = "Code 11";
			}else if(bcType.equals("0B")){
				codeType = "EAN 13";
			}else if(bcType.equals("0E")){
				codeType = "MSI";
			}else if(bcType.equals("0F")){
				codeType = "EAN128";
			}else if(bcType.equals("10")){
				codeType = "UPC E1";
			}else if(bcType.equals("15")){
				codeType = "Trioptic Code 39";
			}else if(bcType.equals("16")){
				codeType = "Bookland EAN";
			}else if(bcType.equals("17")){
				codeType = "Coupon Code";
			}else if(bcType.equals("23")){
				codeType = "RSS-Limited";
			}else if(bcType.equals("24")){
				codeType = "RSS-14";
			}else if(bcType.equals("25")){
				codeType = "RSS-Expanded";
			}
		 return codeType;
	 }
	 
     static public String deAIMId(String aimid){//判断码型根据AIMID
		 String codeType = "未知类型";
		if(aimid.equals("]E0")||aimid.equals("]E3")){
			codeType = "EAN-13/UPC-E/UPC-A";
		}else if(aimid.equals("]E4")){
			codeType = "EAN-8";
		}else if(aimid.equals("]C0")){
			codeType = "Code 128";
		}else if(aimid.equals("]C1")){
			codeType = "GSI-128(UCC/EAN-128)";
		}else if(aimid.equals("]C2")){
			codeType = "AIM-128";
		}else if(aimid.equals("]C4")){
			codeType = "ISBT-128";
		}else if(aimid.equals("]I0")){
			codeType = "Interleaved 2 of 5";
		}else if(aimid.equals("]I1")||aimid.equals("]I3")){
			codeType = "Interleaved 2 of 5/ITF-6/ITF-14";
		}else if(aimid.equals("]S0")){
			codeType = "Industrial 2 of 5";
		}else if(aimid.equals("]R0")||aimid.equals("]R8")||aimid.equals("]R9")){
			codeType = "Standard 2 of 5";
		}else if(aimid.equals("]A0")||aimid.equals("]A1")
				||aimid.equals("]A3")||aimid.equals("]A4")||aimid.equals("]A5")||aimid.equals("]A7")){
			codeType = "Code 39";
		}else if(aimid.equals("]F0")||aimid.equals("]F2")||aimid.equals("]F4")){
			codeType = "Codebar";
		}else if(aimid.equals("]G0")){
			codeType = "Code 93";
		}else if(aimid.equals("]H0")||aimid.equals("]H1")||aimid.equals("]H3")||aimid.equals("]H9")){
			codeType = "Code 11";
		}else if(aimid.equals("]e0")){
			codeType = "GSI-DataBar";
		}else if(aimid.equals("]P0")){
			codeType = "Plessey";
		}else if(aimid.equals("]M0")||aimid.equals("]M1")||aimid.equals("]M8")||aimid.equals("]M9")){
			codeType = "MSI-Plessey";
		}else if(aimid.equals("]X0")||aimid.equals("]X1")||aimid.equals("]X2")||aimid.equals("]X3")){
			codeType = "Matrix 2 of 5";
		}else if(aimid.equals("]X4")){
			codeType = "ISBN";
		}else if(aimid.equals("]X5")){
			codeType = "ISSN";
		}else if(aimid.equals("]L0")){
			codeType = "PDF417";
		}else if(aimid.equals("]d0")||aimid.equals("]d1")||aimid.equals("]d2")||aimid.equals("]d3")
				||aimid.equals("]d4")||aimid.equals("]d5")||aimid.equals("]d6")){
			codeType = "Data Matrix";
		}else if(aimid.equals("]Q0")||aimid.equals("]Q1")||aimid.equals("]Q2")||aimid.equals("]Q3")
				||aimid.equals("]Q4")||aimid.equals("]Q5")||aimid.equals("]Q6")){
			codeType = "QR Code";
		}
		 return codeType; 
	 }
}