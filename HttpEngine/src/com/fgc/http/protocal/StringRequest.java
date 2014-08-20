package com.fgc.http.protocal;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

/**
 * @author FGC
 * @date 2014-08-18
 */

public class StringRequest implements Request{
	private final String LOG_TAG = "StringRequest";
	
	private Request.Type mRequestType = Request.Type.GET;
	private String mUrl;
	private int mId;
	private Map<String, String> mSpecialHeaders = new HashMap<String, String>();
	private String mPostContent;
	
	public StringRequest(String url)
	{
		mUrl = url;
	}
	
	public void setId(int id)
	{
		mId = id;
	}
	
	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return mId;
	}
	
	public void setRequstType(Request.Type type)
	{
		mRequestType = type;
	}

	@Override
	public Request.Type getRequestType() {
		// TODO Auto-generated method stub
		return mRequestType;
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return mUrl;
	}

	public void addSpecialHeader(String key, String value)
	{
		if (key == null || value == null)
		{
			Log.w(LOG_TAG, "addSpecialHeader: check key or value !");
			return;
		}
		
		mSpecialHeaders.put(key, value);
	}
	
	@Override
	public Map<String, String> getSpecialHeader() {
		// TODO Auto-generated method stub
		return mSpecialHeaders;
	}
	
	public void setPostContent(String postContent)
	{
		mPostContent = postContent;
	}
	
	public String getPostContent()
	{
		return mPostContent;
	}
	
	@Override
	public String toString() 
	{
		String s = String.format("req: id=%d, requestType=%s, url=%s, postContent=%s, specialHeaders=",
				mId, mRequestType.toString(), mUrl,  mPostContent,  mSpecialHeaders.toString());
		return s;
	}

}
