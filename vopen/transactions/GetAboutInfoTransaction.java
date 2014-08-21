package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import com.netease.vopen.pal.ErrorToString;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.AboutInfo;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class GetAboutInfoTransaction extends BaseTransaction {

    public GetAboutInfoTransaction(TransactionEngine transMgr) {
        super(transMgr, TRANSACTION_TYPE_GET_ABOUT_INFO);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
    	AboutInfo about = null;
		try {
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
