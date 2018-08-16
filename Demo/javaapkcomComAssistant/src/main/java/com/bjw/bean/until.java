package com.bjw.bean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;

public class until {
	private static DataRecord dataRecord;
	private static File path=Environment.getExternalStorageDirectory();
	
	/**
	 * 反序列化输出
	 * @return
	 * @throws Exception
	 */
    public static DataRecord parser() throws Exception
    {
        InputStream inStream = new FileInputStream(new File(path, "test.xml"));
        //反序列化
        dataRecord=XmlSerialize.Deserialize(inStream, DataRecord.class);
        Log.d("dataRecord", "DevInfoUtil"+dataRecord.toString() );
        inStream.close();
        return dataRecord;
    }
    
    public static void serializer(DataRecord dataRecord) throws IOException
    {
        ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
        try{
            OutputStream outputStream=new FileOutputStream(new File("sdcard/test.xml"));
            XmlSerialize.Serialize(outputstream,dataRecord);
            Log.d("dataRecord", "序列化="+outputstream.toString() );
            outputStream.write(outputstream.toString().getBytes());
            outputstream.flush();
            outputStream.close();
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.d("dataRecord", "序列化错误="+e.getMessage() );
        }
    }
    
    public static int setParity(String sParity){
		int iParity = 'N';
		if(sParity.contains("N")){
			iParity = 'N';
		}else if(sParity.contains("O")){
			iParity = 'O';
		}else if(sParity.contains("E")){
			iParity = 'E';
		}
		return iParity;
	}
}
