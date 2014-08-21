package common.framework.task;

import common.util.NameValuePair;


public class NotifyTransaction extends Transaction
{
	
	// 需要派发的Transaction id
	Integer mTID;
	
	Object mData;
	NameValuePair[] mPairs;
	
	public NotifyTransaction(TransactionEngine transMgr,
			Integer tid,  Object data, NameValuePair[] pairs)
	{
		super(transMgr, -1);
		mTID = tid;
		mData = data;
		mPairs = pairs;
		// TODO Auto-generated constructor stub
	}

	public void onTransact()
	{
		// TODO Auto-generated method stub
		TransactionEngine engine = getTransactionEngine();
		engine.notifyIncomingPrimitive(mTID, mData,mPairs);
	}

	public void onTransactException(Exception e) {
		// TODO Auto-generated method stub
		
	}

}
