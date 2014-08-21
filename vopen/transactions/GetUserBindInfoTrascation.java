package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.BindAccountInfo;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.Util;

public class GetUserBindInfoTrascation extends BaseTransaction {
	private String mUserId;
	
	public GetUserBindInfoTrascation(TransactionEngine transMgr, String userid) {
		super(transMgr, TRANSACTION_GET_USER_BIND_INFO);
		mUserId = userid;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		PalLog.d("GetUserBindInfoTrascation", response);
		if (!Util.isStringEmpty(response)) {
			try {
				JSONObject jsoresult = new JSONObject(response);
				BindAccountInfo info = new BindAccountInfo(jsoresult);
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, info);
				return;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		notifyResponseError(VopenServiceCode.TRANSACTION_FAIL, null);
	}

	@Override
	public void onTransact() {
		HttpRequest httpRequest = VopenProtocol.getInstance()
				.createGetBindAccountInfoRequest(mUserId);
		if (!isCancel()) {
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}
