package com.fgc.http.tools;

/**
 * @author FGC
 * @date 2014-08-18
 */

public class Log {
	public static boolean mNeedLog = false;
	
	public static void v(String tag, String msg)
	{
		if (mNeedLog)
			android.util.Log.v(tag, msg);
	}
	
	public static void d(String tag, String msg)
	{
		if (mNeedLog)
			android.util.Log.d(tag, msg);
	}
	
	public static void i(String tag, String msg)
	{
		if (mNeedLog)
			android.util.Log.i(tag, msg);
	}
	
	public static void w(String tag, String msg)
	{
		if (mNeedLog)
			android.util.Log.w(tag, msg);
	}
	
	public static void e(String tag, String msg)
	{
		if (mNeedLog)
			android.util.Log.e(tag, msg);
	}
}
