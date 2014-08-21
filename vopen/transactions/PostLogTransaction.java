package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

/**
 * 注销任务.
 * 		
 * 
 * @author lxd
 *
 */
public class PostLogTransaction extends BaseTransaction
{
	public PostLogTransaction(TransactionEngine transMgr)
	{
		super(transMgr, TRANSACTION_TYPE_POSTLOG);
		
	}
	
	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		// TODO Auto-generated method stub
		PalLog.i("PostLogTransaction", response);
		JSONObject object = null;
		boolean isSuccess = false;
		String fileId = null;
		try {
			object = (JSONObject) new JSONTokener(response).nextValue();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			isSuccess = object.getBoolean("success");
			PalLog.i("isSuccess=", isSuccess?"成功":"失败");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(isSuccess == false ){
			notifyError(VopenServiceCode.POST_LOG_ERR, null);
		}
		else{
			try {
				fileId = object.getString("fileId");
				PalLog.i("fileId=", fileId);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,fileId);
		}
		
		
	}
	/**
	 * onTransact
	 */
	public void onTransact()
	{
		HttpRequest mHttpRequest = VopenProtocol.getInstance().createPostLogReqeust();

		if (!isCancel()) {
			sendRequest(mHttpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

	
}