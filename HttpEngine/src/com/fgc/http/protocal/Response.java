package com.fgc.http.protocal;

/**
 * @author FGC
 * @date 2014-08-18
 */

public interface Response {
	public enum State
	{
		OK,
		BAD_URL,
		TIME_OUT,
		REQUEST_FAILED,
		IO_ERROR,
		UNKNOWN
	}
	
	public void setResponseState(Response.State state);
	public Response.State getResponseState();
	public void setResponseCode(int code);
	public int getResponseCode();
	public void setMimeType(String mime);
	public String getMimeType();
}
