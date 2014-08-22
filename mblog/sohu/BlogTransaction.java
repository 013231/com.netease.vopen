package mblog.sohu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import common.multipart.StringPart;
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
	

	private String mContent;
	private String mImagePath;

	private String mName;
	private HttpRequest mHttpRequest;
	
	protected BlogTransaction(int type) {
		super(type);
	}
	
	public static AsyncTransaction createSendBlogTransaction(
			String name, String content, String imagePath) {
		int type = TRANSACTION_TYPE_BLOGSEND;
		if(!Util.isStringEmpty(imagePath)){
			type = TRANSACTION_TYPE_UPLOAD;
		}
		BlogTransaction bt = new BlogTransaction(type);
		bt.mImagePath = imagePath;
		bt.mContent = content;
		
		bt.mName = name;
		return bt;
	}
	
	
	
	private void setLogin(String tokenKey, String tokenSecret) {
		SohuService.getInstance().setOauthToken(tokenKey, tokenSecret);
	}
	
	private boolean isLogin(){
		String[] tokens = ManagerWeiboAccount.getWBAccountToken(BaseApplication.getAppInstance(),mName, WeiboAccountColumn.WB_TYPE_SOHU);
		if(tokens!= null && tokens.length == 3){
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
		case TRANSACTION_TYPE_UPLOAD:
		case TRANSACTION_TYPE_BLOGSEND:
				mHttpRequest = createBlogSend(mContent,  mImagePath);
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
				notifyMessage(ErrDescrip.SUCCESS,results);
			}
			else {
				notifyError(ErrDescrip.ERR_PARSE, null);
			}
		}
	}

	@Override
	public void onResponseError(int errCode, String errStr) {
		ErrDescrip error = SohuService.getInstance().parseError(errCode, errStr);		
		notifyError(error.errCode, error);
	}
	
	private SendBlogResult parseJson(String json){
		SendBlogResult result = new SendBlogResult(WeiboAccountColumn.WB_TYPE_SOHU);
		
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
	 * 发布一条微博。
	 * 
	 * @param status
	 *            必选参数，微博内容，不得超过163个字符；
	 * @param in_reply_to_status_id
	 *            可选参数，当评论指定微博时需带上此参数，值为被回复的微博ID；
	 * @param 
	 * @return
	 */
	private HttpRequest createBlogSend(String status, String imagePath)
	{
		if(!Util.isStringEmpty(imagePath)){
			String url = SohuService.getInstance().getRequestUrl(URL_BLOG_UPLOAD);
			try {
				try {
					status = URLEncoder.encode(status, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				Hashtable<String, String> parameter = new Hashtable<String, String>();
				parameter.put("status", status);
				
				ArrayList<Part> parts = new ArrayList<Part>();
				parts.add(new StringPart("status", status, "UTF-8"));
					
				parts.add(new FilePart("pic", null,new File(imagePath),null,null));
				Part[] partArr = new Part[parts.size()];
				parts.toArray(partArr);
				MultipartEntity entity = new MultipartEntity(partArr);
				OAuthHttpRequest request =  SohuService.getInstance().getOauthClient().request(THttpMethod.POST, url, parameter, entity);
				request.setSpecial(OAuthHttpRequest.SPECIAL_SOHU);
				return request;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		}
		else{
			Hashtable<String, String> parameter = new Hashtable<String, String>();
			String url = SohuService.getInstance().getRequestUrl(URL_BLOG_UPDATE);
			
			parameter.put("status", status);
	
			OAuthHttpRequest request = SohuService.getInstance().getOauthClient().request(THttpMethod.POST,url, parameter);
			request.setOAuthDataPost(true);
			return request;
		}
	}

}
