package mblog.Qq;

import mblog.base.BaseService;
import mblog.base.BaseTransListener;
import mblog.base.ErrDescrip;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.util.Log;

import common.framework.task.AsyncTransaction;

public class QqService extends BaseService{
	//腾讯微博
	public static final String QQ_APP_KEY = "801125947";
	public static final String QQ_APP_SECRET = "0cf77f62b7b2288ac72bb38045d3a6b9";
	
	private static final String QQ_SERVER_DOMAIN = "https://open.t.qq.com";
	
	private static QqService s_Instance;
	public static QqService getInstance(){
		if(s_Instance == null)
			s_Instance = new QqService();
		
		return s_Instance;
	}
	public QqService() {
		super(QQ_APP_KEY, QQ_APP_SECRET);
	}
	
	@Override
	public String getRequestUrl(String api) {
		return QQ_SERVER_DOMAIN + api;
	}

	public ErrDescrip parseError(int errCode, String errStr){
		//TODO: errStr 可能为null
		if(errStr == null){
			return new ErrDescrip(WeiboAccountColumn.WB_TYPE_TENCENT, ErrDescrip.ERR_NETWORK, null, null);
		}
		Log.i("QqService", errStr);
		ErrDescrip error = new ErrDescrip(WeiboAccountColumn.WB_TYPE_TENCENT);
		error.errCode = errCode;
		try {
			JSONObject job = new JSONObject(errStr);
			int ret = job.getInt("ret");
			int errcode = job.getInt("errcode");
			
			error.messageCode = String.valueOf(errcode);
			error.description = getDescription(ret, errcode);
	
		} catch (JSONException e) {
			error.errCode = ErrDescrip.ERR_PARSE;
			error.description = ErrDescrip.getDescriptionByCode(ErrDescrip.ERR_PARSE);
			e.printStackTrace();
		}
		
		return error;
	}
	
	public int doLogin(BaseTransListener l){
		resetOauthClient(QQ_APP_KEY, QQ_APP_SECRET);
		LoginTransaction2 t = new LoginTransaction2();
		return startTransaction(t, l);
	}
	
	public int doSendBlog(String name, String content, String imagePath,
			String latitude, String longitude,
			BaseTransListener l){
		AsyncTransaction t = BlogTransaction.createSendBlogTransaction(name, content, imagePath, latitude, longitude);
		return startTransaction(t, l);
	}
	
	private String getDescription(int ret, int errcode){
		if(1 == ret){
			return "参数错误";
		}
		else if(2 == ret){
			return "频率受限";
		}
		else if (3 == ret) {
			switch (errcode) {
			case 1:
				return "无效TOKEN,被吊销";
			case 2:
				return "请求重放";
			case 3:
				return "access_token不存在";
			case 4:
				return "access_token超时";
			case 5:
				return "oauth 版本不对";
			case 6:
				return "oauth 签名方法不对";
			case 7:
				return "参数错";
			case 9:
				return "验证签名失败";
			case 10:
				return "网络错误";
			case 11:
				return "参数长度不对";
			default:
				return "处理失败";
			}
		}
		else if(4 == ret){
			switch(errcode){
			case 4: return "脏话太多";
			case 5: return "禁止访问";
			case 6: return "该记录不存在";
			case 8: return "内容超过最大长度";
			case 9: return "包含垃圾信息";
			case 10: return "发表太快，被频率限制";
			case 11: return "源消息已删除";
			case 12: return "源消息审核中";
			case 13: return "重复发表";
			}
		}
		
		return "未知错误";
	}
}
