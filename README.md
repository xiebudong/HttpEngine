HttpEngine
==========

android平台的一个简单的http引擎，支持并发请求字符串和下载文件， 同时支持断点续传下载

引擎已经打包为lib工程。

另附示例应用 - HttpEngineSample


引擎以单例模式使用。

API使用很简单：

`HttpEngine.instance().init();`

`HttpEngine.instance().uninit();`

`HttpEngine.instance().requestString(...);`

`HttpEngine.instance().requestFile(...);`


比如请求一个网页：

```
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
```

示例程序截图：

![sample screen shot](http://c.hiphotos.bdimg.com/album/s%3D900%3Bq%3D90/sign=8458a0c9fe1f4134e437097e1524e4f7/8b13632762d0f703b3c9a8f60bfa513d2697c5a4.jpg)

