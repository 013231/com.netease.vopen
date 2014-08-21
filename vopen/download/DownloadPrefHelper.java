package vopen.download;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.vopen.pal.Constants;

import vopen.app.BaseApplication;
import common.pal.PalLog;

public class DownloadPrefHelper {
	protected static final String sPrefName = "VOPEN_DOWNLOAD";
	private static final String TAG = "DownloadPrefHelper";

	public static boolean getBoolean(Context context, String prefKey,
			boolean defaultValue) {
		return getSharedPreferences(context).getBoolean(prefKey, defaultValue);
	}

	public static float getFloat(Context context, String prefKey,
			float defaultValue) {
		return getSharedPreferences(context).getFloat(prefKey, defaultValue);
	}

	public static int getInt(Context context, String prefKey, int defaultValue) {
		return getSharedPreferences(context).getInt(prefKey, defaultValue);
	}

	public static long getLong(Context context, String prefKey,
			long defaultValue) {
		return getSharedPreferences(context).getLong(prefKey, defaultValue);
	}

	public static String getString(Context context, String prefKey,
			String defaultValue) {
		return getSharedPreferences(context).getString(prefKey, defaultValue);
	}

	public static void putBoolean(Context context, String prefKey, boolean value) {
		getSharedPreferences(context).edit().putBoolean(prefKey, value).commit();
	}

	public static void putFloat(Context context, String prefKey, float value) {
		getSharedPreferences(context).edit().putFloat(prefKey, value).commit();
	}

	public static void putInt(Context context, String prefKey, int value) {
		getSharedPreferences(context).edit().putInt(prefKey, value).commit();
	}

	public static void putLong(Context context, String prefKey, long value) {
		getSharedPreferences(context).edit().putLong(prefKey, value).commit();
	}

	public static void putString(Context context, String prefKey, String value) {
		getSharedPreferences(context).edit().putString(prefKey, value).commit();
	}

	public static void remove(Context context, String prefKey) {
		getSharedPreferences(context).edit().remove(prefKey).commit();
	}
	
	public static void clear(Context context) {
		getSharedPreferences(context).edit().clear().commit();
	}
	
	/**
	 * 存储下载断点信息
	 * @param context
	 * @param plid
	 * @param pnum
	 * @param tnum
	 * @param startPos
	 */
	public static void recordDownload(Context context, String plid, int pnum, int tnum, int startPos) {
		putInt(context, plid + "_" + pnum + "_" + tnum, startPos);
	}
	
	/**
	 * 获取下载断点信息
	 * @param context
	 * @param plid
	 * @param pnum
	 * @return
	 */
	public static int[] getThreadInfo(Context context, String plid, int pnum) {
		int[] threadInfos = null;
		int count = Constants.Download_Thread_Count;;
		if (count > 0) {
			threadInfos = new int[count];
			for (int i = 0; i < count; i++) {
				threadInfos[i] = DownloadPrefHelper.getInt(context, plid + "_" + pnum
						+ "_" + i, 0);
				PalLog.e(TAG,"threadInfos["+i+"] "+threadInfos[i]);
			}
		}
		return threadInfos;
	}
	
	/**
	 * 判断是否下完
	 * @param context
	 * @param plid
	 * @param pnum
	 * @return
	 */
	public static boolean isDownFinish(Context context, String plid, int pnum, int total) {
		int startPos = 0;	
		int count = Constants.Download_Thread_Count;
		int endPos = 0;		
		if (count > 0) {
			final int block = (int)(total / count);
			for (int i = 0; i < count; i++) {
				endPos = i == count - 1 ? total : block*(i+1);
				startPos = DownloadPrefHelper.getInt(context, plid + "_" + pnum + "_" + i, 0);				
				PalLog.e("DownloadPrefHelper","startPos "+startPos+", endPos "+endPos);
				if(startPos < endPos) return false;
			}
			BaseApplication.initDownCurSize(BaseApplication.getTotalSize());
			return true;
		}
		return false;
	}
	
	/**
	 * 删除下载断点信息
	 * @param context
	 * @param plid
	 * @param pnum
	 */
	public static void removeThreadInfo(Context context, String plid, int pnum) {
		int count = Constants.Download_Thread_Count;
		for(int i=0; i<count; i++) {
			putInt(context, plid + "_" + pnum + "_" + i, 0);
			remove(context, plid + "_" + pnum + "_" + i);
		}
	}
	
    public static SharedPreferences getSharedPreferences(Context context) {
    	return context.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
    }
}
