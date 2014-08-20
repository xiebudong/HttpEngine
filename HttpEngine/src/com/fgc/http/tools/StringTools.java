package com.fgc.http.tools;

import java.io.File;

/**
 * @author FGC
 * @date 2014-08-18
 */

public class StringTools {
	
	public static boolean isFileFullPathValid(String fileFullPath)
	{
		if (fileFullPath==null || fileFullPath.length()==0)
		{
			return false;
		}
		
		int i = fileFullPath.lastIndexOf(File.separator);
		if (i==0 || i==-1)
		{
			return false;
		}
		
		if (i+1 == fileFullPath.length())
		{
			return false;
		}
		
		File f = new File(fileFullPath);
		if (f.isDirectory())
		{
			return false;
		}
		
		String parentPath = fileFullPath.substring(0, i);
		File file = new File(parentPath);
		if (!file.isDirectory())
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean isEmpty(String str)
	{
		if(str == null || str.length() <= 0)
			return true;
		
		return false;
	}
	
	public static boolean isPureNumber(String str)
	{
		if (isEmpty(str))
		{
			return false;
		}
		
		return str.matches("^[0-9]*$");
	}
	
	public static String unicode2Hanzi(String s){
		
		if (s == null || s.length() == 0)
		{
			return new String();
		}
		
		StringBuilder sb = new StringBuilder();
		int i = -1;
		int pos = 0;
		
		while((i=s.indexOf("\\u", pos)) != -1){
			sb.append(s.substring(pos, i));
			if(i+5 < s.length()){
				pos = i+6;
				sb.append((char)Integer.parseInt(s.substring(i+2, i+6), 16));
			}
		}
		
		return sb.toString();
	}
}
