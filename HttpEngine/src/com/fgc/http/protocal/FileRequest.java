package com.fgc.http.protocal;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * @author FGC
 * @date 2014-08-18
 */
public class FileRequest implements Request{
	private final String LOG_TAG = "FileRequest";
	private Request.Type mRequestType = Request.Type.GET;
	private String mUrl;
	private String mFileSaveFullPath;
	private int mId;
	private boolean mIsSupportBreakpointResume = false;
	private Map<String, String> mSpecialHeaders = new HashMap<String, String>();
	private String mPostContent;
	
	public FileRequest(String url, String fileSaveFullPath)
	{
		mUrl = url;
		mFileSaveFullPath = fileSaveFullPath;
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
	public Type getRequestType() {
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
	
	public void setIsSupportBreakpointResume(boolean isSupport)
	{
		mIsSupportBreakpointResume = isSupport;
	}
	
	public boolean isSupportBreakpointResume()
	{
		return mIsSupportBreakpointResume;
	}

	public String getFileSaveFullPath()
	{
		return mFileSaveFullPath;
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
		String s = String.format("req: id=%d, requestType=%s, url=%s, fileSave=%s, postContent=%s, isBreakpointResume=%s, specialHeaders=",
				mId, mRequestType.toString(), mUrl, mFileSaveFullPath, mPostContent, String.valueOf(mIsSupportBreakpointResume), mSpecialHeaders.toString());
		return s;
	}

}
