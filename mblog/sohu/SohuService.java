package mblog.sohu;

import mblog.base.BaseService;
import mblog.base.BaseTransListener;
import mblog.base.ErrDescrip;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.util.Log;

import common.framework.task.AsyncTransaction;
import common.util.oauth.OAuth;

public class SohuService extends BaseService{
	//搜狐
	public static final String SOHU_APP_KEY = "bHBJAaRTZ6KUMxjWdgKh";
	public static final String SOHU_APP_SECRET = OAuth.percentEncode("n-%C%uUyYXZ!AE^I68X)vi*mf72gkYmT#xpYTJlm");
	
	private static final String SOHU_SERVER_DOMAIN = "http://api.t.sohu.com";
	
	private static SohuService s_Instance;
	public static SohuService getInstance(){
		if(s_Instance == null)
			s_Instance = new SohuService();
		
		return s_Instance;
	}
	public SohuService() {
		super(SOHU_APP_KEY, SOHU_APP_SECRET);
	}
	
	@Override
	public String getRequestUrl(String api) {
		return SOHU_SERVER_DOMAIN + api;
	}

	public ErrDescrip parseError(int errCode, String errStr){
		if(errStr == null){
			return new ErrDescrip(WeiboAccountColumn.WB_TYPE_SOHU, ErrDescrip.ERR_NETWORK, null, null);
		}
		
		Log.i("SohuService", errStr);
		ErrDescrip error = new ErrDescrip(WeiboAccountColumn.WB_TYPE_SOHU);
		error.errCode = errCode;
		try {
			JSONObject job = new JSONObject(errStr);
			error.description = job.optString("error");
			error.messageCode = String.valueOf(errCode);
		} catch (JSONException e) {
			error.errCode = ErrDescrip.ERR_PARSE;
			error.description = ErrDescrip.getDescriptionByCode(ErrDescrip.ERR_PARSE);
			e.printStackTrace();
		}
		
		return error;
	}
	
	public int doLogin(BaseTransListener l){
		resetOauthClient(SOHU_APP_KEY, SOHU_APP_SECRET);
		LoginTransaction t = new LoginTransaction();
		return startTransaction(t, l);
	}
	
	public int doSendBlog(String name, String content, String imagePath,
			BaseTransListener l){
		AsyncTransaction t = BlogTransaction.createSendBlogTransaction(name, content, imagePath);
		return startTransaction(t, l);
	}
}
