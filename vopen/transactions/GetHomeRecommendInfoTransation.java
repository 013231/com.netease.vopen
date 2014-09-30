package vopen.transactions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.RecommendInfo;
import android.content.SharedPreferences;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

/**
 * 获取首页上的所有推荐内容： 包括 1.轮播图 2.小编推荐 3.赏课 4.TED
 * 
 * @author ymsong
 * 
 */
public class GetHomeRecommendInfoTransation extends BaseTransaction {

	private static final String FILE_NAME = "home";
	private static final String KEY = "json";

	public GetHomeRecommendInfoTransation(TransactionEngine transMgr) {
		super(transMgr, TRANSACTION_GET_HOME_RECOMMEDN_INFO);
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		List<List<RecommendInfo>> recommendInfos = null;
		// 缓存在preference中
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(FILE_NAME, 0);
		sp.edit().putString(KEY, response).commit();
		recommendInfos = parseResponse(response);
		if (recommendInfos != null && recommendInfos.size() > 0) {
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, recommendInfos);
		} else {
			notifyError(VopenServiceCode.ERR_DATA_PARSE,
					ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
		}
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(FILE_NAME, 0);
		String json = sp.getString(KEY, "");
		if (json.length() == 0) {
			super.onResponseError(errCode, err);
		} else {
			List<List<RecommendInfo>> recommendInfos = null;
			recommendInfos = parseResponse(json);
			if (recommendInfos != null && recommendInfos.size() > 0) {
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
						recommendInfos);
			} else {
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString
								.getString(VopenServiceCode.ERR_DATA_PARSE));
			}
		}
	}

	private static List<List<RecommendInfo>> parseResponse(String response) {
		List<List<RecommendInfo>> recommendInfos = new ArrayList<List<RecommendInfo>>();
		try {
			JSONObject jObj = new JSONObject(response);
			for (int i = 0; i < jObj.length(); i++) {
				JSONArray jArray = jObj.getJSONArray(String.valueOf(i));
				List<RecommendInfo> infoList = new ArrayList<RecommendInfo>();
				if (jArray != null) {
					for (int j = 0; j < jArray.length(); j++) {
						RecommendInfo rInfo = new RecommendInfo(
								jArray.getJSONObject(j));
						infoList.add(rInfo);
					}
				}
				recommendInfos.add(infoList);
			}
		} catch (JSONException e) {

		}
		return recommendInfos;
	}

	public static List<List<RecommendInfo>> getCache() {
		List<List<RecommendInfo>> recommendInfos = null;
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(FILE_NAME, 0);
		String json = sp.getString(KEY, "");
		recommendInfos = parseResponse(json);
		return recommendInfos;
	}

	@Override
	public void onTransact() {
		HttpRequest request = VopenProtocol.getInstance()
				.createGetHomeRecommendInfoRequest();
		if (!isCancel()) {
			sendRequest(request);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}
