package mblog.sina;

import java.util.Hashtable;

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
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;
import common.pal.PalLog;
import common.util.BaseUtil;
import common.util.Util;

/**
 * 登陆任务.
 * 		向服务端验证用户名密码，成功后服务端并返回Token.
 * sina oauth 2.0
 * @author 
 *
 */
public class LoginTransaction2 extends BaseTransaction
{
	private static final String HOST_AUTHORIZE = "/oauth2/authorize";
	private static final String GET_OAUTH2_TOKEN = "/oauth2/get_oauth2_token";
	private static final String URL_USER_SHOW = "/2/users/show.json";

	private final static int STAT_AUTHENTICAT = 1; 
	private final static int STAT_USER_SHOW =  STAT_AUTHENTICAT + 1;
	private final static int STAT_END = STAT_USER_SHOW + 1;
	
	private final static int STAT_GET_OAUTH2_TOKEN = 5;
	
	int mStat = STAT_AUTHENTICAT;
	
	String mUid;

	
	public LoginTransaction2()
	{
		super(TRANSACTION_TYPE_LOGIN);
	}
	
	protected static LoginTransaction2 createGetOauth2TokenTrans() {
		LoginTransaction2 t = new LoginTransaction2();
		t.mStat = STAT_GET_OAUTH2_TOKEN;
		return t;
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
		PalLog.i("LoninTransaction2", response);
		Context context = BaseApplication.getAppInstance();
		switch(mStat){
		case STAT_GET_OAUTH2_TOKEN:
			mStat = STAT_END;
			
			try {
				JSONObject json = new JSONObject(response);
				String accessToken = json.optString("access_token");
				
				if (! TextUtils.isEmpty(accessToken)) {
					LoginResult result = ManagerWeiboAccount.getWBAccount(context, 
							null, 
							WeiboAccountColumn.WB_TYPE_SINA);
					result.setAccessToken(accessToken);
					result.setTokenSecret(null);
//					result.setVersion(2);
					
//					ManagerWeiboAccount.addWBAccount(context, null, result);
					
					notifyMessage(0, result);
					return ;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
			notifyError(0, "获取access token失败");
			break;
		case STAT_USER_SHOW:
			mStat = STAT_END;
			LoginResult result = parseJson(response);
			
			if(result != null){
				PalLog.i("LoninTransaction2", result.getName() +"|"+result.getScreeName() +"|"+ result.getProfile());
				
				String token = SinaService.getInstance().getOauthClient().mTokenKey;
				String secret = SinaService.getInstance().getOauthClient().mTokenSecret;
				
				result.setAccessToken(token);
				result.setTokenSecret(secret);
//				result.setVersion(2);
				
//				ManagerWeiboAccount.addWBAccount(context, null, result);
				ManagerWeiboAccount.setWBAccountToken(context, result.getName(), WeiboAccountColumn.WB_TYPE_SINA, token, secret, result.getDomain(), result.getProfile(), result.getId());
				result.setLoginStep(LoginResult.LOGIN_DONE);
				notifyMessage(ErrDescrip.SUCCESS, result);
			}
			else{
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_SINA, 
						ErrDescrip.ERR_PARSE, null, null));
			}
			
			break;
		}
		
		
		
	}

	public void onTransact()
	{
		PalLog.i("LoninTransaction2", "" + mStat);
		HttpRequest mHttpRequest = null;
		switch(mStat){
		case STAT_AUTHENTICAT:
			mHttpRequest = createAuthenticate();
			break;
		case STAT_GET_OAUTH2_TOKEN:
			mHttpRequest = createGetOauth2Token();
			break;
		case STAT_USER_SHOW:
			mHttpRequest = createUserShow();
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
	
	private HttpRequest createAuthenticate(){
	
		String host = SinaService.getInstance().getRequestUrl(HOST_AUTHORIZE);
		if(Build.VERSION.SDK_INT >= 8)
			host = SinaService.getInstance().getRequestUrl(HOST_AUTHORIZE);
		else//兼容2.2以下, webview无法处理https的问题
			host = "http://api.weibo.com" + HOST_AUTHORIZE;

		StringBuffer url = new StringBuffer(host);
		url.append("?client_id=");
		url.append(SinaService.getInstance().getOauthClient().mConsumerKey);
		url.append("&redirect_uri=");
		url.append(RedirectUrl.SINA_OAUTH_CALLBACK);
		url.append("&response_type=token");
		url.append("&display=mobile");
		
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_SINA);
		Authenticate auth = result.new Authenticate(url.toString(), RedirectUrl.SINA_OAUTH_CALLBACK) {
			
			@Override
			public void onGetToken(String token, String verifier, String uid) {
				if(mUid == null) {
					if(mStat != STAT_AUTHENTICAT){
						doEnd();
					}
					else{
						mStat++;
						mUid = uid;
						SinaService.getInstance().setOauthToken(token, "null");	
						getTransactionEngine().beginTransaction(LoginTransaction2.this);
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
	
	private HttpRequest createGetOauth2Token() {
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		String host = SinaService.getInstance().getRequestUrl(GET_OAUTH2_TOKEN);
		return SinaService.getInstance().getOauthClient().request(THttpMethod.GET, 
				host, parameter);
	}

	public HttpRequest createUserShow(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_SINA);
		result.setLoginStep(LoginResult.LOGIN_GETINFO);
		notifyMessage(0, result);
		
		StringBuffer url = new StringBuffer(SinaService.getInstance().getRequestUrl(URL_USER_SHOW));
		url.append("?access_token=");
		url.append(SinaService.getInstance().getOauthClient().mTokenKey);
		url.append("&uid=");
		url.append(mUid);
		
		HttpRequest request = null;
		request = new HttpRequest(url.toString(), HttpRequest.METHOD_GET);
		
		return request;
		
	}
	
}


