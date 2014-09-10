package vopen.transactions;

import vopen.app.BaseApplication;
import vopen.db.DBApi;
import vopen.db.DBApi.AccountInfo;
import vopen.db.DBUtils;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

public class LoginTransaction extends BaseTransaction{
	private static final String TAG = "LoginTransaction";
    String mUserName;
    String mPwd;

    public LoginTransaction(TransactionEngine transMgr,String username, String pwd) {
        super(transMgr, TRANSACTION_TYPE_LOGIN);
        // TODO Auto-generated constructor stub
        mUserName = username;
        mPwd = pwd;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
        String rst = new String();
        int isok = 0;
        for (int index = 0; index < pairs.length; index++) {

            if ("Set-Cookie".equals(pairs[index].getName())) {
                
                String value = pairs[index].getValue();
                String[] vs = value.split(";");

                for (int i = 0; i < vs.length; i++) {
                    
                    String[] vss = vs[i].split("=");

                    if ("P_INFO".equals(vss[0])
                            || "NTES_SESS".equals(vss[0])
                            || "S_INFO".equals(vss[0])) {

                        rst = rst + vs[i] + ";";
                        isok++;
                    }
                }
            }
        }

        boolean loginsuccess = true;

        if (isok != 3) {
            loginsuccess = false;
        }

        if (loginsuccess) {

            String[] info = rst.split(";");
            String temp = "";

            int j = 0;

            while (j < 3) {
                if (info[j].contains("NTES_SESS")) {
                    temp = temp + info[j] + ";";
                }
                j++;
            }
            rst = temp;//cookie
            loginSuccess(rst);
            notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, rst);
        }else{
        	loginFail();
        	if(response.contains("输入法是否正确")){
        		notifyError(VopenServiceCode.LOGIN_FAIL,ErrorToString.getString(VopenServiceCode.LOGIN_FAIL));
        	}else if(response.contains("Redirect")){
        		//当连接至需要认证网络认证时
        		notifyError(VopenServiceCode.LOGIN_FAIL,ErrorToString.getString(VopenServiceCode.ERR_NETWORK_OTHER));
        	}else{
        		notifyError(VopenServiceCode.LOGIN_FAIL,ErrorToString.getString(VopenServiceCode.LOGIN_FAIL));
        	}
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
        HttpRequest request = VopenProtocol.getInstance().createLoginRequest(mUserName, mPwd);
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }
    /**
     *  登录成功
	 @param cookie     
	*/
    private void loginSuccess(String cookie) {
		// TODO Auto-generated method stub
    	PalLog.i(TAG, "loginSuccess");
    	if (!mUserName.contains("@")) {
            mUserName = mUserName + "@163.com";
        }
    	AccountInfo info = new AccountInfo();
    	info.mUser_account = mUserName;
    	info.mUser_pwd = mPwd;
    	info.mUser_cookie = cookie;
    	info.mIs_login = true;
    	DBUtils.setLoginAccount(BaseApplication.getAppInstance(), info);
	}
    
    /**
     * 登录失败
     */
	private void loginFail(){
		PalLog.i(TAG, "loginFail");
		DBApi.deleteAllCollect(BaseApplication.getAppInstance(), mUserName);
		DBApi.deleteAccountByUserAccount(BaseApplication.getAppInstance(), mUserName);
    }
}
