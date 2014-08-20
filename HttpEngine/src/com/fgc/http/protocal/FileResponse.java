package com.fgc.http.protocal;

import android.util.Log;

/**
 * @author FGC
 * @date 2014-08-18
 */

public class FileResponse implements Response{
	private final String LOG_TAG = "FileResponse";
	private Response.State mResponseState = Response.State.UNKNOWN;
	private long mTotalSize;
	private long mDownloadedSize;
	private int mRespCode;
	private String mMimeType;
	private DownloadState mDownloadState = DownloadState.START;
	
	public enum DownloadState
	{
		START,
		ING,
		END
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

	public void setDownloadState(DownloadState state)
	{
		mDownloadState = state;
	}
	
	public DownloadState getDownloadState()
	{
		return mDownloadState;
	}
	
	public void setTotalSize(long size)
	{
		mTotalSize = size;
	}
	
	public long getTotalSize()
	{
		return mTotalSize;
	}
	
	public void setDownloadedSize(long size)
	{
		mDownloadedSize = size;
	}
	
	public long getDownLoadedSize()
	{
		return mDownloadedSize;
	}
	
	/**
	 * get download percent.
	 * @return present to 0 - 100 number.
	 */
	public int getDownloadPercent()
	{
		if (mTotalSize == 0)
		{
			Log.w(LOG_TAG, "total size is 0 !");
			return 0;
		}
		float decimal = (float)(((double)mDownloadedSize)/((double)mTotalSize));
		return (int)(decimal*100);
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
		String s = String.format("resp: code=%d, state=%s, download state=%s, mimeType=%s, totalSize=%s, downloadedSize=%s",
				mRespCode, mResponseState, mDownloadState, mMimeType, mTotalSize, mDownloadedSize);
		return s;
	}
}
