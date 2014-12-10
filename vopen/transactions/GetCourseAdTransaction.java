package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CourseAdInfo;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.Util;

public class GetCourseAdTransaction extends BaseTransaction {
	private static final String TAG = "GetCourseAdTransaction";
	String mPlid;

	public GetCourseAdTransaction(TransactionEngine transMgr, String plid) {
		super(transMgr, TRANSACTION_TYPE_GET_VIDEO_AD);
		mPlid = plid;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		if (!Util.isStringEmpty(response)) {
			try {
				JSONObject jobj = new JSONObject(response);
				CourseAdInfo adInfo = null;
				if (jobj.length() != 0){
					adInfo = new CourseAdInfo(jobj);
				}
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, adInfo);
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
	public void onTransact() {
		HttpRequest httpRequest = VopenProtocol.getInstance()
				.createGetCourseAdInfoRequest(mPlid);
		if (!isCancel()) {
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}
