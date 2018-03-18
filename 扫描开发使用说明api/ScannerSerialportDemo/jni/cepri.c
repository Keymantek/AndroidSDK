#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "cepri.h"

static const char *TAG = "TTT";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#define IOCTRL_PMU_EXTGPS_SW_ON   0x1000F
#define IOCTRL_PMU_EXTGPS_SW_OFF  0x10010
#define IOCTRL_GPIO_ON_OFF  0x10022
int g_hSri = -1;
int fd = -1;
int waitTime = 150;
struct Data {
	unsigned int name;   //gpio号
	unsigned int value;  //gpio值
};

int LaserIRDA=0;
int IRDA=1;
//int SecurityUnit=2;
int Scanner=2;
int IdCard=3;//身份证
int RESAM=4;
int Serialport=5;
int RFID=6;


int version=0001; //版本号
int company=07;	//厂家编号

struct termios options;
/**
 * 设置串口参数
 * databits 数据位
 * stopbits 停止位
 * parity 校验位
 */
int set_Parity(int fd, int databits, int stopbits, int parity) {
	LOGI("fd:%d databits:%d parity:%d stopbits:%d \r\n", fd, databits, parity,
			stopbits);
	if (tcgetattr(fd, &options) != 0) {
		perror("SetupSerial 1");
		return -1;
	}
	options.c_cflag &= ~CSIZE;
	switch (databits) {
	case 7:
		options.c_cflag |= CS7;
		break;
	case 8:
		options.c_cflag |= CS8;
		break;
	}
	switch (parity) {
	case 0:
		options.c_cflag &= ~PARENB;
		options.c_iflag &= ~INPCK;
		break;
	case 1:
		options.c_cflag |= (PARODD | PARENB);
		options.c_iflag |= INPCK;
		break;
	case 2:
		options.c_cflag |= PARENB;
		options.c_cflag &= ~PARODD;
		options.c_iflag |= INPCK;
		break;
	case 3:
		options.c_cflag &= ~PARENB;
		options.c_cflag &= ~CSTOPB;
		break;
	}
	switch (stopbits) {
	case 1:
		options.c_cflag &= ~CSTOPB;
		break;
	case 2:
		options.c_cflag |= CSTOPB;
		break;
	}
	if (parity != 0)
		options.c_iflag |= INPCK;
	options.c_cc[VTIME] = waitTime; // 15 seconds
	options.c_cc[VMIN] = 0;

	tcflush(fd, TCIFLUSH);
	if (tcsetattr(fd, TCSANOW, &options) != 0) {
		LOGI("set_uart_opt tcsetattr error");
		return 0;
	}
	LOGI("set_uart_opt OK!\r\n");
	return 0;
}

int sri_io_exit() {
	LOGI("sri_exit ++");
	if (g_hSri == -1) //open fail
			{
	} else {
		close(g_hSri); //close device
	}
	return 1;
}

int sri_IOCTLSRI(unsigned int name, unsigned int value) {
	if (g_hSri == -1) { //open fail
		return g_hSri;
	} else {
		struct Data dta;
		dta.name = name;
		dta.value = value;
		ioctl(g_hSri, IOCTRL_GPIO_ON_OFF, &dta);
		//ioctl(fd,controlcode);
		return IOCTRL_GPIO_ON_OFF;
	}
}

static speed_t getBaudrate(jint baudrate) {
	switch (baudrate) {
	case 0:
		return B0;
	case 50:
		return B50;
	case 75:
		return B75;
	case 110:
		return B110;
	case 134:
		return B134;
	case 150:
		return B150;
	case 200:
		return B200;
	case 300:
		return B300;
	case 600:
		return B600;
	case 1200:
		return B1200;
	case 1800:
		return B1800;
	case 2400:
		return B2400;
	case 4800:
		return B4800;
	case 9600:
		return B9600;
	case 19200:
		return B19200;
	case 38400:
		return B38400;
	case 57600:
		return B57600;
	case 115200:
		return B115200;
	case 230400:
		return B230400;
	case 460800:
		return B460800;
	case 500000:
		return B500000;
	case 576000:
		return B576000;
	case 921600:
		return B921600;
	case 1000000:
		return B1000000;
	case 1152000:
		return B1152000;
	case 1500000:
		return B1500000;
	case 2000000:
		return B2000000;
	case 2500000:
		return B2500000;
	case 3000000:
		return B3000000;
	case 3500000:
		return B3500000;
	case 4000000:
		return B4000000;
	default:
		return -1;
	}
}

