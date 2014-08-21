package vopen.transactions;

import org.json.JSONArray;
import org.json.JSONException;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.HeadAd;
import android.content.SharedPreferences;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

/**
 * 获取头图中的广告 
 * @author ymsong
 */
public class GetHeadAdTransation extends BaseTransaction {

	public static final String HEAD_AD_FILE = "headad";
    public static final String HEAD_AD_KEY = "json";
	
	public GetHeadAdTransation(TransactionEngine transMgr) {
		super(transMgr, TRANSACTION_GET_HEAD_ADS);
		
	}
	
	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		HeadAd[] adInfos = null;
		try {
			//缓存在preference中
			SharedPreferences sp = BaseApplication.getAppInstance()
					.getSharedPreferences(HEAD_AD_FILE, 0);
			sp.edit().putString(HEAD_AD_KEY, response).commit();
			JSONArray jArray = new JSONArray(response);
			adInfos = new HeadAd[jArray.length()];
			for (int i = 0; i < jArray.length(); i++){
				adInfos[i] = new HeadAd(jArray.getJSONObject(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(adInfos != null){
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, adInfos);
		}
		else{
			notifyError(VopenServiceCode.ERR_DATA_PARSE, ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
		}
	}
	
	@Override
	public void onResponseError(int errCode, Object err) {
		SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(HEAD_AD_FILE, 0);
		String json = sp.getString(HEAD_AD_KEY, "");
		if (json.length() == 0){
			super.onResponseError(errCode, err);
		}else{
			HeadAd[] adInfos = null;
			try {
				JSONArray jArray = new JSONArray(json);
				adInfos = new HeadAd[jArray.length()];
				for (int i = 0; i < jArray.length(); i++){
					adInfos[i] = new HeadAd(jArray.getJSONObject(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (adInfos != null){
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, adInfos);
			}else{
				notifyError(VopenServiceCode.ERR_DATA_PARSE, ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
			}
		}
	}

	@Override
	public void onTransact() {
		HttpRequest request = VopenProtocol.getInstance().createGetHeadAdInfoRequest();
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
	}

}
