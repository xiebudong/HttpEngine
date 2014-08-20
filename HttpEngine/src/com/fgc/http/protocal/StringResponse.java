package com.fgc.http.protocal;

/**
 * @author FGC
 * @date 2014-08-18
 */

public class StringResponse implements Response{
	private Response.State mResponseState = Response.State.UNKNOWN;
	
	private String mRespString;
	private int mRespCode;
	private String mMimeType;
	
	public StringResponse()
	{
		
	}
	
	@Override
	public void setResponseState(Response.State state)
	{
		mResponseState = state;
	}

	@Override
	public State getResponseState() {
		// TODO Auto-generated method stub
		return mResponseState;
	}
	
	public void setRespString(String string)
	{
		mRespString = string;
	}
	
	public String getRespString()
	{
		return mRespString;
	}

	@Override
	public void setResponseCode(int code) {
		// TODO Auto-generated method stub
		mRespCode = code;
	}

	@Override
	public int getResponseCode() {
		// TODO Auto-generated method stub
		return mRespCode;
	}

	@Override
	public void setMimeType(String mime) {
		// TODO Auto-generated method stub
		mMimeType = mime;
	}

	@Override
	public String getMimeType() {
		// TODO Auto-generated method stub
		return mMimeType;
	}
	
	@Override
	public String toString() 
	{
		String s = String.format("resp: code=%d, state=%s, mimeType=%s, respString=%s",
				mRespCode, mResponseState, mMimeType, mRespString);
		return s;
	}
}
