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

import com.netease.vopen.pal.Constants;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.Util;

public class GetVideoListTransaction extends BaseTransaction {
	private static final String TAG = "GetVideoListTransaction";
	public static final int NOTIFY_STEP_BEGIN = 20;
	public static final int NOTIFY_STEP_GET_DATA = 40;
	public static final int NOTIFY_STEP_SAVE_DATA = 60;
	public static final int NOTIFY_STEP_DATA_OK = 100;
	public static final int NOTIFY_STEP_DATA_LOCAL = 99;

	private long startTime;
	private long endTime;

	public GetVideoListTransaction(TransactionEngine transMgr) {
		super(transMgr, TRANSACTION_TYPE_GET_VIDEO_LIST);
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		PalLog.v(TAG, "onResponseSuccess");
		notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
				createResult(NOTIFY_STEP_GET_DATA, null));//更新进度
		long requestEnd = System.currentTimeMillis();
		PalLog.d(TAG, "数据返回共花费时间:"+(requestEnd-startTime));
		saveAndFormatData(response);
		endTime = System.currentTimeMillis();
		PalLog.d(TAG, "总共花费的时间:" + (endTime - startTime));
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		notifyError(errCode, null);
		saveAndFormatData(null);
	}

	@Override
	public void onTransact() {
		if (!isCancel()) {
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
					createResult(NOTIFY_STEP_BEGIN, null));//更新进度
			HttpRequest httpRequest = VopenProtocol.getInstance()
					.createGetVListRequest();
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

	private void saveAndFormatData(String resultJson) {
		boolean isLocal = Util.isStringEmpty(resultJson);
		if (isLocal) {
			PalLog.d(TAG, "从本地读取！");
			resultJson = DBUtils.getLocalCourseByType(
					BaseApplication.getAppInstance(), CourseType.DATA_TYPE_ALL);
			if (TextUtils.isEmpty(resultJson)) {
				PalLog.e(TAG, "本地无数据！");
				notifyError(VopenServiceCode.GET_VIDEO_NO_DATA, null);
				return;
			} else {
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
						createResult(NOTIFY_STEP_GET_DATA, null));//更新进度
			}
		}
		List<vopen.response.CourseInfo> allDataList = new ArrayList<vopen.response.CourseInfo>();
		//开始解析数据
		long start = System.currentTimeMillis();
		try {
			JSONArray courseArray = new JSONArray(resultJson);
			int len = courseArray.length();
			JSONObject obj = null;
			for (int i = 0; i < len; i++) {
				obj = courseArray.getJSONObject(i);
				vopen.response.CourseInfo info = new vopen.response.CourseInfo(
						obj);
				if (Util.isStringEmpty(info.type))
					continue;
				allDataList.add(info);
			}
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
					createResult(NOTIFY_STEP_SAVE_DATA, null));// 更新进度
			// toutuString = toutuArray.toString();
			long end = System.currentTimeMillis();
			PalLog.d(TAG, "解析数据所用时间："+(end - start));
			
			long start2 = System.currentTimeMillis();
			//开始保存数据
			if (!isLocal) {
				ArrayList<CourseInfo> data = new ArrayList<CourseInfo>();
				addToList(data, Constants.DATA_TYPE_ALL, resultJson);
				DBApi.saveNewAllCourse(BaseApplication.getAppInstance(), data);
			}
			long end2 = System.currentTimeMillis();
			PalLog.d(TAG, "保存数据到数据库所用时间："+(end2- start2));
		} catch (JSONException e) {
			e.printStackTrace();
			notifyError(VopenServiceCode.ERR_DATA_PARSE, null);
		}
		notifyMessage(
				VopenServiceCode.TRANSACTION_SUCCESS,
				createResult(isLocal ? NOTIFY_STEP_DATA_LOCAL
						: NOTIFY_STEP_DATA_OK, allDataList));
	}

	private Object[] createResult(int step,
			List<vopen.response.CourseInfo> allInfo) {
		Object[] obj = new Object[2];
		obj[0] = step;
		obj[1] = allInfo;
		return obj;
	}

	/**
	 * 
	 */
	private void addToList(ArrayList<CourseInfo> list, String type,
			String jsonStr) {

		CourseInfo tempInfo = new CourseInfo();
		tempInfo.mType = type;
		tempInfo.mContent = jsonStr;
		list.add(tempInfo);
	}

}
