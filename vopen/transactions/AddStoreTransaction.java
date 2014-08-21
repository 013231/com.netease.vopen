package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import android.util.Log;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class AddStoreTransaction extends BaseTransaction {
    private String mUserId;
    private String mPlayId;
    private String mCookie;

    public AddStoreTransaction(TransactionEngine transMgr, String userid, String playid, String cookie) {
        super(transMgr, TRANSACTION_TYPE_ADD_STORE);
        // TODO Auto-generated constructor stub
        mUserId = userid;
        mPlayId = playid;
        mCookie = cookie;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
        Log.d("AddStoreTransaction", response);
        String result = "";
        try {
            JSONObject jobj = new JSONObject(response);
            if(null != jobj){
                result = jobj.getString("result");
            }
        } catch (JSONException e) {
            notifyError(VopenServiceCode.ADD_STORE_ERR, ErrorToString.getString(VopenServiceCode.ADD_STORE_ERR));
            e.printStackTrace();
        }
        if(result.contains("success")){
            notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, response);
        }else{
            notifyError(VopenServiceCode.ADD_STORE_ERR, ErrorToString.getString(VopenServiceCode.ADD_STORE_ERR));
        }
    }

    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
        HttpRequest request = VopenProtocol.getInstance().createAddStoreRequest(mUserId, mPlayId, mCookie);
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

}
