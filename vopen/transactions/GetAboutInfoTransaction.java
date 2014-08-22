package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

import com.netease.vopen.pal.ErrorToString;

import vopen.app.BaseApplication;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.AboutInfo;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class GetAboutInfoTransaction extends BaseTransaction {
	
	public static final String ABOUT_INFO_FILE = "about";
    public static final String ABOUT_INFO_KEY = "about_json";
	
    public GetAboutInfoTransaction(TransactionEngine transMgr) {
        super(transMgr, TRANSACTION_TYPE_GET_ABOUT_INFO);
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
    	AboutInfo about = null;
		try {
			SharedPreferences sp = BaseApplication.getAppInstance()
					.getSharedPreferences(ABOUT_INFO_FILE, 0);
			sp.edit().putString(ABOUT_INFO_KEY, response).commit();
			
			JSONObject jobj = new JSONObject(response);
			about = new AboutInfo(jobj);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(about != null){
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, about);
		}
		else{
			notifyError(VopenServiceCode.ERR_DATA_PARSE, ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
		}
    }
    
    @Override
    public void onResponseError(int errCode, Object err) {
    	SharedPreferences sp = BaseApplication.getAppInstance()
				.getSharedPreferences(ABOUT_INFO_FILE, 0);
		String json = sp.getString(ABOUT_INFO_KEY, "");
		if (json.length() == 0){
			super.onResponseError(errCode, err);
		}else{
			JSONObject jobj;
			AboutInfo about = null;
			try {
				jobj = new JSONObject(json);
				about = new AboutInfo(jobj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (about != null){
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, about);
			}else{
				notifyError(VopenServiceCode.ERR_DATA_PARSE, ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
			}
		}
    }

    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
        HttpRequest request = VopenProtocol.getInstance().createGetAboutRequest();
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

}
