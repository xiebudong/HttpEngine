HttpEngine
==========

android平台的一个简单的http引擎，支持请求字符串和下载文件， 同时下载文件支持断点续传

引擎工程已经打包为lib工程。

另附示例应用 - HttpEngineSample


引擎以单例模式使用。

API使用很简单：

HttpEngine.instance().init();
HttpEngine.instance().uninit();

HttpEngine.instance().requestString(...);
HttpEngine.instance().requestFile(...);

比如请求一个网页：

	void requestString()
	{
		  StringRequest req = new StringRequest("http://www.baidu.com");
		  HttpEngine.instance().requestString(req, new StringObserver() {
			
			  @Override
		  	public void done(StringRequest req, StringResponse resp) 
		  	{
				  // TODO Auto-generated method stub
				  Log.i(LOG_TAG, resp.getRespString());
			  }
		  });
	}
