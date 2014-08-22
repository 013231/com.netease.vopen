package mblog.sina;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.FriendshipResult;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.app.BaseApplication;
import vopen.db.ManagerWeiboAccount;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.util.Log;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;
import common.framework.task.AsyncTransaction;
import common.multipart.StringPart;
import common.pal.PalLog;
import common.util.BaseUtil;
import common.util.Util;
import common.util.oauth.OAuthHttpRequest;

/**
 * 处理新浪微博关注接口相关
 * friendships/create 关注某用户（已实现）
 * friendships/show 获取两个用户关系的详细情况（已实现）
 * @author Administrator
 *
 */
public class FriendshipsTransaction extends BaseTransaction {
    private final String TAG = "FriendshipsTransaction";
    //获取用户关系
    private static final String URL_FRIENDSHIPS_SHOW = "/friendships/show.json";
    //发新微博
    private static final String URL_FRIENDSHIPS_FOLLOW = "/friendships/create.json";
    
    // 对应oauth2.0
    //获取用户关系
    private static final String URL_FRIENDSHIPS_SHOW2 = "/2/friendships/show.json";
    //发新微博
    private static final String URL_FRIENDSHIPS_FOLLOW2 = "/2/friendships/create.json";
    
    private String mTargetUid;//关系人账号
    
    private String mAccountName;
    
    protected FriendshipsTransaction(int type,String name, String targetUid) {
        super(type);
        // TODO Auto-generated constructor stub
        mTargetUid = targetUid;
        mAccountName = name;
    }
    
    /**
     * 获取两个用户关系的详细情况
     * @param name
     * @param uid_b
     * @return
     */
    public static AsyncTransaction createFriendshipShowTransaction(String name, String uid_b){
        FriendshipsTransaction showFriendshipTransaction = new FriendshipsTransaction(BaseTransaction.TRANSACTION_TYPE_FRIENDSHIP_SHOW, name, uid_b);
        return showFriendshipTransaction;
    }
    
    /**
     * 关注某用户
     * @param name 当前用户昵称
     * @param to_follow_uid 准备关注的用户uid
     * @return
     */
    public static AsyncTransaction createFollowTransaction(String name, String to_follow_uid){
        FriendshipsTransaction followTransaction = new FriendshipsTransaction(BaseTransaction.TRANSCATION_TYPE_FOLLOW, name, to_follow_uid);
        return followTransaction;
    }

    @Override
    public void onResponseSuccess(String response) {
        // TODO Auto-generated method stub
        Log.d(TAG, "response" + response);
        Object results = null;
        switch (getType()) {
        case TRANSACTION_TYPE_FRIENDSHIP_SHOW:
            results = parseShowFriendshipResponse(response);
            break;
        case TRANSCATION_TYPE_FOLLOW:
            results = response;
            break;
        }
        if (!isCancel()) {
            notifyMessage(ErrDescrip.SUCCESS, results);
        } else {
            notifyError(ErrDescrip.ERR_PARSE, null);
        }
    }

    @Override
    public void onResponseError(int errCode, String errStr) {
        // TODO Auto-generated method stub
        ErrDescrip error = SinaService.getInstance().parseError(errCode, errStr);       
        notifyError(error.errCode, error);
    }
    
    private FriendshipResult parseShowFriendshipResponse(String response){
        FriendshipResult result = new FriendshipResult(WeiboAccountColumn.WB_TYPE_SINA);
        try {
            JSONObject json = new JSONObject(response);
            JSONObject jsonSource = json.getJSONObject("source");
            result.setFollowedBy(jsonSource.getBoolean("followed_by"));
            result.setFollowing(jsonSource.getBoolean("following"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
    
    private HttpRequest createShowRequest(){
        Hashtable<String, String> parameter = new Hashtable<String, String>();
        String url = SinaService.getInstance().getRequestUrl(URL_FRIENDSHIPS_SHOW);
        if (!BaseUtil.isStringEmpty(mTargetUid)) {
            parameter.put("target_id", mTargetUid);
        }
        OAuthHttpRequest request = SinaService.getInstance().getOauthClient().request(THttpMethod.GET, url, parameter);
        request.setOAuthDataPost(true);
        return request;
    }
    
    private HttpRequest createFollowRequest(){
        Hashtable<String, String> parameter = new Hashtable<String, String>();
        String url = SinaService.getInstance().getRequestUrl(URL_FRIENDSHIPS_FOLLOW);
        if (!BaseUtil.isStringEmpty(mTargetUid)) {
            parameter.put("user_id", mTargetUid);
        }
        OAuthHttpRequest request = SinaService.getInstance().getOauthClient().request(THttpMethod.POST, url, parameter);
        request.setOAuthDataPost(true);
        return request;
    }
    
    private HttpRequest createShowRequest2(){
        String url = SinaService.getInstance().getRequestUrl(URL_FRIENDSHIPS_SHOW2);
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		
		parameters.add(new BasicNameValuePair("access_token", SinaService.getInstance().getOauthClient().mTokenKey));
			
		if (!BaseUtil.isStringEmpty(mAccountName)) {
			parameters.add(new BasicNameValuePair("source_screen_name", mAccountName));
		}
		if (!BaseUtil.isStringEmpty(mTargetUid)) {
			parameters.add(new BasicNameValuePair("target_id", mTargetUid));
		}
		if (parameters.size() > 0) {
			url += "?" + URLEncodedUtils.format(parameters, "UTF-8");
		}
		
		HttpRequest request =  new HttpRequest(url.toString(), HttpRequest.METHOD_GET);
		
        return request;
    }
    private HttpRequest createFollowRequest2(){
        String url = SinaService.getInstance().getRequestUrl(URL_FRIENDSHIPS_FOLLOW2);
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		
		parameters.add(new BasicNameValuePair("access_token", SinaService.getInstance().getOauthClient().mTokenKey));
			
	
		if (!BaseUtil.isStringEmpty(mTargetUid)) {
			parameters.add(new BasicNameValuePair("uid", mTargetUid));
		}
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(parameters, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpRequest request =  new HttpRequest(url.toString(), HttpRequest.METHOD_POST);
		if(entity != null)
			request.setHttpEntity(entity);
		
        return request;
    }
    
    private void setLogin(String tokenKey, String tokenSecret) {
		SinaService.getInstance().setOauthToken(tokenKey, tokenSecret);
	}
	
	private boolean isLogin(){
		String[] tokens = ManagerWeiboAccount.getWBAccountToken(BaseApplication.getAppInstance(), mAccountName, WeiboAccountColumn.WB_TYPE_SINA);
		if(tokens!= null && tokens.length == 3){
			if(!Util.isStringEmpty(tokens[0]) && !Util.isStringEmpty(tokens[1])){
				setLogin(tokens[0], tokens[1]);
				return true;
			}
		}
		return false;
	}

    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
    	
    	if(!isLogin())
		{
			notifyError(ErrDescrip.ERR_AUTH_FAIL, null);
			PalLog.e("***********", "Error  withou token & secret to send blog");
			return;
		}
    	
        HttpRequest httpRequest = null;
        switch (getType()) {
        case TRANSACTION_TYPE_FRIENDSHIP_SHOW:
            httpRequest = createShowRequest2();
            break;
        case TRANSCATION_TYPE_FOLLOW:
            httpRequest = createFollowRequest2();
            break;
        }
        if (httpRequest != null && !isCancel()) {
            sendRequest(httpRequest);
        } else {
            notifyError(ErrDescrip.ERR_COMMAND_FAIL, null);
            doEnd();
        }
    }

}
