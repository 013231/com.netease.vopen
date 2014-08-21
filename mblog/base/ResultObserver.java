package mblog.base;

import java.util.LinkedList;
import java.util.List;


public class ResultObserver {
	List<Observer> mListener = new LinkedList<Observer>();
	List<Observer> mCbListener = new LinkedList<Observer>();

	private static ResultObserver s_inst;

	public static ResultObserver getInstance() {
		if (s_inst == null)
			s_inst = new ResultObserver();

		return s_inst;
	}
	
	synchronized private void beginCallBack(){
		mCbListener.clear();
		
		synchronized (mListener) {
			if(mListener.size() == 0)
				return;
			
			for (Observer listener : mListener)
				mCbListener.add(listener);
		}
	}
	
	synchronized private void endCallBack(){
		if(mCbListener != null)
			mCbListener.clear();
	}
	
	public void registerOberver(Observer listener) {
		synchronized (mListener) {
			if (!mListener.contains(listener))
				mListener.add(listener);
		}
	}

	public void unRegisterOberver(Observer listener) {
		synchronized (mListener) {
			mListener.remove(listener);
		}
	}
	
	void onError(int transactionId, int type, ErrDescrip obj){
		beginCallBack();
		for (Observer listener : mCbListener){
			switch(type){
			case BaseTransaction.TRANSACTION_TYPE_LOGIN:
				listener.onLoginError(transactionId, type, obj);
				break;
			case BaseTransaction.TRANSACTION_TYPE_BLOGSEND:
				listener.onBlogSendError(transactionId, type, obj);
				break;
			case BaseTransaction.TRANSACTION_TYPE_UPLOAD:
				listener.onBlogUploadError(transactionId, type, obj);
				break;
			case BaseTransaction.TRANSCATION_TYPE_FOLLOW:
                listener.onBlogFollowError(transactionId, type, obj);
                break;
            case BaseTransaction.TRANSACTION_TYPE_FRIENDSHIP_SHOW:
                listener.onBlogShowFriendshipError(transactionId, type, obj);
                break;
			}
		}
		endCallBack();
	}
	
	void onSuccess(int transactionId, int type, Object obj){
		beginCallBack();
		for (Observer listener : mCbListener){
			switch(type){
			case BaseTransaction.TRANSACTION_TYPE_LOGIN:
				listener.onLogin(transactionId, type, (LoginResult)obj);
				break;
			case BaseTransaction.TRANSACTION_TYPE_BLOGSEND:
				listener.onBlogSend(transactionId, type, (SendBlogResult)obj);
				break;
			case BaseTransaction.TRANSACTION_TYPE_UPLOAD:
				listener.onBlogUpload(transactionId, type, (SendBlogResult)obj);
				break;
			case BaseTransaction.TRANSCATION_TYPE_FOLLOW:
			    listener.onBlogFollow(transactionId, type);
			    break;
			case BaseTransaction.TRANSACTION_TYPE_FRIENDSHIP_SHOW:
			    listener.onBlogShowFriendship(transactionId, type, (FriendshipResult)obj);
			    break;
			}
		}
		endCallBack();
	}
	
	public interface Observer{
//		public static final int TRANSACTION_TYPE_LOGIN = 0x1000;
		void onLogin(int transactionId, int type, LoginResult result);
		void onLoginError(int transactionId, int type, ErrDescrip obj);
//		public static final int TRANSACTION_TYPE_BLOGSEND = 0x1001;
		void onBlogSend(int transactionId, int type, SendBlogResult result);
		void onBlogSendError(int transactionId, int type, ErrDescrip obj);
//		public static final int TRANSACTION_TYPE_UPLOAD = 0x1002;
		void onBlogUpload(int transactionId, int type, SendBlogResult result);
		void onBlogUploadError(int transactionId, int type, ErrDescrip obj);
		void onBlogFollow(int transactionId, int type);
		void onBlogFollowError(int transactionId, int type, ErrDescrip obj);
		void onBlogShowFriendship(int transactionId, int type, FriendshipResult obj);
		void onBlogShowFriendshipError(int transactionId, int type, ErrDescrip obj);
	}
}
