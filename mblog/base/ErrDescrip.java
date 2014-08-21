package mblog.base;

import common.util.Util;


public class ErrDescrip {
	public static final int SUCCESS = 0; //成功
	public static final int ERR_PARSE = -1; //解析错误
	public static final int ERR_AUTH_FAIL = -2; //认证错误
	public static final int ERR_UPLOAD_FAIL = -3; //上传图片失败
	public static final int ERR_COMMAND_FAIL = -4; //发送请求失败"
	public static final int ERR_NETWORK = -5; //网络错误"
	public static final int ERR_UNKNOWN = -1000; //未知错误
	
	public static String getDescriptionByCode(int code){
		switch (code) {
		case SUCCESS:
			return "成功";
		case ERR_PARSE:
			return "解析错误";
		case ERR_AUTH_FAIL:
			return "认证错误";
		case ERR_UPLOAD_FAIL:
			return "上传图片失败";
		case ERR_COMMAND_FAIL:
			return "发送请求失败";
		case ERR_NETWORK:
			return "网络错误";
		default:
			return "未知错误";
		}
	}
	
	public int mBlogType;
	public int errCode = SUCCESS;
	public String messageCode;
	public String description;
	
	public ErrDescrip(int blogType){
		mBlogType = blogType;
	}
	
	public int getBlogType(){
		return mBlogType;
	}
	
	public ErrDescrip(int blogType, int errCode, String messageCode, String description) {
		this.errCode = errCode;
		this.messageCode = messageCode;
		this.description = description;
		
		if(Util.isStringEmpty(messageCode)){
			this.messageCode = String.valueOf(this.errCode);
		}
		
		if(Util.isStringEmpty(this.description)){
			this.description = getDescriptionByCode(errCode);
		}
	}
}
