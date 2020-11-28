package com.serenegiant.usb;


public class UVCOSD {
	static {
		System.loadLibrary("UVCOSD");
	}
	
	static public native int nativeXU_OSD_Timer_Ctrl(int fd, boolean enable);
	static public native int nativeXU_OSD_Set_RTC(int fd, int year, char month, char day, char hour, char minute, char second);
}
