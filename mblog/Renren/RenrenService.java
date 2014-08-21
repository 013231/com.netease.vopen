package mblog.Renren;

import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

import mblog.base.BaseService;
import mblog.base.BaseTransListener;
import mblog.base.ErrDescrip;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import android.os.Bundle;
import android.util.Log;

import common.framework.task.AsyncTransaction;
import common.util.Util;
import common.util.oauth.OAuthNameValuePair;

public class RenrenService extends BaseService{
	//人人
	public static final String RENREN_CLIENT_ID = "193350";
	public static final String RENREN_APP_KEY = "0ef14f9945d743b0bdc7f42969b74555";
	public static final String RENREN_APP_SECRET = "eac4780acd2b4ec9b5a974956b882afa";
	
	private static final String RENREN_SERVER_DOMAIN = "http://api.renren.com/restserver.do";
	private static final String RENREN_SERVER_DOMAIN_SSL = "https://graph.renren.com";
	
	private static RenrenService s_Instance;
	public static RenrenService getInstance(){
		if(s_Instance == null)
			s_Instance = new RenrenService();
		
		return s_Instance;
	}
	public RenrenService() {
		super(RENREN_APP_KEY, RENREN_APP_SECRET);
	}
	
	@Override
	public String getRequestUrl(String api) {
		return RENREN_SERVER_DOMAIN + api;
	}

	//...],"error_code":10231,"error_msg":"url是必须参数"}
	public ErrDescrip parseError(String errStr){
		if(errStr == null){
			return new ErrDescrip(WeiboAccountColumn.WB_TYPE_RENREN, ErrDescrip.ERR_NETWORK, null, null);
		}
		
		Log.i("RenrenService", errStr);
		ErrDescrip error = new ErrDescrip(WeiboAccountColumn.WB_TYPE_RENREN);
		try {
			JSONObject job = new JSONObject(errStr);
			error.errCode = job.optInt("error_code");
			error.description = job.optString("error_msg");
			error.messageCode = String.valueOf(error.errCode);
			error.description = getDescription(error.errCode, error.messageCode);
		} catch (JSONException e) {
			error.errCode = ErrDescrip.ERR_PARSE;
			error.description = ErrDescrip.getDescriptionByCode(ErrDescrip.ERR_PARSE);
			e.printStackTrace();
		}
		
		return error;
	}
	
	public int doLogin(BaseTransListener l){
		resetOauthClient(RENREN_APP_KEY, RENREN_APP_SECRET);
		LoginTransaction t = new LoginTransaction();
		return startTransaction(t, l);
	}
	
	/**
	 * 
	 * @param type
	 * @param name
	 * @param rr_name 必填,新鲜事标题 注意：最多30个字符
	 * @param rr_description 必填,新鲜事主体内容 注意：最多200个字符。
	 * @param rr_url 必填,新鲜事标题和图片指向的链接。
	 * @param rr_image 可选, 新鲜事图片地址
	 * @param rr_caption 可选, 新鲜事副标题 注意：最多20个字符
	 * @param rr_action_name 可选, 新鲜事动作模块文案。 注意：最多10个字符
	 * @param rr_action_link 可选, 新鲜事动作模块链接。
	 * @param rr_message 可选, 用户输入的自定义内容。注意：最多200个字符。
	 * @param listener
	 * @return
	 */
	public int doSendBlog(int type,
			String name,
			String rr_name, String rr_description, 
			String rr_url, String rr_image, String rr_caption, String rr_action_name,
			String rr_action_link, String rr_message,
			BaseTransListener listener){
		AsyncTransaction tx = BlogTransaction.createSendRenRenBlogTransaction(type, name, 
				rr_name, rr_description, rr_url, 
				rr_image, rr_caption, rr_action_name, rr_action_link, rr_message);
		
		return startTransaction(tx, listener);
	}

	
	public static String encodeUrl(Bundle parameters) {
        if (parameters == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : parameters.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            //sb.append(key + "=" + URLEncoder.encode(parameters.getString(key)));
            sb.append(key + "=" + parameters.getString(key));
        }
        return sb.toString();
    }
	
