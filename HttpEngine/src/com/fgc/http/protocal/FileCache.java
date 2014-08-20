package com.fgc.http.protocal;

import java.io.File;

/**
 * @author FGC
 * @date 2014-08-18
 */
public interface FileCache {
	public String getFile(String url);
	public void  putFile(String url, String fileFullPath);
	public void uninit();
}
