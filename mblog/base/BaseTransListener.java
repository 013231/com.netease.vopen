package mblog.base;

import android.os.Handler;
import android.os.Message;

import common.framework.task.TransactionListener;


public abstract class BaseTransListener implements TransactionListener {
	Handler mHandler = new InternalHandler();

	private static final int TRANSACTION_ERROR = 0x1;
	private static final int TRANSACTION_SUCCESS = 0x2;
	private class InternalHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case TRANSACTION_ERROR:
				onError(msg.arg1, msg.arg2, (ErrDescrip)msg.obj);
				ResultObserver.getInstance().onError(msg.arg1, msg.arg2, (ErrDescrip)msg.obj);
				break;
				
			case TRANSACTION_SUCCESS:
				onSuccess(msg.arg1, msg.arg2, msg.obj);
				ResultObserver.getInstance().onSuccess(msg.arg1, msg.arg2, msg.obj);
				break;

			}
			super.handleMessage(msg);
		}
	}
	@Override
	public void onTransactionMessage(int code, int type, int tid, Object arg3) {
		mHandler.obtainMessage(TRANSACTION_SUCCESS, tid, type, arg3).sendToTarget();
	}

	@Override
	public void onTransactionError(int errCode, int type, int tid, Object arg3) {
		mHandler.obtainMessage(TRANSACTION_ERROR, tid, type, arg3).sendToTarget();
	}

	abstract public void onError(int transactionId, int type, ErrDescrip obj);
	abstract public void onSuccess(int transactionId, int type, Object obj);
}
