package vopen.download;

import java.io.File;

import com.netease.vopen.pal.Constants;

public class DownloadTaskInfo {
	public int mPnumber;
    public String mPlid;
    public String mUrl;
    public int mDownloadSize;   
    public int mThreadCount = Constants.Download_Thread_Count;
    public int mTotalSize;
    public int mStartPos;
    public File mSaveFile;
    public int mCurThread;
    public int mID;
    
    //downloadListener
    public DownloadListener mDownloadListener;
}
