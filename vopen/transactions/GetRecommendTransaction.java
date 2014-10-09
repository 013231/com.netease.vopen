package vopen.transactions;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.vopen.pal.ErrorToString;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.RecommendItem;
import android.content.SharedPreferences;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.StringUtil;

/**
 * 获取个性化推荐内容
 * 
 * @author ymsong
 * 
 */
public class GetRecommendTransaction extends BaseTransaction {
	/**
	 * 缓存配置文件中的key
	 */
	public static final String RECOMMEND_FILE = "recommend";
	public static final String RECOMMEND_KEY = "recommend_json";
	private String mUuid;
	private String mUserId;
	private int mCount;

	public GetRecommendTransaction(TransactionEngine transMgr, String uuid,
			String usrId, int count) {
		super(transMgr, TRANSACTION_GET_RECOMMENDS);
		mUuid = uuid;
		mUserId = usrId;
		mCount = count;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		List<RecommendItem> list = parseResponse(response);
		if (list != null && list.size() > 0) {
			// 缓存写入本地preference
			SharedPreferences sp = BaseApplication.getAppInstance()
					.getSharedPreferences(RECOMMEND_FILE, 0);
			sp.edit().putString(RECOMMEND_KEY, response).commit();
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, list);
		} else {
			notifyResponseError(VopenServiceCode.ERR_DATA_PARSE,
					ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
		}
	}

	private static List<RecommendItem> parseResponse(String response) {
		List<RecommendItem> list = new LinkedList<RecommendItem>();
		try {
			JSONObject jsoresult = new JSONObject(response);
			JSONArray jsonarr = jsoresult.getJSONArray("result");
			if (StringUtil.checkObj(jsonarr)) {
				int length = jsonarr.length();
				for (int index = 0; index < length; index++) {
					RecommendItem info = new RecommendItem(
							jsonarr.getString(index));
					list.add(info);
				}
			}
		} catch (JSONException e1) {
		}
		return list;
	}

	public static List<RecommendItem> getCache() {
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(RECOMMEND_FILE, 0);
		String json = sp.getString(RECOMMEND_KEY, "");
		List<RecommendItem> list = parseResponse(json);
		return list;
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		PalLog.d("GetRecommendTransaction", "No web connect,load from local");
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(RECOMMEND_FILE, 0);
		String json = sp.getString(RECOMMEND_KEY, "");
		List<RecommendItem> list = parseResponse(json);
		if (list != null && list.size() > 0) {
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, list);
		} else {
			notifyError(VopenServiceCode.TRANSACTION_FAIL, null);
		}
	}

	@Override
	public void onTransact() {
		HttpRequest httpRequest = VopenProtocol.getInstance()
				.createRecommendRequest(mUuid, mUserId, mCount);
		if (!isCancel()) {
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}