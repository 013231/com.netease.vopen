package vopen.transactions;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.HotWordInfo;
import android.content.SharedPreferences;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.StringUtil;
import common.util.Util;

/**
 * 获取搜索热词
 * @author ymsong
 */
public class GetHotWordsTransaction extends BaseTransaction {
	/**
     * 缓存配置文件中的key
     */
	public static final String HOT_WORDS_FILE = "hot_words";
    public static final String HOT_WORDS_KEY = "hot_words_json";

	public GetHotWordsTransaction(TransactionEngine transMgr) {
		super(transMgr, TRANSACTION_GET_HOT_WORDS);
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		if (!Util.isStringEmpty(response)) {
			// 写入本地
			SharedPreferences sp = BaseApplication.getAppInstance()
					.getSharedPreferences(HOT_WORDS_FILE, 0);
			sp.edit().putString(HOT_WORDS_KEY, response).commit();
			try {
				JSONArray jsonarr = new JSONArray(response);
				List<HotWordInfo> list = new LinkedList<HotWordInfo>();
				if (StringUtil.checkObj(jsonarr)) {
					int length = jsonarr.length();
					for (int index = 0; index < length; index++) {
						HotWordInfo info = new HotWordInfo(
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
		PalLog.d("GetHotWordsTransaction", "No web connect,load from local");
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(HOT_WORDS_FILE, 0);
		String json = sp.getString(HOT_WORDS_KEY, "");
		if (json != null) {
			// 解析后发送
			JSONArray jsonarr;
			try {
				jsonarr = new JSONArray(json);
				List<HotWordInfo> list = new LinkedList<HotWordInfo>();
				if (StringUtil.checkObj(jsonarr)) {
					int length = jsonarr.length();
					for (int index = 0; index < length; index++) {
						HotWordInfo info = new HotWordInfo(
								jsonarr.getString(index));
						list.add(info);
					}
				}
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, list);
			} catch (JSONException e) {
				notifyError(VopenServiceCode.TRANSACTION_FAIL, null);
				e.printStackTrace();
			}
		} else {
			notifyError(VopenServiceCode.TRANSACTION_FAIL, null);
		}
	}

	@Override
	public void onTransact() {
		HttpRequest httpRequest = VopenProtocol.getInstance().createHotSearchRequest();
		if (!isCancel()) {
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}