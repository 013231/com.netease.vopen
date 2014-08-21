package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CheckVersionInfo;
import android.content.Context;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

public class GetVersionInfoTransaction extends BaseTransaction {
    Context mContext;
    String mVersion;

    public GetVersionInfoTransaction(TransactionEngine transMgr, Context context, String version) {
        super(transMgr, TRANSACTION_TYPE_GET_VERSION_INFO);
        // TODO Auto-generated constructor stub
        mContext = context;
        mVersion = version;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
    	 PalLog.e("GetVersionInfoTransaction", response);
		CheckVersionInfo versionInfo = null;
		try {
			JSONObject jso = new JSONObject(response);
			versionInfo = new CheckVersionInfo(jso);
			// 开始检查更新
			if (null != versionInfo) {

				long nv = 0l;
				long ov = 0l;

				nv = Long.parseLong(versionInfo.version.replace(".", ""));
				ov = Long.parseLong(mVersion.replace(".", ""));

				if (nv > ov) {
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,
							versionInfo);
				} else {
					notifyError(VopenServiceCode.DONOT_NEED_UPDATE, null);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
        HttpRequest httpRequest = VopenProtocol.getInstance().createGetVersionInfoRequest();

        if (!isCancel()) {
            sendRequest(httpRequest);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

}
