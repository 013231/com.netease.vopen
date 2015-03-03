package vopen.transactions;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.db.DBUtils;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;

import android.content.Context;
import android.util.Log;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

@Deprecated
public class DelStoreTransaction extends BaseTransaction {
    private String mUserId;
    private List<String> mPlayIds;
    private String mCookie;
    private Context mContext;
    
    public DelStoreTransaction(TransactionEngine transMgr,Context context, String userid, List<String> playid, String cookie) {
        super(transMgr, TRANSACTION_TYPE_DEL_STORE);
        // TODO Auto-generated constructor stub
        mUserId = userid;
        mPlayIds = playid;
        mCookie = cookie;
        mContext = context;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
        Log.d("DelStoreTransaction", response);
        String result = "";
        try {
            JSONObject jobj = new JSONObject(response);
            if(null != jobj){
                result = jobj.getString("result");
            }
        } catch (JSONException e) {
            notifyError(VopenServiceCode.DEL_STORE_ERR, ErrorToString.getString(VopenServiceCode.DEL_STORE_ERR));
            e.printStackTrace();
        }
        if(result.contains("success")){
            for(int i = 0;i<mPlayIds.size();i++){
                String id = mPlayIds.get(i);
                DBUtils.removeCollect(mContext, mUserId, id);
            }
            notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, response);
        }else{
            notifyError(VopenServiceCode.DEL_STORE_ERR, ErrorToString.getString(VopenServiceCode.DEL_STORE_ERR));
        }
    }

    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
        HttpRequest request = VopenProtocol.getInstance().createDelRequest(mUserId, mPlayIds, mCookie);
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

}
