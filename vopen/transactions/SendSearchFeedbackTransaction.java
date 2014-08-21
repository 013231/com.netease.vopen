package vopen.transactions;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import android.util.Log;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class SendSearchFeedbackTransaction extends BaseTransaction {
	private String mUserId;
	private String mUUID;
	private String mSearchKey;
	private String mLocation;
	private String mSystem;
	private String mDeviceId;
	private String mIP;
	private String mMAC;
	private String mVersion;

	public SendSearchFeedbackTransaction(TransactionEngine transMgr,
			String ursId, String uuid, String key, String loc, String sys,
			String deviceId, String ip, String mac, String version) {
		super(transMgr, TRANSACTION_SEND_SEARCH_FEEDBACK);
		mUserId = ursId;
		mUUID = uuid;
		mSearchKey = key;
		mLocation = loc;
		mSystem = sys;
		mDeviceId = deviceId;
		mIP = ip;
		mMAC = mac;
		mVersion = version;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		Log.d("SendSearchFeedbackTransaction", response);
		notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, response);
	}

	@Override
	public void onTransact() {
		HttpRequest request = VopenProtocol.getInstance()
				.createSendSearchFeedBackRequest(mUserId, mUUID, mSearchKey,
						mLocation, mSystem, mDeviceId, mIP, mMAC, mVersion);
		if (!isCancel()) {
			sendRequest(request);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}
