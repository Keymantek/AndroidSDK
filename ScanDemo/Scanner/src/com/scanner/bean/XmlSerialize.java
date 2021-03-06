package com.scanner.bean;


import android.util.Log;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public final class XmlSerialize
{
	private static final String defaultCharSetName = "UTF-8";

	/**
	 * @param data ä¼ é?è¿æ¥çbyeæ°ç»
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, Class<T> cls) throws IOException
	{
		return Deserialize(data, 0, data.length, cls);
	}

	/**
	 * @param charsetName ç¼ç å­ç¬¦ä¸?
	 * @param data ä¼ é?è¿æ¥çbyeæ°ç»
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, Class<T> cls, String charsetName) throws IOException
	{
		return Deserialize(data, 0, data.length, cls, charsetName);
	}

	/**
	 * @param /data ä¼ é?è¿æ¥çbyeæ°ç»
	 * @param /index æ°ç»ååºååçèµ·å§ä½ç½?
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, int offset, Class<T> cls) throws IOException
	{
		return Deserialize(data, offset, data.length - offset, cls);
	}

	/**
	 * @param /charsetName ç¼ç å­ç¬¦ä¸?
	 * @param /data ä¼ é?è¿æ¥çbyeæ°ç»
	 * @param /index æ°ç»ååºååçèµ·å§ä½ç½?
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, int offset, Class<T> cls, String charSetName) throws IOException
	{
		return Deserialize(data, offset, data.length - offset, cls, charSetName);
	}

	/**
	 * @param data  ä¼ é?è¿æ¥çbyeæ°ç»
	 * @param /index  æ°ç»ååºååçèµ·å§ä½ç½?
	 * @param /len é?è¦ååºååçæ°æ®é¿åº¦
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, int offset, int length, Class<T> cls) throws IOException
	{
		return Deserialize(data, offset, length, cls, defaultCharSetName);
	}

	/**
	 * @param /charsetName ç¼ç å­ç¬¦ä¸?
	 * @param data ä¼ é?è¿æ¥çbyeæ°ç»
	 * @param /index æ°ç»ååºååçèµ·å§ä½ç½?
	 * @param /len é¿åº¦
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static <T> T Deserialize(byte[] data, int offset, int length, Class<T> cls, String charSetName)
			throws IOException
	{
		return Deserialize(new ByteArrayInputStream(data, offset, length), cls, charSetName);
	}

	/**
	 * @param /inStream è¾å¥æµ?
	 * @param cls  é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(InputStream inputStream, Class<T> cls) throws IOException
	{
		Log.d("devinfo", "PMCCheckUtil-->Deserialize" );
		return Deserialize(inputStream, cls, defaultCharSetName);
	}


	/**
	 * @param inputStream è¾å¥æµ?
	 * @param cls  é?è¦ååºååçç±?
	 * @param charSetName ç¼ç æ ¼å¼å­ç¬¦ä¸?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(InputStream inputStream, Class<T> cls, String charSetName) throws IOException
	{
		Log.d("devinfo", "PMCCheckUtil-->Deserialize01" );
		XStream xstream = new XStream(new DomDriver(charSetName));
		// xstream.autodetectAnnotations(true);
		xstream.processAnnotations(cls);
		@SuppressWarnings("unchecked")
		T retobj = (T) (xstream.fromXML(inputStream));
		return retobj;
	}

	/**
	 * @param fileNameOrXmlStr  æä»¶åæxmlå­ç¬¦ä¸?
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(String fileNameOrXmlStr, Class<T> cls) throws IOException
	{
		File file = new File(fileNameOrXmlStr);
		if (file.exists())
		{
			return Deserialize(fileNameOrXmlStr, cls, defaultCharSetName);
		}
		else
		{
			XStream xstream = new XStream(new DomDriver());
			// xstream.autodetectAnnotations(true);
			xstream.processAnnotations(cls);
			@SuppressWarnings("unchecked")
			T retobj = (T) (xstream.fromXML(fileNameOrXmlStr));
			return retobj;
		}
	}

	/**
	 * @param filename  æä»¶å?
	 * @param charSetName ç¼ç æ ¼å¼å­ç¬¦ä¸?
	 * @param cls é?è¦ååºååçç±?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(String filename, Class<T> cls, String charSetName) throws IOException
	{
		FileInputStream fileInputStream = null;
		try
		{
			fileInputStream = new FileInputStream(new File(filename));
			return Deserialize(fileInputStream, cls, charSetName);
		}
		finally
		{
			if (fileInputStream != null)
			{
				fileInputStream.close();
			}
		}

	}

	/**
	 * @param /ms è¾åºæµ?
	 * @param obj é?è¦åºååçç±»
	 * @throws IOException
	 */
	public static void Serialize(OutputStream outputStream, Object obj) throws IOException
	{
		Serialize(outputStream, obj, defaultCharSetName);
	}

	/**
	 * @param /ms  è¾åºæµ?
	 * @param obj é?è¦åºååçç±»
	 * @param charSetName ç¼ç æ ¼å¼å­ç¬¦ä¸?
	 * @throws IOException
	 */
	public static void Serialize(OutputStream outputStream, Object obj, String charSetName) throws IOException
	{
		XStream xstream = new XStream(new DomDriver(charSetName));
		xstream.processAnnotations(obj.getClass());
		xstream.toXML(obj, outputStream);
	}

	/**
	 * @param filename æä»¶å?
	 * @param obj é?è¦åºååçç±»
	 * @throws IOException
	 */
	public static void Serialize(String filename, Object obj) throws IOException
	{
		Serialize(filename, obj, defaultCharSetName);
	}

	/**
	 * @param filename æä»¶å?
	 * @param obj é?è¦åºååçç±»
	 * @param charSetName ç¼ç æ ¼å¼å­ç¬¦ä¸?
	 * @throws IOException
	 */
	public static void Serialize(String filename, Object obj, String charSetName) throws IOException
	{
		File file = new File(filename);
		file.createNewFile();
		FileOutputStream fileOutputStream = null;
		try
		{
			fileOutputStream = new FileOutputStream(file);
			Serialize(fileOutputStream, obj, charSetName);
		}
		finally
		{
			if (fileOutputStream != null)
			{
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}
	}

	/**
	 * @param obj  é?è¦åºååçç±»
	 * @return
	 * @throws IOException
	 */
	public static byte[] SerializeToByte(Object obj) throws IOException
	{
		return SerializeToByte(obj, defaultCharSetName);
	}

	/**
	 * @param obj é?è¦åºååçç±»
	 * @param charSetName åºåååçç»æ?
	 * @return
	 * @throws IOException
	 */
	public static byte[] SerializeToByte(Object obj, String charSetName) throws IOException
	{
		return ((ByteArrayOutputStream) SerializeToStream(obj, charSetName)).toByteArray();
	}

	/**
	 * @param obj é?è¦åºååçç±»
	 * @return åºåååçç»æ?
	 * @throws IOException
	 */
	public static OutputStream SerializeToStream(Object obj) throws IOException
	{
		return SerializeToStream(obj, defaultCharSetName);
	}

	/**
	 * @param obj é?è¦åºååçç±»
	 * @param charsetName  ç¼ç æ ¼å¼çå­ç¬¦ä¸²
	 * @return åºåååçç»æ?
	 * @throws IOException
	 */
	public static OutputStream SerializeToStream(Object obj, String charsetName) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Serialize(byteArrayOutputStream, obj, charsetName);
		return byteArrayOutputStream;
	}

	/**
	 * @param /instance é?è¦åºååçç±»
	 * @return åºåååçç»æ?
	 */
	public static String SerializeToString(Object obj)
	{
		XStream xstream = new XStream(new DomDriver());
		// ç´æ¥ç¨jaxp domæ¥è§£é?
		// XStream xstream=new XStream(new DomDriver("utf-8"));
		// //æå®ç¼ç è§£æå?,ç´æ¥ç¨jaxp domæ¥è§£é?
		//// å¦ææ²¡æè¿å¥ï¼xmlä¸­çæ ¹åç´ ä¼æ?<å?.ç±»å>ï¼æèè¯´ï¼æ³¨è§£æ ¹æ¬å°±æ²¡çæï¼æ?ä»¥çåç´ åå°±æ¯ç±»çå±æ?
		xstream.processAnnotations(obj.getClass()); // éè¿æ³¨è§£æ¹å¼çï¼ä¸?å®è¦æè¿å¥è¯
		return xstream.toXML(obj);
	}
}

