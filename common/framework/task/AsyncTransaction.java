package common.framework.task;

import java.io.InputStream;
import java.util.zip.DataFormatException;

import common.util.NameValuePair;



/**
 * 	异步请求任务.
 * 	数据通过DataChannel发送数据.
 * 	异步调用onResponseError,onResponseSuccess返回请求结果.
 *
 * @author Roy.Wong
 *
 */
public abstract class AsyncTransaction extends Transaction
{

	
	
	DataChannel mDataChannel;
	
	protected AsyncTransaction(TransactionEngine transMgr, int type)
	{
		super(transMgr, type);
	}
	
	/**
	 * 重新执行Transaction任务
	 */
	protected void reassignTransaction()
	{
		mTransMgr.reassignTransactionId(getId());
	}
	
	/**
	 * 为Transaction设置数据通道.
	 * @param dataChannel
	 */
	public void setDataChannel(DataChannel dataChannel) 
	{
		mDataChannel = dataChannel;
	}
	
	/**
	 * 发送数据
	 * @param obj
	 */
	protected void sendRequest(Object obj){
		if(mDataChannel != null)
		{
			mDataChannel.sendRequest(obj,this);
		}
	}
	
	
	public void run(){
	  	onTransact();
	}
	
	/**
	 * 派发出错事务
	 */
	protected void notifyResponseError(int errCode, String err){
		onResponseError(errCode,err);
		mTransMgr.endTransaction(this);
	}
	
	/**
	 * 派发数据回应事务
	 * @throws DataFormatException 
	 */
	protected void notifyResponseSuccess(Object response, NameValuePair[] pairs)
	{
		if(response instanceof String)
			onResponseSuccess((String)response, pairs);
		else if(response instanceof InputStream)
			onResponseSuccess((InputStream)response, pairs);
		else
			 throw new IllegalArgumentException("not support response params");
		
		mTransMgr.endTransaction(this);
	}
	
	public void doCancel() {
		super.doCancel();
		if (mDataChannel != null) 
		{
			mDataChannel.cancelRequest(this);
		}
	}
	
	/**
	 * 
	 * @param error
	 * @return  
	 */
	public abstract void onResponseError(int errCode,Object err);


	/**
	 * 
	 * @param data
	 * @return
	 */
	public abstract void onResponseSuccess(String response, NameValuePair[] pairs);
	
	public void onResponseSuccess(InputStream in, NameValuePair[] pairs){
		
	}
}
