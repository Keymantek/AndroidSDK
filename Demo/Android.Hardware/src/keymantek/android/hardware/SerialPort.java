package keymantek.android.hardware;

import java.io.FileDescriptor;

/**
 * @author li
 *
 */
public final class SerialPort
{
	// 加载本地.so类库
	static
	{
		System.loadLibrary("serialport");
	}

	/**
	 * JNI关闭串口操作
	 */
	public native static void close();

	/**
	 * JNI配置串口操作
	 * 
	 * @param baudrate
	 *            波特率
	 * @param nBits
	 *            数据位
	 * @param parity
	 *            奇偶校验
	 * @param nStop
	 *            停止位
	 * @return 配置结果
	 */
	public native static int config(int baudrate, int nBits, int parity, int nStop);

	/**
	 * JNI打开串口操作
	 * 
	 * @param port
	 *            串口
	 * @param baudrate
	 *            波特率
	 * @param nBits
	 *            数据位
	 * @param parity
	 *            奇偶校验
	 * @param nStop
	 *            停止位
	 * @return 串口句柄
	 */
	public native static FileDescriptor open(int port, int baudrate, int nBits, int parity, int nStop);

	private SerialPort()
	{

	}
}
