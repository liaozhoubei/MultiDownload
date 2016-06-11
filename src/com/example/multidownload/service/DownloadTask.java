package com.example.multidownload.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.multidownload.db.ThreadDAO;
import com.example.multidownload.db.ThreadDAOImple;
import com.example.multidownload.entitis.FileInfo;
import com.example.multidownload.entitis.ThreadInfo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadTask {
	private Context mComtext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinished = 0;
	private int mThreadCount = 1;
	public boolean mIsPause = false;
	private List<DownloadThread> mThreadlist = null;
	public static ExecutorService sExecutorService = Executors.newCachedThreadPool();

	public DownloadTask(Context comtext, FileInfo fileInfo, int threadCount) {
		super();
		this.mThreadCount = threadCount;
		this.mComtext = comtext;
		this.mFileInfo = fileInfo;
		this.mDao = new ThreadDAOImple(mComtext);
	}

	public void download() {
		// 从数据库中获取下载的信息
		List<ThreadInfo> list = mDao.queryThreads(mFileInfo.getUrl());
		if (list.size() == 0) {
			int length = mFileInfo.getLength();
			int block = length / mThreadCount;
			for (int i = 0; i < mThreadCount; i++) {
				// 划分每个线程开始下载和结束下载的位置
				int start = i * block;
				int end = (i + 1) * block - 1;
				if (i == mThreadCount - 1) {
					end = length - 1;
				}
				ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), start, end, 0);
				list.add(threadInfo);
			}
		}
		mThreadlist = new ArrayList<DownloadThread>();
		for (ThreadInfo info : list) {
			DownloadThread thread = new DownloadThread(info);
//			thread.start();
			// 使用线程池执行下载任务
			DownloadTask.sExecutorService.execute(thread);
			mThreadlist.add(thread);
			// 如果數據庫不存在下載信息，添加下載信息
			mDao.insertThread(info);
		}
	}

	public synchronized void checkAllFinished() {
		boolean allFinished = true;
		for (DownloadThread thread : mThreadlist) {
			if (!thread.isFinished) {
				allFinished = false;
				break;
			}
		}
		if (allFinished == true) {
			// 下載完成后，刪除數據庫信息
			mDao.deleteThread(mFileInfo.getUrl());
			// 通知UI哪个线程完成下载
			Intent intent = new Intent(DownloadService.ACTION_FINISHED);
			intent.putExtra("fileInfo", mFileInfo);
			mComtext.sendBroadcast(intent);

		}
	}

	class DownloadThread extends Thread {
		private ThreadInfo threadInfo = null;
		// 标识线程是否执行完毕
		public boolean isFinished = false;

		public DownloadThread(ThreadInfo threadInfo) {
			this.threadInfo = threadInfo;
		}

		@Override
		public void run() {

			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream is = null;
			try {
				URL url = new URL(mFileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");

				int start = threadInfo.getStart() + threadInfo.getFinished();
				// 設置下載文件開始到結束的位置
				conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
				File file = new File(DownloadService.DownloadPath, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				mFinished += threadInfo.getFinished();
				Intent intent = new Intent();
				intent.setAction(DownloadService.ACTION_UPDATE);
				int code = conn.getResponseCode();
				if (code == HttpURLConnection.HTTP_PARTIAL) {
					is = conn.getInputStream();
					byte[] bt = new byte[1024];
					int len = -1;
					// 定义UI刷新时间
					long time = System.currentTimeMillis();
					while ((len = is.read(bt)) != -1) {
						raf.write(bt, 0, len);
						// 累计整个文件完成进度
						mFinished += len;
						// 累加每个线程完成的进度
						threadInfo.setFinished(threadInfo.getFinished() + len);
						// 設置爲500毫米更新一次
						if (System.currentTimeMillis() - time > 1000) {
							time = System.currentTimeMillis();
								// 发送已完成多少
								intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
								// 表示正在下载文件的id
								intent.putExtra("id", mFileInfo.getId());
								Log.i("test", mFinished * 100 / mFileInfo.getLength() + "");
								// 發送廣播給Activity
								mComtext.sendBroadcast(intent);
						}
						if (mIsPause) {
							mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), threadInfo.getFinished());
							return;
						}
					}
				}
				// 标识线程是否执行完毕
				isFinished = true;
				// 判断是否所有线程都执行完毕
				checkAllFinished();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
				try {
					if (is != null) {
						is.close();
					}
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
