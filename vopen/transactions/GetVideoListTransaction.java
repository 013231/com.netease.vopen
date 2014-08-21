package vopen.transactions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.DBApi;
import vopen.db.DBApi.CourseInfo;
import vopen.db.DBApi.CourseType;
import vopen.db.DBUtils;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import android.text.TextUtils;
import android.util.Log;

import com.netease.vopen.pal.Constants;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.Util;

public class GetVideoListTransaction extends BaseTransaction {
	private static final String TAG = "GetVideoListTransaction";
	private int NOTIFY_STEP_BEGIN = 20;
	private int NOTIFY_STEP_GET_DATA = 40;
	private int NOTIFY_STEP_SAVE_DATA = 60;
	private int NOTIFY_STEP_DATA_OK = 100;
	
	private long startTime;
	private long endTime;
	
	
	public GetVideoListTransaction(TransactionEngine transMgr) {
		super(transMgr, TRANSACTION_TYPE_GET_VIDEO_LIST);
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		// TODO Auto-generated method stub
		PalLog.v(TAG,"onResponseSuccess");
		notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, createResult(NOTIFY_STEP_GET_DATA, null));//更新进度
		
		
		
		long saveStart = System.currentTimeMillis();
		saveAndFormatData(response);
		long saveEnd = System.currentTimeMillis();
		Log.d(TAG, "GetVideoListTransaction-save consume time:" + (saveEnd - saveStart));
		endTime = System.currentTimeMillis();
		Log.d(TAG, "GetVideoListTransaction-all consume time:" + (endTime - startTime));
	}
	@Override
	public void onResponseError(int errCode, Object err) {
		// TODO Auto-generated method stub
//		if(errCode < 1000 && errCode >= 0) {
//			errCode = getHttpErrorCode(errCode, getType());
//		}
//
//		notifyError(errCode, err);
		saveAndFormatData(null);
	}

	@Override
	public void onTransact() {
		if (!isCancel()) {
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, createResult(NOTIFY_STEP_BEGIN, null));//更新进度
			if(Util.CheckNetwork(BaseApplication.getAppInstance())){ // 网络正常
				PalLog.v(TAG,"CheckNetwork is true");
				HttpRequest httpRequest = VopenProtocol.getInstance().createGetVListRequest();
				sendRequest(httpRequest);
			}else{  // 网络异常	
				saveAndFormatData(null);
				getTransactionEngine().endTransaction(this);
			}
			
		} else {
			getTransactionEngine().endTransaction(this);
		}

	}
	
	private Object[] createResult(int step, List<vopen.response.CourseInfo> allInfo){
		Object[] obj = new Object[2];
		obj[0] = step;
		obj[1] = allInfo;
		
		return obj;
	}
	
    private void saveAndFormatData(String resultJson){
		boolean isLocal = Util.isStringEmpty(resultJson);
		
		if(isLocal){
			resultJson = DBUtils.getLocalCourseByType(BaseApplication.getAppInstance(), CourseType.DATA_TYPE_ALL);
			if(TextUtils.isEmpty(resultJson)){
				PalLog.v(TAG,"saveAndFormatData 01");
				notifyError(VopenServiceCode.GET_VIDEO_NO_DATA,null);
				return;
			}else{
				PalLog.v(TAG,"saveAndFormatData 02");
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, createResult(NOTIFY_STEP_GET_DATA, null));//更新进度
			}
		}
		
		List<vopen.response.CourseInfo> allDataList = new ArrayList<vopen.response.CourseInfo>();
		PalLog.v(TAG, "saveAndFormatData 03");
		long start = System.currentTimeMillis();
		try {
			JSONArray courseArray = new JSONArray(resultJson);
			long time1 = System.currentTimeMillis();
			int len = courseArray.length();

//			JSONArray toutuArray = new JSONArray();
//			JSONArray enjoyArray = new JSONArray();
//			JSONArray tedArray = new JSONArray();
//			JSONArray qitaArray = new JSONArray();
//			JSONArray jingjiArray = new JSONArray();
//			JSONArray renwenArray = new JSONArray();
//			JSONArray shuliArray = new JSONArray();
//			JSONArray xinliArray = new JSONArray();
//			JSONArray zhexueArray = new JSONArray();
			
			long time2 = System.currentTimeMillis();
			/**
			 * 将各种类型归类 将各类型存入数据库
			 */
			JSONObject obj = null;
			for (int i = 0; i < len; i++) {
				obj = courseArray.getJSONObject(i);
				vopen.response.CourseInfo info = new vopen.response.CourseInfo(obj);
				if (Util.isStringEmpty(info.type))
					continue;
				allDataList.add(info);
//				if (info.type.equals(Constants._TAG_head)) { // 头图
//                    toutuArray.put(obj);
//                } else if (info.type.equals(Constants._TAG_hot)) { // 最热
//                    enjoyArray.put(obj);
//                } else if (info.type.equals(Constants._TAG_TED)) { // TED
//                    tedArray.put(obj);
//                } else if (info.type.equals(Constants._TAG_other)) { // 其他
//                    qitaArray.put(obj);
//                } else { // 类型重叠情况
//                    if (info.type.contains(Constants._TAG_economy)) { // 经济
//                        jingjiArray.put(obj);
//                    }
//                    if (info.type.contains(Constants._TAG_humanity)) { // 人文
//                        renwenArray.put(obj);
//                    }
//                    if (info.type.contains(Constants._TAG_mathematisch)) { // 数理
//                        shuliArray.put(obj);
//                    }
//                    if (info.type.contains(Constants._TAG_psychology)) { // 心理学
//                        xinliArray.put(obj);
//                    }
//                    if (info.type.contains(Constants._TAG_philosophy)) { // 哲学
//                        zhexueArray.put(obj);
//                    }
//                }
				
			}
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
					createResult(NOTIFY_STEP_SAVE_DATA, null));// 更新进度
			// toutuString = toutuArray.toString();
			long time3 = System.currentTimeMillis();
			if(!isLocal){
				ArrayList<CourseInfo> data = new ArrayList<CourseInfo>();
				addToList(data, Constants.DATA_TYPE_ALL, resultJson);
//				addToList(data, Constants.DATA_TYPE_TOUTU, toutuArray.toString());
//				addToList(data, Constants.DATA_TYPE_ENJOY, enjoyArray.toString());
//				addToList(data, Constants.DATA_TYPE_TED, tedArray.toString());
//				addToList(data, Constants.DATA_TYPE_QITA, qitaArray.toString());
//				addToList(data, Constants.DATA_TYPE_JINGJI, jingjiArray.toString());
//				addToList(data, Constants.DATA_TYPE_RENWEN, renwenArray.toString());
//				addToList(data, Constants.DATA_TYPE_SHULI, shuliArray.toString());
//				addToList(data, Constants.DATA_TYPE_XINLI, xinliArray.toString());
//				addToList(data, Constants.DATA_TYPE_ZHEXUE, zhexueArray.toString());
				DBApi.saveNewAllCourse(BaseApplication.getAppInstance(), data);
			}
			long end = System.currentTimeMillis();
			PalLog.i(TAG, "saveAndFormatData timeAll = " + (end - start)
					+ "time1 = " + (time1 - start) + "time2 = "
					+ (time2 - time1) + "time3 = " + (time3 - time2)
					+ "time4 = " + (end - time3));
		} catch (JSONException e) {
			e.printStackTrace();
			notifyError(VopenServiceCode.ERR_DATA_PARSE, null);
		}
		PalLog.v(TAG, "saveAndFormatData 04");
		notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
				createResult(NOTIFY_STEP_DATA_OK, allDataList));
	}
    /**
	 * 
	 */
    private void addToList(ArrayList<CourseInfo> list, String type, String jsonStr){
		
    	CourseInfo tempInfo = new CourseInfo();
		tempInfo.mType = type;
		tempInfo.mContent = jsonStr;
		list.add(tempInfo);
	}
    
}
