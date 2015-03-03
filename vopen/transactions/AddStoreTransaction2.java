package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.framework.task.TransactionListener;
import common.pal.PalLog;
import common.util.NameValuePair;

/**
 * 添加一个收藏
 */
public class AddStoreTransaction2 extends BaseTransaction {
	static final String TAG = "AddStoreTransaction2";

	private String mPlayId;
	private String mMobToken;
	private String mUrsId;
	private boolean mResend;

	public AddStoreTransaction2(TransactionEngine transMgr, String playid,
			String token, String ursId) {
		super(transMgr, TRANSACTION_TYPE_ADD_STORE2);
		mPlayId = playid;
		mMobToken = token;
		mUrsId = ursId;
		mResend = false;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		try {
			JSONObject jobj = new JSONObject(response);
			JSONObject status = jobj.optJSONObject("status");
			if (status != null) {
				int code = status.optInt("code");
				if (code == 0) {
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
							response);
				} else if (code == -10000) {// mob token过期，重新获取mob token
					if (mResend) {// 第二次请求还是返回-10000
						notifyError(
								VopenServiceCode.ADD_STORE_ERR,
								ErrorToString
										.getString(VopenServiceCode.ADD_STORE_ERR));
					} else {
						PalLog.d(TAG, "mob-token过期，重新获取");
						GetMobTokenTransation tran = new GetMobTokenTransation(
								getTransactionEngine(), mUrsId);
						tran.setListener(new TransactionListener() {
							@Override
							public void onTransactionMessage(int code,
									int arg1, int arg2, Object arg3) {
								// 获取到新的mobtoken，重新执行这个transaction
								PalLog.d(TAG, "获取到新的mob-token");
								mMobToken = (String) arg3;
								getTransactionEngine().beginTransaction(
										AddStoreTransaction2.this);
							}

							@Override
							public void onTransactionError(int errCode,
									int arg1, int arg2, Object arg3) {
								notifyError(
										VopenServiceCode.ADD_STORE_ERR,
										ErrorToString
												.getString(VopenServiceCode.ADD_STORE_ERR));
							}
						});
						getTransactionEngine().beginTransaction(tran);
						mResend = true;
					}

				} else if (code == -11111) {// urs token过期
					PalLog.d(TAG, "urs token过期，需要重新登录");
					notifyError(VopenServiceCode.RELOGIN_NEEDED,
							ErrorToString
									.getString(VopenServiceCode.RELOGIN_NEEDED));
				} else {
					notifyError(VopenServiceCode.ADD_STORE_ERR,
							ErrorToString
									.getString(VopenServiceCode.ADD_STORE_ERR));
				}
			}
		} catch (JSONException e) {
			notifyError(VopenServiceCode.ADD_STORE_ERR,
					ErrorToString.getString(VopenServiceCode.ADD_STORE_ERR));
			e.printStackTrace();
		}
	}

	@Override
	public void onTransact() {
		HttpRequest request = VopenProtocol.getInstance()
				.createAddStoreRequest2(mPlayId, mMobToken);
		if (!isCancel()) {
			sendRequest(request);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}