jint openType(jint type) //上电类型
{
	switch (type) {
	case 0: //激光
		sri_IOCTLSRI(96, 1);
		sri_IOCTLSRI(99, 0);
		sri_IOCTLSRI(100, 1);
		sri_IOCTLSRI(128, 0);
		sri_IOCTLSRI(78, 1);
		break;
	case 1: //普通
		sri_IOCTLSRI(96, 1);
		sri_IOCTLSRI(99, 1);
		sri_IOCTLSRI(100, 0);
		sri_IOCTLSRI(128, 0);
		sri_IOCTLSRI(78, 1);
		break;
	case 2: //扫描
		sri_IOCTLSRI(124, 1);
		sri_IOCTLSRI(127, 1);
		sri_IOCTLSRI(101, 1);
		sri_IOCTLSRI(102, 1);
		break;
	case 3: //身份证
			sri_IOCTLSRI(96, 1);
			sri_IOCTLSRI(125, 0);
			sri_IOCTLSRI(126, 1);
			break;
	case 4: //ESAM加密模块
		sri_IOCTLSRI(96, 1);
		sri_IOCTLSRI(98, 1);
		sri_IOCTLSRI(126, 0);
		sri_IOCTLSRI(42, 1);
		sri_IOCTLSRI(44, 0);
		break;
	case 5: //RS485模块
		sri_IOCTLSRI(96, 1);
		sri_IOCTLSRI(128, 0);
		sri_IOCTLSRI(78, 0);
		break;
	case 6: //RFID模块
		sri_IOCTLSRI(96, 1);
		sri_IOCTLSRI(94, 1);
		sri_IOCTLSRI(128, 1);
		sri_IOCTLSRI(78, 0);
		break;
	}
	return 0;
}

jint closeType(jint type) ////下电类型
{
	switch (type) {
	case 0: //激光
		sri_IOCTLSRI(78, 0);
		sri_IOCTLSRI(128, 1);
		sri_IOCTLSRI(100, 0);
		sri_IOCTLSRI(99, 0);
		sri_IOCTLSRI(96, 0);
		break;
	case 1: //普通
		sri_IOCTLSRI(78, 0);
		sri_IOCTLSRI(128, 1);
		sri_IOCTLSRI(100, 0);
		sri_IOCTLSRI(99, 0);
		sri_IOCTLSRI(96, 0);
		break;
	case 2: //扫描
		sri_IOCTLSRI(124, 0);

		break;
	case 3: //身份证
		sri_IOCTLSRI(96, 0);
		sri_IOCTLSRI(125, 1);
		sri_IOCTLSRI(126, 0);
		break;
	case 4: //ESAM加密模块
		sri_IOCTLSRI(44, 1);
		sri_IOCTLSRI(42, 0);
		sri_IOCTLSRI(126, 1);
		sri_IOCTLSRI(98, 0);
		sri_IOCTLSRI(96, 0);
		break;
	case 5: //RS485模块
		sri_IOCTLSRI(78, 1);
		sri_IOCTLSRI(128, 1);
		sri_IOCTLSRI(96, 0);
		break;
	case 6: //RFID模块
		sri_IOCTLSRI(78, 1);
		sri_IOCTLSRI(128, 0);
		sri_IOCTLSRI(94, 0);
		sri_IOCTLSRI(96, 0);
		break;
	}
	return 0;
}

jint openFd(jint type) {
	switch (type) {
	case 0: //激光
	case 1: //普通
	case 3: //身份证
	case 5: //RS485模块
		fd = open("/dev/ttyMT3", O_RDWR | 0);
		break;
	case 2: //扫描
	case 4: //ESAM加密模块
	case 6: //RFID模块
		fd = open("/dev/ttyMT2", O_RDWR | 0);
		break;
	}
	return 0;
}

jint init(jint type) {
	if (g_hSri == -1) {
		g_hSri = open("/dev/sri", O_RDWR);
		if (g_hSri == 0) {
			LOGD("open sri failed!\n");
			return -1;
		}
	}
	return openType(type); //上电
}


jint close(jint type) {
	return closeType(type);
}

