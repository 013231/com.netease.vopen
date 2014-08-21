package vopen.transactions;

import java.util.List;

import vopen.app.BaseApplication;
import vopen.db.DBApi;
import vopen.db.DBApi.CollectInfo;
import vopen.db.DBUtils;
import vopen.protocol.VopenServiceCode;
import vopen.response.AllCourseInfo;
import vopen.response.CourseInfo;
import android.content.Context;

import com.netease.vopen.pal.ErrorToString;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;
import common.util.Util;
import common.pal.PalLog;

public class SyncTranslateNumTransaction extends BaseTransaction {
	List<CourseInfo> mAllInfo;
	String 	mUserId;

    public SyncTranslateNumTransaction(TransactionEngine transMgr, List<CourseInfo> allInfo, String user) {
        super(transMgr, TRANSACTION_TYPE_SYNC_TRANSLATE_NUM);
        mAllInfo = allInfo;
        mUserId = user;
    }

    @Override
    public void onTransact() {
    	PalLog.i("SyncTranslateNumTransaction", "onTransact()");
    	
    	if(doSyncTranslateNum()){
    		notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, null);
    	}
    	else{
    		notifyError(VopenServiceCode.TRANSACTION_FAIL, ErrorToString.getString(VopenServiceCode.TRANSACTION_FAIL));
    	}
    	getTransactionEngine().endTransaction(this);
    }
    
    private boolean doSyncTranslateNum() {

    	if(null == mAllInfo) 
    		return false;
    	
    	Context context = BaseApplication.getAppInstance();
    	
    	List<CollectInfo> list;
    	if(!Util.isStringEmpty(mUserId))
    		list = DBUtils.getAllCollectLogin(context, mUserId);
    	else
    		list = DBUtils.getAllCollectNoLogin(context);
    		
    	for(CollectInfo info : list){
    		
    		//所有课程
    		for(CourseInfo listInfo : mAllInfo){
    			if(listInfo.plid.equals(info.mCourse_id)){
    				int newTranslateNum = listInfo.updated_playcount - info.mCourse_translatecount;
    				if(newTranslateNum > 0){
    					updateNewTranslate(context, info.mCourse_id, newTranslateNum);
    				}
    				break;
    			}
    		}
    	}
    	
    	return true;
    }

    /**
     * 修改课程新翻译数据
     * @param uri
     * @param userid
     * @param isLogin
     * @param plid
     * @param newTranslateNum
     */
	private void updateNewTranslate(Context context, String plid, int newTranslateNum) {
		DBApi.updateNewTranslateNum(context, mUserId, plid, newTranslateNum);
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		
	}
}


