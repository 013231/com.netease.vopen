package vopen.transactions;

import vopen.protocol.VopenServiceCode;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import common.framework.task.AsyncTransaction;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;
import common.util.Util;

public abstract class BaseTransaction extends AsyncTransaction {

	protected BaseTransaction(TransactionEngine transMgr, int type) {
		super(transMgr, type);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 登陆
	 */
	@Deprecated
	public static final int TRANSACTION_TYPE_LOGIN = 0x0 << 8 | 0x01;
	
	/**
	 * 注销
	 */
	public static final int TRANSACTION_TYPE_LOGOUT = 0x0 << 8 | 0x02;
	
	/**
	 * 获取视频信息列表
	 */
	public static final int TRANSACTION_TYPE_GET_VIDEO_LIST = 0x0 << 8 | 0x03;
	
	/**
	 * 获取课程详情
	 */
	public static final int TRANSACTION_TYPE_GET_VIDEO_DETAIL = 0x0 << 8 | 0x04;
	
	/**
	 * 获取课程详情中的广告信息
	 */
	public static final int TRANSACTION_TYPE_GET_VIDEO_AD = 0x0 << 8 | 0x05;
	
	/**
	 * 取得版本信息
	 */
	public static final int TRANSACTION_TYPE_GET_VERSION_INFO = 0x0 << 8 | 0x06;
	
	/**
	 * 注册
	 */
	public static final int TRANSACTION_TYPE_REGISTER = 0x0 << 8 | 0x07;
	
	/**
	 * 同步收藏
	 */
	@Deprecated
	public static final int TRANSACTION_TYPE_SYNC_FAVORITE = 0x0 <<8 | 0x08;
	
	/**
	 * 反馈
	 */
	public static final int TRANSACTION_TYPE_FEED_BACK = 0x0 << 8 | 0x09;
	
	/**
	 * 获取关于信息
	 */
	public static final int TRANSACTION_TYPE_GET_ABOUT_INFO = 0x0 << 8 | 0x0A;
	/**
	 * UI事件通知及数据传递
	 */
	public static final int TRANSACTION_TYPE_UI_EVENT = 0x0 << 8 | 0x0B;
	
	/**
	 * 添加收藏
	 */
	@Deprecated
	public static final int TRANSACTION_TYPE_ADD_STORE = 0x0 << 8 | 0x0C;
	
	/**
	 * 删除收藏
	 */
	@Deprecated
	public static final int TRANSACTION_TYPE_DEL_STORE = 0x0 << 8 | 0x0D;
	
	/**
	 * 获取推送课程
	 */
	public static final int TRANSACTION_TYPE_GET_PUSH_COURSE = 0x0 << 8 | 0x0E;
	
	/**
	 * 获取推送课程
	 */
	public static final int TRANSACTION_TYPE_SYNC_TRANSLATE_NUM = 0x0 << 8 | 0x0F;
	
	/**
	 * 获取热门跟帖
	 */
	public static final int TRANSACTION_GET_HOT_COMMENT = 0x0 << 8 | 0x10;
	
	/**
	 * 获取最新跟帖
	 */
	public static final int TRANSACTION_GET_LATEST_COMMENT = 0x0 << 8 | 0x11;
	
	/**
	 * 获取全部跟帖
	 */
	public static final int TRANSACTION_GET_WHOLE_COMMENT = 0x0 << 8 | 0x12;
	
	/**
	 * 跟帖
	 */
	public static final int TRANSACTION_POST_COMMENT = 0x0 << 8 | 0x13;
	
	/**
	 * 顶
	 */
	public static final int TRANSACTION_VOTE_COMMENT = 0x0 << 8 | 0x14;
	/**
	 * 获取评论图片短连
	 */
	public static final int TRANSACTION_GET_COMMENT_PIC_SHORT_URL = 0x0 << 8 | 0x15;
	/**
	 * 获取评论短链
	 */
	public static final int TRANSACTION_GET_COMMENT_SHORT_URL = 0x0 << 8 | 0x16;
	
	/**
	 * 反馈：反馈日志
	 */
	public static final int TRANSACTION_TYPE_POSTLOG = 0x0 << 8 | 0x17;
	
	/**
	 * 反馈：反馈意见
	 */
	public static final int TRANSACTION_TYPE_FEEDBACK = 0x0 << 8 | 0x18;
	/**
	 * 获取推荐APP
	 */
	public static final int TRANSACTION_GET_RECOMM_APP = 0x0 << 8 | 0x19;
	
	/**
	 * 获取文章跟帖数
	 */
	public static final int TRANSACTION_GET_COMMENT_COUNT = 0x0 << 8 | 0x20;
	
	/**
	 * 获取搜索热词
	 */
	public static final int TRANSACTION_GET_HOT_WORDS = 0x0 << 8 | 0x21;
	
	/**
	 * 获取个性化推荐内容
	 */
	public static final int TRANSACTION_GET_RECOMMENDS = 0x0 << 8 | 0x22;
	
	/**
	 * 发送搜索反馈
	 */
	public static final int TRANSACTION_SEND_SEARCH_FEEDBACK = 0x0 << 8 | 0x23;
	
	/**
	 * 查看课程详情反馈 
	 */
	public static final int TRANSACTION_SEND_VIEW_DETAIL_FEEDBACK = 0x0 << 8 | 0x24;
	
	/**
	 * 下载课程反馈 
	 */
	public static final int TRANSACTION_SEND_DOWNLOAD_FEEDBACK = 0x0 << 8 | 0x25;	
	
	/**
	 * 获取绑定用户的信息
	 */
	public static final int TRANSACTION_GET_USER_BIND_INFO = 0x0 << 8 | 0x26;
	
	/**
	 * 获取头图上的推广信息
	 */
	public static final int TRANSACTION_GET_HEAD_ADS = 0x0 << 8 | 0x27;
	
	/**
	 * 获取主页的所有推荐
	 */
	public static final int TRANSACTION_GET_HOME_RECOMMEDN_INFO = 0x0 << 8 | 0x28;
	
	/**
	 * 获取用户提交urs-token的公钥
	 */
//	public static final int TRANSACTION_GET_PUBLIC_KEY = 0x0 << 8 | 0x29;
	
	/**
	 * 获取mob-token
	 */
	public static final int TRANSACTION_GET_MOB_TOKEN = 0x0 << 8 | 0x30; 
	
	/**
	 * 登录
	 */
	public static final int TRANSACTION_TYPE_LOGIN2 = 0x0 << 8 | 0x31;
	
	/**
	 * 同步收藏(新接口)
	 */
	public static final int TRANSACTION_TYPE_SYNC_FAVORITE2 = 0x0 <<8 | 0x32;
	
	/**
	 * 添加收藏(新接口)
	 */
	public static final int TRANSACTION_TYPE_ADD_STORE2 = 0x0 << 8 | 0x33;
	
	/**
	 * 删除收藏(新接口)
	 */
	public static final int TRANSACTION_TYPE_DEL_STORE2 = 0x0 << 8 | 0x34;
	
	protected int getHttpErrorCode(int code,int type) {

		switch (code) {	
		case 403:
		case 404:
		case 500:
		case 507:
		case 508:
		case 509:
			return VopenServiceCode.SEVER_ERR;
			
		default:
			return VopenServiceCode.TRANSACTION_FAIL;
		}
	}
	public void onResponseError(int errCode, Object err) {
		// TODO Auto-generated method stub
		if(errCode < 1000 && errCode >= 0) {
			errCode = getHttpErrorCode(errCode, getType());
		}

		notifyError(errCode, err);
	}
//	public void onResponseSuccess(byte[] response,String charset) {
//		// TODO Auto-generated method stub
//		try {
//			
//			PalLog.i("BaseTransaction onResponseSuccess", "get charset:"+charset);
//
//			String string = new String(response, charset);
//
//			onResponseSuccess(string);
//			string = null;
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//			notifyError(VopenServiceCode.ERR_DATA_PARSE, null);
//			return ;
//		} catch(Exception e) {
//			e.printStackTrace();
//			notifyError(VopenServiceCode.ERR_DATA_PARSE, null);
//		}
//		
//	}
	
	public abstract void onResponseSuccess(String response, NameValuePair[] pairs);
	
	
	
	
	public void onTransactException(Exception e) 
	{
		onResponseError(VopenServiceCode.TRANSACTION_FAIL, null);
	}
	
	protected void notifyMessage(int msgCode,Object arg3)
	{
		notifyMessage(msgCode, mType, getId(), arg3);
	}

	protected void notifyError(int msgCode,Object arg3)
	{
		notifyError(msgCode, mType, getId(), arg3);
	}
	public SharedPreferences set_prefs;
	public String account = "";
	
	public boolean isLogin(Context context) {
		refreshAccount(context);
		if (!Util.isStringEmpty(account)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void refreshAccount(Context context) {
		set_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		account = set_prefs.getString("account", "");
	}
}