jint config(JNIEnv *env, jclass thiz, jint baudrate, jint databits, jint parity,
		jint stopbits, jint type) {
	speed_t speed;
	/* Check arguments */
	{
		speed = getBaudrate(baudrate);
		if (speed == -1) {
			/* TODO: throw an exception */
			LOGE("Invalid baudrate");
		}
	}

	/* Opening device */
	{
		openFd(type);
		if (fd == -1) {
			/* Throw an exception */
			LOGE("Cannot open port");
			/* TODO: throw an exception */
			return -1;
		}
	}

	/* Configure device */
	{
		struct termios cfg;
		LOGI(TAG, "serial_port_open,Configuring serial port");
		if (tcgetattr(fd, &cfg)) {
			LOGI(TAG, "serial_port_open,tcgetattr() failed");
			LOGE(TAG, "serial_port_open", "tcgetattr() failed");
			/* TODO: throw an exception */
		}

		cfmakeraw(&cfg);
		cfsetispeed(&cfg, speed);
		cfsetospeed(&cfg, speed);

		if (tcsetattr(fd, TCSANOW, &cfg)) {
			LOGI(TAG, "serial_port_open", "tcsetattr() failed");
			LOGE(TAG, "serial_port_open", "tcsetattr() failed");
			/* TODO: throw an exception */
		}
		return set_Parity(fd, databits, stopbits, parity);
	}
}

jint sendData(JNIEnv *env, jclass thiz, jbyteArray data, jint offset,
		jint length, jint type) {
	openFd(type);
	if (fd < 0) {
		LOGE("fd open failure ,non't write");
		return -1;
	}

	//		unsigned char * temp[length];
	//			(*env)->GetByteArrayRegion(env, data, offset, length, temp);
	//		char arrayData[length];
	//			memcpy(arrayData, temp, length);

	unsigned char *arrayData = (*env)->GetByteArrayElements(env, data, 0);

	int re = write(fd, arrayData + offset, length);
	if (re == -1)
		LOGE("write device error");

	LOGI("spi length is by ccy %x\n", re);
	(*env)->ReleaseByteArrayElements(env, data, arrayData, 0); //释放内存
	return re;
}

jint recvData(JNIEnv *env, jclass thiz, jbyteArray data, jint offset, jint type) {
	unsigned char *arrayData = (*env)->GetByteArrayElements(env, data, NULL); //指向java数组地址
	openFd(type);
	if (fd < 0) {
		LOGE("fd open failure ,non't write");
		return -1;
	}
	if (arrayData == NULL) {
		LOGE("GetByteArrayElements failed add by ccy!");
		return -2;
	}
	jsize arrayLength = (*env)->GetArrayLength(env, data);
	int len = (int) arrayLength;
	LOGI("RecvArrayLength: %d", len);

	int re = read(fd, arrayData, len);

	LOGI("spi length is by ccy %x\n", re);
	if (0 == re)
		re = -1;
	if (re != -1)
		(*env)->SetByteArrayRegion(env, data, 0, re - offset,
				arrayData + offset);
	return re - offset;
}

jint clearSendCache( jint type) {
	return tcflush(fd, TCOFLUSH);
}

jint clearRevCache( jint type) {
	return tcflush(fd, TCIOFLUSH);
}

jint setTimeOut(jint direction, jint timeout, jint type) {
	if (direction == 1) {
		LOGI("recv timeout %x\n", timeout);
		options.c_cc[VTIME] = timeout/100;
		waitTime=timeout;
	}
	return tcsetattr(fd, TCSANOW, &options);
}


jint Java_com_cepri_dev_IRDA_init(JNIEnv *env, jobject thiz) {
	return init(IRDA);
}

jint Java_com_cepri_dev_IRDA_deInit(JNIEnv *env, jobject thiz) {
	return close(IRDA);
}

jint Java_com_cepri_dev_IRDA_config(JNIEnv *env, jclass thiz, jint baudrate,
		jint databits, jint parity, jint stopbits) {
	return config(env, thiz, baudrate, databits, parity, stopbits, IRDA);
}

jint Java_com_cepri_dev_IRDA_sendData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset, jint length) {
	return sendData(env, thiz, data, offset, length, IRDA);
}

jint Java_com_cepri_dev_IRDA_recvData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset) {
	return recvData(env, thiz, data, offset, IRDA);
}

jint Java_com_cepri_dev_IRDA_clearSendCache(JNIEnv *env, jobject thiz) {
	return clearSendCache(IRDA);
}

jint Java_com_cepri_dev_IRDA_clearRevCache(JNIEnv *env, jobject thiz) {
	return clearRevCache(IRDA);
}

