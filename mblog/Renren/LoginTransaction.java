package mblog.Renren;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.LoginResult;
import mblog.base.RedirectUrl;
import mblog.base.LoginResult.Authenticate;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.ManagerWeiboAccount;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.os.Bundle;
import android.util.Log;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;
import common.util.BaseUtil;
import common.util.oauth.OAuth;
import common.util.oauth.OAuthNameValuePair;

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
//	private static final String URL_VERIFY = "/account/verify_credentials.json";
	
	
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
		mStat = STAT_AUTHENTICAT;
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
		ErrDescrip error = RenrenService.getInstance().parseError(errStr);		
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
				RenrenService.getInstance().setOauthToken(oauth_token, oauth_token_secret);				
			} catch (Exception e)
			{
				e.printStackTrace();
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_RENREN, ErrDescrip.ERR_PARSE, null, null));
				mStat = STAT_END;
			}
			break;
			
		case STAT_VERIFY:
			mStat++;
			LoginResult result = parseJson(response);
			
			if(result != null){
				Log.i("LoninTransaction", result.getName() +"|"+result.getScreeName() +"|"+ result.getProfile());
				
				String token = RenrenService.getInstance().getOauthClient().mTokenKey;
				String secret = RenrenService.getInstance().getOauthClient().mTokenSecret;
				ManagerWeiboAccount.setWBAccountToken(BaseApplication.getAppInstance(), result.getName(), 
												WeiboAccountColumn.WB_TYPE_RENREN, token, secret, 
												result.getDomain(), result.getProfile(), result.getId());
				result.setLoginStep(LoginResult.LOGIN_DONE);
				notifyMessage(ErrDescrip.SUCCESS, result);
			}
			else{
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_RENREN, ErrDescrip.ERR_PARSE, null, null));
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
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_RENREN);
		
		Log.i("renren", json);
		JSONObject userJson;
		try {
			JSONArray jsonArray = new JSONArray(json);
			userJson = jsonArray.optJSONObject(0);
			if(userJson == null){
				throw new JSONException("parse renren error");
			}
			result.setName(BaseUtil.nullStr(userJson.optString("name")));
			result.setScreeName(BaseUtil.nullStr(userJson.optString("uid")));
			result.setDomain("http://www.renren.com/profile.do?id=" + result.getScreeName());
			result.setProfile(BaseUtil.nullStr(userJson.optString("tinyurl")));
		} catch (JSONException e) {
			result = null;
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	private HttpRequest createRequestToken(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_RENREN);
		result.setLoginStep(LoginResult.LOGIN_REQUEST);
		notifyMessage(0, result);
		
		String host = RenrenService.getInstance().getRequestUrl(HOST_REQUEST_TOKEN);
		return RenrenService.getInstance().getOauthClient().request(THttpMethod.GET,host, null);
	}

	private HttpRequest createAuthenticate(){
		String host = "https://graph.renren.com" + HOST_AUTHORIZE;

		StringBuffer url = new StringBuffer(host);
		url.append("?");
		Bundle params = new Bundle();
        params.putString("client_id", RenrenService.RENREN_APP_KEY);
        params.putString("redirect_uri", RedirectUrl.RENREN_OAUTH_CALLBACK);
        params.putString("response_type", "token");//"code"
        params.putString("display", "touch");
        params.putString("scope","publish_feed");
	        
	    url.append(RenrenService.encodeUrl(params));
	        
	    LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_RENREN);
		Authenticate auth = result.new Authenticate(url.toString(), RedirectUrl.RENREN_OAUTH_CALLBACK) {
			
			@Override
			public void onGetToken(String token, String verifier,String uid) {
				if(mVerifier == null) {
					if(mStat != STAT_AUTHENTICAT){
						doEnd();
					}
					else{
						mStat = STAT_VERIFY;
						RenrenService.getInstance().setOauthToken(token, "null");
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
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_RENREN);
		result.setLoginStep(LoginResult.LOGIN_ACCESS);
		notifyMessage(0, result);
		
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		if(verifier != null)
			parameter.put(OAuth.OAUTH_VERIFIER, verifier);
		
		String host = RenrenService.getInstance().getRequestUrl(HOST_ACCESS_TOKEN);
		return RenrenService.getInstance().getOauthClient().request(THttpMethod.GET, host, parameter);
	}
	
	public HttpRequest createVerify(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_RENREN);
		result.setLoginStep(LoginResult.LOGIN_GETINFO);
		notifyMessage(0, result);
		
		String url = RenrenService.getInstance().getRequestUrl("");
		List<OAuthNameValuePair> list = new ArrayList<OAuthNameValuePair>();
		
        RenrenService.getInstance().prepareParams(list, "users.getInfo");
        HttpRequest request = null;
		request = new HttpRequest(url, HttpRequest.METHOD_POST);
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(list);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		request.setHttpEntity(entity);
		
		return request;
	}
	
}


