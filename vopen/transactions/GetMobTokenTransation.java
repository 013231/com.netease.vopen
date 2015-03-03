package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import android.content.SharedPreferences;

import com.netease.loginapi.NEConfig;
import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

/**
 * 获取mob-token。<br/>
 * 
 * @author ymsong
 */
public class GetMobTokenTransation extends BaseTransaction {
	static final String TAG = "GetMobTokenTransation";

	private static final String MOB_TOKEN_FILE = "mob_token";
	private static final String MOB_TOKEN_KEY = "token";

	private String usrId;

	public GetMobTokenTransation(TransactionEngine transMgr, String ursId) {
		super(transMgr, TRANSACTION_GET_MOB_TOKEN);
		this.usrId = ursId;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		String mobToken = null;
		try {
			JSONObject jobj = new JSONObject(response);
			JSONObject result = jobj.optJSONObject("results");
			if (result != null) {
				mobToken = result.optString("mob-token");
			}
			// 缓存在preference中
			SharedPreferences sp = BaseApplication.getAppInstance()
					.getSharedPreferences(MOB_TOKEN_FILE, 0);
			sp.edit().putString(MOB_TOKEN_KEY, mobToken).commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (mobToken != null) {
			PalLog.d(TAG, "获取mob-token成功");
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, mobToken);
		} else {
			PalLog.e(TAG, "获取mob-token失败");
			notifyError(VopenServiceCode.ERR_DATA_PARSE,
					ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
		}
	}

	private String calculateXparam() {
		// 开始换取mob-token
		JSONObject obj = new JSONObject();
		try {
			obj.put("tokenId", NEConfig.getId());
			obj.put("token", NEConfig.getToken());
			obj.put("email", usrId);
			obj.put("timestamp", System.currentTimeMillis());
		} catch (JSONException e) {
		}
		String str = obj.toString();
		return str;
	}

	@Override
	public void onTransact() {
		PalLog.d(TAG, "pulic key有缓存，直接换取mob-token...");
		HttpRequest request = VopenProtocol.getInstance()
				.createGetMobTokenRequest(-1, usrId, calculateXparam());
		if (!isCancel()) {
			sendRequest(request);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

	public static String getCacheMobToken() {
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(MOB_TOKEN_FILE, 0);
		return sp.getString(MOB_TOKEN_KEY, null);
	}

}
