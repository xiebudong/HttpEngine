package com.fgc.http.engine;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator.RequestorType;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fgc.http.protocal.FileCache;
import com.fgc.http.protocal.RequestController;
import com.fgc.http.protocal.FileRequest;
import com.fgc.http.protocal.FileResponse;
import com.fgc.http.protocal.Controller;
import com.fgc.http.protocal.Request;
import com.fgc.http.protocal.Response;
import com.fgc.http.protocal.Response.State;
import com.fgc.http.protocal.StringRequest;
import com.fgc.http.protocal.StringResponse;
import com.fgc.http.protocal.FileResponse.DownloadState;
import com.fgc.http.tools.StringTools;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import com.fgc.http.tools.Log;
/**
 * main class of http engine.
 * @author FGC
 */
public class HttpEngine {
	private static final String LOG_TAG = "HttpEngine";
	
	private static final int CORE_THREAD_NUM = 4;
	private static final int MAX_THREAD_NUM = 6;
	private static final int CORE_THREAD_NUM_File = 4;
	private static final int MAX_THREAD_NUM_File = 6;
	
	private static final int KEEP_ALIVE_SECOND = 60;
	private static final int FILE_BUFFER_LEN = 4 *1024;
	private static final int TXT_BUFFER_LEN = 1024;
	private static final int NET_TRY_CONN_TIMES = 5;
	
	private static final int CONNECT_TIMEOUT = 10*1000;
	private static final int TRANSMIT_TIMEOUT = 10*1000;
	
	private volatile boolean mIsInited = false;
	private ExecutorService mExecutorForString = null;
	private ExecutorService mExecutorForFile = null;
	
	private static HttpEngine mHttpEngine;
	private FileCache mFileCache;
	
	private HttpEngine()
	{
		Log.i(LOG_TAG, "construct a engine");
	}
	
	public static HttpEngine instance()
	{
		if (mHttpEngine == null)
		{
			mHttpEngine = new HttpEngine();
		}
		return mHttpEngine;
	}
	
	/**
	 * show http engine debug log or not.
	 * @param isNeed
	 */
	public void needDebug(boolean isNeed)
	{
		Log.mNeedLog = isNeed;
	}
	
	/**
	 * must be called before all operation.
	 */
	public void init()
	{
		Log.i(LOG_TAG, "init");
		if (mIsInited == true)
		{
			Log.w(LOG_TAG, "engine has inited.");
			return;
		}
		mExecutorForString = new ThreadPoolExecutor(CORE_THREAD_NUM,MAX_THREAD_NUM,KEEP_ALIVE_SECOND, TimeUnit.SECONDS,  new LinkedBlockingQueue<Runnable>());
		
		//thread factory for download file.
		ThreadFactory threadFactory = new ThreadFactory(){
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setPriority(Thread.MIN_PRIORITY);
				return thread;
			}
			
		};
		mExecutorForFile = new ThreadPoolExecutor(CORE_THREAD_NUM_File,MAX_THREAD_NUM_File,KEEP_ALIVE_SECOND, TimeUnit.SECONDS,  new LinkedBlockingQueue<Runnable>(),threadFactory);
		
