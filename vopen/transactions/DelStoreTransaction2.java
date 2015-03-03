package vopen.transactions;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.db.DBUtils;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;

import com.netease.vopen.app.VopenApp;
import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.framework.task.TransactionListener;
import common.pal.PalLog;
import common.util.NameValuePair;

/**
 * 删除几个收藏
 */
public class DelStoreTransaction2 extends BaseTransaction {
	static final String TAG = "DelStoreTransaction2";

	private List<String> mPlayIds;
	private String mUrsId;
	private String mMobToken;
	private boolean mResend;

	public DelStoreTransaction2(TransactionEngine transMgr, String userid,
			List<String> playid, String mobToken) {
		super(transMgr, TRANSACTION_TYPE_DEL_STORE2);
		mUrsId = userid;
		mPlayIds = playid;
		mMobToken = mobToken;
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
					for (int i = 0; i < mPlayIds.size(); i++) {
						String id = mPlayIds.get(i);
						DBUtils.removeCollect(VopenApp.getAppInstance(),
								mUrsId, id);
					}
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
							response);
				} else if (code == -10000) {// mob token过期，重新获取mob token
					if (mResend) {// 第二次请求还是返回-10000，不再进行重试
						notifyError(
								VopenServiceCode.DEL_STORE_ERR,
								ErrorToString
										.getString(VopenServiceCode.DEL_STORE_ERR));
					} else {
						PalLog.d(TAG, "mob-token过期，重新获取");
						GetMobTokenTransation tran = new GetMobTokenTransation(
								getTransactionEngine(), mUrsId);
						tran.setListener(new TransactionListener() {
							@Override
							public void onTransactionMessage(int code,
									int arg1, int arg2, Object arg3) {
								// 获取到新的mobtoken，这执行这个Transaction
								PalLog.d(TAG, "获取到新的mob-token");
								mMobToken = (String) arg3;
								getTransactionEngine().beginTransaction(DelStoreTransaction2.this);
							}
							@Override
							public void onTransactionError(int errCode,
									int arg1, int arg2, Object arg3) {
								notifyError(
										VopenServiceCode.DEL_STORE_ERR,
										ErrorToString
												.getString(VopenServiceCode.DEL_STORE_ERR));
							}
						});
						getTransactionEngine().beginTransaction(tran);
						mResend = true;
					}
				} else if (code == -11111) {
					PalLog.d(TAG, "urs-token过期，需要重新登录");
					notifyError(VopenServiceCode.RELOGIN_NEEDED,
							ErrorToString
									.getString(VopenServiceCode.RELOGIN_NEEDED));
				} else {
					notifyError(VopenServiceCode.DEL_STORE_ERR,
							ErrorToString
									.getString(VopenServiceCode.DEL_STORE_ERR));
				}
			} else {
				notifyError(VopenServiceCode.DEL_STORE_ERR,
						ErrorToString.getString(VopenServiceCode.DEL_STORE_ERR));
			}
		} catch (JSONException e) {
			notifyError(VopenServiceCode.DEL_STORE_ERR,
					ErrorToString.getString(VopenServiceCode.DEL_STORE_ERR));
			e.printStackTrace();
		}
	}

	@Override
	public void onTransact() {
		HttpRequest request = VopenProtocol.getInstance()
				.createDelStoreRequest2(mPlayIds, mMobToken);
		if (!isCancel()) {
			sendRequest(request);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

}
