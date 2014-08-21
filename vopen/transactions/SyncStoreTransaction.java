package vopen.transactions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.DBApi;
import vopen.db.DBApi.CollectInfo;
import vopen.db.DBUtils;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CourseInfo;
import vopen.response.SyncItemInfo;

import com.netease.vopen.app.VopenApp;
import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.StringUtil;
import common.util.Util;

//import common.pal.PalLog;

public class SyncStoreTransaction extends BaseTransaction {
	static final String TAG = "SyncStoreTransaction";
	String mUserId;
	String mCookie;
	boolean mIsSyncLocal;

	public SyncStoreTransaction(TransactionEngine transMgr, String userid,
			String cookie, boolean isSyncLocal) {
		super(transMgr, TRANSACTION_TYPE_SYNC_FAVORITE);
		mUserId = userid;
		mCookie = cookie;
		mIsSyncLocal = isSyncLocal;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		//		PalLog.d(TAG, "response:" + response);
		//		PalLog.d(TAG, "getResponse time="+System.currentTimeMillis() +
		//				"interval="+(System.currentTimeMillis()- time) );
		//		time = System.currentTimeMillis();
		JSONObject jobj = null;
		try {
			jobj = new JSONObject(response);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		if (jobj == null) {
			notifyError(
					VopenServiceCode.SYNC_FAVORITE_ERR_INNER,
					ErrorToString
							.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
			return;
		}
		try {
			String result = jobj.getString("result");
			if (!Util.isStringEmpty(response) && result.contains("success")) {
				//				PalLog.d(TAG, "parse response begin time="+System.currentTimeMillis() +
				//						"interval="+(System.currentTimeMillis()- time) );
				//				time = System.currentTimeMillis();
				JSONArray array = jobj.getJSONArray("playlist");
				List<SyncItemInfo> list = SyncItemInfo.parseSyncResult(array);
				//				PalLog.d(TAG, "parse response end time="+System.currentTimeMillis() +
				//						"interval="+(System.currentTimeMillis()- time) );
				//				time = System.currentTimeMillis();
				syncDb(list);
				//				PalLog.d(TAG, "syncDb interval="+(System.currentTimeMillis()- time) );
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, list);
			} else {
				notifyError(
						VopenServiceCode.SYNC_FAVORITE_ERR_INNER,
						ErrorToString
								.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
			}

		} catch (JSONException e) {
			notifyError(
					VopenServiceCode.SYNC_FAVORITE_ERR_INNER,
					ErrorToString
							.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
		}
	}

	private void syncDb(List<SyncItemInfo> newList) {
		//先进行合并，获取数据表中原有的数据，并做成一个hashmap方便查询
		List<CollectInfo> oldList = DBUtils.getAllCollectLogin(
				BaseApplication.getAppInstance(), mUserId);
		Map<String, CollectInfo> oldMap = new HashMap<String, CollectInfo>();
		for (CollectInfo info : oldList) {
			oldMap.put(info.mCourse_id, info);
		}
		//清空数据
		DBUtils.removeCollectAll(BaseApplication.getAppInstance(), mUserId);
		//重新插入新数据
		List<CollectInfo> dataList = new ArrayList<CollectInfo>();
		for (SyncItemInfo obj : newList) {
			CollectInfo info = new CollectInfo();
			info.mCourse_id = obj.playid;
			info.mData_time = obj.storetime;
			info.mUser_id = mUserId;
			info.mIs_synchronized = true;
			CourseInfo courseInfo = VopenApp.getAppInstance().getCourseByPlid(
					info.mCourse_id);
			if (courseInfo == null) {
				PalLog.w(TAG, "找不到课程" + info.mCourse_id);
				continue;
			}
			//优先使用大图片
			info.mCourse_img = courseInfo.largeImg;
			if (StringUtil.isEmpty(info.mCourse_img)) {
				info.mCourse_img = courseInfo.imgpath;
			}
			info.mCourse_title = courseInfo.title;
			info.mCourse_playcount = courseInfo.playcount;
			info.mCourse_translatecount = courseInfo.updated_playcount;
			CollectInfo oldData = oldMap.get(info.mCourse_id);
			if (oldData != null) {//对于旧数据
				info.mCourse_new_translate_num = info.mCourse_translatecount
						- oldData.mCourse_translatecount;
				info.mCourse_new_translate_num += oldData.mCourse_new_translate_num;
			} else {//从服务器拿到的新收藏数据
				info.mCourse_new_translate_num = 0;
			}
			dataList.add(info);
		}
		DBApi.bulkInsertCollect(BaseApplication.getAppInstance(), dataList);
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		notifyError(errCode, err);
	}

	//	private long time;

	@Override
	public void onTransact() {
		//		time = System.currentTimeMillis();
		//		PalLog.d(TAG, "begin Transact time="+time);
		boolean isRequestSend = false;
		if (!Util.isStringEmpty(mUserId) && !Util.isStringEmpty(mCookie)) {
			List<SyncItemInfo> synInfos = new ArrayList<SyncItemInfo>();
			//如果需要同步本地收藏，先上传本地收藏数据
			if (mIsSyncLocal) {
				List<CollectInfo> collectInfos = DBUtils
						.getAllCollectNoLogin(BaseApplication.getAppInstance());
				for (CollectInfo info : collectInfos) {
					synInfos.add(new SyncItemInfo(info.mCourse_id,
							info.mData_time));
				}
			}
			HttpRequest request;
			try {
				request = VopenProtocol.getInstance()
						.createSyncFavoriteRequest(mUserId, synInfos, mCookie);
				if (!isCancel()) {
					sendRequest(request);
					isRequestSend = true;
					//					PalLog.d(TAG, "send request time="+System.currentTimeMillis() );
					//					time = System.currentTimeMillis();
				}
			} catch (UnsupportedEncodingException e) {
				notifyError(
						VopenServiceCode.SYNC_FAVORITE_ERR_INNER,
						ErrorToString
								.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
				e.printStackTrace();
			}
		} else {
			notifyError(
					VopenServiceCode.SYNC_FAVORITE_ERR_INNER,
					ErrorToString
							.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
		}
		if (!isRequestSend)
			getTransactionEngine().endTransaction(this);
	}

}
