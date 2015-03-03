package vopen.transactions;

import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import android.content.Context;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

/**
 * 设置里面的意见反馈
 */
public class FeedBackTransactionNew extends BaseTransaction
{
	String mUser;
	String mTitle;
	String mContent;
	String mFileId;
	String mContact;
	Context mContext;
	
	public FeedBackTransactionNew(TransactionEngine transMgr,String user,String title,String content,String fileId,String contact,Context context)
	{
		super(transMgr, TRANSACTION_TYPE_FEEDBACK);
		mUser = user;
		mTitle = title;
		mContent = content;
		mFileId = fileId;
		mContact = contact;	
		mContext = context;
	}
	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		// TODO Auto-generated method stub
		PalLog.i("FeedBackTransaction", response);
		
		if(response.equalsIgnoreCase("true")){
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS,null);
			PalLog.i("FeedBack response", "成功");
		}
		else{
			notifyError(VopenServiceCode.FEEDBACK_ERR, null);
			PalLog.i("FeedBack response", "失败");
		}
	}

	/**
	 * onTransact
	 */
	public void onTransact()
	{
		HttpRequest mHttpRequest = VopenProtocol.getInstance().createFeedBackReqeust(mUser,mTitle,mContent,mFileId,mContact,mContext);

		if (!isCancel()) {
			sendRequest(mHttpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}


}