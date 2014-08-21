package common.framework.task;


public abstract class MultiPhaseTransaction extends AsyncTransaction
{

	public static final short TRANSACTION_COMPLETED = 0;
	
	public static final short TRANSACTION_CONTINUE = 1;
	
	protected MultiPhaseTransaction(TransactionEngine transMgr, int type)
	{
		super(transMgr, type);
	} 
	
	/**
	 * 派发出错事务
	 */
	protected void notifyResponseError(int errCode,Object err){
		short state = onPhaseResponseError(errCode,err);
		if(state == TRANSACTION_COMPLETED){
			mTransMgr.endTransaction(this);	
		}
	}
	
	/**
	 * 派发数据回应事务
	 */
	protected void notifyResponseSuccess(byte[] response)
	{
		short state = onPhaseResponseSuccess(response);
		if(state == TRANSACTION_COMPLETED){
			mTransMgr.endTransaction(this);
		}
		else {
			mTransMgr.reassignTransactionId(getId());
		}
	}
	
	public abstract short onPhaseResponseError(int errCode ,Object error);
	
	
	public abstract short onPhaseResponseSuccess(byte[] response);
	
	
	
	/**
	 * 
	 * @param error
	 * @return  
	 */
	public final void onResponseError(int err, Object error){
		
	}


	/**
	 * 
	 * @param data
	 * @return
	 */
	public final void onResponseSuccess(byte[] data){
		
	}
	
	
	
	
	
}
