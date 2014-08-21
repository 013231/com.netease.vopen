
package vopen.app;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


import vopen.db.DBApi.DownLoadInfo;
import vopen.db.DBApi.EDownloadStatus;
import vopen.download.DownloadListener;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class BaseApplication extends Application  implements DownloadListener{
	private static BaseApplication sBaseApp;
    private static final HandlerThread sWorkerThread = new HandlerThread("BaseApplication");
    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    static public BaseApplication getAppInstance(){
		if(sBaseApp == null)
			throw new NullPointerException("sBaseApp not create or be terminated!");
		return sBaseApp;
	}

    @Override
    public void onCreate() {
        super.onCreate();
        sBaseApp = this;
        
        if(mListDownloadListener == null){
        	mListDownloadListener = new LinkedList<DownloadListener>();
        }
//        BaseException.init(this);
    }


    /**
     * 执行线程任务.
     */
    public void postExe(Runnable run) {
        sWorker.post(run);
    }
    
    /**
     * 当程序异常退出时调用<br/>
     * s
     * @param e
     */
    public void onExceptionExit(Throwable exception) {
        
    }
    
    public static class BaseException implements UncaughtExceptionHandler {
        private static BaseApplication sApp;
        private static Thread.UncaughtExceptionHandler sDefaultExceptionHandler = Thread
                .getDefaultUncaughtExceptionHandler();

        private static BaseException sCustomException = new BaseException();

        @Override
        public void uncaughtException(Thread thread, Throwable exception) {
            sApp.onExceptionExit(exception);
            sDefaultExceptionHandler.uncaughtException(thread, exception);
        }

        public static void init(BaseApplication app) {
            sApp = app;
            Thread.setDefaultUncaughtExceptionHandler(sCustomException);
        }
    }
    
    /**
     * 当前下载任务的ID
     */
	private static int mID = -1;
	public static int getID() {
		return mID;
	}
	public static void setID(int id) {
		mID = id;
	}
	
	/**
	 * 当前下载文件的总大小
	 */
	private static long mTotalSize;
	public synchronized static void initTotalSize(long size) {
		mTotalSize = size;
	}
	
	public static int getTotalSize() {
		return (int)mTotalSize;
	}
	
	/**
	 * 当前下载文件已下载的大小
	 */
	private static AtomicLong mDownloadCurrrentSize = new AtomicLong();
	public  static void setDownCurSize(long offset) {
		mDownloadCurrrentSize.set(mDownloadCurrrentSize.get() + offset);
		if(mDownloadCurrrentSize.get() >= mTotalSize){
			mDownloadCurrrentSize.set(mTotalSize);
		}
		//Logger.e("mDownloadCurrrentSize "+mDownloadCurrrentSize);
	}
	
	public static void initDownCurSize(long size) {
		mDownloadCurrrentSize.set(size);
	}
	
	public static int getDownCurSize() {
		return (int)mDownloadCurrrentSize.get();
	}
	
	private static String mDownloadCurrentName;
	private static int    mDownloadCurrentNumber;
	public static String getmDownloadCurrentName() {
		return mDownloadCurrentName;
	}

	public static void setmDownloadCurrentName(String mDownloadCurrentName) {
		BaseApplication.mDownloadCurrentName = mDownloadCurrentName;
	}

	public static int getmDownloadCurrentNumber() {
		return mDownloadCurrentNumber;
	}

	public static void setmDownloadCurrentNumber(int mDownloadCurrentNumber) {
		BaseApplication.mDownloadCurrentNumber = mDownloadCurrentNumber;
	}
	

	protected List<DownloadListener> mListDownloadListener;
	public void addListener(DownloadListener listener){
		synchronized (mListDownloadListener) {
			if(mListDownloadListener != null){
				mListDownloadListener.add(listener);
			}else{
				mListDownloadListener = new LinkedList<DownloadListener>();
				mListDownloadListener.add(listener);
			}
		}
		
	}
	public void rmListener(DownloadListener listener){
		synchronized (mListDownloadListener) {
			if(mListDownloadListener != null){
				mListDownloadListener.remove(listener);
			}
		}
	}
	@Override
	public void onAddDownloadBean(List<DownLoadInfo> list) {
		Log.v("BaseApplication", "DownloadService.onAddDownloadBean");
		synchronized (mListDownloadListener) {
			for(DownloadListener listener: mListDownloadListener){
				listener.onAddDownloadBean(list);
			}
		}
	}
	
	@Override
	public void onFinishDownload(EDownloadStatus status, int id, int down,
			int total) {
		// TODO Auto-generated method stub
		Log.v("BaseApplication", "DownloadService.finish");
		synchronized (mListDownloadListener) {
			for(DownloadListener listener: mListDownloadListener){
				listener.onFinishDownload(status, id, down, total);
			}
		}
		
	}
	@Override
	public void onStartDownload(int id, int downSize, int total) {
		// TODO Auto-generated method stub
		Log.v("BaseApplication", "DownloadListener.onStartDownload and id = " + id);
		synchronized (mListDownloadListener) {
			for(DownloadListener listener: mListDownloadListener){
				listener.onStartDownload(id, downSize, total);
			}
		}
		
	}

	@Override
	public void onDownloadProgeress(int offset) {
		// TODO Auto-generated method stub
//		Log.v("BaseApplication", "DownloadListener.onStartDownload and offset = " + offset);
		synchronized (mListDownloadListener) {
			for(DownloadListener listener: mListDownloadListener){
				listener.onDownloadProgeress(offset);
			}
		}
	}
}