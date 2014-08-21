package vopen.download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vopen.app.BaseApplication;
import vopen.db.DBApi;
import vopen.db.DBApi.DownLoadInfo;
import vopen.db.DBApi.EDownloadStatus;
import vopen.db.DBUtils;
import vopen.db.VopenContentProvider.DownloadManagerHelper;
import vopen.response.CourseInfo;
import vopen.response.VideoInfo;
import vopen.response.VideoSubTitleInfo;
import vopen.tools.FileUtils;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.text.TextUtils;

import com.netease.vopen.pal.Constants;
import common.pal.PalLog;
import common.util.Util;

public class DownloadService extends IntentService {
	public static final int MIN_VIDEO_SIZE = 102400;
	
	private final static String TAG = "DownloadService";

	private DownloadService mContext;
	private ContentResolver mContentResolver;
	private DownloadServiceObserver mDownloadServiceObserver;
	private BaseApplication mApp;
	
	//Service监听器
    private DownloadListener mDownloadListener;

	public DownloadService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

	private class DownloadServiceObserver extends ContentObserver {

		public DownloadServiceObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean deliverSelfNotifications() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);

			//监听数据库变化，控制下载状态的变化
			if(BaseApplication.getID() <= 0)
				return;
			DownLoadInfo c = DBUtils.getDownloadInfoById(mContext, String.valueOf(BaseApplication.getID()));
			//下载状态不为“下载中”的时候，中断下载任务
			if (c.mDownload_status != EDownloadStatus.DOWNLOAD_DOING) {
				removeAllDownloadTaskFromList();
				//取消阻塞，下载下一个文件
                synchronized(mContext) {
					mContext.notifyAll();
				}
			}
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = this;
		mApp = (BaseApplication)((IntentService)this).getApplication();
		mContentResolver = mContext.getContentResolver();
		mDownloadServiceObserver = new DownloadServiceObserver(new Handler());
		mContentResolver.registerContentObserver(
				DownloadManagerHelper.getUri(), true, mDownloadServiceObserver);
		
