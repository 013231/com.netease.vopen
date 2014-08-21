package vopen.transactions;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

public class RegisterTransaction extends BaseTransaction {
    
    String mUserName;
    String mPwd;

    public RegisterTransaction(TransactionEngine transMgr,String username, String pwd) {
        super(transMgr, TRANSACTION_TYPE_REGISTER);
        // TODO Auto-generated constructor stub
        mUserName = username;
        mPwd = pwd;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
            
        PalLog.d("RegisterTransaction", "status =" + response);

        String resultString = response.substring(0, 3);
        int errCode;
        if ("200".equals(resultString)) {
            notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, resultString);
        } else if ("401".equals(resultString)) {
            errCode = VopenServiceCode.REGISTER_ERR_REGACCOUNTINVAILD;
            notifyError(errCode,ErrorToString.getString(errCode));
        } else if ("421".equals(resultString)) {
            errCode = VopenServiceCode.REGISTER_ERR_USEREXIST;
            notifyError(errCode,ErrorToString.getString(errCode));
        } else {
            errCode = VopenServiceCode.REGISTER_ERR_REGFAIL;
            notifyError(errCode,ErrorToString.getString(errCode));
        }
    }
    
    @Override
    public void onResponseError(int errCode, Object err) {
        // TODO Auto-generated method stub
        notifyError(errCode, err);
    }

    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
        HttpRequest request = VopenProtocol.getInstance().createRegeisterRequest(mUserName, mPwd);
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

}
