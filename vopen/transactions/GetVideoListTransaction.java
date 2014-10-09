package vopen.transactions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CourseInfo;
import vopen.tools.FileUtils;
import android.text.TextUtils;

import com.netease.vopen.app.VopenApp;
import com.netease.vopen.pal.ErrorToString;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

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
		PalLog.d(TAG, "返回数据成功");
		notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
				createResult(NOTIFY_STEP_GET_DATA, null));// 更新进度
		long requestEnd = System.currentTimeMillis();
		PalLog.d(TAG, "数据返回共花费时间:" + (requestEnd - startTime));
		List<CourseInfo> allDataList = parseResponse(response);
		if (allDataList != null && allDataList.size() > 0) {
			// 保存数据到缓存文件
			long start2 = System.currentTimeMillis();
			FileUtils.writeCourseListToCache(VopenApp.getAppInstance(),
					response, TAG);
			long end2 = System.currentTimeMillis();
			PalLog.d(TAG, "保存数据到缓存文件所用时间：" + (end2 - start2));

			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
					createResult(NOTIFY_STEP_DATA_OK, allDataList));
		} else {
			notifyResponseError(VopenServiceCode.ERR_DATA_PARSE,
					ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
		}
		endTime = System.currentTimeMillis();
		PalLog.d(TAG, "总共花费的时间:" + (endTime - startTime));
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		PalLog.d(TAG, "返回数据失败，尝试读取本地缓存");
		String resultJson = FileUtils.readCourseListFromCache(
				VopenApp.getAppInstance(), TAG);
		if (TextUtils.isEmpty(resultJson)) {
			PalLog.e(TAG, "本地缓存文件为空！");
			notifyError(VopenServiceCode.GET_VIDEO_NO_DATA, null);
			return;
		}
		List<CourseInfo> allDataList = parseResponse(resultJson);
		if (allDataList != null && allDataList.size() > 0) {
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
					createResult(NOTIFY_STEP_DATA_LOCAL, allDataList));
		} else {
			notifyError(VopenServiceCode.GET_VIDEO_NO_DATA, null);
		}
	}

	@Override
	public void onTransact() {
		if (!isCancel()) {
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
					createResult(NOTIFY_STEP_BEGIN, null));// 更新进度
			HttpRequest httpRequest = VopenProtocol.getInstance()
					.createGetVListRequest();
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

	private static List<CourseInfo> parseResponse(String resultJson) {
		List<CourseInfo> allDataList = new ArrayList<CourseInfo>();
		// 开始解析数据
		long start = System.currentTimeMillis();
		try {
			JSONArray courseArray = new JSONArray(resultJson);
			int len = courseArray.length();
			JSONObject obj = null;
			for (int i = 0; i < len; i++) {
				obj = courseArray.getJSONObject(i);
				CourseInfo info = new CourseInfo(obj);
				if (TextUtils.isEmpty(info.type))
					continue;
				allDataList.add(info);
			}
			long end = System.currentTimeMillis();
			PalLog.d(TAG, "解析数据所用时间：" + (end - start));
		} catch (JSONException e) {
			PalLog.e(TAG, "解析数据失败");
		}
		return allDataList;
	}
	
	public static List<CourseInfo> getCache(){
		String resultJson = FileUtils.readCourseListFromCache(
				VopenApp.getAppInstance(), TAG);
		List<CourseInfo> allDataList = parseResponse(resultJson);
		return allDataList;
	}

	private Object[] createResult(int step, List<CourseInfo> allInfo) {
		Object[] obj = new Object[2];
		obj[0] = step;
		obj[1] = allInfo;
		return obj;
	}

}
