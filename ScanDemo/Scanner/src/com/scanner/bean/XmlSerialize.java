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
	 * @param data 传�?�进来的bye数组
	 * @param cls �?要反序列化的�?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, Class<T> cls) throws IOException
	{
		return Deserialize(data, 0, data.length, cls);
	}

	/**
	 * @param charsetName 编码字符�?
	 * @param data 传�?�进来的bye数组
	 * @param cls �?要反序列化的�?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, Class<T> cls, String charsetName) throws IOException
	{
		return Deserialize(data, 0, data.length, cls, charsetName);
	}

	/**
	 * @param /data 传�?�进来的bye数组
	 * @param /index 数组反序列化的起始位�?
	 * @param cls �?要反序列化的�?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, int offset, Class<T> cls) throws IOException
	{
		return Deserialize(data, offset, data.length - offset, cls);
	}

	/**
	 * @param /charsetName 编码字符�?
	 * @param /data 传�?�进来的bye数组
	 * @param /index 数组反序列化的起始位�?
	 * @param cls �?要反序列化的�?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, int offset, Class<T> cls, String charSetName) throws IOException
	{
		return Deserialize(data, offset, data.length - offset, cls, charSetName);
	}

	/**
	 * @param data  传�?�进来的bye数组
	 * @param /index  数组反序列化的起始位�?
	 * @param /len �?要反序列化的数据长度
	 * @param cls �?要反序列化的�?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(byte[] data, int offset, int length, Class<T> cls) throws IOException
	{
		return Deserialize(data, offset, length, cls, defaultCharSetName);
	}

	/**
	 * @param /charsetName 编码字符�?
	 * @param data 传�?�进来的bye数组
	 * @param /index 数组反序列化的起始位�?
	 * @param /len 长度
	 * @param cls �?要反序列化的�?
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
	 * @param /inStream 输入�?
	 * @param cls  �?要反序列化的�?
	 * @return
	 * @throws IOException
	 */
	public static <T> T Deserialize(InputStream inputStream, Class<T> cls) throws IOException
	{
		Log.d("devinfo", "PMCCheckUtil-->Deserialize" );
		return Deserialize(inputStream, cls, defaultCharSetName);
	}


	/**
	 * @param inputStream 输入�?
	 * @param cls  �?要反序列化的�?
	 * @param charSetName 编码格式字符�?
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
	 * @param fileNameOrXmlStr  文件名或xml字符�?
	 * @param cls �?要反序列化的�?
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
	 * @param filename  文件�?
	 * @param charSetName 编码格式字符�?
	 * @param cls �?要反序列化的�?
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
	 * @param /ms 输出�?
	 * @param obj �?要序列化的类
	 * @throws IOException
	 */
	public static void Serialize(OutputStream outputStream, Object obj) throws IOException
	{
		Serialize(outputStream, obj, defaultCharSetName);
	}

	/**
	 * @param /ms  输出�?
	 * @param obj �?要序列化的类
	 * @param charSetName 编码格式字符�?
	 * @throws IOException
	 */
	public static void Serialize(OutputStream outputStream, Object obj, String charSetName) throws IOException
	{
		XStream xstream = new XStream(new DomDriver(charSetName));
		xstream.processAnnotations(obj.getClass());
		xstream.toXML(obj, outputStream);
	}

	/**
	 * @param filename 文件�?
	 * @param obj �?要序列化的类
	 * @throws IOException
	 */
	public static void Serialize(String filename, Object obj) throws IOException
	{
		Serialize(filename, obj, defaultCharSetName);
	}

	/**
	 * @param filename 文件�?
	 * @param obj �?要序列化的类
	 * @param charSetName 编码格式字符�?
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
	 * @param obj  �?要序列化的类
	 * @return
	 * @throws IOException
	 */
	public static byte[] SerializeToByte(Object obj) throws IOException
	{
		return SerializeToByte(obj, defaultCharSetName);
	}

	/**
	 * @param obj �?要序列化的类
	 * @param charSetName 序列化后的结�?
	 * @return
	 * @throws IOException
	 */
	public static byte[] SerializeToByte(Object obj, String charSetName) throws IOException
	{
		return ((ByteArrayOutputStream) SerializeToStream(obj, charSetName)).toByteArray();
	}

	/**
	 * @param obj �?要序列化的类
	 * @return 序列化后的结�?
	 * @throws IOException
	 */
	public static OutputStream SerializeToStream(Object obj) throws IOException
	{
		return SerializeToStream(obj, defaultCharSetName);
	}

	/**
	 * @param obj �?要序列化的类
	 * @param charsetName  编码格式的字符串
	 * @return 序列化后的结�?
	 * @throws IOException
	 */
	public static OutputStream SerializeToStream(Object obj, String charsetName) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Serialize(byteArrayOutputStream, obj, charsetName);
		return byteArrayOutputStream;
	}

	/**
	 * @param /instance �?要序列化的类
	 * @return 序列化后的结�?
	 */
	public static String SerializeToString(Object obj)
	{
		XStream xstream = new XStream(new DomDriver());
		// 直接用jaxp dom来解�?
		// XStream xstream=new XStream(new DomDriver("utf-8"));
		// //指定编码解析�?,直接用jaxp dom来解�?
		//// 如果没有这句，xml中的根元素会�?<�?.类名>；或者说：注解根本就没生效，�?以的元素名就是类的属�?
		xstream.processAnnotations(obj.getClass()); // 通过注解方式的，�?定要有这句话
		return xstream.toXML(obj);
	}
}