	public void prepareParams(List<OAuthNameValuePair> list, String method) {
        list.add(new OAuthNameValuePair("access_token", URLDecoder.decode(getOauthClient().mTokenKey)));
        list.add(new OAuthNameValuePair("method", method));
        list.add(new OAuthNameValuePair("v", "1.0"));
        list.add(new OAuthNameValuePair("call_id", String.valueOf(System.currentTimeMillis())));
        list.add(new OAuthNameValuePair("format", "JSON"));
//        list.add(new BasicNameValuePair("xn_ss", "1");

        StringBuffer sb = new StringBuffer();
        Collections.sort(list);
        for (NameValuePair kv : list) {
			sb.append(kv.getName());
			sb.append("=");
			sb.append(kv.getValue());
		}
        sb.append(RENREN_APP_SECRET);
        Log.i("prepareParams", sb.toString());
        
        String sig = md5(sb.toString());
        Log.i("prepareParams", sig);
        list.add(new OAuthNameValuePair("sig", sig));
    }
	
	public static String md5(String string) {
        if (string == null || string.trim().length() < 1) {
            return null;
        }
        try {
            return getMD5(string.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
	
	private static String getMD5(byte[] source) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            StringBuffer result = new StringBuffer();
            for (byte b : md5.digest(source)) {
                result.append(Integer.toHexString((b & 0xf0) >>> 4));
                result.append(Integer.toHexString(b & 0x0f));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
	
	private String getDescription(int errcode, String defaultDes){
		switch(errcode){
		case 1: return "抱歉,服务临时不可用";
		case 2: return "调用数据服务出现异常，请稍候再试";
		case 3: return "请求未知方法";
		case 4: return "应用已达到设定的请求上限";
		case 5: return "请求来自未经授权的IP地址";
		case 8: return "服务器繁忙，请稍后再试";
		case 100: return "请求包含无效参数";
		case 101: return "提交的api_key不属于任何已知的应用";
		case 103: return "必须是POST提交";
		case 104: return "sig认证失败";
		case 107: return "无效的文本内容";
		case 110: return "所操作资源已经不存在";
		case 200: return "没有权限进行操作";
		case 450: return "当前用户的sessionKey过期";
		case 452: return "Session key 无效";
		case 453: return "调用此方法时，session key 是一个必须的参数";
		case 2000: return "没有传入access_token参数";
		case 2001: return "access_token无效";
		case 2002: return "access_token过期";
		case 10200: return "当前请求缺少必需参数";
		case 10300: return "日志所有者不存在";
		case 10301: return "日志所有者的用户ID是必须的参数";
		case 10302: return "日志的ID是必须参数";
		case 10303: return "你没有权限阅读此篇日志";
		case 10304: return "发表的日志可能含有非法信息";
		case 10305: return "日志标题和内容不能为空或不能过长";
		case 10306: return "删除日志时发生未知异常";
		case 10307: return "日志评论的ID是必须的参数";
		case 10308: return "删除日志评论时发生未知异常";
		case 10309: return "评论超过了规定的次数";
		case 10310: return "日志标题为必须参数";
		case 10311: return "日志正文为必须参数";
		case 10312: return "密码错误";
		case 10313: return "此日志不允许分享";
		case 10400: return "状态更新过于频繁";
		case 10401: return "状态字数超过限定长度";
		case 10402: return "状态的内容含有非法字符";
		case 10500: return "照片所有者的用户ID是必须的参数";
		case 10501: return "照片所有者不存在";
		case 10502: return "照片未知异常";
		case 10503: return "相册ID是必须的参数";
		case 10504: return "相册不存在";
		case 10505: return "相册所有者不存在";
		case 10506: return "你没有权限查看此相册";
		case 10507: return "照片的ID是必须的参数";
		case 10508: return "照片不存在";
		case 10509: return "你没有权限评论此照片";
		case 10510: return "你发表的评论含有违禁信息";
		case 10511: return "无效的照片格式,照片的宽和高不能小于50,照片的宽与高的比不能小于1:3";
		case 10512: return "相册空间不足";
		case 10513: return "盗用的照片";
		case 10514: return "相册名为必须的参数";
		case 10516: return "密码错误";
		case 10517: return "此相册或照片不允许分享";
		case 10518: return "删除失败，可能是相册或照片不存在，或者是不允许删除";
		case 10600: return "此接口的调用超过了限定的次数";
		case 10601: return "Feed标题模板是无效的";
		case 10602: return "显示内容应该在100个字符之内";
		case 10604: return "title_data 参数不是一个有效的JSON 格式数组";
		case 10606: return "只能包含case a>或者case xn:name>标签";
		case 10607: return "内容部分是可选的，内容部分的最终显示的字符数应该控制在200个以内";
		case 10609: return "传入title_data或者Feed标题模板中含有非法标签";
		case 10610: return "传入内容中含有非法标签";
		case 10615: return "Feed内容模板中定义的变量和JSON数组中的定义不匹配";
		case 10616: return " Feed标题模板中定义的变量和title_data JSON数组中的定义不匹配";
		case 10617: return "根据传入的模板号，没有找到对应的模板";
		case 10618: return "无效的URL";
		case 10619: return "参数不是一个有效的JSON 格式数组";
		case 10621: return "feed_Token过期";
		case 10700: return "XNML片段格式不符合要求，传入文本信息错误";
		case 10701: return "to_ids 参数的格式不符合要求";
		case 10702: return "发送者已超过当天发送配额，此接口调用过于频繁，请明天再试";
		case 10703: return " AppToUser已超过发送配额";
		case 10704: return "通知发送过快";
		case 10709: return "根据传入的模板号，没有找到对应的模板";
		case 10711: return "email内容中只能包含case a>或者case xn:name>标签";
		case 10712: return "email标题只能是文本，不能包括其它标签";
		case 10713: return "email模板中的xnml语法有问题";
		case 10714: return "body_data参数不是一个有效的JSON 格式数组";
		case 10716: return "接收者当天接收的通知量超过配额";
		case 10800: return "传递订单号已失效，无法获取到token";
		case 10801: return " 无效的订单号 (小于零)";
		case 10802: return "消费金额无效:不支持大笔消费金额>1000或者小于零";
		case 10803: return "人人网支付平台应用资料审核未通过，没有使用平台的资格";
		case 10804: return "该订单不存在";
		case 10900: return "站内信不存在";
		case 10901: return "站内信的发送超过了限制";
		case 20000: return "可能有违禁信息";
		case 20200: return "需要用户授权";
		case 20201: return "需要用户授予status_update权限";
		case 20202: return "需要用户授予email权限";
		case 20203: return "需要用户授予publish_stream权限";
		case 20204: return "需要用户授予read_stream权限";
		case 20205: return "需要用户授予photo_upload权限";
		case 20207: return "需要用户授予offline_access权限";
		case 20213: return "需要用户授予feed_publish权限";
		case 20300: return "不是已开放的公共主页";
		case 20301: return "该用户不是此公共主页的管理员";
		case 20302: return "该公共主页不存在";
		case 20303: return "该公共主页关闭了留言或评论";
		case 20304: return "只有粉丝才能留言或评论";
		case 20305: return "你没有权限留言，请联系管理员";
		case 20306: return "管理员只能回复，不能留言";
		case 20307: return "此接口不允许公共主页调用";
		case 20308: return "操作太过频繁";
		case 1001: return "输入的帐号或者密码不对";
		case 1002: return "当前登录的ip地址处于封禁状态";
		case 1003: return "用户不存在";
		case 1004: return "帐号处于未激活的状态";
		case 1005: return "帐号处于被封禁的状态";
		case 1006: return "帐号处于注销的状态";
		case 1007: return "登录服务暂时不可用，请重试";
		case 1008: return "请检查传入的api_key参数是否正确";
		case 1009: return "提交必须是Post方式";
		case 1010: return "帐号存在安全问题";
		case 1011: return "用户输入不合法";
		case 1012: return "产品处于测试阶段，请耐心等待";
		case 1013: return "您的账号存在异常行为";

		default: return Util.isStringEmpty(defaultDes) ? "未知错误" : defaultDes;
		}

	}
}
