package common.framework.task;


public abstract class Transaction
{
	
	/**
	 * 事务类型，建议（24主类型 + 子类型）
	 */
	protected int mType;
	
	/**
	 * Transaction 唯一标识.
	 */
	private int mId;
	
	/**
	 * 是否被取消标记
	 */
	private boolean isCancel;
	
	/**
	 * 事务管理器
	 */
	TransactionEngine mTransMgr;
	
	/**
	 * 事务监听器
	 */
	TransactionListener mListener;
	
	static short mTransactionId = 0;
	
	protected Transaction(TransactionEngine transMgr, int type) {
		mType = type;
    	mId = getNextTransactionId();
    	mTransMgr = transMgr;
	}
	
	public void setTransactionEngine(TransactionEngine transMgr) 
	{
		mTransMgr = transMgr;
	}
	
	public TransactionEngine getTransactionEngine()
	{		
		return mTransMgr;
	}
	

	public TransactionListener getListener() {
		return mListener;
	}

	public void setListener(TransactionListener listener) {
		mListener = listener;
	}


	/**
     * Gets the id of this transaction.
     *
     * @return the id of this transaction.
     */
    public int getId() 
    {
        return mId;
    }
    
//    /**
//     * Get the main type of this transaction
//     * @return
//     */
//    public int getMainType() {
//    	return (mType >> 8) << 8;
//    }
//    
//    /**
//     * Get the sub type of this transaction
//     * @return
//     */
//    public int getSubType() {
//    	return (mType & 0xFF);
//    }
//    
    /**
     * Get the type of this transaction
     * @return
     */
    public int getType() {
    	return mType;
    }
    
	
    /**
     * 是否取消
     * @return 如果取消返回true,否则返回false
     */
    public boolean isCancel() {
    	
    	return isCancel;
    }
    
    /**
     * 取消事务
     */
    public void doCancel() {
    	isCancel = true;
    }
    
    
    public void run(){
    	onTransact();
    	mTransMgr.endTransaction(this);
    }
    
    /**
     * Transaction 任务执行入口
     */
    public abstract void onTransact();
    
    /**
     * Transaction 执行异常.
     * @param e
     */
    public abstract void onTransactException(Exception e);
    
    
    protected void notifyMessage(int msgCode,int arg1,int arg2,Object arg3) 
    {
    	if (mListener != null) {
    		mListener.onTransactionMessage(msgCode,arg1, arg2, arg3);
    	}
    }
    
    public void notifyError(int errCode,int arg1,int arg2,Object arg3) {
    	if (mListener != null) {
    		mListener.onTransactionError(errCode,arg1, arg2, arg3);
    	}
    }
    
    /**
     *  
     * @return
     */
    private synchronized static short getNextTransactionId(){
    	if (mTransactionId >= 999)
		{
    		mTransactionId = 0;
		}
		return mTransactionId++;
    }
}
