package mblog.base;


import vopen.protocol.VopenService;


import common.framework.task.AsyncTransaction;
import common.framework.task.TransactionListener;
import common.util.BaseUtil;
import common.util.oauth.OAuthClient;

public abstract class BaseService {
	OAuthClient mOauthClient;
	
	public BaseService(String consumerKey, String consumerSecret) {
		resetOauthClient(consumerKey, consumerSecret);
	}
	
	public void setOauthToken(String tokenKey,String tokenSecret){
		mOauthClient.setToken(tokenKey, tokenSecret);
		mOauthClient.setHMACSHA1Key(BaseUtil.toString(mOauthClient.mConsumerSecret) + '&'
				+ BaseUtil.toString(mOauthClient.mTokenSecret));
	}
	
	public void resetOauthClient(String consumerKey, String consumerSecret){
		mOauthClient = new OAuthClient();
		mOauthClient.setConsumer(consumerKey, consumerSecret);
		mOauthClient.setHMACSHA1Key(BaseUtil.toString(mOauthClient.mConsumerSecret) + '&'
				+ BaseUtil.toString(mOauthClient.mTokenSecret));
	}
	/**
	 * 注册事务监听器
	 * @param t
	 * @param listener
	 * @return
	 */
	protected int startTransaction(AsyncTransaction t, TransactionListener listener) {
		t.setListener(listener);
		return VopenService.getInstance().beginTransaction(t);
	}

	public void cancelTransactionsByTID(int tid) {
		VopenService.getInstance().doCancelTransactionByID(tid);
	}
	
	public OAuthClient getOauthClient(){
		return mOauthClient;
	}
	
	public abstract String getRequestUrl(String api);
}
