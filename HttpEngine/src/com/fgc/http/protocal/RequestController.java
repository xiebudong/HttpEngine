package com.fgc.http.protocal;

/**
 * @author FGC
 * @date 2014-08-18
 */

public class RequestController implements Controller{

	private boolean mIsStop = false;
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		mIsStop = true;
	}
	
	@Override
	public boolean isStoped()
	{
		return mIsStop;
	}
}
