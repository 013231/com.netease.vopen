package vopen.protocol;

public class VopenServiceCode {

		
    public static final int SERVICE_ERR_CODE = 0x100000; 
	public static final int SERVICE_MSG_CODE = 0x200000;
	public static final int SERVICE_HTTP_CODE = 0x300000;
	
	
	/**
	 *	公共消息值定义
	 * 
	 */
	public static final int TRANSACTION_SUCCESS = SERVICE_MSG_CODE + 0x001;
	// 事件执行失败
	public static final int TRANSACTION_FAIL 	= SERVICE_ERR_CODE + 0x002;
	// 如果oauth认证失败返回401
	public static final int ERR_AUTH_FAIL 		= SERVICE_ERR_CODE + 0x003;
	// 根据id没有找到对应微博则返回404状态。
	public static final int ERR_BLOG_NOT_FOUND 	= SERVICE_ERR_CODE + 0x004;
	// 指定用户不存在 返回404状态.
	public static final int ERR_USER_NOT_FOUND 	= SERVICE_ERR_CODE + 0x005;
	// 服务端内部错误 返回500错误.
	public static final int ERR_INTERNAL_ERROR	= SERVICE_ERR_CODE + 0x006;
	// 数据解析出错
	public static final int ERR_DATA_PARSE	= SERVICE_ERR_CODE + 0x007;
	// GZIP压缩或解压缩错误
	public static final int ERR_GZIP_EXCEPTION = SERVICE_ERR_CODE + 0x008;
	
	//case 404:
	//case 500:
	//case 507:
	//case 508:
	//case 509:
	public static final int SEVER_ERR = SERVICE_ERR_CODE + 0x009;
	
	//510:非法字符
	public static final int ILLEGAL_KEYWORD_EXCEPTION = SERVICE_ERR_CODE + 0x00a;
	
	//503:服务器正在维护
	public static final int SEVER_BEING_MAINTAIN = SERVICE_ERR_CODE + 0x00b;
	
	
	
	/**
	 * Net 网络错误值.
	 */
	// 网络安全异常错误
	public static final int ERR_NETWORK_SECURITY = SERVICE_HTTP_CODE + 1;
	// 网络无法链接
	public static final int ERR_NETWORK_DONT_CONNECT = SERVICE_HTTP_CODE + 2;
	// 其它网络错误
	public static final int ERR_NETWORK_OTHER = SERVICE_HTTP_CODE + 3;
	// 网络请求取消
	public static final int ERR_NETWORK_CANCEL = SERVICE_HTTP_CODE + 4;
	
	
	/**
	 * token 过期
	 */
	public static final int TOKEN_ERR = SERVICE_HTTP_CODE + 5;
	
	/**
	 * 登陆
	 */
	public static final int LOGIN_FAIL = SERVICE_ERR_CODE + 0x11000;
	//401:URL语法错误或参数值非法
	public static final int GRAMMAR_PARAM_ERR = LOGIN_FAIL + 0x001;
	//412:登陆次数超过规定次数，这里为6次
	public static final int LOGIN_EXCEED = LOGIN_FAIL + 0x002;
	//420:没有该用户
	public static final int USERID_NOT_EXIST = LOGIN_FAIL + 0x003;
	//422:账号锁定
	public static final int USERID_LOCKED = LOGIN_FAIL + 0x004;
	//460:口令错误
	public static final int PASSWORD_ERR = LOGIN_FAIL + 0x005;

	
	/**
	 * 获取视频列表
	 */
	public static final int GET_VIDEO_NO_DATA = SERVICE_ERR_CODE + 0x41000;//没有本地数据
	
	/**
	 * 获取视频详情
	 */
	public static final int GET_VIDEO_DETAIL_FAIL = SERVICE_ERR_CODE + 0x10100;
	public static final int NO_DETAIL_WEB_AND_LOCAL = GET_VIDEO_DETAIL_FAIL + 0x001;//获取视频详情失败，即无网络数据也无本地数据
	/**
	 * 获取视频版本信息
	 */
	public static final int DONOT_NEED_UPDATE = SERVICE_MSG_CODE + 0x20100;//不需要更新
	
	/**
	 * 注册
	 */
	public static final int REGISTER_ERR = SERVICE_ERR_CODE + 0x12000;
	public static final int REGISTER_ERR_UNACTIVIENET = REGISTER_ERR + 0x001;// 网络异常
	public static final int REGISTER_ERR_USERNAMENULL = REGISTER_ERR + 0x002;// 用户名为空
	public static final int REGISTER_ERR_INVALIDUSERNAME = REGISTER_ERR + 0x003;// 用户名不合法
	public static final int REGISTER_ERR_PASSWORDNULL = REGISTER_ERR + 0x004;// 密码为空
	public static final int REGISTER_ERR_INVALIDPASSWORD = REGISTER_ERR + 0x005;// 密码不合法
	public static final int REGISTER_ERR_REGSUCCESS = REGISTER_ERR + 0x006;// 注册成功
	public static final int REGISTER_ERR_USEREXIST = REGISTER_ERR + 0x007;// 用户名已存在
	public static final int REGISTER_ERR_REGFAIL = REGISTER_ERR + 0x008;// 注册失败
	public static final int REGISTER_ERR_REGACCOUNTINVAILD = REGISTER_ERR + 0x009;// 用户名不合法（注册服务器验证）
	/**
	 * 同步
	 */
	public static final int SYNC_FAVORITE_ERR = SERVICE_ERR_CODE + 0x13000;
	public static final int SYNC_FAVORITE_ERR_INNER = SYNC_FAVORITE_ERR + 0x001;//内部错误
	/**
	 * 添加收藏
	 */
	public static final int ADD_STORE_ERR = SERVICE_ERR_CODE + 0x14000;
	/**
	 * 删除收藏
	 */
	public static final int DEL_STORE_ERR = SERVICE_ERR_CODE + 0x15000;
	
	/**
	 * 反馈日志
	 */
	public static final int POST_LOG_ERR = SERVICE_ERR_CODE + 0x16000;
	
	/**
	 * 反馈意见
	 */
	public static final int FEEDBACK_ERR = SERVICE_ERR_CODE + 0x16001;
	
	/**
	 * 需要重新登录
	 */
	public static final int RELOGIN_NEEDED = SERVICE_ERR_CODE + 0x18000;
	
	/**
	 * 服务器返回code=-10000表示mob token过期
	 */
	public static final int ERR_MOB_TOKEN_INVALID = -10000;
	
	/**
	 * 服务器返回code=-11111表示urs token过期
	 */
	public static final int ERR_URS_TOKEN_INVALID = -10001;
}
