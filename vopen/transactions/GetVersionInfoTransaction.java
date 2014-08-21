package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CheckVersionInfo;
import android.content.Context;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class GetVersionInfoTransaction extends BaseTransaction {
    Context mContext;
    String mVersion;//当前版本

    public GetVersionInfoTransaction(TransactionEngine transMgr, Context context, String version) {
        super(transMgr, TRANSACTION_TYPE_GET_VERSION_INFO);
        mContext = context;
        mVersion = version;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
		CheckVersionInfo versionInfo = null;
		try {
			JSONObject jso = new JSONObject(response);
			versionInfo = new CheckVersionInfo(jso);
			// 开始检查更新
			if (null != versionInfo) {
				int result = versionInfo.version.compareTo(mVersion);
				if (result > 0) {
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
							versionInfo);
				} else {
					notifyError(VopenServiceCode.DONOT_NEED_UPDATE, null);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			notifyError(VopenServiceCode.ERR_DATA_PARSE, null);
		}
	}

    @Override
    public void onTransact() {
        HttpRequest httpRequest = VopenProtocol.getInstance().createGetVersionInfoRequest();

        if (!isCancel()) {
            sendRequest(httpRequest);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

}