jint Java_com_cepri_dev_IRDA_setTimeOut(JNIEnv *env, jobject thiz,
		jint direction, jint timeout) {
	return setTimeOut(direction, timeout, IRDA);
}

jint Java_com_cepri_dev_LaserIRDA_init(JNIEnv *env, jobject thiz) {
	return init(LaserIRDA);
}

jint Java_com_cepri_dev_LaserIRDA_deInit(JNIEnv *env, jobject thiz) {
	return close(LaserIRDA);
}

jint Java_com_cepri_dev_LaserIRDA_config(JNIEnv *env, jclass thiz, jint baudrate,
		jint databits, jint parity, jint stopbits) {
	return config(env, thiz, baudrate, databits, parity, stopbits, LaserIRDA);
}

jint Java_com_cepri_dev_LaserIRDA_sendData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset, jint length) {
	return sendData(env, thiz, data, offset, length, LaserIRDA);
}

jint Java_com_cepri_dev_LaserIRDA_recvData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset) {
	return recvData(env, thiz, data, offset, LaserIRDA);
}

jint Java_com_cepri_dev_LaserIRDA_clearSendCache(JNIEnv *env, jobject thiz) {
	return clearSendCache(LaserIRDA);
}

jint Java_com_cepri_dev_LaserIRDA_clearRevCache(JNIEnv *env, jobject thiz) {
	return clearRevCache(LaserIRDA);
}

jint Java_com_cepri_dev_LaserIRDA_setTimeOut(JNIEnv *env, jobject thiz,
		jint direction, jint timeout) {
	return setTimeOut(direction, timeout, LaserIRDA);
}

//jint Java_com_cepri_dev_SecurityUnit_init(JNIEnv *env, jobject thiz) {
//	return init(SecurityUnit);
//}
//
//jint Java_com_cepri_dev_SecurityUnit_deInit(JNIEnv *env, jobject thiz) {
//	return close(SecurityUnit);
//}
//
//jint Java_com_cepri_dev_SecurityUnit_config(JNIEnv *env, jclass thiz, jint baudrate,
//		jint databits, jint parity, jint stopbits) {
//	return config(env, thiz, baudrate, databits, parity, stopbits, SecurityUnit);
//}
//
//jint Java_com_cepri_dev_SecurityUnit_sendData(JNIEnv *env, jclass thiz, jbyteArray data,
//		jint offset, jint length) {
//	return sendData(env, thiz, data, offset, length, SecurityUnit);
//}
//
//jint Java_com_cepri_dev_SecurityUnit_recvData(JNIEnv *env, jclass thiz, jbyteArray data,
//		jint offset) {
//	return recvData(env, thiz, data, offset, SecurityUnit);
//}
//
//jint Java_com_cepri_dev_SecurityUnit_clearSendCache(JNIEnv *env, jobject thiz) {
//	return clearSendCache(SecurityUnit);
//}
//
//jint Java_com_cepri_dev_SecurityUnit_clearRevCache(JNIEnv *env, jobject thiz) {
//	return clearRevCache(SecurityUnit);
//}

//jint Java_com_cepri_dev_SecurityUnit_setTimeOut(JNIEnv *env, jobject thiz,
//		jint direction, jint timeout) {
//	return setTimeOut(direction, timeout, SecurityUnit);
//}

jint Java_com_cepri_dev_RESAM_init(JNIEnv *env, jobject thiz) {
	return init(RESAM);
}

jint Java_com_cepri_dev_RESAM_deInit(JNIEnv *env, jobject thiz) {
	return close(RESAM);
}

jint Java_com_cepri_dev_RESAM_config(JNIEnv *env, jclass thiz, jint baudrate,
		jint databits, jint parity, jint stopbits) {
	return config(env, thiz, baudrate, databits, parity, stopbits, RESAM);
}

jint Java_com_cepri_dev_RESAM_sendData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset, jint length) {
	return sendData(env, thiz, data, offset, length, RESAM);
}

jint Java_com_cepri_dev_RESAM_recvData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset) {
	return recvData(env, thiz, data, offset, RESAM);
}

jint Java_com_cepri_dev_RESAM_clearSendCache(JNIEnv *env, jobject thiz) {
	return clearSendCache(RESAM);
}

jint Java_com_cepri_dev_RESAM_clearRevCache(JNIEnv *env, jobject thiz) {
	return clearRevCache(RESAM);
}

jint Java_com_cepri_dev_RESAM_setTimeOut(JNIEnv *env, jobject thiz,
		jint direction, jint timeout) {
	return setTimeOut(direction, timeout, RESAM);
}