		if(VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD){
			if(mExecutorForString instanceof ThreadPoolExecutor)
				((ThreadPoolExecutor) mExecutorForString).allowCoreThreadTimeOut(true); 
			if(mExecutorForFile instanceof ThreadPoolExecutor)
				((ThreadPoolExecutor) mExecutorForFile).allowCoreThreadTimeOut(true); 
		}	
		mIsInited = true;
	}
		
	/**
	 * must be called when you application finished.
	 */
	public void uninit(){
		Log.i(LOG_TAG, "uninit.");
		if (mIsInited == false)
		{
			throw new IllegalStateException("must init engine first before use it !");
		}
		mExecutorForString.shutdownNow();
		mExecutorForFile.shutdownNow();
		if (mFileCache != null)
		{
			mFileCache.uninit();
			mFileCache = null;
		}
		mIsInited = false;
	}
	
	/**
	 * install a cache for file requesting (which can not support breakpoint resume download mode.)
	 * @param cache  a instance that implements {@code FileCache} interface
	 */
	public void installFileCache(FileCache cache)
	{
		mFileCache = cache;
	}
	
	public interface StringObserver
	{
		public void done(StringRequest req, StringResponse resp);
	}
	
	public interface FileObserver
	{
		public void done(FileRequest req, FileResponse resp);
	}
	
	/**
	 * request a string
	 * @param req see {@code StringRequest}
	 * @param observer
	 * @return A Controller which can terminate the task
	 */
	public Controller requestString(StringRequest req, StringObserver observer)
	{
		if (mIsInited == false)
		{
			throw new IllegalStateException("must init engine first before use it !");
		}
		if (observer == null)
		{
			throw new NullPointerException("http observer is can not null !");
		}
		Log.i(LOG_TAG, "request string: " + req.getUrl());
		Controller controller = new RequestController();
		mExecutorForString.execute(new HttpStringTask(req, observer, controller));
		return controller;
	}
	
	/**
	 * request a file
	 * @param req  see {@code FileRequest}
	 * @param observer
	 * @return A Controller which can terminate the task
	 */
	public Controller requestFile(FileRequest req, FileObserver observer)
	{
		if (mIsInited == false)
		{
			throw new IllegalStateException("must init engine first before use it !");
		}
		if (req == null || observer == null)
		{
			throw new NullPointerException("http observer is can not null !");
		}
		Log.i(LOG_TAG, "request file: " + req.getUrl());
		Controller controller = new RequestController();
		mExecutorForFile.execute(new HttpFileTask(req, observer, controller));
		return controller;
	}
	
	private void notifyUser(StringObserver observer, StringRequest req, StringResponse resp)
	{
		if (mIsInited)
			observer.done(req, resp);
	}
	
	private class HttpStringTask implements Runnable{

		private StringRequest m_Req;
		private StringResponse m_Response;
		private StringObserver m_Observer;
		private Controller m_Controller;
		public HttpStringTask(StringRequest req, StringObserver o, Controller controller){
			m_Req = req;
			m_Response = new StringResponse();
			m_Observer = o;
			m_Controller = controller;
		}
		
		@Override
		public void run() {
			if(m_Req.getUrl() == null)
			{
				m_Response.setResponseState(Response.State.BAD_URL);
				notifyUser(m_Observer, m_Req, m_Response);
				return;
			}
			
			int retryTime = NET_TRY_CONN_TIMES;
			boolean retryFlag = true;
			while(retryFlag)
			{
				HttpURLConnection urlConnection = null;
				try {
					URL url = new URL(m_Req.getUrl());
					urlConnection = (HttpURLConnection)url.openConnection();

					Map<String, String> specialHeaders = m_Req.getSpecialHeader();
					if (specialHeaders.size() != 0)
					{
						Set<String> keySet = specialHeaders.keySet();
						for (String s : keySet)
						{
							urlConnection.setRequestProperty(s, specialHeaders.get(s));
						}
					}
					
					urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
					urlConnection.setReadTimeout(TRANSMIT_TIMEOUT);
					if(m_Req.getRequestType() == Request.Type.POST){
						urlConnection.setDoOutput(true);
						urlConnection.setDoInput(true);
						urlConnection.setRequestMethod("POST");
						urlConnection.setInstanceFollowRedirects(true);
						urlConnection.setUseCaches(false); 
						urlConnection.connect();    
						
						if (m_Req.getPostContent() != null)
						{
							urlConnection.getOutputStream().write(m_Req.getPostContent().getBytes());
						}
						urlConnection.getOutputStream().flush();
						urlConnection.getOutputStream().close();
					}
					
					int respCode = urlConnection.getResponseCode();					
					
					m_Response.setResponseCode(respCode);
					if(respCode != 200)
					{
						m_Response.setResponseState(Response.State.REQUEST_FAILED);
					}
					else
					{
						InputStream is = urlConnection.getInputStream();
						String mime = getMimeType(urlConnection);
						m_Response.setMimeType(mime);
						String charsetName = getContentEncode(urlConnection);
						String result = null;
						if (charsetName != null)
							result = readString(m_Controller, is, charsetName);
						else
							result = readString(m_Controller, is, "UTF-8");
						m_Response.setRespString(result);
						m_Response.setResponseState(Response.State.OK);
						is.close();
					}
					retryTime = 0;
					
				}catch(IllegalStateException e){
					m_Response.setResponseState(Response.State.REQUEST_FAILED);
					retryTime = 0;
					Log.w(LOG_TAG, "failed to do http req by "+ e.toString());
					e.printStackTrace();
					
				}catch (MalformedURLException e) {
					m_Response.setResponseState(Response.State.BAD_URL);
					retryTime = 0;
					Log.w(LOG_TAG, "failed to do http req by "+ e.toString());
					e.printStackTrace();
				} catch (IOException e){
					if(e instanceof SocketTimeoutException){	
						retryTime--;
						m_Response.setResponseState(Response.State.TIME_OUT);
						Log.w(LOG_TAG, "socket timeout !  retry " + (NET_TRY_CONN_TIMES-retryTime) + " times." + e.toString());
					}else{
						retryTime = 0;
						m_Response.setResponseState(Response.State.REQUEST_FAILED);
						Log.w(LOG_TAG, "failed to do http req by "+ e.toString());
						e.printStackTrace();
					}
				}finally{
					if(retryTime == 0){
						retryFlag = false;
					}
					else {
						Log.w(LOG_TAG, "retry to do http req ...  ");
						retryFlag = true;
					}
					if(urlConnection != null)
						urlConnection.disconnect();
				}
			}
			notifyUser(m_Observer, m_Req, m_Response);
		}
	}
	
	private String readString(Controller controller, InputStream in, String charsetName)
	{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    byte[] buffer = new byte[TXT_BUFFER_LEN];
	    int len = -1;
	    
	    try {
			while( (len = in.read(buffer)) != -1 && !controller.isStoped() && mIsInited){
			    outStream.write(buffer, 0, len);
			}
		} catch (IOException e) {
			Log.w(LOG_TAG, "readString failed by "+ e.toString());
			e.printStackTrace();
		}
	    
	    if (controller.isStoped() || false == mIsInited)
	    {
	    	Log.i(LOG_TAG, "request is stoped or engine uninited");
	    	return new String();
	    }
	    
	    String resultStr = null;
	    if(outStream.size() > 0){
	    	byte[] data = outStream.toByteArray();
	    	try {
				resultStr = new String(data, charsetName);
			} catch (UnsupportedEncodingException e) {
				resultStr = new String();
				Log.w(LOG_TAG, "readString failed by "+ e.toString());
				e.printStackTrace();
			}
	    }else{
	    	resultStr = new String();
	    }
	    
	    try {
			outStream.close();
		} catch (IOException e) {
			Log.w(LOG_TAG, "readString failed by "+ e.toString());
			e.printStackTrace();
		}
	    return resultStr;
	}
	
	private void notifyUser(FileObserver observer, FileRequest req, FileResponse resp)
	{
		if (mIsInited)
			observer.done(req, resp);
	}
	
	private class HttpFileTask implements Runnable{

		private FileRequest m_Req;
		private FileResponse m_Response;
		private FileObserver m_Observer;
		private Controller m_Controller;
		
		public HttpFileTask(FileRequest req, FileObserver o, Controller handle){
			m_Req = req;
			m_Response = new FileResponse();
			m_Observer = o;
			m_Controller = handle;
		}
		
		@Override
		public void run() {
			if(m_Req.getUrl() == null)
			{
				m_Response.setResponseState(Response.State.BAD_URL);
				notifyUser(m_Observer, m_Req, m_Response);
				return;
			}
			
			if (!StringTools.isFileFullPathValid(m_Req.getFileSaveFullPath()))
			{
				m_Response.setResponseState(Response.State.IO_ERROR);
				notifyUser(m_Observer, m_Req, m_Response);
				return;
			}
			
			// cache
			if (mFileCache != null && !m_Req.isSupportBreakpointResume())
			{
				String fromCache = mFileCache.getFile(m_Req.getUrl());
				boolean isSuccess = true;
				if (fromCache == null)
				{
					isSuccess = false;
				}
				File file = new File(fromCache);
				if (!file.exists() || !file.isFile() || file.length()==0)
				{
					isSuccess = false;
				}
				if (isSuccess == true)
				{
					m_Response.setResponseCode(200);
					m_Response.setResponseState(State.OK);
					m_Response.setDownloadState(DownloadState.END);
					m_Response.setDownloadedSize(file.length());
					m_Response.setTotalSize(file.length());
					notifyUser(m_Observer, m_Req, m_Response);
					return;
				}
				else
				{
					Log.i(LOG_TAG, "cache not hit, for : " + m_Req.getUrl());
				}
			}
			
			int retryTime = NET_TRY_CONN_TIMES;
			boolean retryFlag = true;
			while(retryFlag)
			{
				HttpURLConnection urlConnection = null;
				try {
					URL url = new URL(m_Req.getUrl());
					urlConnection = (HttpURLConnection)url.openConnection();

					Map<String, String> specialHeaders = m_Req.getSpecialHeader();
					if (specialHeaders.size() != 0)
					{
						Set<String> keySet = specialHeaders.keySet();
						for (String s : keySet)
						{
							urlConnection.setRequestProperty(s, specialHeaders.get(s));
						}
					}
					
					urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
					urlConnection.setReadTimeout(TRANSMIT_TIMEOUT);
					
					if (m_Req.isSupportBreakpointResume())
					{
						long downloadedSize = 0;
						File file = new File(m_Req.getFileSaveFullPath());
						if (file.exists())
							downloadedSize = file.length();
						urlConnection.setRequestProperty("Range", "bytes=" + downloadedSize + "-");
						Log.i(LOG_TAG, "breakpoint download from : " + downloadedSize);
					}
					
					if(m_Req.getRequestType() == Request.Type.POST){
						urlConnection.setDoOutput(true);
						urlConnection.setDoInput(true);
						urlConnection.setRequestMethod("POST");
						urlConnection.setInstanceFollowRedirects(true);
						urlConnection.setUseCaches(false); 
						urlConnection.connect();    
						
						if (m_Req.getPostContent() != null)
						{
							urlConnection.getOutputStream().write(m_Req.getPostContent().getBytes());
						}
						urlConnection.getOutputStream().flush();
						urlConnection.getOutputStream().close();
					}
					
					int respCode = urlConnection.getResponseCode();
					m_Response.setResponseCode(respCode);
					
					if (m_Req.isSupportBreakpointResume())
					{
						if(respCode != 200 && respCode != 206)
						{
							m_Response.setResponseState(Response.State.REQUEST_FAILED);
						}
						else
						{
							InputStream is = urlConnection.getInputStream();
							m_Response.setMimeType(getMimeType(urlConnection));
							int remainSize = urlConnection.getContentLength();
							int totalSize = (int)getTotalSizeFileHttpResponseHeader(urlConnection.getHeaderFields());
							if (remainSize <= 0)
							{
								m_Response.setResponseState(Response.State.UNKNOWN);
							}
							else if (totalSize == 0)
							{
								m_Response.setResponseState(Response.State.UNKNOWN);
							}
							else
							{
								if(true == readFileBreakpoint(m_Controller, is, m_Req.getFileSaveFullPath(), m_Req, m_Response, totalSize, m_Observer))
								{
									m_Response.setResponseState(Response.State.OK);
									m_Response.setDownloadState(DownloadState.END);
								}
								else
								{
									m_Response.setResponseState(Response.State.UNKNOWN);
								}
							}
							
							is.close();
						}
					}
					else
					{
						if(respCode != 200)
						{
							m_Response.setResponseState(Response.State.REQUEST_FAILED);
						}
						else{
							InputStream is = urlConnection.getInputStream();
							m_Response.setMimeType(getMimeType(urlConnection));
							int contentLen = urlConnection.getContentLength();
							if(true == readFile(m_Controller, is, m_Req.getFileSaveFullPath(), m_Req, m_Response, contentLen, m_Observer))
							{
								m_Response.setResponseState(Response.State.OK);
								m_Response.setDownloadState(DownloadState.END);
							}
							else
							{
								m_Response.setResponseState(Response.State.IO_ERROR);
							}
							is.close();
						}
					}
					
					retryTime = 0;

				}catch(IllegalStateException e){
					m_Response.setResponseState(Response.State.REQUEST_FAILED);
					retryTime = 0;
					Log.w(LOG_TAG, "failed to do http req by "+ e.toString());
					e.printStackTrace();
					
				}catch (MalformedURLException e) {
					m_Response.setResponseState(Response.State.BAD_URL);
					retryTime = 0;
					Log.w(LOG_TAG, "failed to do http req by "+ e.toString());
					e.printStackTrace();
				} catch (IOException e){
					if(e instanceof SocketTimeoutException){	
						retryTime--;
						m_Response.setResponseState(Response.State.TIME_OUT);
						Log.w(LOG_TAG, "socket timeout !  retry " + (NET_TRY_CONN_TIMES-retryTime) + " times." + e.toString());
					}else{
						retryTime = 0;
						m_Response.setResponseState(Response.State.REQUEST_FAILED);
						Log.w(LOG_TAG, "failed to do http req by "+ e.toString());
						e.printStackTrace();
					}
				}finally{
					if(retryTime == 0){
						retryFlag = false;
					}
					else {
						Log.w(LOG_TAG, "retry to do http req ...  ");
						retryFlag = true;
					}
					if(urlConnection != null)
						urlConnection.disconnect();
				}
			}
			
			notifyUser(m_Observer, m_Req, m_Response);
			// cache
			if (mFileCache != null && !m_Req.isSupportBreakpointResume())
			{
				if (m_Response.getResponseState() == State.OK && m_Response.getDownloadState()==DownloadState.END && m_Response.getTotalSize()==m_Response.getDownLoadedSize() && m_Response.getTotalSize()>0)
				{
					mFileCache.putFile(m_Req.getUrl(), m_Req.getFileSaveFullPath());
				}
			}
		}
	}
	
	private boolean readFile(Controller controller, InputStream in, String filePath, FileRequest req, FileResponse response, int contentSize, FileObserver observer){
		Log.i(LOG_TAG, "readFile");
		if(in == null)
			return false;
		
		File downloadFile = new File(filePath);

		if(downloadFile.exists() == true){
			if(false ==downloadFile.delete()){
				Log.e(LOG_TAG, " failed to delete the existed file !!!");
				return false;
			}
			else
			{
//				Log.i(LOG_TAG, "delete the old file, " + filePath);
			}
		}
		
		try {
			if(false == downloadFile.createNewFile()){
				Log.w(LOG_TAG, " failed to createNewFile !!!");
				return false;
			}
			else
			{
//				Log.i(LOG_TAG, "create a new file, " + filePath);
			}
		} catch (IOException e) {
			Log.w(LOG_TAG, "createNewFile exception");
			e.printStackTrace();
			return false;
		}
		
		boolean ok = false;
		OutputStream ouput = null;
		try {
			ouput = new FileOutputStream(downloadFile);
			 byte tmpBuffer[] = new byte[FILE_BUFFER_LEN];
			 int gotLen = 0;
			 int gotTotalLen = 0;
			 do{
				 gotLen = in.read(tmpBuffer);
				 if(gotLen > 0){
					ouput.write(tmpBuffer,0,gotLen);
					gotTotalLen += gotLen;
					response.setTotalSize(contentSize);
					response.setDownloadedSize(gotTotalLen);
					response.setResponseState(State.OK);
					response.setDownloadState(DownloadState.ING);
					notifyUser(observer, req, response);
				 }
			 }
			 while(gotLen != -1 && !controller.isStoped() && mIsInited);
			 ouput.flush();
			 if (gotLen == -1)
				 ok = true;
			 else
			 {
				 Log.i(LOG_TAG, "request is stoped or engine uninited");
				 ok = false;
			 }

		} catch (FileNotFoundException e) {
			Log.w(LOG_TAG, "FileNotFoundException");
			e.printStackTrace();
			ok = false;
		} catch (IOException e) {
			Log.w(LOG_TAG, "IOException");
			e.printStackTrace();
			ok = false;
		}finally{
			try {
				if(ouput != null)
					ouput.close();
			} catch (IOException e) {
				Log.w(LOG_TAG, "when close ouput failed to readFile by " + e.toString());
				e.printStackTrace();
				ok = false;
			}
		}

		Log.i(LOG_TAG, "wrote file Len: " + downloadFile.length() + "  total content length: " + contentSize);
		return ok;
	}
	
	private boolean readFileBreakpoint(Controller controller, InputStream in, String filePath, FileRequest req, FileResponse response, int totalSize, FileObserver observer){
		Log.i(LOG_TAG, "readFileBreakpoint");
		if(in == null)
			return false;

		
		File downloadFile = new File(filePath);
		
		if (!downloadFile.exists())
		{
			try {
				if(false == downloadFile.createNewFile()){
					Log.w(LOG_TAG, " failed to createNewFile !!!");
					return false;
				}
			} catch (IOException e) {
				Log.w(LOG_TAG, "createNewFile exception");
				e.printStackTrace();
				return false;
			}
		}
		
		BufferedInputStream bufferedIn = null;
		RandomAccessFile fileAccess = null;
		byte[] buf = new byte[FILE_BUFFER_LEN];
		
		
		try {
			fileAccess = new RandomAccessFile(filePath, "rw");
			long downloadedSize = fileAccess.length();
			fileAccess.seek(downloadedSize);

			bufferedIn = new BufferedInputStream(in);
			while (!controller.isStoped() && mIsInited) {
				int len = bufferedIn.read(buf, 0, FILE_BUFFER_LEN);
				if (len == -1) {
					break;
				}
				downloadedSize += len;
				fileAccess.write(buf, 0, len);
	
//				Log.i(LOG_TAG, "breakpoint downloaded : " + downloadedSize + " got len: " + len + "  total len: " + totalSize);
				if (downloadedSize <= totalSize) {
					response.setTotalSize(totalSize);
					response.setDownloadedSize(downloadedSize);
					response.setResponseState(State.OK);
					response.setDownloadState(DownloadState.ING);
					notifyUser(observer, req, response);
				}
			}
	
			if (downloadedSize == totalSize) {
				return true;
			}
			else
			{
				Log.i(LOG_TAG, "request is stoped or engine uninited");
			}
		
		} catch (FileNotFoundException e) {
			Log.w(LOG_TAG, "FileNotFoundException");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.w(LOG_TAG, "IOException");
			e.printStackTrace();
			return false;
		}finally{
			try {
				if(bufferedIn != null)
					bufferedIn.close();
			} catch (IOException e) {
				Log.w(LOG_TAG, "when close ouput failed to readFile by " + e.toString());
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	private long getTotalSizeFileHttpResponseHeader(Map<String, List<String>> map)
	{
		String rangeContent = "content-range";
		List<String> rangeList = null;
		for (Map.Entry<String, List<String>>  entry : map.entrySet()) {
			String key = entry.getKey();
			if (rangeContent.equalsIgnoreCase(key))
			{
				rangeList = entry.getValue();
				break;
			}
		}
		if (rangeList != null && rangeList.size()>0)
		{
			String content = rangeList.get(0);
			int i = content.lastIndexOf("/");
			String totalStr = content.substring(i+1);
			if (StringTools.isPureNumber(totalStr))
			{
				return Long.parseLong(totalStr);
			}
		}
		
		return 0;
	}
	
	/**
	 * get response body encoding(charset), e.g. GBK, UTF-8, iso-8859-1 and so on.
	 * @param HttpURLConnection
	 * @return charset string.
	 */
	private String getContentEncode(HttpURLConnection conn)
	{
		String contentType = conn.getContentType();
		if (contentType == null || contentType.length()==0)
		{
			return null;
		}
		contentType = contentType.toLowerCase();
		
		String charset = null;
		
		if (!contentType.contains("charset"))
		{
			return null;
		}
		
		int i = contentType.indexOf("charset");
		String remainStr = contentType.substring(i);
		
		String[] array = remainStr.split("=");
		if (array.length!=2)
		{
			return null;
		}
		
		charset = array[1].trim();
		if (charset.length()==0)
		{
			return null;
		}
		
		return charset;
	}

	/**
	 * get mime type, e.g. text/html.
	 * @param HttpURLConnection
	 * @return mime
	 */
	private String getMimeType(HttpURLConnection conn)
	{
//		Log.i(LOG_TAG, "http resp headers -------------------------");
//		Map<String, List<String>> map = conn.getHeaderFields();
//		for (Map.Entry<String, List<String>>  entry : map.entrySet()) {
//			Log.i(LOG_TAG, "header:" + entry.getKey() + "  value:" + entry.getValue());
//		}
//		Log.i(LOG_TAG, "http resp headers ------------------------- end");
		
		String contentType = conn.getContentType();
		if (contentType == null || contentType.length()==0)
		{
			return null;
		}		
		if (!contentType.contains(";"))
		{
			return contentType;
		}
		
		String[] array = contentType.split(";");
		String mime = array[0].trim();
		if (mime.length()==0)
		{
			return null;
		}
		
		return mime;
	}
}
