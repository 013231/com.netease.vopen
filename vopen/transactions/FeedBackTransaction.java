package vopen.transactions;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class FeedBackTransaction extends BaseTransaction {
    String mProductType;
    String mOsVersion;
    String mAppVersion;
    String mEmail;
    String mMessage;
    
    public FeedBackTransaction(TransactionEngine transMgr,String productType, String osVersion, String appVersion, String msg, String email) {
        super(transMgr, TRANSACTION_TYPE_FEED_BACK);
        // TODO Auto-generated constructor stub
        mProductType = productType;
        mOsVersion = osVersion;
        mAppVersion = appVersion;
        mEmail = email;
        mMessage = msg;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
        notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, null);
    }
    
    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
        HttpRequest request = VopenProtocol.getInstance().createFeedBackRequest(mProductType, mOsVersion, mAppVersion, mMessage, mEmail);
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

}
