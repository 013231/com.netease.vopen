package mblog.Qq;

import java.util.Hashtable;
import java.util.Vector;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.LoginResult;
import mblog.base.RedirectUrl;
import mblog.base.LoginResult.Authenticate;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.ManagerWeiboAccount;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.util.Log;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;
import common.util.BaseUtil;
import common.util.Util;
import common.util.oauth.OAuth;

/**
 * 登陆任务.
 * 		向服务端验证用户名密码，成功后服务端并返回Token.
 * 
 * @author Roy
 *
 */
public class LoginTransaction extends BaseTransaction
{
	private static final String HOST_REQUEST_TOKEN = "/cgi-bin/request_token";
	private static final String HOST_AUTHORIZE = "/cgi-bin/authorize";
	private static final String HOST_ACCESS_TOKEN = "/cgi-bin/access_token";
	private static final String URL_VERIFY = "/api/user/info";
	
	private final static int STAT_REQUEST_TOKEN = 1; 
	private final static int STAT_AUTHENTICAT = STAT_REQUEST_TOKEN + 1; 
	private final static int STAT_ACCESS_TOKEN =  STAT_AUTHENTICAT + 1;
	private final static int STAT_VERIFY =  STAT_ACCESS_TOKEN + 1;
	private final static int STAT_END = STAT_VERIFY + 1;
	
	int mStat = STAT_REQUEST_TOKEN;
	
