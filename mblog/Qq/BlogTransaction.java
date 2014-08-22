package mblog.Qq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.SendBlogResult;
import mblog.sina.SinaService;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.ManagerWeiboAccount;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;
import common.framework.task.AsyncTransaction;
import common.multipart.FilePart;
import common.multipart.MultipartEntity;
import common.multipart.Part;
import common.multipart.StringPart;
import common.pal.PalLog;
import common.util.BaseUtil;
import common.util.Util;
import common.util.oauth.OAuth;
import common.util.oauth.OAuthClient;
import common.util.oauth.OAuthHttpRequest;
import common.util.oauth.OAuthNameValuePair;

/**
 * 主要负责以下几大协议部份:
 * 		微博  StatuesInfo）
 * 		收藏  
 * 
 * @author Roy
 * 
 * 
 *
 */
public class BlogTransaction extends BaseTransaction {
	// 上传图片(statuses/upload)
	private static final String URL_BLOG_UPLOAD = "/api/t/add_pic";
	//发新微博
	private static final String URL_BLOG_UPDATE = "/api/t/add";

	private String mContent;
	private String mImagePath;
	private String mLatitude;
	private String mLongitude;

	private String mName;
	private String mUserId;
	private HttpRequest mHttpRequest;

	protected BlogTransaction(int type) {
		super(type);
	}

	public static AsyncTransaction createSendBlogTransaction(String name,
			String content, String imagePath, String latitude, String longitude) {
		int type = TRANSACTION_TYPE_BLOGSEND;
		if (!Util.isStringEmpty(imagePath)) {
			type = TRANSACTION_TYPE_UPLOAD;
		}
		BlogTransaction bt = new BlogTransaction(type);
		bt.mImagePath = imagePath;
		bt.mContent = content;

		bt.mLatitude = latitude;
		bt.mLongitude = longitude;

		bt.mName = name;
		return bt;
	}

	private void setLogin(String tokenKey, String tokenSecret) {
		QqService.getInstance().setOauthToken(tokenKey, tokenSecret);
	}

	private boolean isLogin() {
		String[] tokens = ManagerWeiboAccount.getWBAccountToken(
				BaseApplication.getAppInstance(), mName,
				WeiboAccountColumn.WB_TYPE_TENCENT);
		if (tokens != null && tokens.length == 3) {
			if (!Util.isStringEmpty(tokens[0])
					&& !Util.isStringEmpty(tokens[1])) {
				setLogin(tokens[0], tokens[1]);
				mUserId = tokens[2];
				return true;
			}
		}
		return false;
	}

	public void onTransact() {
		if (!isLogin()) {
			notifyError(ErrDescrip.ERR_AUTH_FAIL, null);
			PalLog.e("***********", "Error  withou token & secret to send blog");
			return;
		}
		switch (getType()) {
		case TRANSACTION_TYPE_UPLOAD:
		case TRANSACTION_TYPE_BLOGSEND:
			mHttpRequest = createBlogSend(mContent, mLatitude, mLongitude,
					mImagePath);
			break;

		default:
			break;
		}

		if (mHttpRequest != null && !isCancel()) {
			sendRequest(mHttpRequest);
		} else {
			notifyError(ErrDescrip.ERR_COMMAND_FAIL, null);
			doEnd();
		}
	}

	//	 {
	//		    "error_code" : "403",
	//		    "request" : "/statuses/friends_timeline.json",
	//		    "error" : "40302:Error: auth faild!"
	//		}

	public void onResponseSuccess(String response) {

		Object results = null;

		switch (getType()) {
		case TRANSACTION_TYPE_BLOGSEND:
		case TRANSACTION_TYPE_UPLOAD:
			results = parseJson(response);
			break;

		}

		if (!isCancel()) {
			if (results != null) {
				if (results instanceof ErrDescrip) {
					notifyError(((ErrDescrip) results).errCode, results);
				} else {
					notifyMessage(ErrDescrip.SUCCESS, results);
				}
			} else {
				notifyError(ErrDescrip.ERR_PARSE, null);
			}
		}
	}

	@Override
	public void onResponseError(int errCode, String errStr) {
		ErrDescrip error = QqService.getInstance().parseError(errCode, errStr);
		notifyError(error.errCode, error);
	}

	private Object parseJson(String json) {
		SendBlogResult result = new SendBlogResult(
				WeiboAccountColumn.WB_TYPE_TENCENT);

		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(json);
			int ret = jsonObj.getInt("ret");
			if (ret != 0) {
				return QqService.getInstance().parseError(ret, json);
			} else {
				JSONObject data = jsonObj.optJSONObject("data");
				result.setId(BaseUtil.nullStr(data.optString("id")));
			}
		} catch (JSONException e) {
			result = null;
			e.printStackTrace();
		}

		if (result != null && Util.isStringEmpty(result.getId())) {
			result = null;
		}

