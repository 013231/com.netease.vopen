package common.framework.task;

public abstract class DataChannel {

	
	protected TransactionEngine mTransactionEngine;
	
	public DataChannel(TransactionEngine engine)
	{
		mTransactionEngine = engine;
	}
	
	/**
	 * 发送数据请求.
	 * @param obj
	 */
	public abstract void sendRequest(Object obj,Transaction t);
	
	/**
	 * 取消数据请求
	 * @param transactionId
	 */
	public abstract void cancelRequest(Transaction t);
	
	/**
	 * 关闭数据通道.
	 */
	public abstract void close();
}
