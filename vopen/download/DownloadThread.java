package vopen.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;


import vopen.app.BaseApplication;
import vopen.db.DBApi;
import vopen.db.DBApi.DownLoadInfo;
import vopen.db.DBApi.EDownloadStatus;
import android.content.Context;

import common.framework.http.AndroidHttpClient;
import common.framework.http.HttpUtils;
import common.net.NetUtils;
import common.pal.PalLog;

public class DownloadThread extends Thread {
	private final static String TAG = "DownloadThread";
	private DownloadTaskInfo mInfo;
	private Context mContext;

	/**
	 * {@inheritDoc}
	 * 
	 * @param context
	 * @param taskType
	 * @param callback
	 */
	public DownloadThread(Context context, DownloadTaskInfo info) {
		mInfo = info;
		mContext = context;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		final DownloadTaskInfo info = mInfo;
		if (info != null) {
			download(info);
		}
	}
	
	/**
	 * 下载文件缓冲的大小
	 */
	public static int BUFFER_SIZE = 8 * 1024;

	/**
	 * 下载文件
	 * @param info
	 */
	private void download(DownloadTaskInfo info) {
		final int total = info.mTotalSize;
		//当前线程id
		final int tnum = info.mCurThread;
		final String url = info.mUrl;
		//分块大小
		final int block = (int)(total / info.mThreadCount);
		//断点开始处
		int startPos = info.mStartPos;
		//断点结束处
		final int endPos = tnum == info.mThreadCount - 1 ? total : block*(tnum+1);
		final File saveFile = info.mSaveFile;
		//课程第几集
		final int pnum = info.mPnumber;
		final String plid = info.mPlid;
		final int id = info.mID;
		
		//计算初始断点
		if(startPos < block*tnum) {
			startPos = block*tnum;
		}
		//断点部分已下载完成，直接返回
		if(startPos >= endPos) {
			return;
		}
		AndroidHttpClient httpClient = HttpUtils.getAndroidHttpClient(mContext);
		try {
			HttpResponse response = DownloadUtils.httpDownload(httpClient, url, startPos, endPos, null, null);
			if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT && response.getFirstHeader("Content-Range") != null) {
				HttpEntity entity = response.getEntity();
				if (entity != null && entity.getContentLength() > DownloadService.MIN_VIDEO_SIZE) {
					InputStream inStream = entity.getContent();
					byte[] buffer = new byte[BUFFER_SIZE];
					int offset = 0;
					RandomAccessFile threadfile = new RandomAccessFile(saveFile, "rwd");
					threadfile.seek(startPos);
	
					while (!mIsPaused && (offset = inStream.read(buffer)) != -1 ) {
						threadfile.write(buffer, 0, offset);
						startPos += offset;
//						PalLog.d("test", "TreadId:"+mInfo.mCurThread+"|offset:"+offset);
						//added by cxl
						if(mInfo.mDownloadListener != null){
							mInfo.mDownloadListener.onDownloadProgeress(offset);
						}
						//记录下载的大小
						BaseApplication.setDownCurSize(offset);
						Thread.sleep(10);
					}
					mIsPaused = false;
				    //记录断点开始处
					DownloadPrefHelper.recordDownload(mContext, plid, pnum, tnum, startPos);
//					PalLog.e("download","DownloadTask****************************** paused"+", tnum "+tnum+" ,startPos "+startPos + " ,download:"+(startPos - initStart));
					if (null != httpClient) {
						httpClient.close();
						httpClient = null;
					}
					threadfile.close();
					inStream.close();
					//判断整个文件是否下载完成，如果下载完成，取消阻塞
					if(DownloadPrefHelper.isDownFinish(mContext, plid, pnum, total)) {
		                synchronized(mContext) {
							mContext.notifyAll();
						}
					}
				}else{
					doOnFail(EDownloadStatus.DOWNLOAD_FAILED_VIDEO_ERROR, plid, pnum, tnum, startPos, total, id);
				}
			} else {
				doOnFail(EDownloadStatus.DOWNLOAD_FAILED, plid, pnum, tnum, startPos, total, id);
			}
			
		} catch (IOException e) {
			//DownloadService退出时，不记录错误
			PalLog.v(TAG, e.toString());
			if(!mIsExited) {
				if(!NetUtils.isAvailable(mContext)){
					doOnFail(EDownloadStatus.DOWNLOAD_FAILED, plid, pnum, tnum, startPos, total, id);
				}else{
					doOnFail(EDownloadStatus.DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME, plid, pnum, tnum, startPos, total, id);
				}
				
//				long sdSize = 0;
//				if(saveFile.canWrite() && saveFile.canRead()){
//					sdSize = SystemUtils.readSDCardRemainSize(saveFile.getAbsolutePath());
//				}
//				Log.e("ioerror", "sdSize:" + sdSize);
//				if(sdSize < 10000){//小于10K时认为没有足够空间
//					// ENOSPC (No space left on device)
//					doOnFail(EDownloadStatus.DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME, plid, pnum, tnum, startPos, total, id);
//				}else{
//					doOnFail(EDownloadStatus.DOWNLOAD_FAILED, plid, pnum, tnum, startPos, total, id);
//				}
			} 
			mIsExited = false;
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			mIsExited = false;
			PalLog.v(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (null != httpClient) {
				httpClient.close();
				httpClient = null;
			}
		}
	}
	
	/**
	 * 下载出错时，记录下载大小和状态
	 * @param plid
	 * @param pnum
	 * @param tnum
	 * @param startPos
	 * @param total
	 * @param id
	 * @param failType 
	 */
	private void doOnFail(EDownloadStatus status, String plid, int pnum, int tnum, int startPos, int total, int id) {		
		DownloadPrefHelper.recordDownload(mContext, plid, pnum, tnum, startPos);
		
//		ContentValues values = new ContentValues();
//		values.put(DownloadManagerHelper.DOWNLOAD_STATUS, DownloadGridItemInfo.STATUS_DOWNLOAD_FAILED);
//		int downSize = BaseApplication.getDownCurSize();
//		if(downSize > 0) values.put(DownloadManagerHelper.DOWNLOAD_SIZE, downSize);
//		if(total > 0) values.put(DownloadManagerHelper.TOTAL_SIZE, total);
//		mContext.getContentResolver().update(DownloadManagerHelper.getUri(), values, "_id=" + id, null); 
//		
		//更新状态
		DownLoadInfo updateDownloadInfo = new DownLoadInfo();
		
		updateDownloadInfo.mDownload_status = status;
		updateDownloadInfo.mCourse_pnumber = pnum;
		int downSize = BaseApplication.getDownCurSize();
		if(downSize > 0) updateDownloadInfo.mDownload_size = downSize;
		if(total > 0) updateDownloadInfo.mTotal_size = total;
		updateDownloadInfo.m_id = String.valueOf(BaseApplication.getID());
	    DBApi.updateDownloadInfo(mContext, updateDownloadInfo);
		
		//added by cxl
		if(mInfo.mDownloadListener != null){
			mInfo.mDownloadListener.onFinishDownload(status, id, downSize, total);
		}
		//下载失败时发送广播，通知更新下载状态
//		DownloadUtils.sendCommpleteBroadcast(mContext, DownloadGridItemInfo.STATUS_DOWNLOAD_FAILED, id, downSize, total);	
	}

	private boolean mIsPaused = false;
	public void pause() {
		mIsPaused = true;
	}
	private boolean mIsExited = false;
	public void exit() {
		mIsExited = true;
	}
}
