package mblog.Douban;

import mblog.base.BaseService;
import mblog.base.BaseTransListener;
import mblog.base.ErrDescrip;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.util.Log;

import common.framework.task.AsyncTransaction;
import common.util.Util;

public class DoubanService extends BaseService{
	//豆瓣
	public static final String DOUBAN_APP_KEY = "0fb873efabd577e22f295bcf038067a6";
	public static final String DOUBAN_APP_SECRET = "d390f64218b411ad";
	
	private static final String DOUBAN_SERVER_DOMAIN = "http://www.douban.com";
	
	private static DoubanService s_Instance;
	public static DoubanService getInstance(){
		if(s_Instance == null)
			s_Instance = new DoubanService();
		
		return s_Instance;
	}
	public DoubanService() {
		super(DOUBAN_APP_KEY, DOUBAN_APP_SECRET);
	}
	
	@Override
	public String getRequestUrl(String api) {
		return DOUBAN_SERVER_DOMAIN + api;
	}

	public ErrDescrip parseError(int errCode, String errStr){
		if(errStr == null){
			return new ErrDescrip(WeiboAccountColumn.WB_TYPE_DOUBAN,ErrDescrip.ERR_NETWORK, null, null);
		}
		
		Log.i("DoubanService","errorCode:" + errCode + "|" + errStr);
		ErrDescrip error = new ErrDescrip(WeiboAccountColumn.WB_TYPE_DOUBAN);
		error.errCode = errCode;

		error.messageCode = String.valueOf(errCode);
		error.description = getDescription(errCode, errStr);
		
		return error;
	}
	
	public int doLogin(BaseTransListener l){
		resetOauthClient(DOUBAN_APP_KEY, DOUBAN_APP_SECRET);
		LoginTransaction t = new LoginTransaction();
		return startTransaction(t, l);
	}
	
	public int doSendBlog(String name, String content, String imagePath,
			BaseTransListener l){
		AsyncTransaction t = BlogTransaction.createSendBlogTransaction(name, content, imagePath);
		return startTransaction(t, l);
	}
	
	private String getDescription(int errcode, String defaultDes){
		switch(errcode){
		case 400: return "请求的地址不存在或者包含不支持的参数";
		case 401: return "未授权";
		case 403: return "被禁止访问";
		case 404: return "请求的资源不存在";
		case 500: return "内部错误";
		default: return Util.isStringEmpty(defaultDes) ? "未知错误" : defaultDes;
		}

	}
}
