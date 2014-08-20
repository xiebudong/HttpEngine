package com.fgc.http.protocal;

import java.util.Map;

/**
 * @author FGC
 * @date 2014-08-18
 */

public interface Request {
	public enum Type
	{
		GET, 
		POST
	}
	
	public int getId();
	public Request.Type getRequestType();
	public String getUrl();
	public Map<String, String> getSpecialHeader();
}
