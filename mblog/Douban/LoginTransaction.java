package mblog.Douban;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.LoginResult;
import mblog.base.LoginResult.Authenticate;
import mblog.base.RedirectUrl;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import vopen.app.BaseApplication;
import vopen.db.ManagerWeiboAccount;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.util.Log;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;
import common.util.BaseUtil;
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
	//douban oauth
	private static final String HOST_REQUEST_TOKEN_DOUBAN = "/service/auth/request_token";
	private static final String HOST_AUTHORIZE_DOUBAN = "/service/auth/authorize";
	private static final String HOST_ACCESS_TOKEN_DOUBAN = "/service/auth/access_token";
	private static final String HOST_VERIFY_DOUBAN = "http://api.douban.com/people/%40me";
	
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
	}
	
//	@Override
//	protected void notifyResponseSuccess(byte[] response,String charset)
//	{
//		super.onResponseSuccess(response,charset);
//		mTransMgr.endTransaction(this);
//	}
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
		ErrDescrip error = DoubanService.getInstance().parseError(errCode, errStr);		
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
				DoubanService.getInstance().setOauthToken(oauth_token, oauth_token_secret);				
			} catch (Exception e)
			{
				e.printStackTrace();
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_DOUBAN,ErrDescrip.ERR_PARSE, null, null));
				mStat = STAT_END;
			}
			break;
			
		case STAT_VERIFY:
			mStat++;
			LoginResult result = parsDoubanXml(response);
			
			if(result != null){
				Log.i("LoninTransaction", result.getName() +"|"+result.getScreeName() +"|"+ result.getProfile());
				
				String token = DoubanService.getInstance().getOauthClient().mTokenKey;
				String secret = DoubanService.getInstance().getOauthClient().mTokenSecret;
				ManagerWeiboAccount.setWBAccountToken(BaseApplication.getAppInstance(), result.getName(), 
												WeiboAccountColumn.WB_TYPE_DOUBAN, token, secret, 
												result.getDomain(), result.getProfile(), result.getId());
				result.setLoginStep(LoginResult.LOGIN_DONE);
				notifyMessage(ErrDescrip.SUCCESS, result);
			}
			else{
				notifyError(ErrDescrip.ERR_PARSE, new ErrDescrip(WeiboAccountColumn.WB_TYPE_DOUBAN,ErrDescrip.ERR_PARSE, null, null));
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
	

	private LoginResult parsDoubanXml(String xml) {
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_DOUBAN);
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(xml));
			int eventType = parser.getEventType();

			// 当没有解析的文档的末尾的时候，一直执行
			loop: while (eventType != XmlPullParser.END_DOCUMENT) {

				// switch 解析的位置
				switch (eventType) {

				// 开始解析文档的时候，初始化对象集合
				case XmlPullParser.START_DOCUMENT:
					break;

				// 开始解析标签的时候，根据标签的不同名称。做不同操作
				case XmlPullParser.START_TAG:
					if ("title".equals(parser.getName())) {
						result.setName(parser.nextText());
					}
					else if ("link".equals(parser.getName())) {
						String key = parser.getAttributeValue(null, "rel");
						String value = parser.getAttributeValue(null, "href");
						if(key != null){
							if(key.equals("alternate")){
								result.setDomain(value);
							}
							else if(key.equals("icon")){
								result.setProfile(value);
							}
						}
					}
					break;

				// 当解析到标签结束的时候执行
				case XmlPullParser.END_TAG:
					break;
				}

				// 当前解析位置结束，指向下一个位置
				eventType = parser.next();
			}

		} catch (Exception e) {
			result = null;
			e.printStackTrace();
		}
		
		if(result != null){
			result.setScreeName(result.getName());
		}
		return result;
	}
	private HttpRequest createRequestToken(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_DOUBAN);
		result.setLoginStep(LoginResult.LOGIN_REQUEST);
		notifyMessage(0, result);
		
		String host = DoubanService.getInstance().getRequestUrl(HOST_REQUEST_TOKEN_DOUBAN);
		return DoubanService.getInstance().getOauthClient().request(THttpMethod.GET,host, null);
	}

	private HttpRequest createAuthenticate(){
		String host = DoubanService.getInstance().getRequestUrl(HOST_AUTHORIZE_DOUBAN);

		StringBuffer url = new StringBuffer(host);
		url.append("?");
	
		url.append(OAuth.OAUTH_TOKEN);
		url.append("=");
		url.append(DoubanService.getInstance().getOauthClient().mTokenKey);
		url.append("&");
		url.append(OAuth.OAUTH_CALLBACK);
		url.append("=");
		url.append(RedirectUrl.OAUTH_CALLBACK);
		
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_DOUBAN);
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
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_DOUBAN);
		result.setLoginStep(LoginResult.LOGIN_ACCESS);
		notifyMessage(0, result);
		
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		if(verifier != null)
			parameter.put(OAuth.OAUTH_VERIFIER, verifier);
		
		String host = DoubanService.getInstance().getRequestUrl(HOST_ACCESS_TOKEN_DOUBAN);
		return DoubanService.getInstance().getOauthClient().request(THttpMethod.GET, host, parameter);
	}
	
	public HttpRequest createVerify(){
		LoginResult result = new LoginResult(WeiboAccountColumn.WB_TYPE_DOUBAN);
		result.setLoginStep(LoginResult.LOGIN_GETINFO);
		notifyMessage(0, result);
		
		String url = HOST_VERIFY_DOUBAN;
		return DoubanService.getInstance().getOauthClient().request(THttpMethod.GET,url, null);
	}
}


