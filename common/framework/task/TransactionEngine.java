package common.framework.task;

import java.util.Hashtable;

import common.pal.PalLog;
import common.util.LinkedBlockingQueue;
import common.util.NameValuePair;

public class TransactionEngine implements Runnable
{
	// do transaction thread
	Thread mThread;
	// engine stop flag
	boolean mStop;
	// transaction map
	// Hashtable<Short(TransactionID),Transaction>
	Hashtable mTransactionMap;

	// LinkedBlockingQueue<Transaction>
	LinkedBlockingQueue mTransactionQueue;

	Transaction mRunTask;
	
	boolean 	mTaskRuning;
	
	DataChannel mDataChannel;
	

	public TransactionEngine()
	{
		mTransactionQueue = new LinkedBlockingQueue();
		mTransactionMap = new Hashtable();
	}
	
	
	public void setDataChannel(DataChannel dataChannel)
	{
		mDataChannel = dataChannel;
	}
	
	public void shutdown()
	{
		if (mThread != null)
		{
			mStop = true;
			mThread = null;
			mTransactionQueue.interrupt();
			if(mTaskRuning  && mRunTask!= null){
				mRunTask.doCancel();
				mRunTask = null;
			}
		}
	}
	
	/**
	 * 进入执行队列
	 * @param tx
	 */
	public void beginTransaction(Transaction tx)
	{
		if (tx != null)
		{	
			tx.setTransactionEngine(this);
			synchronized (mTransactionMap)
			{
				if(tx instanceof AsyncTransaction){
					((AsyncTransaction) tx).setDataChannel(mDataChannel);
				}
				Integer key = new Integer(tx.getId());
				mTransactionMap.put(key, tx);
				mTransactionQueue.put(tx);
				if (mThread == null)
				{
					start();
				}
			}
		}
	}

	/**
	 * 结束执行
	 * @param tx
	 */
	public void endTransaction(Transaction tx)
	{
		synchronized (mTransactionMap)
		{
			Integer key = new Integer(tx.getId());
			if (mTransactionMap.containsKey(key))
			{
				Transaction trans = (Transaction) mTransactionMap.remove(key);
				mTransactionQueue.remove(trans);
			}
		}
	}
	
	/**
	 * 添加一个等待事务
	 * @param tx
	 */
	public void addWaitTransaction(Transaction tx) {
		if (tx != null) {
			tx.setTransactionEngine(this);
			
			synchronized (mTransactionMap)
			{
				if(tx instanceof AsyncTransaction){
					((AsyncTransaction) tx).setDataChannel(mDataChannel);
				}
				
				Integer key = new Integer(tx.getId());
				mTransactionMap.put(key, tx);
			}
		}
	}
	
	/**
	 * 取消事务执行
	 * 
	 * @param tId
	 */
	public void cancelTransaction(int tId)
	{
		synchronized (mTransactionMap)
		{
			Integer key = new Integer(tId);
			if (mTransactionMap.containsKey(key))
			{
				//TODO: 如果事务正在执行或等待数据,应保留在mTransactionMap.
				//TODO: 如果事务未被执行或还在执行等待队列中，则可从mTransactionMap中移除.
				Transaction trans = (Transaction)mTransactionMap.remove(key);
				trans.doCancel();
				mTransactionQueue.remove(trans);
				
			}
		}
	}
	
	
	public void notifyIncomingPrimitive(Integer tId, Object response, NameValuePair[] pairs)
	{
		AsyncTransaction tx  = null;
		synchronized (mTransactionMap) {
			if(mTransactionMap.containsKey(tId)){
				tx = (AsyncTransaction)mTransactionMap.get(tId);
			}
		}
		if(tx != null){
			tx.notifyResponseSuccess(response,pairs);
		}
	}
	
	public void notifyError(Integer tId,int errCode, String errStr)
	{
		
		AsyncTransaction tx  = null;
		synchronized (mTransactionMap) {
			if(mTransactionMap.containsKey(tId)){
				tx = (AsyncTransaction)mTransactionMap.get(tId);
			}	
		}
		if(tx != null){
			tx.notifyResponseError(errCode, errStr);
		}
	}

	public void reassignTransactionId(int transactionId)
	{
		synchronized (mTransactionMap)
		{
			Integer key = new Integer(transactionId);
			if (mTransactionMap.containsKey(key))
			{
				Transaction tx = (Transaction) mTransactionMap.get(key);
				mTransactionQueue.put(tx);
			}
		}
	}

	
	
	public void run()
	{
		// TODO Auto-generated method stub
	
		while (!mStop)
		{
			Object obj = mTransactionQueue.take();
			if (obj != null)
			{
				Transaction task = (Transaction) obj;
				mRunTask = task;
				mTaskRuning = true;
				try
				{
					//task.onTransact();
					task.run();
				} catch (Exception e)
				{
					e.printStackTrace();
					PalLog.d("[Error]TransactionEngine Thread", e.toString());
					task.doCancel();
					task.onTransactException(e);
					endTransaction(task);
				}
				mTaskRuning = false;
				mRunTask = null;
				obj = null;
			}
			
		}
		
		mThread = null;
	}

	
	private void start()
	{
		if (mThread == null)
		{
			mThread = new Thread(this);
			mStop = false;
			mThread.start();
		}
	}



	// <Short, Transaction>
	public Hashtable getPaddingTransactionMap() {
		return mTransactionMap;
	}
	
}
