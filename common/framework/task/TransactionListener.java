package common.framework.task;

public interface TransactionListener {

	/**
	 * 事务消息回调接口
	 * @param code
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void onTransactionMessage(int code,int arg1, int arg2, Object arg3);

	/**
	 * 事务消息回调接口
	 * @param errCode
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void onTransactionError(int errCode, int arg1,int arg2, Object arg3);
	
}
