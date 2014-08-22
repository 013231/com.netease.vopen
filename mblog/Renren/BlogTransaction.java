package mblog.Renren;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.SendBlogResult;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.ManagerWeiboAccount;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;

import common.framework.http.HttpRequest;
import common.framework.task.AsyncTransaction;
import common.pal.PalLog;
import common.util.Util;
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
public class BlogTransaction extends BaseTransaction{
	private String rr_name;
	private String rr_description;
	private String rr_url;
	private String rr_image;
	private String rr_caption;
	private String rr_action_name;
	private String rr_action_link;
	private String rr_message;

	private String mName;
	private HttpRequest mHttpRequest;
	
	protected BlogTransaction(int type) {
		super(type);
	}
	
	public static AsyncTransaction createSendRenRenBlogTransaction(
			int type,
			String name,
			String rr_name, String rr_description, 
			String rr_url, String rr_image, String rr_caption, String rr_action_name,
			String rr_action_link, String rr_message) {
				
		BlogTransaction bt = new BlogTransaction(TRANSACTION_TYPE_BLOGSEND);
		bt.rr_name = rr_name;
		bt.rr_description = rr_description;
		bt.rr_url = rr_url;
		
		bt.rr_image = rr_image;
		bt.rr_caption = rr_caption;
		bt.rr_action_name = rr_action_name;
		bt.rr_action_link = rr_action_link;
		bt.rr_message = rr_message;
		
		bt.mName = name;
		
		return bt;
	}
	
	
	
	private void setLogin(String tokenKey, String tokenSecret) {
		RenrenService.getInstance().setOauthToken(tokenKey, tokenSecret);
	}
	
	private boolean isLogin(){
		String[] tokens = ManagerWeiboAccount.getWBAccountToken(BaseApplication.getAppInstance(),mName, WeiboAccountColumn.WB_TYPE_RENREN);
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
		case TRANSACTION_TYPE_BLOGSEND:
				mHttpRequest = createBlogSend();
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
			PalLog.i("BlogTransaction", response);
			results = parseJson(response);
			break;
		case TRANSACTION_TYPE_UPLOAD:
			results = parseJson(response);
			break;
		
		}
		
		if (!isCancel()) {
			if (results != null) {
				if(results instanceof ErrDescrip)
					notifyError(((ErrDescrip)results).errCode, results);
				else
					notifyMessage(ErrDescrip.SUCCESS,results);
			}
			else {
				notifyError(ErrDescrip.ERR_PARSE, null);
			}
		}
	}

	@Override
	public void onResponseError(int errCode, String errStr) {
		ErrDescrip error = RenrenService.getInstance().parseError(errStr);		
		notifyError(error.errCode, error);
	}
	
	private Object parseJson(String json){
		SendBlogResult result = new SendBlogResult(WeiboAccountColumn.WB_TYPE_RENREN);
		
		JSONObject userJson;
		try {
			userJson = new JSONObject(json);
			int postId = userJson.optInt("post_id");
			if(postId <= 0){
				return RenrenService.getInstance().parseError(json);
			}
			result.setId(String.valueOf(postId));
		} catch (JSONException e) {
			result = null;
			e.printStackTrace();
		}
		
		if(result != null && Util.isStringEmpty(result.getId())){
			result = null;
		}
	
		return result;
	}
	
	
	private HttpRequest createBlogSend() {
		String url = RenrenService.getInstance().getRequestUrl("");
				
		List<OAuthNameValuePair> list = new ArrayList<OAuthNameValuePair>();
		list.add(new OAuthNameValuePair("name", rr_name));
		list.add(new OAuthNameValuePair("description", rr_description));
		list.add(new OAuthNameValuePair("url", rr_url));

		if (!Util.isStringEmpty(rr_image))
			list.add(new OAuthNameValuePair("image", rr_image));
		if (!Util.isStringEmpty(rr_caption))
			list.add(new OAuthNameValuePair("caption", rr_caption));
		if (!Util.isStringEmpty(rr_action_name))
			list.add(new OAuthNameValuePair("action_name", rr_action_name));
		if (!Util.isStringEmpty(rr_action_link))
			list.add(new OAuthNameValuePair("action_link", rr_action_link));
		if (!Util.isStringEmpty(rr_message))
			list.add(new OAuthNameValuePair("message", rr_message));
		
		RenrenService.getInstance().prepareParams(list, "feed.publishFeed");

		HttpRequest request = null;
		request = new HttpRequest(url, HttpRequest.METHOD_POST);
		
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(list,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		request.setHttpEntity(entity);

		return request;

	}

}
