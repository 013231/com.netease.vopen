package vopen.transactions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.RecommendColumn;
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
		List<RecommendColumn> recommendColumns = null;
		// 缓存在preference中
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(FILE_NAME, 0);
		sp.edit().putString(KEY, response).commit();
		recommendColumns = parseResponse(response);
		if (recommendColumns != null && recommendColumns.size() > 0) {
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, recommendColumns);
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
			List<RecommendColumn> recommendColumns = parseResponse(json);
			if (recommendColumns != null && recommendColumns.size() > 0) {
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
						recommendColumns);
			} else {
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString
								.getString(VopenServiceCode.ERR_DATA_PARSE));
			}
		}
	}

	private static List<RecommendColumn> parseResponse(String response) {
		List<RecommendColumn> recommendInfos = new ArrayList<RecommendColumn>();
		try {
			JSONArray jsArray = new JSONArray(response);
			for (int i = 0; i < jsArray.length(); i++) {
				JSONObject jsObj = jsArray.getJSONObject(i);
				RecommendColumn column = new RecommendColumn(jsObj);
				recommendInfos.add(column);
			}
		} catch (JSONException e) {

		}
		return recommendInfos;
	}

	public static List<RecommendColumn> getCache() {
		List<RecommendColumn> recommendInfos = null;
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
