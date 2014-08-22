package vopen.db;

import java.util.HashMap;
import java.util.Map;

import common.util.Util;

import vopen.db.DBApi.AccountInfo;

import mblog.base.LoginResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class ManagerWeiboAccount {
	
/*******帐号管理**********/
	
	public interface WeiboAccount 
	{
		static String C_ACCOUNT  	= "account";
		static String C_NAME  		= "name";
//		static String C_PASSWORD 	= "password";  				/*base64 encode*/
		static String C_LAST_LOGIN 	= "last_login";				/*long */
		static String C_TYPE_WB 	= "type_weibo";				/* int */
		static String C_OTHER	 	= "other";					/* int for update*/
		
		static String C_TOKEN 		= "token_weibo";
		static String C_SECRET 		= "secret_weibo";
		//add by ljb 2011-6-29
		static String C_MAINURL		= "main_url";
		static String C_PORTRAIT 	="portrait_url";

	}

	
	public static final class WeiboAccountColumn implements WeiboAccount,BaseColumns
	{
		public WeiboAccountColumn() {
			// TODO Auto-generated constructor stub
		}
		
		public static final String TABLE_NAME = "weibo_account";
		public static final Uri CONTENT_URI = Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/" + TABLE_NAME);
		
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.netease." + TABLE_NAME;
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.netease." + TABLE_NAME;
		

		
		public static final int WB_BOUND_UNBOUND = 0;						/* unbound weibo*/
		public static final int WB_BOUND_BOUND 	= 1;						/* bound weibo*/
		
		

		public static final int WB_TYPE_UNKNOW 		= 0;
		public static final int WB_TYPE_NETEASE 	= 1;							/*Netease weibo*/
		public static final int WB_TYPE_SINA 		= 2;							/*sina weibo*/
		public static final int WB_TYPE_TENCENT 	= 3;							/*Tencent weibo*/

		public static final int WB_TYPE_RENREN	 	= 4;							/*Renren weibo*/
		public static final int WB_TYPE_SOHU 		= 5;							/*Sohu weibo*/
		public static final int WB_TYPE_DOUBAN		= 6;							/*Douban weibo*/
		
		public static final int WB_NUMBER = 4;
		
	}
	

	public static String[] WEIBOACCOUNT_PRO = new String[] { 
		WeiboAccountColumn._ID,				//0
		WeiboAccountColumn.C_ACCOUNT,		//1
		WeiboAccountColumn.C_NAME,			//2	
		WeiboAccountColumn.C_LAST_LOGIN,	//3
		WeiboAccountColumn.C_TYPE_WB, 		//4
		WeiboAccountColumn.C_TOKEN,			//5
		WeiboAccountColumn.C_SECRET,		//6
		WeiboAccountColumn.C_MAINURL,		//7
		WeiboAccountColumn.C_PORTRAIT,		//8
		WeiboAccountColumn.C_OTHER,			//9
		};

	public static final int COLUMNS_ID 			= 0;
	public static final int COLUMNS_ACCOUNT 	= 1;
	public static final int COLUMNS_NAME		= 2;
	public static final int COLUMNS_LAST_LOGIN 	= 3;
	public static final int COLUMNS_TYPE_WB   	= 4;
	public static final int COLUMNS_TOKEN   	= 5;
	public static final int COLUMNS_SECRET   	= 6;
	public static final int COLUMNS_MAINURL		= 7;
	public static final int COLUMNS_PORTRAIT	= 8;
	public static final int COLUMNS_OTHER   	= 9;
	
	public static final String ANONYMOUS_USER = "anonymous";
	
	static public boolean queryWBAccountBind(Context context,
			String accountName, int type) {
		boolean ret = true;
		Cursor cursor = null;

		try {
			if (context == null)
				return ret;

			String where = WeiboAccountColumn.C_TYPE_WB + " = "
					+ type;

			cursor = context.getContentResolver().query(
					WeiboAccountColumn.CONTENT_URI, WEIBOACCOUNT_PRO, where,
					null, null);
			if (cursor == null || !cursor.moveToFirst()) {
				ret = false;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}

		return ret;
	}
	
	/**
	 * 修改OAuth1.0至OAuth2.0
	 * 弃用token_secret,统一填写字符串"null"，作为OAuth2.0的标识
	 * @param context
	 * @param name
	 * @param token
	 */
	public static void changeSinaWBAccountTokenTo2(Context context, String name, String token){
		if(context == null || TextUtils.isEmpty(token))
			return;
		String where =WeiboAccountColumn.C_NAME  +  " = '" + name + "' "  + " AND " + WeiboAccountColumn.C_TYPE_WB + " = " + WeiboAccountColumn.WB_TYPE_SINA;
		Cursor cursor = context.getContentResolver().query(WeiboAccountColumn.CONTENT_URI,WEIBOACCOUNT_PRO, where, null, null);
		boolean ret = true;
		if(cursor == null || !cursor.moveToFirst())
		{
				ret = false;
		}
		if(cursor != null)
			cursor.close();
		
		if(ret)
		{	
			ContentValues cv = new ContentValues();
			cv.put(WeiboAccountColumn.C_TOKEN, token);
			cv.put(WeiboAccountColumn.C_SECRET, "null");
			
			context.getContentResolver().update(WeiboAccountColumn.CONTENT_URI, cv, where, null);
		}
	}
	
	static public void setWBAccountTokenInner(Context context,String accountName,
			String username,int type,String token,String secret, String mainUrl, 
			String portrait, String userid)
	{
		//added by cxl 为了设置当前登录用户的相关信息
		if(context == null || TextUtils.isEmpty(username) || TextUtils.isEmpty(accountName))
			return;
		
		String where = WeiboAccountColumn.C_ACCOUNT  +  " = '" + accountName + "' "  + " AND " + 
				WeiboAccountColumn.C_NAME  +  " = '" + username + "' "  + " AND " + WeiboAccountColumn.C_TYPE_WB + " = " + type;
		
		Cursor cursor = context.getContentResolver().query(WeiboAccountColumn.CONTENT_URI,WEIBOACCOUNT_PRO, where, null, null);
		boolean ret = true;
		if(cursor == null || !cursor.moveToFirst())
		{
				ret = false;
		}
		if(cursor != null)
			cursor.close();
		
		if(ret)
		{	
			ContentValues cv = new ContentValues();
			cv.put(WeiboAccountColumn.C_TOKEN, token);
			cv.put(WeiboAccountColumn.C_SECRET, secret);
			cv.put(WeiboAccountColumn.C_MAINURL, mainUrl);
			cv.put(WeiboAccountColumn.C_PORTRAIT, portrait);
			cv.put(WeiboAccountColumn.C_ACCOUNT, userid);
			context.getContentResolver().update(WeiboAccountColumn.CONTENT_URI, cv, where, null);
		} else {
			ContentValues cv = new ContentValues();
			cv.put(WeiboAccountColumn.C_TOKEN, token);
			cv.put(WeiboAccountColumn.C_SECRET, secret);
		
			cv.put(WeiboAccountColumn.C_ACCOUNT,accountName);
			cv.put(WeiboAccountColumn.C_NAME,username);
			cv.put(WeiboAccountColumn.C_TYPE_WB, type);
			cv.put(WeiboAccountColumn.C_MAINURL, mainUrl);
			cv.put(WeiboAccountColumn.C_PORTRAIT, portrait);
			cv.put(WeiboAccountColumn.C_ACCOUNT, userid);
			context.getContentResolver().insert(WeiboAccountColumn.CONTENT_URI, cv);
		}
	}
	
	public static LoginResult getWBAccount(Context context, String accountName, int type) {
		String where = WeiboAccountColumn.C_TYPE_WB + " = ?";
		
		Cursor cursor = context.getContentResolver().query(WeiboAccountColumn.CONTENT_URI, 
				WEIBOACCOUNT_PRO, where, new String[] {String.valueOf(type)}, null);
		
		try {
			if (cursor != null && cursor.moveToFirst()) {
				LoginResult result = new LoginResult(type);
				result.setAccessToken(cursor.getString(COLUMNS_TOKEN));
				result.setTokenSecret(cursor.getString(COLUMNS_SECRET));
				result.setId(cursor.getString(COLUMNS_ACCOUNT));
				return result;
			}
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
	
	/**
	 *  String[0]  == token
	 *  String[1]  == secret 
	 * @param context
	 * @param accountName
	 * @param username
	 * @param type
	 */
	static public String[] getWBAccountTokenInner(Context context,String accountName,String username,int type)
	{
		//modified by cxl
		if(context == null || TextUtils.isEmpty(username)|| TextUtils.isEmpty(accountName))
			return null;
		
		String where = /*WeiboAccountColumn.C_ACCOUNT  +  " = '" + accountName + "' "  + " AND " + */
			WeiboAccountColumn.C_NAME  +  " = '" + username + "' "  + " AND " + WeiboAccountColumn.C_TYPE_WB + " = " + type;
		
		Cursor cursor = context.getContentResolver().query(WeiboAccountColumn.CONTENT_URI, WEIBOACCOUNT_PRO, where, null, null);
		String[] retString = new String[3];
		if(cursor != null && cursor.moveToFirst())
		{
			retString[0] = cursor.getString(COLUMNS_TOKEN);
			retString[1] = cursor.getString(COLUMNS_SECRET);
			retString[2] = cursor.getString(COLUMNS_ACCOUNT);
		}
		if(cursor != null)
			cursor.close();
		
		return retString;
	}

	public static Cursor getAccountBoundInner(Context context, String accountName)
	{
		//added by cxl 为了获得当前登录用户的相关信息
		if(context == null || TextUtils.isEmpty(accountName))
			return null;
		
		String where = /*WeiboAccountColumn.C_ACCOUNT  +  " = '" + accountName + "'"*/ null;
		return context.getContentResolver().query(WeiboAccountColumn.CONTENT_URI, WEIBOACCOUNT_PRO, where, null, WeiboAccount.C_TYPE_WB+" ASC");
	}
	
	public static void bindAccountWithWBInner(Context context,String accountName,String name,int wbType)
	{
		if(TextUtils.isEmpty(name) || context == null)
			return;
		
		if(wbType != WeiboAccountColumn.WB_TYPE_SINA && wbType != WeiboAccountColumn.WB_TYPE_TENCENT && wbType != WeiboAccountColumn.WB_TYPE_NETEASE)	
		{
			return ;
		}
		
		String where = WeiboAccountColumn.C_NAME + " = '" + name + "'";
		
		ContentValues cv = new ContentValues();
		cv.put(WeiboAccountColumn.C_TYPE_WB, wbType);
		
		context.getContentResolver().update(WeiboAccountColumn.CONTENT_URI, cv, where, null);
		
	}
	
	public static void unbindAccountWithWBInner(Context context,String accountName,String name)
	{
		if(TextUtils.isEmpty(name) || context == null)
			return;
		
		String where = WeiboAccountColumn.C_NAME + " = '" + name + "'";
		
//		ContentValues cv = new ContentValues();
//		cv.put(WeiboAccountColumn.C_TYPE_WB, WeiboAccountColumn.WB_TYPE_UNKNOW);
		
		context.getContentResolver().delete(WeiboAccountColumn.CONTENT_URI,  where, null);
		
	}
	
	static public Map<String,String> getBindAccountInfoInner(Context context,String accountName)
	{
		if(context == null)
			return null;
		
		Map<String,String> mapAccount = new HashMap<String, String>();
		
		
		Cursor cursor = context.getContentResolver().query(
				WeiboAccountColumn.CONTENT_URI,WEIBOACCOUNT_PRO, null, null, null);
		
		if(cursor != null && cursor.moveToFirst())
		{
			do
			{
				mapAccount.put(cursor.getString(COLUMNS_ACCOUNT), cursor.getString(COLUMNS_ID));
			}
			while(cursor.moveToNext());
			
		}
		
		if(cursor != null)
			cursor.close();
		
		return mapAccount;
		
	}
	

	public static int getbindAccountNumberInner(Context context,String accountName)
	{
		if(context == null)
			return 0;
		
		Cursor cursor = context.getContentResolver().query(WeiboAccountColumn.CONTENT_URI, WEIBOACCOUNT_PRO, null, null, null);
		int ret = 0;
		if(cursor != null)
		{
			ret =cursor.getCount();
			cursor.close();
		}
		return ret;
		
	}
	
	
	/****************************/
	
	
	static public void setWBAccountToken(Context context,String username,int type,
			String token,String secret, String mainPageUrl, String portrait, String userid)
	{
		setWBAccountTokenInner(context, getCurrAccountName(context), 
				username, type, token, secret, mainPageUrl, portrait, userid);
	}
	
	
	/**
	 *  String[0]  == token
	 *  String[1]  == secret 
	 * @param context
	 * @param accountName
	 * @param username
	 * @param type
	 */
	static public String[] getWBAccountToken(Context context,String username,int type)
	{
		return getWBAccountTokenInner(context, getCurrAccountName(context), username, type);
	}
	

	public static Cursor getAccountBound(Context context)
	{
			return getAccountBoundInner(context, getCurrAccountName(context));
	}
	
	public static void unbindAccountWithWB(Context context,int wbType)
	{
		if(context == null)
			return;
		
		//modified by cxl
//		String accountName = getCurrAccountName(context);
//		String where = WeiboAccountColumn.C_ACCOUNT  +  " = '" + accountName + "' "  + " AND " + WeiboAccountColumn.C_TYPE_WB + " = " + wbType;
		String where = WeiboAccountColumn.C_TYPE_WB + " = " + wbType;
		
		context.getContentResolver().delete(WeiboAccountColumn.CONTENT_URI, where, null);
	}
	
	public static void unbindAccountWithWB(Context context,String name)
	{
		unbindAccountWithWBInner(context, getCurrAccountName(context), name);
	}
	
	static public Map<String,String> getBindAccountInfo(Context context)
	{
		return getBindAccountInfoInner(context, getCurrAccountName(context));
	}
	

	public static int getbindAccountNumber(Context context)
	{
		return getbindAccountNumberInner(context, getCurrAccountName(context));
	}
	
	public static String getCurrAccountName(Context context) {
//		String accName = null;
//		AccountInfo accInfo = DBUtils.getLoginAccount(context);
//		if(null !=  accInfo) {
//			accName = accInfo.mUser_account;
//		}
//		if(Util.isStringEmpty(accName)){
//			accName = ANONYMOUS_USER;
//		}
		return ANONYMOUS_USER;
		
	}

}
