package vopen.protocol;

import java.util.LinkedList;
import java.util.List;

import com.netease.vopen.pal.ErrorToString;
import common.framework.task.TransactionListener;
import common.pal.PalLog;
import common.util.Util;

public class GroupTransactionListener implements TransactionListener {
	List<VopenCallBack> mListener;
	List<VopenCallBack> mCbListener;
	
	synchronized private void beginCallBack(){
		if(mCbListener == null){
			mCbListener = new LinkedList<VopenCallBack>();
		}
		mCbListener.clear();
		
		if(mListener == null || mListener.size() == 0)
			return;
		
		for(VopenCallBack listener : mListener)
			mCbListener.add(listener);
	}
	private void endCallBack(){
		if(mCbListener != null)
			mCbListener.clear();
	}
	
	@Override
	synchronized public void onTransactionError(int errCode,int arg1, int arg2, Object arg3) {
		PalLog.i("onTransactionError errCode", "errCode = " + errCode);
		String errStr = null;
		
		if(arg3 instanceof String) {
			errStr = (String) arg3;
		}
		
		if(Util.isStringEmpty(errStr)){
			PalLog.log("get errStr");
			errStr = ErrorToString.getString(errCode);
		}
			
		
		beginCallBack();
		for(VopenCallBack listener : mCbListener) {
			listener.callError(arg1, arg2, errCode, errStr);
		}
		endCallBack();
	}

	@Override
	synchronized public void onTransactionMessage(int code, int arg1,int arg2, Object arg3) {
		
		if(VopenServiceCode.TRANSACTION_SUCCESS != code)
			return;
		
//		PalLog.i("TIME-TEST", "callSuccess" + System.currentTimeMillis());
		beginCallBack();
		for(VopenCallBack listener : mCbListener) {
			listener.callSuccess(arg1, arg2, arg3);
		}
		endCallBack();
	}
	
	/**
	 * 
	 * @param type
	 * @param listener
	 */
	synchronized public void addListener(VopenCallBack listener){
		if(mListener == null){
			mListener = new LinkedList<VopenCallBack>();
		}
		
		if(!mListener.contains(listener))
			mListener.add(listener);
	}
	
	synchronized public void removeListener(VopenCallBack listener){
		if(mListener != null){
    		mListener.remove(listener);
		}
	}
}
