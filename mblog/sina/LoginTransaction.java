package mblog.sina;

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
	private static final String HOST_REQUEST_TOKEN = "/oauth/request_token";
	private static final String HOST_AUTHORIZE = "/oauth/authorize";
	private static final String HOST_ACCESS_TOKEN = "/oauth/access_token";
	private static final String URL_VERIFY = "/account/verify_credentials.json";
	
	
	private final static int STAT_REQUEST_TOKEN = 1; 
	private final static int STAT_AUTHENTICAT = STAT_REQUEST_TOKEN + 1; 
	private final static int STAT_ACCESS_TOKEN =  STAT_AUTHENTICAT + 1;
	private final static int STAT_VERIFY =  STAT_ACCESS_TOKEN + 1;
	private final static int STAT_END = STAT_VERIFY + 1;
	
	int mStat = STAT_REQUEST_TOKEN;
	
	String mVerifier;

	
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
		if (mStat != STAT_END) {
			getTransactionEngine().beginTransaction(this);
		}
		else {
			doEnd();
		}
	}

	@Override
	public void onResponseError(int errCode, String errStr) {
		ErrDescrip error = SinaService.getInstance().parseError(errCode, errStr);		
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
				SinaService.getInstance().setOauthToken(oauth_token, oauth_token_secret);				
			} catch (Exception e)
			{
				e.printStackTrace();
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_SINA, ErrDescrip.ERR_PARSE, null, null));
				mStat = STAT_END;
			}
			break;
			
		case STAT_VERIFY:
			mStat++;
			LoginResult result = parseJson(response);
			
			if(result != null){
				Log.i("LoninTransaction", result.getName() +"|"+result.getScreeName() +"|"+ result.getProfile());
				
				String token = SinaService.getInstance().getOauthClient().mTokenKey;
				String secret = SinaService.getInstance().getOauthClient().mTokenSecret;
				ManagerWeiboAccount.setWBAccountToken(BaseApplication.getAppInstance(), result.getName(), 
												WeiboAccountColumn.WB_TYPE_SINA, token, secret, 
												result.getDomain(), result.getProfile(), result.getId());
				result.setLoginStep(LoginResult.LOGIN_DONE);
				notifyMessage(ErrDescrip.SUCCESS, result);
			}
			else{
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_SINA, ErrDescrip.ERR_PARSE, null, null));
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
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_SINA);
		
		JSONObject userJson;
		try {
			userJson = new JSONObject(json);
			result.setId(BaseUtil.nullStr(userJson.optString("id")));
			result.setName(BaseUtil.nullStr(userJson.optString("name")));
			result.setScreeName(BaseUtil.nullStr(userJson.optString("screen_name")));
			String domain = BaseUtil.nullStr(userJson.optString("domain"));
			result.setDomain("http://weibo.com/"+domain);
			result.setProfile(BaseUtil.nullStr(userJson.optString("profile_image_url")));
			if(Util.isStringEmpty(result.getName()))
				result.setName(result.getScreeName());
		} catch (JSONException e) {
			result = null;
			e.printStackTrace();
		}
		
		return result;
	}
	
	private HttpRequest createRequestToken(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_SINA);
		result.setLoginStep(LoginResult.LOGIN_REQUEST);
		notifyMessage(0, result);
		
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		String host = SinaService.getInstance().getRequestUrl(HOST_REQUEST_TOKEN);
		parameter.put(OAuth.OAUTH_CALLBACK, RedirectUrl.OAUTH_CALLBACK);
		return SinaService.getInstance().getOauthClient().request(THttpMethod.GET,host, parameter);
	}

	private HttpRequest createAuthenticate(){
		String host = SinaService.getInstance().getRequestUrl(HOST_AUTHORIZE);

		StringBuffer url = new StringBuffer(host);
		url.append("?");
	
		url.append(OAuth.OAUTH_TOKEN);
		url.append("=");
		url.append(SinaService.getInstance().getOauthClient().mTokenKey);

		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_SINA);
		Authenticate auth = result.new Authenticate(url.toString(), RedirectUrl.OAUTH_CALLBACK) {
			
			@Override
			public void onGetToken(String token, String verifier,String uid) {
				if(mStat != STAT_AUTHENTICAT){
					doEnd();
				}
				else{
					mStat++;
					mVerifier = verifier;
					getTransactionEngine().beginTransaction(LoginTransaction.this);
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
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_SINA);
		result.setLoginStep(LoginResult.LOGIN_ACCESS);
		notifyMessage(0, result);
		
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		if(verifier != null)
			parameter.put(OAuth.OAUTH_VERIFIER, verifier);
		
		String host = SinaService.getInstance().getRequestUrl(HOST_ACCESS_TOKEN);
		return SinaService.getInstance().getOauthClient().request(THttpMethod.GET, host, parameter);
	}
	
	public HttpRequest createVerify(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_SINA);
		result.setLoginStep(LoginResult.LOGIN_GETINFO);
		notifyMessage(0, result);
		
		String url = SinaService.getInstance().getRequestUrl(URL_VERIFY);
		return SinaService.getInstance().getOauthClient().request(THttpMethod.GET,url, null);
	}
	
}


