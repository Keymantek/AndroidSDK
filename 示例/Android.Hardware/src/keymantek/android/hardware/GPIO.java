package keymantek.android.hardware;

/**
 * P41电源控制
 *
 * @author li
 *
 */
public final class GPIO
{
	/**
	 * TAG
	 */
	private static final String TAG = "GPIO";

	static
	{
		System.loadLibrary("dev");
	}

	/**
	 * 电源控制
	 *
	 * @param nGpioName
	 *            GPIO名字
	 * @param nlevel
	 *            1上电 0下电
	 */
	public native static void setGpio(int nGpioName, int nlevel);

	/**
	 * 电源控制
	 *
	 * @param nGpioName
	 *            GPIO名字
	 * @param bOpen
	 *            true 上电 false 下电
	 */
	public static void setGPIO(int nGpioName, boolean bOpen)
	{
		setGpio(nGpioName, bOpen ? 1 : 0);
	}

	private GPIO()
	{

	}
}
