package common.net;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import common.pal.PalLog;

public class ApnReference {
	public static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	
	private static ApnReference sInstance;
	
	private Cursor mCursor;
	private Context mContext;
	private ApnWrapper mApnWrapper;
	ConnectivityManager mCm;
	
	private ChangeObserver mChangeObserver = new ChangeObserver();
	
	synchronized public static ApnReference getInstance(Context context){
		if(sInstance == null)
			sInstance = new ApnReference(context);
		
		return sInstance;
	}
	
	private ApnReference(Context context){
		mContext = context;
		mCm =(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public void ini(){
		if(Build.VERSION.SDK_INT >= 17){
			return;//level17不支持apn读取的权限
		}
		String[] projection = new String[]{"apn", "name", "port", "proxy"};
		
		unIni();
		
		mCursor = mContext.getContentResolver().query(PREFERRED_APN_URI, projection, null, null, null);
		if(mCursor != null){
			mCursor.registerContentObserver(mChangeObserver);
//			mCursor.registerDataSetObserver(observer)
		}
	}
	
	public void unIni(){
		if(mCursor != null){
			mCursor.unregisterContentObserver(mChangeObserver);
			mCursor.close();
			mCursor = null;
			mApnWrapper = null;
		}
	}
	
	
	public ApnWrapper getCurrApn(){
		if(Build.VERSION.SDK_INT >= 17){
			return null;//level17不支持apn读取的权限
		}
		NetworkInfo nInfo = mCm.getActiveNetworkInfo();
		if(nInfo == null || nInfo.getType() != ConnectivityManager.TYPE_MOBILE)
			return null;
			
		if(mApnWrapper != null)
			return mApnWrapper;
		
		if(mCursor == null){
			ini();
		}
		
		if(mCursor!= null && mCursor.moveToFirst()){
			ApnWrapper aw = new ApnWrapper();
			aw.apn = mCursor.getString(mCursor.getColumnIndex("apn"));
			aw.name = mCursor.getString(mCursor.getColumnIndex("name"));
			String port = mCursor.getString(mCursor.getColumnIndex("port"));
			try {
				aw.port = Integer.parseInt(port);
			} catch (NumberFormatException e) {
				aw.port = Proxy.getDefaultPort();
				e.printStackTrace();
			}
			
			aw.proxy = mCursor.getString(mCursor.getColumnIndex("proxy"));
			PalLog.i("ApnReference", "apn:" + aw.apn + "-name:" + aw.name + "-port:" + aw.port + "-proxy:" + aw.proxy);
			mApnWrapper = aw;
			return aw;
		}
		
		return null;
	}
	
	
	private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
        	/*?mCursor.requery()  why???*/
//            if(mCursor != null && !mCursor.isClosed()){
//            	PalLog.i("ApnReference", "NET work changed");
//            	mCursor.requery();
//            	mApnWrapper = null;
//            }
        	
        	ini();
        }
    }
	
	
	public class ApnWrapper {
		public String apn;
		public String name;
		public int port;
		public String proxy;
	}
}