jint Java_com_cepri_dev_Serialport_init(JNIEnv *env, jobject thiz) {
	return init(Serialport);
}

jint Java_com_cepri_dev_Serialport_deInit(JNIEnv *env, jobject thiz) {
	return close(Serialport);
}

jint Java_com_cepri_dev_Serialport_config(JNIEnv *env, jclass thiz, jint baudrate,
		jint databits, jint parity, jint stopbits) {
	return config(env, thiz, baudrate, databits, parity, stopbits, Serialport);
}

jint Java_com_cepri_dev_Serialport_sendData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset, jint length) {
	return sendData(env, thiz, data, offset, length, Serialport);
}

jint Java_com_cepri_dev_Serialport_recvData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset) {
	return recvData(env, thiz, data, offset, Serialport);
}

jint Java_com_cepri_dev_Serialport_clearSendCache(JNIEnv *env, jobject thiz) {
	return clearSendCache(Serialport);
}

jint Java_com_cepri_dev_Serialport_clearRevCache(JNIEnv *env, jobject thiz) {
	return clearRevCache(Serialport);
}

jint Java_com_cepri_dev_Serialport_setTimeOut(JNIEnv *env, jobject thiz,
		jint direction, jint timeout) {
	return setTimeOut(direction, timeout, Serialport);
}

jint Java_com_cepri_dev_LibInfo_getVersion(JNIEnv *env, jclass thiz) {
	return version;
}

jint Java_com_cepri_dev_LibInfo_getCompany(JNIEnv *env, jclass thiz) {
	return company;
}


//身份证
jint Java_com_cepri_dev_IdCard_init(JNIEnv *env, jobject thiz) {
	return init(IdCard);
}

jint Java_com_cepri_dev_IdCard_deInit(JNIEnv *env, jobject thiz) {
	return close(IdCard);
}

jint Java_com_cepri_dev_IdCard_config(JNIEnv *env, jclass thiz, jint baudrate,
		jint databits, jint parity, jint stopbits) {
	return config(env, thiz, baudrate, databits, parity, stopbits, IdCard);
}

jint Java_com_cepri_dev_IdCard_sendData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset, jint length) {
	return sendData(env, thiz, data, offset, length, IdCard);
}

jint Java_com_cepri_dev_IdCard_recvData(JNIEnv *env, jclass thiz, jbyteArray data,
		jint offset) {
	return recvData(env, thiz, data, offset, IdCard);
}

jint Java_com_cepri_dev_IdCard_clearSendCache(JNIEnv *env, jobject thiz) {
	return clearSendCache(IdCard);
}

jint Java_com_cepri_dev_IdCard_clearRevCache(JNIEnv *env, jobject thiz) {
	return clearRevCache(IdCard);
}

jint Java_com_cepri_dev_IdCard_setTimeOut(JNIEnv *env, jobject thiz,
		jint direction, jint timeout) {
	return setTimeOut(direction, timeout, IdCard);
}

jint Java_com_cepri_dev_Scanner_init(JNIEnv *env, jobject thiz) {
	return init(Scanner);
}

jint Java_com_cepri_dev_Scanner_deInit(JNIEnv *env, jobject thiz) {
	 return  close(Scanner);
}

jint Java_com_cepri_dev_Scanner_decode(JNIEnv *env, jclass thiz, jint timeout, jbyteArray code, jint offset) {

	sri_IOCTLSRI(127, 0);
	sri_IOCTLSRI(101, 0);
	sri_IOCTLSRI(102, 0);
	setTimeOut(1, timeout, Scanner);
	config(env, thiz, 9600, 8, 0, 1, Scanner);
//	sri_IOCTLSRI(127, 1);
	unsigned char *arrayData = (*env)->GetByteArrayElements(env, code, NULL); //指向java数组地址

	if (arrayData == NULL) {
		LOGE("GetByteArrayElements failed add by ccy!");
		return -2;
	}
	jsize arrayLength = (*env)->GetArrayLength(env, code);
	int len = (int) arrayLength;
	LOGI("RecvArrayLength: %d", len);

	int re = read(fd, arrayData, len);

	LOGI("spi length is by ccy %x\n", re);
	if (0 == re)
		re = -1;
	if (re != -1)
		(*env)->SetByteArrayRegion(env, code, 0, re - offset,
				arrayData + offset);
	closeType(Scanner);
	return re - offset;
}
