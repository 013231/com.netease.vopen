package vopen.transactions;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.preference.PreferenceManager;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.RecommAppInfo;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.StringUtil;
import common.util.Util;

public class GetRecommAppTransaction extends BaseTransaction {
	/**
     * 推荐APP,缓存记录的配置文件
     */
    public static final String RECOMM_APP_JSON = "recomm_app_json";

	public GetRecommAppTransaction(TransactionEngine transMgr) {
		super(transMgr, TRANSACTION_GET_RECOMM_APP);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		// TODO Auto-generated method stub
		if (!Util.isStringEmpty(response)) {
			// 写入本地
			PreferenceManager
					.getDefaultSharedPreferences(
							BaseApplication.getAppInstance()).edit()
					.putString(RECOMM_APP_JSON, response).commit();
			try {
				JSONArray jsonarr = new JSONArray(response);
				List<RecommAppInfo> list = new LinkedList<RecommAppInfo>();
				if (StringUtil.checkObj(jsonarr)) {
					int length = jsonarr.length();
					for (int index = 0; index < length; index++) {
						RecommAppInfo info = new RecommAppInfo(
								jsonarr.getString(index));
						list.add(info);
					}
				}
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, list);
				return;
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		notifyResponseError(VopenServiceCode.TRANSACTION_FAIL, null);
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		// TODO Auto-generated method stub
		PalLog.d("GetRecommAppTransaction", "No web connect,load from local");
		String json = PreferenceManager
				.getDefaultSharedPreferences(
						BaseApplication.getAppInstance()).getString(RECOMM_APP_JSON, "");
		if (json != null) {
			// 解析后发送
			JSONArray jsonarr;
			try {
				jsonarr = new JSONArray(json);
				List<RecommAppInfo> list = new LinkedList<RecommAppInfo>();
				if (StringUtil.checkObj(jsonarr)) {
					int length = jsonarr.length();
					for (int index = 0; index < length; index++) {
						RecommAppInfo info = new RecommAppInfo(
								jsonarr.getString(index));
						list.add(info);
					}
				}
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, list);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				notifyError(VopenServiceCode.TRANSACTION_FAIL, null);
				e.printStackTrace();
			}
		} else {
			notifyError(VopenServiceCode.TRANSACTION_FAIL, null);
		}
	}

	@Override
	public void onTransact() {
		// TODO Auto-generated method stub
		HttpRequest httpRequest = VopenProtocol.getInstance().createGetRecommAppRequest();
		if (!isCancel()) {
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}