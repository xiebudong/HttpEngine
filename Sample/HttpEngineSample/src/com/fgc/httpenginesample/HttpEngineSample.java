package com.fgc.httpenginesample;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fgc.http.engine.HttpEngine;
import com.fgc.http.engine.HttpEngine.FileObserver;
import com.fgc.http.engine.HttpEngine.StringObserver;
import com.fgc.http.protocal.Controller;
import com.fgc.http.protocal.FileRequest;
import com.fgc.http.protocal.FileResponse;
import com.fgc.http.protocal.Response.State;
import com.fgc.http.protocal.StringRequest;
import com.fgc.http.protocal.StringResponse;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HttpEngineSample extends Activity {
	final String LOG_TAG = "HttpEngineSample";
	
	Button mBtnLog;
	
	ProgressBar mPgb1;
	TextView mText1;
	ToggleButton mTb1;
	
	ProgressBar mPgb2;
	TextView mText2;
	ToggleButton mTb2;
	
	ProgressBar mPgb3;
	TextView mText3;
	ToggleButton mTb3;
	
	ProgressBar mPgb4;
	TextView mText4;
	ToggleButton mTb4;
	
	ProgressBar mPgb5;
	TextView mText5;
	ToggleButton mTb5;
	
	ProgressBar mPgb6;
	TextView mText6;
	ToggleButton mTb6;
	
	TextView mTextClearBreakpointFiles;
	
	Map<Integer, Controller> mControllerMap = new HashMap<Integer, Controller>();
	
	String mFileDownloadUrl = "http://upgrade.top123.tv/Uploads/apk/targetv_phone_client_lastest.apk";
	String mNormalDownloadSavePath = Environment.getExternalStorageDirectory().getPath() + "/" + "engine_@.apk";
	String mBreakpointDownloadSavePath = Environment.getExternalStorageDirectory().getPath() + "/" + "engine_breakpoint_@.apk";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "onCreate");
		setContentView(R.layout.activity_main);
		HttpEngine.instance().init();
		HttpEngine.instance().needDebug(true);
		
		mBtnLog = (Button)findViewById(R.id.btn_request_string);
		
		mPgb1 = (ProgressBar)findViewById(R.id.pgb_1);
		mText1 = (TextView)findViewById(R.id.text_1);
		mTb1 = (ToggleButton)findViewById(R.id.tb_1);
		mTb1.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		mPgb2 = (ProgressBar)findViewById(R.id.pgb_2);
		mText2 = (TextView)findViewById(R.id.text_2);
		mTb2 = (ToggleButton)findViewById(R.id.tb_2);
		mTb2.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		mPgb3 = (ProgressBar)findViewById(R.id.pgb_3);
		mText3 = (TextView)findViewById(R.id.text_3);
		mTb3 = (ToggleButton)findViewById(R.id.tb_3);
		mTb3.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		mPgb4 = (ProgressBar)findViewById(R.id.pgb_4);
		mText4 = (TextView)findViewById(R.id.text_4);
		mTb4 = (ToggleButton)findViewById(R.id.tb_4);
		mTb4.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		mPgb5 = (ProgressBar)findViewById(R.id.pgb_5);
		mText5 = (TextView)findViewById(R.id.text_5);
		mTb5 = (ToggleButton)findViewById(R.id.tb_5);
		mTb5.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		mPgb6 = (ProgressBar)findViewById(R.id.pgb_6);
		mText6 = (TextView)findViewById(R.id.text_6);
		mTb6 = (ToggleButton)findViewById(R.id.tb_6);
		mTb6.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		mTextClearBreakpointFiles = (TextView)findViewById(R.id.text_clear_file);
		mTextClearBreakpointFiles.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClearFileClick();
			}
		});
	}

	// for request string btn
	public void onClickLogBtn(View v)
	{
		Log.i(LOG_TAG, "onClickLogBtn");
		requestString();
	}
	
	// clear breakpoint download files.
	public void onClearFileClick()
	{
		Log.i(LOG_TAG, "onClearFileClick");
		File file = null;
		file = new File(mBreakpointDownloadSavePath.replace("@", String.valueOf(4)));
		if (file.exists())
		{
			file.delete();
			mPgb4.setProgress(0);
			mTb4.setChecked(false);
			mText4.setText("%0");
		}
		file = new File(mBreakpointDownloadSavePath.replace("@", String.valueOf(5)));
		if (file.exists())
		{
			file.delete();
			mPgb5.setProgress(0);
			mTb5.setChecked(false);
			mText5.setText("%0");
		}
		file = new File(mBreakpointDownloadSavePath.replace("@", String.valueOf(6)));
		if (file.exists())
		{
			file.delete();
			mPgb6.setProgress(0);
			mTb6.setChecked(false);
			mText6.setText("%0");
		}
	}
	
	private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener()
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			switch (buttonView.getId())
			{
			case R.id.tb_1:
				if (isChecked)
				{
					downloadFile(1, false);
				}
				else
				{
					mControllerMap.get(1).stop();
				}
				break;
			case R.id.tb_2:
				if (isChecked)
				{
					downloadFile(2, false);
				}
				else
				{
					mControllerMap.get(2).stop();
				}
				break;
			case R.id.tb_3:
				if (isChecked)
				{
					downloadFile(3, false);
				}
				else
				{
					mControllerMap.get(3).stop();
				}
				break;
			case R.id.tb_4:
				if (isChecked)
				{
					downloadFile(4, true);
				}
				else
				{
					mControllerMap.get(4).stop();
				}
				break;
			case R.id.tb_5:
				if (isChecked)
				{
					downloadFile(5, true);
				}
				else
				{
					mControllerMap.get(5).stop();
				}
				break;
			case R.id.tb_6:
				if (isChecked)
				{
					downloadFile(6, true);
				}
				else
				{
					mControllerMap.get(6).stop();
				}
				break;
			}
		}
		
	};
	
	void requestString()
	{
		Log.i(LOG_TAG, "requestString");
		StringRequest req = new StringRequest("http://www.baidu.com");
		Controller controller = HttpEngine.instance().requestString(req, new StringObserver() {
			
			@Override
			public void done(StringRequest req, StringResponse resp) {
				// TODO Auto-generated method stub
				Log.i(LOG_TAG, req.toString());
				Log.i(LOG_TAG, resp.getRespString());
			}
		});
		if (controller == null)
		{
			Log.w(LOG_TAG, "request failed , check your network, or filter HttpEngine log for more detail info.");
		}
	}
	
	void downloadFile(int id, boolean isBreakpoint)
	{
		Log.i(LOG_TAG, "downloadFile id: " + id);
		FileRequest req = null;
		if (isBreakpoint)
		{
			req = new FileRequest(mFileDownloadUrl, mBreakpointDownloadSavePath.replace("@", String.valueOf(id)));
			req.setIsSupportBreakpointResume(true);
		}
		else
		{
			req = new FileRequest(mFileDownloadUrl, mNormalDownloadSavePath.replace("@", String.valueOf(id)));
		}
		req.setId(id);
		Controller controller = HttpEngine.instance().requestFile(req, new FileObserver() {
			
			@Override
			public void done(FileRequest req, FileResponse resp) {
				// TODO Auto-generated method stub				
//				Log.i(LOG_TAG, req.toString());
//				Log.i(LOG_TAG, resp.toString());
				
				if (resp.getResponseState() != State.OK)
				{
					Log.w(LOG_TAG, "req " + req.getId() + " failed");
				}
				
				runOnUiThread(new UiUpdater(req.getId(), resp.getDownloadPercent()));
			}
		});
		if (controller == null)
		{
			Log.w(LOG_TAG, "request failed , check your network, or filter HttpEngine log for more detail info.");
		}
		else
		{
			mControllerMap.put(id, controller);
		}
	}

	private class UiUpdater implements Runnable
	{
		int m_Id;
		int m_Percent;
		
		public UiUpdater(int id, int percent)
		{
			m_Id = id;
			m_Percent = percent;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (m_Id == 1)
			{
				mPgb1.setProgress(m_Percent);
				mText1.setText("%" + m_Percent);
			}
			if (m_Id == 2)
			{
				mPgb2.setProgress(m_Percent);
				mText2.setText("%" + m_Percent);
			}
			if (m_Id == 3)
			{
				mPgb3.setProgress(m_Percent);
				mText3.setText("%" + m_Percent);
			}
			if (m_Id == 4)
			{
				mPgb4.setProgress(m_Percent);
				mText4.setText("%" + m_Percent);
			}
			if (m_Id == 5)
			{
				mPgb5.setProgress(m_Percent);
				mText5.setText("%" + m_Percent);
			}
			if (m_Id == 6)
			{
				mPgb6.setProgress(m_Percent);
				mText6.setText("%" + m_Percent);
			}
		}
		
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.i(LOG_TAG, "onPause  " + isFinishing());
		if (isFinishing())
		{
			HttpEngine.instance().uninit();
		}
	}
}
