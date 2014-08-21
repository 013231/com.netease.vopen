package vopen.transactions;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import android.util.Log;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class SendViewCourseFeedbackTransaction extends BaseTransaction {
	private String mUserId;
	private String mUUID;
	private String mPlid;
	private String mMid;
	private String mLocation;
	private String mSystem;
	private String mDeviceId;
	private String mIP;
	private String mMAC;
	private String mVersion;

	public SendViewCourseFeedbackTransaction(TransactionEngine transMgr,
			String ursId, String uuid, String plid, String mid, String loc,
			String sys, String deviceId, String ip, String mac, String version) {
		super(transMgr, TRANSACTION_SEND_VIEW_DETAIL_FEEDBACK);
		mUserId = ursId;
		mUUID = uuid;
		mPlid = plid;
		mMid = mid;
		mLocation = loc;
		mSystem = sys;
		mDeviceId = deviceId;
		mIP = ip;
		mMAC = mac;
		mVersion = version;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		Log.d("SendViewCourseFeedbackTransaction", response);
		notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, response);
	}

	@Override
	public void onTransact() {
		HttpRequest request = VopenProtocol.getInstance()
				.createSendViewDetailFeedBackRequest(mUserId, mUUID, mPlid,
						mMid, mLocation, mSystem, mDeviceId, mIP, mMAC,
						mVersion);
		if (!isCancel()) {
			sendRequest(request);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}
