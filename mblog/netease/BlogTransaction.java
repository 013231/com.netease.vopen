package mblog.netease;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.SendBlogResult;

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
import common.pal.PalLog;
import common.util.BaseUtil;
import common.util.Util;
import common.util.oauth.OAuthHttpRequest;


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
	private static final String URL_BLOG_UPLOAD = "/statuses/upload.json";
	//发新微博
	private static final String URL_BLOG_UPDATE = "/statuses/update.json";
	
	private static final String UPLOAD_IMAGE_URL = "upload_image_url";
	

//	private static final byte PHASE_UPLAOD_IMAGE		= 0;
	private static final byte PHASE_SEND_TWEET			= 1;
	
	private byte mTransactionPhase;
	
//	private byte mType;
	private String mContent;
	private String mImagePath;
	private String mLatitude;
	private String mLongitude;
	private String mVid;

	private String mName;
	private HttpRequest mHttpRequest;
	private String mImageUrl;
	protected BlogTransaction(int type) {
		super(type);
		mTransactionPhase = 0;
	}
	
	public static AsyncTransaction createSendBlogTransaction(
			String name, String content, String imagePath,
			String latitude, String longitude, String vid) {
				
		BlogTransaction bt = new BlogTransaction(TRANSACTION_TYPE_BLOGSEND);
		bt.mImagePath = imagePath;
		bt.mContent = content;

		bt.mLatitude = latitude;
		bt.mLongitude = longitude;
		bt.mVid = vid;
		
		bt.mName = name;
		return bt;
	}
	
	
	
	private void setLogin(String tokenKey, String tokenSecret) {
		NeteaseService.getInstance().setOauthToken(tokenKey, tokenSecret);
	}
	
	private boolean isLogin(){
		String[] tokens = ManagerWeiboAccount.getWBAccountToken(BaseApplication.getAppInstance(),mName, WeiboAccountColumn.WB_TYPE_NETEASE);
		if(tokens!= null && tokens.length == 2){
			if(!Util.isStringEmpty(tokens[0]) && !Util.isStringEmpty(tokens[1])){
				setLogin(tokens[0], tokens[1]);
				return true;
			}
		}
		return false;
	}
	
	public void onTransact() {
		if(!isLogin())
		{
			notifyError(ErrDescrip.ERR_AUTH_FAIL, null);
			PalLog.e("***********", "Error  withou token & secret to send blog");
			return;
		}
		switch (getType()) {
		case TRANSACTION_TYPE_BLOGSEND:
			if (mTransactionPhase == 0 && !Util.isStringEmpty(mImagePath)) {
				mHttpRequest = createBlogUpload(mImagePath);
			}
			else {
				
					String content = mContent;
					if (mImageUrl != null) {
						content += ' ' + mImageUrl;
					}				
					mHttpRequest = createBlogSend(content, mLatitude, mLongitude, mVid);
			

			}
			break;
			
					
		default:
			break;
		}
		
		if (mHttpRequest != null && !isCancel()) {
			sendRequest(mHttpRequest);
		}
		else {
			notifyError(ErrDescrip.ERR_COMMAND_FAIL, null);
			doEnd();
		}
	}
	
//	 {
//		    "error_code" : "403",
//		    "request" : "/statuses/friends_timeline.json",
//		    "error" : "40302:Error: auth faild!"
//		}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		super.onTransactionSuccess(code, obj);
		if (getType() == TRANSACTION_TYPE_BLOGSEND
				&& mTransactionPhase == PHASE_SEND_TWEET) {
			getTransactionEngine().beginTransaction(this);
		}
		else {
			doEnd();
		}
	}
	
	public void onResponseSuccess(String response) {
		
		Object results = null;

		switch (getType()) {
		case TRANSACTION_TYPE_BLOGSEND:
			PalLog.i("BlogTransaction", response);
			if (mTransactionPhase == 0 && !Util.isStringEmpty(mImagePath)) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					mImageUrl = BaseUtil.nullStr(jsonObject.optString(UPLOAD_IMAGE_URL) );
					mTransactionPhase = 1;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(mImageUrl == null)
					notifyError(ErrDescrip.ERR_UPLOAD_FAIL, null);
				return ;
			} else {
				mTransactionPhase = -1;

				results = parseJson(response);
			}
			break;

		
		}
		
		if (!isCancel()) {
			if (results != null) {
				notifyMessage(ErrDescrip.SUCCESS,results);
			}
			else {
				notifyError(ErrDescrip.ERR_PARSE, null);
			}
		}
	}

	@Override
	public void onResponseError(int errCode, String errStr) {
		ErrDescrip error = NeteaseService.getInstance().parseError(errCode, errStr);		
		notifyError(error.errCode, error);
	}
	
	private SendBlogResult parseJson(String json){
		SendBlogResult result = new SendBlogResult(WeiboAccountColumn.WB_TYPE_NETEASE);
		
		JSONObject userJson;
		try {
			userJson = new JSONObject(json);
			result.setId(BaseUtil.nullStr(userJson.optString("id")));
		} catch (JSONException e) {
			result = null;
			e.printStackTrace();
		}
		
		if(result != null && Util.isStringEmpty(result.getId())){
			result = null;
		}
		
		return result;
	}
	
	
	/**
	 * 上传图片
	 * @param file
	 * @return
	 */
	private HttpRequest createBlogUpload(String file)
	{
		FilePart filePart = null;
		try {
			filePart = new FilePart("pic","upload.jpg", new File(file),null,null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		MultipartEntity entity = new MultipartEntity(new Part[]{filePart});
		HttpRequest req = NeteaseService.getInstance().getOauthClient().request(THttpMethod.POST,
				NeteaseService.getInstance().getRequestUrl(URL_BLOG_UPLOAD), null, entity);
		
		return req;
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
	private HttpRequest createBlogSend(String status,
			String latitude, String longitude, String vid)
	{
		Hashtable<String, String> parameter = new Hashtable<String, String>();
		String url = NeteaseService.getInstance().getRequestUrl(URL_BLOG_UPDATE);
		
		parameter.put("status", status);
			
			if (!BaseUtil.isStringEmpty(latitude)) {
				parameter.put("lat", latitude);
			}
			if (!BaseUtil.isStringEmpty(longitude)) {
				parameter.put("long", longitude);
			}
			if (!BaseUtil.isStringEmpty(vid)) {
				parameter.put("vid", vid);
			}
	
	
		OAuthHttpRequest request = NeteaseService.getInstance().getOauthClient().request(THttpMethod.POST,url, parameter);
		request.setOAuthDataPost(true);
		
		return request;
	}
}