		return result;
	}

	/**
	 * 发布一条微博。
	 * 
	 * @param status
	 *            必选参数，微博内容，不得超过163个字符；
	 * @param in_reply_to_status_id
	 *            可选参数，当评论指定微博时需带上此参数，值为被回复的微博ID；
	 * @param 
	 * @return
	 */
	private HttpRequest createBlogSend(String status, String latitude,
			String longitude, String imagePath) {
		//		if(!Util.isStringEmpty(imagePath)){
		//			String url = QqService.getInstance().getRequestUrl(URL_BLOG_UPLOAD);
		//			try {
		//				ArrayList<Part> parts = new ArrayList<Part>();
		//				parts.add(new StringPart("format", "json", "utf-8"));
		//				parts.add(new StringPart("content", status, "utf-8"));
		//				if (!BaseUtil.isStringEmpty(latitude)) {
		//					parts.add(new StringPart("wei", latitude, "utf-8"));
		//				}
		//				if (!BaseUtil.isStringEmpty(longitude)) {
		//					parts.add(new StringPart("jing", longitude, "utf-8"));
		//				}
		//				
		//				
		//				parts.add(new FilePart("pic", new File(imagePath)));
		//				Part[] partArr = new Part[parts.size()];
		//				parts.toArray(partArr);
		//				MultipartEntity entity = new MultipartEntity(partArr);
		//				OAuthHttpRequest request =  QqService.getInstance().getOauthClient().request(THttpMethod.POST, url, null, entity);
		//				request.setOauthdataQuery(true);
		//				return request;
		//			} catch (FileNotFoundException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//				return null;
		//			}
		//			
		//		}
		//		else{
		String url = QqService.getInstance().getRequestUrl(URL_BLOG_UPDATE);
		if (!Util.isStringEmpty(imagePath)) {
			Hashtable<String, String> parameter = new Hashtable<String, String>();
			parameter.put("format", "json");
			parameter.put("content", status);
			if (!BaseUtil.isStringEmpty(latitude)) {
				parameter.put("latitude", latitude);
			}
			if (!BaseUtil.isStringEmpty(longitude)) {
				parameter.put("longitude", longitude);
			}
			//无法获取到外网ip，这里使用开发时的ip
			parameter.put("clientip", "123.58.191.68");
			url = QqService.getInstance().getRequestUrl(URL_BLOG_UPLOAD);
			//				parameter.put("pic", mImagePath);
			AddPicHttpRequest request = new AddPicHttpRequest(url,
					THttpMethod.POST, QqService.getInstance().getOauthClient());
			if (parameter != null) {
				for (Entry<String, String> entry : parameter.entrySet()) {
					request.addParameter(entry.getKey(), entry.getValue());
				}
			}
			return request;
		} else {
			List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
			parameters.add(new BasicNameValuePair("format", "json"));
			parameters.add(new BasicNameValuePair("content", status));
			parameters.add(new BasicNameValuePair("access_token", QqService.getInstance().getOauthClient().mTokenKey));
			parameters.add(new BasicNameValuePair("oauth_consumer_key", QqService.QQ_APP_KEY));
			parameters.add(new BasicNameValuePair("openid", mUserId));
			parameters.add(new BasicNameValuePair("oauth_version", "2.a"));
			if (!BaseUtil.isStringEmpty(latitude)) {
				parameters.add(new BasicNameValuePair("latitude", latitude));
			}
			if (!BaseUtil.isStringEmpty(longitude)) {
				parameters.add(new BasicNameValuePair("longitude", longitude));
			}
			
			UrlEncodedFormEntity entity = null;
			try {
				entity = new UrlEncodedFormEntity(parameters, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			HttpRequest request =  new HttpRequest(url.toString(), HttpRequest.METHOD_POST);
			if(entity != null)
				request.setHttpEntity(entity);
			return request;
		}
		//		}
	}

	public class AddPicHttpRequest extends OAuthHttpRequest {
		public AddPicHttpRequest(String url, OAuthClient client) {
			super(url, client);
			mOAuthUrl = url;
		}

		public AddPicHttpRequest(String url, THttpMethod type,
				OAuthClient client) {
			super(url, type, client);
			mOAuthUrl = url;
		}

		@Override
		public void doBefore() {
			List<OAuthNameValuePair> list = new ArrayList<OAuthNameValuePair>();

			Hashtable paramters = getHttpParamters();
			if (paramters != null && paramters.size() > 0) {
				Enumeration enu = paramters.keys();
				while (enu.hasMoreElements()) {
					String key = (String) enu.nextElement();
					String value = (String) paramters.get(key);
					list.add(new OAuthNameValuePair(key, value));
				}
			}

			if (mOAuthClient.mTokenKey != null) {
				list.add(new OAuthNameValuePair(OAuth.OAUTH_TOKEN,
						mOAuthClient.mTokenKey));
			}
			list.add(new OAuthNameValuePair(OAuth.OAUTH_CONSUMER_KEY,
					mOAuthClient.mConsumerKey));
			list.add(new OAuthNameValuePair(OAuth.OAUTH_SIGNATURE_METHOD,
					mOAuthClient.getSignatureMethod()));
			list.add(new OAuthNameValuePair(OAuth.OAUTH_VERSION,
					OAuth.VERSION_1_0));
			list.add(new OAuthNameValuePair(OAuth.OAUTH_TIMESTAMP, String
					.valueOf((System.currentTimeMillis() / 1000))));
			list.add(new OAuthNameValuePair(OAuth.OAUTH_NONCE, String
					.valueOf((System.currentTimeMillis())
							+ Math.abs(mNoceRandom.nextLong()))));

			String baseString = getBaseString(getMethod().toString(),
					super.getUrl(), list);
			String signature = mOAuthClient.getHMAC_SHA1().getSignature(
					baseString);
			list.add(new OAuthNameValuePair(OAuth.OAUTH_SIGNATURE, signature));

			ArrayList<Part> parts = new ArrayList<Part>();
			for (NameValuePair kv : list) {
				parts.add(new StringPart(kv.getName(), kv.getValue(), "UTF-8"));
			}

			//			parts.add(new StringPart("content", mContent, "UTF-8"));
			try {
				parts.add(new FilePart("pic", mImagePath, new File(mImagePath),
						null, null));//注：腾讯微博上传图片一定要有文件名，否则会返回size error
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			Part[] partArr = new Part[parts.size()];
			parts.toArray(partArr);
			MultipartEntity entity = new MultipartEntity(partArr);
			setHttpEntity(entity);

		}

	}
}