	String mVerifier = null;

	
	public LoginTransaction()
	{
		super(TRANSACTION_TYPE_LOGIN);
//		if(BlogProtocol.getInstance().getBlogFor() == BlogProtocol.BLOG_RENREN){
//			mStat = STAT_AUTHENTICAT;
//		}
	}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		super.onTransactionSuccess(code, obj);
//		Log.i("onTransactionSuccess", "mStat is " + mStat);
		if (mStat != STAT_END) {
			getTransactionEngine().beginTransaction(this);
		}
		else {
			doEnd();
		}
	}
	

	@Override
	public void onResponseError(int errCode, String errStr) {
		ErrDescrip error = QqService.getInstance().parseError(errCode, errStr);		
		notifyError(error.errCode, error);
	}
	
	public void onResponseSuccess(String response)
	{
		Log.i("LoninTransaction", response);
		switch(mStat){
		case STAT_ACCESS_TOKEN:
		case STAT_REQUEST_TOKEN:
			mStat++;
			try
			{
				Vector token = BaseUtil.split(response, "&");
				
				String oauth_token = null, oauth_token_secret = null;
				for (int i = 0; i < token.size(); i++)
				{// 反正要编码, 干脆不解码
					String t = (String) token.elementAt(i);
					if (t.startsWith(OAuth.OAUTH_TOKEN_SECRET))
					{
						oauth_token_secret = t.substring(OAuth.OAUTH_TOKEN_SECRET
								.length() + 1);
					} else if (t.startsWith(OAuth.OAUTH_TOKEN + "="))
					{
						oauth_token = t.substring(OAuth.OAUTH_TOKEN.length() + 1);
					}
				}
				QqService.getInstance().setOauthToken(oauth_token, oauth_token_secret);				
			} catch (Exception e)
			{
				e.printStackTrace();
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_TENCENT, ErrDescrip.ERR_PARSE, null, null));
				mStat = STAT_END;
			}
			break;
			
		case STAT_VERIFY:
			mStat++;
			LoginResult result = parseJson(response);
			
			if(result != null){
				Log.i("LoninTransaction", result.getName() +"|"+result.getScreeName() +"|"+ result.getProfile());
				
				String token = QqService.getInstance().getOauthClient().mTokenKey;
				String secret = QqService.getInstance().getOauthClient().mTokenSecret;
				ManagerWeiboAccount.setWBAccountToken(BaseApplication.getAppInstance(), result.getName(), 
												WeiboAccountColumn.WB_TYPE_TENCENT, token, secret, 
												result.getDomain(), result.getProfile(), result.getId());
				result.setLoginStep(LoginResult.LOGIN_DONE);
				notifyMessage(ErrDescrip.SUCCESS, result);
			}
			else{
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_TENCENT, ErrDescrip.ERR_PARSE, null, null));
			}
			
			break;
		}
		
		
		
	}

	public void onTransact()
	{
		Log.i("LoninTransaction", "" + mStat);
		HttpRequest mHttpRequest = null;
		switch(mStat){
		case STAT_REQUEST_TOKEN:
			mHttpRequest = createRequestToken();
			break;
		case STAT_AUTHENTICAT:
			mHttpRequest = createAuthenticate();
			break;
		case STAT_ACCESS_TOKEN:
			mHttpRequest = createAccessToken(mVerifier);
			break;
		case STAT_VERIFY:
			mHttpRequest = createVerify();
			break;
		}

		if (!isCancel() && mHttpRequest != null) {
			sendRequest(mHttpRequest);
		} else if(mStat != STAT_AUTHENTICAT){
			doEnd();
		}
	}
	

	
	private LoginResult parseJson(String json){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_TENCENT);
		JSONObject userJson;
		try {
			userJson = new JSONObject(json);
			JSONObject data = userJson.optJSONObject("data");
			result.setName(BaseUtil.nullStr(data.optString("nick")));
			result.setScreeName(BaseUtil.nullStr(data.optString("name")));
			result.setDomain("http://t.qq.com/"+result.getScreeName());
			String profile_url = BaseUtil.nullStr(data.optString("head"));
			if(!Util.isStringEmpty(profile_url))//腾讯头像需要填大小
				result.setProfile(profile_url + "/50");
		} catch (JSONException e) {
			result = null;
			e.printStackTrace();
		}
		
		return result;
	}
	
	private HttpRequest createRequestToken(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_TENCENT);
		result.setLoginStep(LoginResult.LOGIN_REQUEST);
		notifyMessage(0, result);
		
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		String host = QqService.getInstance().getRequestUrl(HOST_REQUEST_TOKEN);
		parameter.put(OAuth.OAUTH_CALLBACK, RedirectUrl.OAUTH_CALLBACK);
		return QqService.getInstance().getOauthClient().request(THttpMethod.GET,host, parameter);
	}

	private HttpRequest createAuthenticate(){
		String host = QqService.getInstance().getRequestUrl(HOST_AUTHORIZE);

		StringBuffer url = new StringBuffer(host);
		url.append("?");
	
		url.append(OAuth.OAUTH_TOKEN);
		url.append("=");
		url.append(QqService.getInstance().getOauthClient().mTokenKey);

		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_TENCENT);
		Authenticate auth = result.new Authenticate(url.toString(), RedirectUrl.OAUTH_CALLBACK) {
			
			@Override
			public void onGetToken(String token, String verifier,String uid) {
//				Log.i("LoninTransaction", "createAuthenticate onGetToken mStat is " + mStat);
				if(mVerifier == null) {
					if(mStat != STAT_AUTHENTICAT){
						doEnd();
					}
					else{
						mStat++;
						mVerifier = verifier;
						getTransactionEngine().beginTransaction(LoginTransaction.this);
					}
				}
			}
			
//			@Override
//			public void onCancel() {
//				doEnd();
//			}
		};
		result.setAuthenticate(auth);
		result.setLoginStep(LoginResult.LOGIN_AUTHENTICATE);
		notifyMessage(0, result);
		
		return null;
	}
	private HttpRequest createAccessToken(String verifier){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_TENCENT);
		result.setLoginStep(LoginResult.LOGIN_ACCESS);
		notifyMessage(0, result);
		
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		if(verifier != null)
			parameter.put(OAuth.OAUTH_VERIFIER, verifier);
		
		String host = QqService.getInstance().getRequestUrl(HOST_ACCESS_TOKEN);
		return QqService.getInstance().getOauthClient().request(THttpMethod.GET, host, parameter);
	}
	
	public HttpRequest createVerify(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_TENCENT);
		result.setLoginStep(LoginResult.LOGIN_GETINFO);
		notifyMessage(0, result);
		
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		String url = QqService.getInstance().getRequestUrl(URL_VERIFY);
		parameter.put("format", "json");
		return QqService.getInstance().getOauthClient().request(THttpMethod.GET,url, parameter);
	}
	
}


