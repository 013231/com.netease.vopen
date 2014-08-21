package mblog.base;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import vopen.protocol.VopenService;

import common.framework.task.AsyncTransaction;
import common.pal.PalLog;
import common.util.NameValuePair;

public abstract class BaseTransaction extends AsyncTransaction {
	public static final int TRANSACTION_TYPE_LOGIN = 0x1000;
	public static final int TRANSACTION_TYPE_BLOGSEND = 0x1001;
	public static final int TRANSACTION_TYPE_UPLOAD = 0x1002;
	
	public static final int TRANSACTION_TYPE_FRIENDSHIP_SHOW = 0x1003;//用户关系
	public static final int TRANSCATION_TYPE_FOLLOW = 0x1004;//关注
	

	protected BaseTransaction(int type) {
		super(null, type);
		setTransactionEngine(VopenService.getInstance().getTransationEngine());
	}
	
	protected void onTransactionError(int errCode, Object obj) {
		onResponseError(errCode, (String)obj);
		PalLog.i("BaseTransaction:", "error:" + (String)obj);
	}

	protected void onTransactionSuccess(int code, Object obj) {
		onResponseSuccess((String) obj);
		PalLog.i("BaseTransaction:", "success:" + (String)obj);
	}
	
	
	

	public void onTransactException(int code, Exception e) {
		onResponseError(code, null);
	}
	
	public abstract void onResponseSuccess(String response);

	public abstract void onResponseError(int errCode, String errStr);

	

	protected void notifyMessage(int msgCode,Object arg3)
	{
		notifyMessage(msgCode, mType, getId(), arg3);
	}

	protected void notifyError(int msgCode,Object arg3)
	{
		if(arg3 == null){
			arg3 = new ErrDescrip(0, msgCode, null, null);
		}
		notifyError(msgCode, mType, getId(), arg3);
		doEnd();
	}
	/**
	 * 派发成功事务
	 */
	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		// TODO Auto-generated method stub
			onTransactionSuccess(0,response);	
	}
//	@Override
	public void onResponseError(int errCode, Object err) {
		// TODO Auto-generated method stub

		onTransactionError(errCode, err);
	}
	@Override
	public void onTransactException(Exception e) {
		// TODO Auto-generated method stub
		onTransactionError(ErrDescrip.ERR_UNKNOWN, null);
		
	}
	
	/**
	 * 派发出错事务,重写为了不移除Transaction
	 */
	@Override
	protected void notifyResponseError(int errCode, String err){
		onResponseError(errCode,err);
//		mTransMgr.endTransaction(this);
	}
	
	/**
	 * 派发数据回应事务,重写为了不移除Transaction
	 */
	@Override
	protected void notifyResponseSuccess(Object response, NameValuePair[] pairs)
	{
		if(response instanceof String)
			onResponseSuccess((String)response, pairs);
		else if(response instanceof InputStream)
			onResponseSuccess((InputStream)response, pairs);
		else
			 throw new IllegalArgumentException("not support response params");
//		mTransMgr.endTransaction(this);
	}
	/**
	 * 为了LoginTransaction自行从map里面移除
	 */
	public void doEnd() {
		if (getTransactionEngine() != null) {
			getTransactionEngine().endTransaction(this);
		}
	}
}
