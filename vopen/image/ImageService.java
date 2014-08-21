package vopen.image;

import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import common.framework.http.HttpEngine;
import common.framework.task.AsyncTransaction;
import common.framework.task.Transaction;
import common.framework.task.TransactionEngine;
import common.framework.task.TransactionListener;
import common.pal.PalLog;

public class ImageService implements TransactionListener {
	private static final String TAG = "ImageService";
	private TransactionEngine mTransactionEngine;// 事务执行线程
	private HttpEngine mHttpEngine; // 协议Http线程
	
	//图片缓存
	private CacheManager<String, Bitmap> mCache = new CacheManager<String, Bitmap>();
	//任务ID和回调的对应表
	private Map<Integer, BitmapCbInfo> mCbInfos = new LinkedHashMap<Integer, BitmapCbInfo>();
	
	static ImageService mInstance;

	private ImageService() {

		mTransactionEngine = new TransactionEngine();
		mHttpEngine = new HttpEngine();

		ImageDataChannel channel = new ImageDataChannel(mTransactionEngine,
				mHttpEngine);
		mTransactionEngine.setDataChannel(channel);
	}

	synchronized public static ImageService getInstance() {
		if (mInstance == null) {
			mInstance = new ImageService();
		}
		return mInstance;
	}

	/**
	 * 注册事务监听器
	 * 
	 * @param t
	 * @param listener
	 * @return
	 */
	private int startTransaction(AsyncTransaction t,
			TransactionListener listener) {
		t.setListener(listener);

		return beginTransaction(t);
	}

	/**
	 * 添加一个最简单的事务
	 * 
	 * @param t
	 * @return
	 */
	private int beginTransaction(Transaction t) {
		mTransactionEngine.beginTransaction(t);
		return t.getId();
	}


	/**
	 * 关闭Service方法
	 */
	public void shutdown() {
		mInstance = null;

		mTransactionEngine.shutdown();
		mHttpEngine.shutdown();
	}
	
	
	/**
	 * 按id取消获取图片事务
	 */
	public void cancelGetImage(int id) {
		mTransactionEngine.cancelTransaction(id);
		mCbInfos.remove(id);
	}

	/**
	 * 获取image, 
	 * @param url
	 * @param width
	 * @param height
	 * @return 返回事务ID, 如果在缓存里找到图片, 返回id为0
	 */
	public int getImage(String url, int width, int height, IBitmapCb cb){
		String file = ImageHelper.getLocalImageName(url, width, height);
		Bitmap bmp = mCache.get(file);
		if(bmp != null){
			cb.onGetCacheImage(bmp);
			return 0;
		}
		else{
			ImageTransaction tx = new ImageTransaction(mTransactionEngine, url, width, height);
			startTransaction(tx, this);
			int missionId = tx.getId();
			mCbInfos.put(missionId, new BitmapCbInfo(cb, width, height));
			return missionId;
		}		
	}

	@Override
	public void onTransactionMessage(int code, int arg1, int arg2, Object arg3) {
		Bitmap bmp = null;
		String path = null;
		if(arg3 != null ){
			if(arg3 instanceof String){
				path = (String)arg3;
				bmp = ImageHelper.getBitmapFromFileLimitSize(path, 0);
			} else if(arg3 instanceof Bitmap) {
				bmp = (Bitmap)arg3;
			}
			if(bmp != null){
				BitmapCbInfo cbInfo = mCbInfos.get(arg2);
				if(cbInfo != null){
					//取消缩放，使用imageview自己的缩放模式
//					bmp = ImageHelper.resizeImage(bmp, cbInfo.width, cbInfo.height);
					if(bmp != null && path != null){
						String file = ImageHelper.getNameFromPath(path);
						mCache.put(file, bmp);
					}
				}
			}
		}
		
		if(bmp != null){
			postGetBitmapSuccess(arg2, bmp);
		}
		else{
			postGetBitmapFail(arg2, 0);
		}
	}

	@Override
	public void onTransactionError(int errCode, int arg1, int arg2, Object arg3) {
		postGetBitmapFail(arg2, errCode);		
	}
	
	public interface IBitmapCb {
		public void onGetImage(int missionId, Bitmap bitmap);
		/**
		 * 找到缓存图片, 会直接调用这个回调, 此时, 为阻塞式 , 所以missionid没有意义
		 * @param bitmap
		 */
		public void onGetCacheImage(Bitmap bitmap);
		public void onGetImageError(int missionId, int errCode);
//		public void onDownloading(int max, int curr);
	}

	private class BitmapCbInfo{
		int width;
		int height;
		IBitmapCb cb;
		
		public BitmapCbInfo(IBitmapCb cb, int width, int height){
			this.cb = cb;
			this.width = width;
			this.height = height;
		}
	}
	
	private void postGetBitmapSuccess(int id, Bitmap bmp){
		mCbHandler.obtainMessage(MESSAGE_ONGETIMAGE, id, 0, bmp).sendToTarget();
	}
	
	private void postGetBitmapFail(int id, int errCode){
		mCbHandler.obtainMessage(MESSAGE_ONGETIMAGE_FAIL, id, errCode).sendToTarget();
	}
	
	private static final int MESSAGE_ONGETIMAGE = 100;
	private static final int MESSAGE_ONGETIMAGE_FAIL = 101;
	private Handler mCbHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			BitmapCbInfo cbInfo = null;
			switch (msg.what) {
			case MESSAGE_ONGETIMAGE:
				cbInfo = mCbInfos.get(msg.arg1);
				if (cbInfo != null) {
					cbInfo.cb.onGetImage(msg.arg1, (Bitmap)msg.obj);
					mCbInfos.remove(msg.arg1);
				}
				return;
			case MESSAGE_ONGETIMAGE_FAIL:
				cbInfo = mCbInfos.get(msg.arg1);
				if (cbInfo != null) {
					cbInfo.cb.onGetImageError(msg.arg1, msg.arg2);
					mCbInfos.remove(msg.arg1);
				}
				return;
				
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	public void test(){
		PalLog.i(TAG, "cache size:" + mCache.size());
	}
//	/**
//	 * transaction里最大999, 所以这里不能小于999, 以免重复
//	 */
//	private static int mMissionId = 0xFFFF0000;
//
//	private synchronized static int getNextMissionId() {
//		if (mMissionId < 0x0FFF0000) {
//			mMissionId = 0x0FFF0000;
//		}
//		return mMissionId++;
//	}
}
