package com.fgc.http.engine;

import com.fgc.http.engine.HttpEngine.FileObserver;
import com.fgc.http.engine.HttpEngine.StringObserver;
import com.fgc.http.protocal.Controller;
import com.fgc.http.protocal.Response.State;
import com.fgc.http.protocal.FileRequest;
import com.fgc.http.protocal.FileResponse;
import com.fgc.http.protocal.StringRequest;
import com.fgc.http.protocal.StringResponse;

/**
 * a helper class for request a string or file quickly and easily.
 * @author FGC
 */
public class EngineHelper {
	private static final String LOG_TAG = "EngineHelper";
	
	public interface StringRequestObserver
	{
		public void done(boolean isOk, String respString);
	}
	
	public interface FileRequestObserver
	{
		public void done(boolean isOk, int percent);
	}
	
	/**
	 * need the engine instance create by {@code HttpEngine.instance()}
	 * @param engine
	 * @param url
	 * @param observer
	 * @return task is delivered success or not.
	 */
	public static boolean requestString(HttpEngine engine, String url, final StringRequestObserver observer)
	{
		if (engine == null)
		{
			return false;
		}
		StringRequest req = new StringRequest(url);
		Controller c = engine.requestString(req, new StringObserver() {
			
			@Override
			public void done(StringRequest req, StringResponse resp) {
				// TODO Auto-generated method stub
				if (observer != null)
				{
					if (resp.getResponseState() == State.OK)
					{
						observer.done(true, resp.getRespString());
					}
					else
					{
						observer.done(false, null);
					}
				}
			}
		});
		if (c == null)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * need the engine instance create by {@code HttpEngine.instance()}
	 * @param engine
	 * @param url
	 * @param fileSaveFullPath  the location of the file be downloaded.
	 * @param observer
	 * @return task is delivered success or not.
	 */
	public static boolean requestFile(HttpEngine engine, String url, String fileSaveFullPath, final FileRequestObserver observer)
	{
		if (engine == null)
		{
			return false;
		}
		FileRequest req = new FileRequest(url, fileSaveFullPath);
		Controller c = HttpEngine.instance().requestFile(req, new FileObserver() {
			
			@Override
			public void done(FileRequest req, FileResponse resp) {
				// TODO Auto-generated method stub				
				if (observer != null)
				{
					if (resp.getResponseState() == State.OK)
					{
						observer.done(true, resp.getDownloadPercent());
					}
					else
					{
						observer.done(false, resp.getDownloadPercent());
					}
				}
			}
		});
		
		if (c == null)
		{
			return false;
		}
		return true;
	}
}
