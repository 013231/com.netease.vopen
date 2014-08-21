package mblog.netease;

import mblog.base.BaseService;
import mblog.base.BaseTransListener;
import mblog.base.ErrDescrip;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.util.Log;

import common.framework.task.AsyncTransaction;

public class NeteaseService extends BaseService{
	//网易微博 consumer
	public static final String NETEASE_APP_KEY = "OE00SVvHa0nHgkut";
	public static final String NETEASE_APP_SECRET = "gnbNWRswuXcOPlioYSNX9EOf61C8glCC";
	
	private static final String NETEASE_SERVER_DOMAIN = "http://api.t.163.com";
	
	private static NeteaseService s_Instance;
	public static NeteaseService getInstance(){
		if(s_Instance == null)
			s_Instance = new NeteaseService();
		
		return s_Instance;
	}
	public NeteaseService() {
		super(NETEASE_APP_KEY, NETEASE_APP_SECRET);
	}
	
	@Override
	public String getRequestUrl(String api) {
		return NETEASE_SERVER_DOMAIN + api;
	}

	public ErrDescrip parseError(int errCode, String errStr){
		if(errStr == null){
			return new ErrDescrip(WeiboAccountColumn.WB_TYPE_NETEASE, ErrDescrip.ERR_NETWORK, null, null);
		}
		
		Log.i("NeteaseService", errStr);
		ErrDescrip error = new ErrDescrip(WeiboAccountColumn.WB_TYPE_NETEASE);
		error.errCode = errCode;
		try {
			JSONObject job = new JSONObject(errStr);
			error.messageCode = job.optString("message_code");
			error.description = job.optString("error");
		} catch (JSONException e) {
			error.errCode = ErrDescrip.ERR_PARSE;
			error.description = ErrDescrip.getDescriptionByCode(ErrDescrip.ERR_PARSE);
			e.printStackTrace();
		}
		
		return error;
	}
	
	public int doLogin(BaseTransListener l){
		resetOauthClient(NETEASE_APP_KEY, NETEASE_APP_SECRET);
		LoginTransaction t = new LoginTransaction();
		return startTransaction(t, l);
	}
	
	public int doSendBlog(String name, String content, String imagePath,
			String latitude, String longitude, String vid, 
			BaseTransListener l){
		AsyncTransaction t = BlogTransaction.createSendBlogTransaction(name, content, imagePath, latitude, longitude, vid);
		return startTransaction(t, l);
	}
}
