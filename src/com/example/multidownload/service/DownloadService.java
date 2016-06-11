package com.example.multidownload.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.multidownload.entitis.FileInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * 下載服務類，用於執行下載任務，并且將下載進度傳遞到Activity中
 */
public class DownloadService extends Service {

	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final String ACTION_FINISHED = "ACTION_FINISHED";
	// 文件的保存路徑
	public static final String DownloadPath = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/download/";
	public static final int MSG_INIT = 0;

	private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<Integer, DownloadTask>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获得Activity穿来的参数
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.i("test", "START" + fileInfo.toString());
			InitThread initThread = new InitThread(fileInfo);
			DownloadTask.sExecutorService.execute(initThread);			
		} else if (ACTION_STOP.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			DownloadTask task = mTasks.get(fileInfo.getId());
			if (task != null) {
				// 停止下载任务
				task.mIsPause = true;
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// 從InitThread綫程中獲取FileInfo信息，然後開始下載任務
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo = (FileInfo) msg.obj;
				Log.i("test", "INIT:" + fileInfo.toString());
				// 獲取FileInfo對象，開始下載任務
				DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 3);
				task.download();
				// 把下载任务添加到集合中
				mTasks.put(fileInfo.getId(), task);
				// 发送启动下载的通知
				Intent intent = new Intent(ACTION_START);
				intent.putExtra("fileInfo", fileInfo);
				sendBroadcast(intent);
				break;
			}
		};
	};

	// 初始化下載綫程，獲得下載文件的信息
	class InitThread extends Thread {
		private FileInfo mFileInfo = null;

		public InitThread(FileInfo mFileInfo) {
			super();
			this.mFileInfo = mFileInfo;
		}

		@Override
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			try {
				URL url = new URL(mFileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");
				int code = conn.getResponseCode();
				int length = -1;
				if (code == HttpURLConnection.HTTP_OK) {
					length = conn.getContentLength();
				}
				//如果文件长度为小于0，表示获取文件失败，直接返回
				if (length <= 0) {
					return;
				}
				// 判斷文件路徑是否存在，不存在這創建
				File dir = new File(DownloadPath);
				if (!dir.exists()) {
					dir.mkdir();
				}
				// 創建本地文件
				File file = new File(dir, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.setLength(length);
				// 設置文件長度
				mFileInfo.setLength(length);
				// 將FileInfo對象傳遞給Handler
				Message msg = Message.obtain();
				msg.obj = mFileInfo;
				msg.what = MSG_INIT;
				mHandler.sendMessage(msg);
//				msg.setTarget(mHandler);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
				try {
					if (raf != null) {
						raf.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			super.run();
		}
	}

}