		if(mApp != null){
			mDownloadListener = (DownloadListener)mApp;
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (mDownloadServiceObserver != null) {
			mContentResolver.unregisterContentObserver(mDownloadServiceObserver);
			mDownloadServiceObserver = null;
		}
//		BaseApplication.setID(-1);
//		mApp.pauseUpdateProgress();
		// 此service销毁时,暂停所有下载
		removeAllDownloadTaskFromList();
		
		//更新状态
//		synchronized (mDownloadListener) {
//			if(BaseApplication.getID() >= 0){
//				DownLoadInfo updateDownloadInfo = new DownLoadInfo();
//				updateDownloadInfo.mDownload_status = EDownloadStatus.DOWNLOAD_WAITTING;
//				
//				int downSize = BaseApplication.getDownCurSize();
//				int total = BaseApplication.getTotalSize();
//				if(downSize > 0) updateDownloadInfo.mDownload_size = downSize;
//				if(total > 0) updateDownloadInfo.mTotal_size = total;
//				updateDownloadInfo.m_id = String.valueOf(BaseApplication.getID());
//			    DBApi.updateDownloadInfo(mContext, updateDownloadInfo);
//			    
//			    if(mDownloadListener != null){
//					mDownloadListener.onFinishDownload(EDownloadStatus.DOWNLOAD_WAITTING, BaseApplication.getID(), downSize, total);
//				}
//			}
//		}
	    
		PalLog.v(TAG, "DownloadService onDestroy");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

		ContentValues values = new ContentValues();
		int status = 0;
		EDownloadStatus enumValues = EDownloadStatus.DOWNLOAD_NO;
		int downSize = 0;
		int total = 0;
		
		final int id = intent.getIntExtra(DownloadManagerHelper.Download_ID, -1);
		PalLog.v(TAG, "Download_ID " + id);

		Cursor c = DBApi.queryDownloadById(mContext, String.valueOf(id),null);
		
		try {

			if (c != null && c.moveToFirst()) {
				status = c.getInt(c.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS));
				PalLog.v(TAG, "status = " + status);
				final String plid = c.getString(c.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
				final String strName = c.getString(c.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
		        final int pnum = c.getInt(c.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
		        downSize = c.getInt(c.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
		        final int totalSize = c.getInt(c.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
		        final String url = c.getString(c.getColumnIndex(DownloadManagerHelper.DOWNLOAD_URL));
		        c.close();
		        
				if (DBUtils.intToEDownloadStatus(status) == EDownloadStatus.DOWNLOAD_WAITTING) {
					
					//更新状态
					DownLoadInfo updateDownloadInfo = new DownLoadInfo();
					updateDownloadInfo.mDownload_status = EDownloadStatus.DOWNLOAD_DOING;
					
//					if(downSize > 0) updateDownloadInfo.mDownload_size = downSize;
//					if(total > 0) updateDownloadInfo.mTotal_size = total;
//					updateDownloadInfo.m_id = String.valueOf(BaseApplication.getID());
//				    DBApi.updateDownloadInfo(mContext, updateDownloadInfo);
				    //更新下载状态
				    DBApi.updateDownloadStatus(mContext, String.valueOf(id), EDownloadStatus.DOWNLOAD_DOING);
					
					//获取文件大小//视频大小小于100k时认为视频出错，不下载该视频，以数据库视频大小为准为准
					total = DownloadUtils.getHttpFileSize(mContext, url, null, null);
//					if(total == 0)total = totalSize;
					PalLog.v(TAG, "total is "+total);
					if(total > MIN_VIDEO_SIZE) {
						//如果获取的文件大小和数据库中的不一致，更新数据库
                        if(total != totalSize ) {
        				    //更新文件大小
        				    DBApi.updateTotalSize(mContext, String.valueOf(id), total);
                        }
                        
						BaseApplication.setID(id);
						BaseApplication.setmDownloadCurrentName(strName);
						BaseApplication.setmDownloadCurrentNumber(pnum);
						//初始化已下载的大小
						BaseApplication.initDownCurSize(downSize);
						BaseApplication.initTotalSize(total);
						
						//added by cxl
						if(mDownloadListener != null){
							mDownloadListener.onStartDownload(id, downSize, total);
						}
/*
 * 	从课程表里面查找出当前课程详情，判断是否是字幕分离版本，是的话起http阻塞下载字幕，字幕下载成功才继续下载视频，字幕下载失败不再下载视频					
 */
						String subUrl1 = null;//"http://swf.ws.126.net/v/open/swf/caption_test_cn.srt";
						String subUrl2 = null;//"http://swf.ws.126.net/v/open/swf/caption_test_en.srt";
						boolean shouldDownVideo = true;
//						if(Util.isStringEmpty(FileUtils.getSavedDownloadSubPath(plid, pnum, 1, , true))){
						CourseInfo cInfo = DBUtils.getCourseByPlid(mContext, plid);
						if(cInfo != null && cInfo.videoList != null && cInfo.videoList.size() >= pnum){
							VideoInfo vInfo = cInfo.videoList.get(pnum-1);
							if(vInfo != null && vInfo.protoVersion >= 2){
								List<VideoSubTitleInfo> subList = vInfo.subList;
								if(subList != null && subList.size() >= 1 ) {
									subUrl1 = subList.get(0).subUrl;
									if(subList.size() >= 2){
										subUrl2 = subList.get(1).subUrl;
									}
								}
							}
						}
						cInfo = null;
						if (!TextUtils.isEmpty(subUrl1) && TextUtils.isEmpty(FileUtils.getSavedDownloadSubPath(this,plid, pnum, 1, subUrl1, true, false))) {
							EDownloadStatus subDownStatus = EDownloadStatus.DOWNLOAD_DONE;
							// 下载第一个字幕
							subDownStatus = DownloadUtils.downloadSubFile(
									mContext, subUrl1, plid, pnum, 1);
							if (subDownStatus.value() == EDownloadStatus.DOWNLOAD_DONE.value()) {
								if (!TextUtils.isEmpty(subUrl2) && TextUtils.isEmpty(FileUtils.getSavedDownloadSubPath(this,plid, pnum, 2, subUrl2, true, false))) {
									subDownStatus = DownloadUtils.downloadSubFile(mContext, subUrl2, plid, pnum, 2);
									if (subDownStatus.value() != EDownloadStatus.DOWNLOAD_DONE.value()) {
										shouldDownVideo = false;
										enumValues = subDownStatus;
									}
								} else {
									shouldDownVideo = false;
									enumValues = subDownStatus;
								}
							}
						}
						
                        //开启多线程下载
						if(shouldDownVideo){
							startMultiDownloadTask(id, downSize, pnum, plid, total, url);
							//阻塞，以便一次只下载一个文件
		                	synchronized(mContext) {
		                		mContext.wait();
		                	}	
						}
						
	                	//下载结束时停止更新进度条,并获取已下载的大小
//	                	downSize = mApp.getDownCurSizeAndPauseUpdateProgress();
	    				//判断是否下载完成
	                	if(DownloadPrefHelper.isDownFinish(mContext, plid, pnum, total)) {
	                		enumValues = EDownloadStatus.DOWNLOAD_DONE;	
	    					downSize = total;
	    					//再刷新一次，使 进度条 走满
	    					mDownloadListener.onDownloadProgeress(BaseApplication.getTotalSize() - BaseApplication.getDownCurSize());
		        			//修改已下载完成文件的后缀
	    					DownloadUtils.renameCompletedFile(this,plid, pnum);	  	    					
	    				}
					} else {
						
						BaseApplication.setID(id);
						BaseApplication.setmDownloadCurrentName(strName);
						BaseApplication.setmDownloadCurrentNumber(pnum);
						//初始化已下载的大小
						BaseApplication.initDownCurSize(downSize);
						BaseApplication.initTotalSize(totalSize);
						
						//可能为需要验证网络,也可能视频错误，归为网络错误 2012-12-14 by echo_chen
						enumValues = EDownloadStatus.DOWNLOAD_FAILED;
//						if(total == -1){
//							//可能为需要验证网络的重定向，也归为网络错误
//							enumValues = EDownloadStatus.DOWNLOAD_FAILED;
//						}else if(total == -2){
//							//网络错误
//							enumValues = EDownloadStatus.DOWNLOAD_FAILED;
//						}else{
//							enumValues = EDownloadStatus.DOWNLOAD_FAILED_VIDEO_ERROR;
//						}
					}
					
					downSize = BaseApplication.getDownCurSize();
					//更新下载完成后的数据库状态
//				    DBApi.updateFinishDownload(mContext, String.valueOf(id), downSize, enumValues);
					//一个文件的下载结束时，更新数据库中的数据
					if (enumValues == EDownloadStatus.DOWNLOAD_FAILED
							|| enumValues == EDownloadStatus.DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME
							|| enumValues == EDownloadStatus.DOWNLOAD_DONE
							|| enumValues == EDownloadStatus.DOWNLOAD_FAILED_VIDEO_ERROR) {
						PalLog.v(TAG, "DownloadService.finish and status = " + enumValues);
						updateDownloadInfo = new DownLoadInfo();
						updateDownloadInfo.mDownload_status = enumValues;
						updateDownloadInfo.mCourse_pnumber = pnum;
						if(enumValues != EDownloadStatus.DOWNLOAD_FAILED_VIDEO_ERROR){
							//不是视频错误时才更新视频大小
							if(downSize > 0) updateDownloadInfo.mDownload_size = downSize;
							if(total > MIN_VIDEO_SIZE) updateDownloadInfo.mTotal_size = total;
							else total = totalSize;//避免未验证网络
						}else {
							total = totalSize;
						}
						PalLog.v(TAG, "totalSize = " + totalSize + " total = " + total);
						updateDownloadInfo.m_id = String.valueOf(id);
					    DBApi.updateDownloadInfo(mContext, updateDownloadInfo);
					    
						//added by cxl
						if(mDownloadListener != null){   
							mDownloadListener.onFinishDownload(enumValues, id, downSize, total);
						}
					}
					//设置正在下载的id为-1
					BaseApplication.setID(-1);
				}
			} else {
				DownLoadInfo updateDownloadInfo = new DownLoadInfo();
				updateDownloadInfo.mDownload_status = EDownloadStatus.DOWNLOAD_FAILED;
				PalLog.v(TAG, "downSize = " + downSize + " total = " + total);
				//更新下载完成后的数据库状态
			    DBApi.updateFinishDownload(mContext, String.valueOf(id), downSize, EDownloadStatus.DOWNLOAD_FAILED);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(c != null) {
				c.close();
				c = null;
			}
		}

	}
  
	/**
	 * 开启一个下载的异步任务
	 * @param info
	 */
	private void startDownloadTask(DownloadTaskInfo info) {
/*		DownloadTask task = new DownloadTask(mContext, DataAsyncTask.TASK_DOWNLOAD, null);
		task.execute(info);
		addDownloadTaskToList(task);*/
		vopen.download.DownloadThread task = new vopen.download.DownloadThread(mContext, info);
		task.start();
		addDownloadTaskToList(task);
	}
	
	/**
	 * 开启多线程下载
	 * @param id
	 * @param downSize
	 * @param pnum
	 * @param plid
	 * @param total
	 * @param url
	 * @throws IOException
	 */
	private void startMultiDownloadTask(int id, int downSize, int pnum, 
			String plid, int total, String url) throws IOException{
		int[] threadInfos = DownloadPrefHelper.getThreadInfo(mContext, plid, pnum);
		boolean hasDownloadThreadInfo = false;
		for(int threadInfo : threadInfos){
			if(threadInfo != 0){
				hasDownloadThreadInfo = true;
				break;
			}
		}
		File saveFile = DownloadUtils.getSaveFile(mContext, plid, pnum, hasDownloadThreadInfo);
		threadInfos = DownloadPrefHelper.getThreadInfo(mContext, plid, pnum);//再次获得断点数据
		DownloadTaskInfo info = null;
		int len = Constants.Download_Thread_Count;
		if (threadInfos != null) {
			for (int i=0; i<len; i++) {
				info = new DownloadTaskInfo();
				info.mID = id;
				info.mDownloadSize = downSize;
				info.mPnumber = pnum;
				info.mPlid = plid;
				info.mTotalSize = total;
				info.mUrl = url;
				info.mSaveFile = saveFile;
				info.mCurThread = i;
				info.mStartPos = threadInfos[i];
				
				//added by cxl
				info.mDownloadListener = (mDownloadListener == null?null: mDownloadListener);
				
				startDownloadTask(info);
			}
		}
	}
	
	private ArrayList<vopen.download.DownloadThread> mDownloadTaskList;

	private ArrayList<vopen.download.DownloadThread> getDownloadTaskList() {
		if (null == mDownloadTaskList) {
			mDownloadTaskList = new ArrayList<vopen.download.DownloadThread>();
		}
		return mDownloadTaskList;
	}

	public void addDownloadTaskToList(vopen.download.DownloadThread task) {
		ArrayList<vopen.download.DownloadThread> aList = getDownloadTaskList();
		aList.add(task);
	}

	public void removeDownloadTaskFromList(vopen.download.DownloadThread task) {
		getDownloadTaskList().remove(task);
		task.pause();
		//task.cancel(true, true);
		task = null;
	}

	/**
	 * 删除并结束所有的task
	 * 
	 */
	private void removeAllDownloadTaskFromList() {
		if (mDownloadTaskList != null) {
			int len = mDownloadTaskList.size();
			vopen.download.DownloadThread a = null;
			for (int i = 0; i < len; i++) {
				a = mDownloadTaskList.get(i);
/*				if (a != null && !a.isCancelled()) {
					a.pause();
					a.cancel(true, true);
			
					Logger.e("a.cancel "+a.isCancelled());
					a = null;
				}*/
				if (a != null) {
					a.pause();
					a.exit();
					a = null;
				}
			}
			mDownloadTaskList.clear();
			mDownloadTaskList = null;
		}

	}
	


}
