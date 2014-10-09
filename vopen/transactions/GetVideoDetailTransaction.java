package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import com.netease.vopen.pal.ErrorToString;

import vopen.app.BaseApplication;
import vopen.db.DBUtils;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CourseInfo;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.Util;

public class GetVideoDetailTransaction extends BaseTransaction {
	private static final String TAG = "GetVideoDetailTransaction";
	String mPlid;

	// int mScreenType;
	// boolean mIsNetWork;

	public GetVideoDetailTransaction(TransactionEngine transMgr, String plid) {
		super(transMgr, TRANSACTION_TYPE_GET_VIDEO_DETAIL);
		mPlid = plid;
		// mScreenType = screenType;
		// mIsNetWork = isNetWork;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		if (!Util.isStringEmpty(response)) {
			// 写入数据库
			DBUtils.insertOrUpdateCourse(BaseApplication.getAppInstance(),
					mPlid, response);
			try {
				JSONObject jobj = new JSONObject(response);
				CourseInfo vDetail = new CourseInfo(jobj);
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, vDetail);
				return;
			} catch (JSONException e1) {
				PalLog.e(TAG, "解析数据失败:" + e1.toString());
				notifyResponseError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
			}
		} else {
			notifyResponseError(VopenServiceCode.GET_VIDEO_DETAIL_FAIL, null);
		}
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		PalLog.d(TAG, "返回数据失败，尝试读取本地缓存");
		CourseInfo course = DBUtils.getCourseByPlid(
				BaseApplication.getAppInstance(), mPlid);
		if (course != null) {
			if (Util.isStringEmpty(course.plid)) {
				notifyError(VopenServiceCode.NO_DETAIL_WEB_AND_LOCAL, null);
			} else {
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, course);
			}
		} else {
			notifyError(VopenServiceCode.NO_DETAIL_WEB_AND_LOCAL, null);
		}
	}

	@Override
	public void onTransact() {
		HttpRequest httpRequest = VopenProtocol.getInstance()
				.createGetVideoDetailRequest(mPlid);
		if (!isCancel()) {
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

	// private String addImageUrlFromScreenType(String imageUrl, int
	// screenType){
	// if(screenType==3){
	// imageUrl = imageUrl + imageUrl.replace("http://", "")+".93x124.auto.jpg";
	// }else if(screenType==5){
	// imageUrl = imageUrl + imageUrl.replace("http://", "")+".93x124.auto.jpg";
	// }else{
	// imageUrl = imageUrl + imageUrl.replace("http://", "")+".63x86.auto.jpg";
	// }
	// return imageUrl;
	// }

}
