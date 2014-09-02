
package vopen.app;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import vopen.db.DBApi.EDownloadStatus;
import vopen.download.DownloadListener;
import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

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
	private static AtomicLong mTotalSize = new AtomicLong();
	public synchronized static void initTotalSize(long size) {
		mTotalSize.set(size);
	}
	
	public static int getTotalSize() {
		return (int)mTotalSize.get();
	}
	
	/**
	 * 当前下载文件已下载的大小
	 */
	private static AtomicLong mDownloadCurrrentSize = new AtomicLong();
	public  static void setDownCurSize(long offset) {
		mDownloadCurrrentSize.set(mDownloadCurrrentSize.get() + offset);
		if(mDownloadCurrrentSize.get() >= mTotalSize.get()){
			mDownloadCurrrentSize.set(mTotalSize.get());
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
	public void addDownloadListener(DownloadListener listener){
		synchronized (mListDownloadListener) {
			if(mListDownloadListener != null){
				mListDownloadListener.add(listener);
			}else{
				mListDownloadListener = new LinkedList<DownloadListener>();
				mListDownloadListener.add(listener);
			}
		}
		
	}
	public void removeDownloadListener(DownloadListener listener){
		synchronized (mListDownloadListener) {
			if(mListDownloadListener != null){
				mListDownloadListener.remove(listener);
			}
		}
	}
	

	@Override
	public void onStartDownload(int id, int downSize, int total) {
		synchronized (mListDownloadListener) {
			for(DownloadListener listener: mListDownloadListener){
				listener.onStartDownload(id, downSize, total);
			}
		}
	}

	@Override
	public void onDownloadProgeress(int id, int offset, int total) {
		synchronized (mListDownloadListener) {
			for(DownloadListener listener: mListDownloadListener){
				listener.onDownloadProgeress(id, offset, total);
			}
		}
	}
	
	@Override
	public void onFinishDownload(int id, EDownloadStatus status, int down,
			int total) {
		synchronized (mListDownloadListener) {
			for(DownloadListener listener: mListDownloadListener){
				listener.onFinishDownload(id, status, down, total);
			}
		}
	}
}