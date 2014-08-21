package vopen.transactions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.DBApi;
import vopen.db.DBApi.CollectInfo;
import vopen.db.DBApi.CourseType;
import vopen.db.DBUtils;
import vopen.protocol.VopenCallBack;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenService;
import vopen.protocol.VopenServiceCode;
import vopen.response.CourseInfo;
import vopen.response.SyncItemInfo;
import vopen.response.UiEventTransport;
import android.util.Log;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.Util;

public class SyncStoreTransaction extends BaseTransaction {
    String mUserId;
    String mCookie;
    boolean mIsSyncLocal;
    
    ArrayList<String> mTobeSyncId = new ArrayList<String>();
    
    public SyncStoreTransaction(TransactionEngine transMgr, String userid, String cookie, boolean isSyncLocal) {
        super(transMgr, TRANSACTION_TYPE_SYNC_FAVORITE);
        // TODO Auto-generated constructor stub
        mUserId = userid;
        mCookie = cookie;
        mIsSyncLocal = isSyncLocal;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
        Log.i("SyncStoreTransaction", "response:"+response);
        JSONObject jobj = null;
		try {
			jobj = new JSONObject(response);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
        
        if (null != jobj) {

            try {
                String result = jobj.getString("result");

                if (!Util.isStringEmpty(response) && result.contains("success")) {

                    JSONArray array = jobj.getJSONArray("playlist");
                    List<SyncItemInfo> list = SyncItemInfo.parseSyncResult(array);
                    
                    syncDb(list);
                    
                    notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, list);
                } else {
                    notifyError(VopenServiceCode.SYNC_FAVORITE_ERR_INNER, ErrorToString.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
                }

            } catch (JSONException e) {
                notifyError(VopenServiceCode.SYNC_FAVORITE_ERR_INNER, ErrorToString.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
            }

        } else {
            notifyError(VopenServiceCode.SYNC_FAVORITE_ERR_INNER, ErrorToString.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
        }
    }
    
    @Override
    public void onResponseError(int errCode, Object err) {
        // TODO Auto-generated method stub
        notifyError(errCode, err);
    }

    @Override
    public void onTransact() {
        boolean isRequestSend = false;
        if(!Util.isStringEmpty(mUserId)
                && !Util.isStringEmpty(mCookie)){
            List<SyncItemInfo> mSynInfos = new ArrayList<SyncItemInfo>();
            if(mIsSyncLocal){
                //同步本地数据
                List<CollectInfo> collectInfo = DBUtils.getAllCollectNoLogin(BaseApplication.getAppInstance());
                for(CollectInfo info : collectInfo){
                    mSynInfos.add(new SyncItemInfo(info.mCourse_id, info.mData_time));
                }
//                collectInfo.clear();
//                collectInfo = DBUtils.getAllCollectLogin(BaseApplication.getAppInstance(), mUserId);
//                for(CollectInfo info : collectInfo){
//                    mSynInfos.add(new SyncItemInfo(info.mCourse_id, info.mData_time));
//                }
            }
            HttpRequest request;
            try {
                request = VopenProtocol.getInstance().createSyncFavoriteRequest(mUserId, mSynInfos, mCookie);
                if (!isCancel()) {
                    sendRequest(request);
                    isRequestSend = true;
                } 
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                notifyError(VopenServiceCode.SYNC_FAVORITE_ERR_INNER, ErrorToString.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
                e.printStackTrace();
            }
        }else{
            notifyError(VopenServiceCode.SYNC_FAVORITE_ERR_INNER, ErrorToString.getString(VopenServiceCode.SYNC_FAVORITE_ERR_INNER));
        }
        
        if(!isRequestSend)
        	getTransactionEngine().endTransaction(this);
    }

    private void syncDb(List<SyncItemInfo> newList){
    	//删除数据库
    	DBUtils.removeCollectAll(BaseApplication.getAppInstance(), mUserId);
    	//插入新数据
    	Map<String, CourseInfo> allMap = getAllCourse();
    	mTobeSyncId.clear();
    	final int size = newList.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				CollectInfo info = new CollectInfo();
				SyncItemInfo obj = newList.get(i);
				info.mCourse_id = obj.playid;
				info.mData_time = obj.storetime;
				info.mUser_id = mUserId;
				info.mIs_synchronized = true;
				CourseInfo courseInfo = DBUtils.getCourseByPlid(BaseApplication.getAppInstance(), info.mCourse_id);
				if(courseInfo == null && allMap.get(info.mCourse_id) != null){
					synchronized (mTobeSyncId){
						mTobeSyncId.add(info.mCourse_id);
					}
				}
				
				if(syncLoaclData(allMap, courseInfo, info)) {
					DBApi.insertCollect(BaseApplication.getAppInstance(), info);
				}
			}
			
			syncDetail();
		}
    }
    
    private Map<String, CourseInfo> getAllCourse(){
    	Map<String, CourseInfo> allMap = new LinkedHashMap<String, CourseInfo>();
    	String resultJson = DBUtils.getLocalCourseByType(BaseApplication.getAppInstance(), CourseType.DATA_TYPE_ALL);
    	try {
			JSONArray courseArray = new JSONArray(resultJson);
			int len = courseArray.length();
			JSONObject obj = null;
			for (int i = 0; i < len; i++) {
				obj = courseArray.getJSONObject(i);
				CourseInfo info = new CourseInfo(obj);
				allMap.put(info.plid, info);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return allMap;
    }

    private boolean syncLoaclData(Map<String, CourseInfo> allMap, CourseInfo courseInfo, CollectInfo info){
    	boolean result = false;
    	if(info == null || Util.isStringEmpty(info.mCourse_id))
    		return result;
    	
    	if(courseInfo == null)
    		courseInfo = allMap.get(info.mCourse_id);
    	if(courseInfo != null){
    		info.mCourse_img = courseInfo.imgpath;
    		info.mCourse_title = courseInfo.title;
    		info.mCourse_playcount = courseInfo.playcount;
    		info.mCourse_translatecount = courseInfo.updated_playcount;
    		result = true;
    	}
    	return result;
    }
    
    private void syncDetail(){
    	synchronized (mTobeSyncId) {
    		if(mTobeSyncId.size() > 0){
    			VopenService.getInstance().addListener(mListener);
        		for(String id : mTobeSyncId){
        			VopenService.getInstance().doGetVideoDetail(id);
        		}
        	}
		}
    }
    
    private VopenCallBack mListener = new VopenCallBack(){
    	public void onGetVideoDetail(int transactionId, CourseInfo videoDetail) {
    		if(videoDetail != null && mTobeSyncId.contains(videoDetail.plid)){
    			CollectInfo info = new CollectInfo();
				info.mCourse_id = videoDetail.plid;
				info.mData_time = null;
				info.mUser_id = mUserId;
				info.mIs_synchronized = true;
    			info.mCourse_img = videoDetail.imgpath;
        		info.mCourse_title = videoDetail.title;
        		info.mCourse_playcount = videoDetail.playcount;
        		info.mCourse_translatecount = videoDetail.updated_playcount;
        		
        		DBApi.updateCollectByCourseID(BaseApplication.getAppInstance(), info);
        		
        		mTobeSyncId.remove(videoDetail.plid);
    		}
//    		PalLog.i("SyncStoreTransaction", "mTobeSyncId.size() is " + mTobeSyncId.size());
    		if(mTobeSyncId.size() == 0){
    			
    			VopenService.getInstance().removeListener(mListener);
    			UiEventTransport event = new UiEventTransport(UiEventTransport.UIEVENT_TYPE_UPDATE_SYNCSTORE);
    			VopenService.getInstance().doNotifyOtherWindow(event);
    		}
    	};
    };
}


