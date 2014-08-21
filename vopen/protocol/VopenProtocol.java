package vopen.protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import vopen.response.SyncItemInfo;
import android.content.Context;
import android.util.Log;

import com.netease.vopen.pal.Constants;
import common.framework.http.HttpRequest;
import common.multipart.FilePart;
import common.multipart.MultipartEntity;
import common.multipart.Part;
import common.pal.PalLog;
import common.util.Util;

public class VopenProtocol
{
	//服务器域名
//	private String SERVER_DOMAIN = "http://so.v.163.com";//原北京
	private String SERVER_DOMAIN = "http://mobile.open.163.com";//现杭州
//	private String SERVER_DOMAIN = "http://so.open.163.com";//测试服
	
	private final static String sUserAgent = "NETS_Android";
	private final static int sTimeout = 10 * 1000;
	private final static int PUSH_TIME_OUT = 11 * 60 * 1000;//推送协议的超时时间
	
	//反馈产品ID
	private static String PRODUCT_ID = "17001";
	
	//反馈类型ID
	private static String FEEDBACK_ID = "21001";
	
	/*************************************************
	 * API 
	 */
	//登陆
	private static final String URL_LOGIN = "https://reg.163.com/logins.jsp";
	//注册
	private static final String URL_REGISTER = "http://c.3g.163.com/urs/reg";
	//反馈
	private static final String URL_FEEDBACK = "/movie/store/feedback.do";
	//关于
	private static final String URL_ABOUT = "http://v.163.com/special/open_roster/";
	//版本检查
	private static final String URL_UPDATE_CHECK = Constants.URL_UPDATE_CHECK;
	//视频列表
	private static final String URL_VIDIO_LIST = "/movie/2/getPlaysForAndroid.htm";
	//添加收藏
	private static final String URL_ADDSTORE = "/movie/store/addstore.do";
	//删除收藏
	private static final String URL_DELSTORE = "/movie/store/delstore.do";
	//收藏同步
	private static final String URL_SYNCSTORE = "/movie/store/syncstore.do";
	//获取课程
	private static final String URL_GET_MOVIES = "/movie/%s/getMoviesForAndroid.htm";
	//push信息
	private static final String URL_PUSH_OPENCOURSE = "http://p.3g.163.com/push/opencourse/";
	//推荐APP
	private static final String URL_RECOMM_APP = "http://app.zs.163.com/dma/android/phone/opencourse/app.json";
	
	private static VopenProtocol mInstance;

	VopenProtocol()
	{
	}
	
	

	public static VopenProtocol getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new VopenProtocol();
		}
		return mInstance;
	}

	/**
	 * 
	 * @return
	 */
	public String getSever_Domain() {
		return SERVER_DOMAIN;
	}

	
	/**
	 * 获取完整请求URL
	 * @param url
	 * @return
	 */
	private String getRequestUrl(String url) {
		return SERVER_DOMAIN + url;
	}
	
	/**
	 * 取得视频列表
	 * 
	 * @GET
	 * @return
	 */
	public HttpRequest createGetVListRequest()
	{
		HttpRequest request = new HttpRequest(getRequestUrl(URL_VIDIO_LIST),HttpRequest.METHOD_GET);
		request.addHeaderField(HTTP.USER_AGENT, sUserAgent);
		request.setRequestTimeOut(sTimeout*3);
		
		return request;
	}
	
	
	/**
	 * 取得视频详情
	 * @param plid
	 * @return
	 */
	public HttpRequest createGetVideoDetailRequest(String plid){
		String url = String.format(URL_GET_MOVIES, plid);
	    HttpRequest request = new HttpRequest(getRequestUrl(url));
	    request.addHeaderField(HTTP.USER_AGENT, sUserAgent);
	    request.setRequestTimeOut(sTimeout);
	    
	    return request;
	}
	
	/**
	 * 取得版本信息
	 * @return
	 */
	public HttpRequest createGetVersionInfoRequest(){
		HttpRequest request = new HttpRequest(URL_UPDATE_CHECK);
	    request.addHeaderField(HTTP.USER_AGENT, sUserAgent);
        request.setRequestTimeOut(sTimeout);
        
        return request;
	}
	
	/**
	 * 登录协议
	 * @return
	 */
	public HttpRequest createLoginRequest(String username, String pwd){
		HttpRequest request = new HttpRequest(URL_LOGIN);
		request.setRequestMethod(HttpRequest.METHOD_POST);
	    
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", pwd));
        params.add(new BasicNameValuePair("product", "t"));
        params.add(new BasicNameValuePair("type", "1"));
        
        request.setRequestTimeOut(60000);//MC原版本设置60000
        
        
        
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    return request;
	}
	
	/**
	 * 注册协议
	 * @param username
	 * @param pwd
	 * @return
	 */
	public HttpRequest createRegeisterRequest(String username, String pwd){
		HttpRequest request = new HttpRequest(URL_REGISTER);
		request.setRequestMethod(HttpRequest.METHOD_POST);
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", pwd));
        
        request.setRequestTimeOut(60000);//MC原版本设置60000
       
        
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return request;
    }
	
	/**
	 * 同步收藏夹
	 * @param favorites
	 * @param cookie
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public HttpRequest createSyncFavoriteRequest(String userId, List<SyncItemInfo> favorites,String cookie) throws UnsupportedEncodingException{
		HttpRequest request = new HttpRequest(getRequestUrl(URL_SYNCSTORE));
		request.setRequestMethod(HttpRequest.METHOD_POST);
	    
	    if(!Util.isStringEmpty(cookie)){
	        request.addHeaderField("Cookie", cookie);
        }
	    
	    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
	    pairs.add(new BasicNameValuePair("ursid", userId));
	    JSONArray arr = new JSONArray();
	    for(SyncItemInfo item : favorites){
	    	arr.put(item.toJsonObject());
	    }
	    pairs.add(new BasicNameValuePair("playids", arr.toString()));

	    request.addHeaderField(HTTP.USER_AGENT, sUserAgent);
	    request.setHttpEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
        //该句没有实现
//          request.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,false);
	    return request;
	}
	
	/**
	 * 反馈协议
	 * @param productType 手机型号
	 * @param osVersion 系统版本
	 * @param appVersion 程序版本
	 * @param msg 反馈信息
	 * @param email 反馈email
	 * @return
	 */
	public HttpRequest createFeedBackRequest(String productType, String osVersion, String appVersion, String msg, String email){
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("mtype", productType));
        params.add(new BasicNameValuePair("os", osVersion));
        params.add(new BasicNameValuePair("appversion", appVersion));
        params.add(new BasicNameValuePair("content", msg));
        params.add(new BasicNameValuePair("email", email));
        
        StringBuffer sb = new StringBuffer(getRequestUrl(URL_FEEDBACK)).append("?").append(
        URLEncodedUtils.format(params, "GBK"));
        
	    HttpRequest request = new HttpRequest(sb.toString());
	    return request;
	}
	
	/**
	 * 取得关于信息
	 * @return
	 */
	public HttpRequest createGetAboutRequest(){
		HttpRequest request = new HttpRequest(URL_ABOUT);
		return request;
	}
	
	/**
	 * 添加收藏
	 * @param userId 用户名
	 * @param plid 课程ID
	 * @param cookie 用户cookie
	 * @return
	 */
	public HttpRequest createAddStoreRequest(String userId, String plid, String cookie){
		HttpRequest request = new HttpRequest(getRequestUrl(URL_ADDSTORE));
   
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String storetime = sdf.format(new Date()).toString();
		        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ursid", userId));
        params.add(new BasicNameValuePair("playid", plid));
        params.add(new BasicNameValuePair("storetime", storetime));
        
        request.addHeaderField("Cookie", cookie);
        request.setRequestMethod(HttpRequest.METHOD_POST);
        
        request.addHeaderField(HTTP.USER_AGENT, sUserAgent);
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d("createAddStoreRequest", "request:"+request);
        return request;
        
	}
	
	/**
	 * 删除收藏
	 * @param userId 用户名
     * @param plid 课程ID
     * @param cookie 用户cookie
     * @return
	 */
	public HttpRequest createDelRequest(String userId, List<String> plidList, String cookie){
		HttpRequest request = new HttpRequest(getRequestUrl(URL_DELSTORE));
      
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ursid", userId));
        StringBuilder plids = new StringBuilder();
        for(int i = 0; i < plidList.size(); i++){
        	String plid = plidList.get(i);
        	plids.append(plid);
        	if(i != plidList.size() -1)
        		plids.append(",");
        }
        
        params.add(new BasicNameValuePair("playids", plids.toString()));
        
        request.addHeaderField("Cookie", cookie);
        request.setRequestMethod(HttpRequest.METHOD_POST);
        
        request.addHeaderField(HTTP.USER_AGENT, sUserAgent);
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return request;
	}
	
	/**
	 * 获取推送课程
	 * @param latestPushId
	 * @return
	 */
	public HttpRequest createGetPushCourseRequest(String latestPushId){
	    HttpRequest request = new HttpRequest(URL_PUSH_OPENCOURSE + "?lastvideoid=" + latestPushId);
	    request.addHeaderField(HTTP.USER_AGENT, sUserAgent);
	    request.setRequestTimeOut(PUSH_TIME_OUT);
	    //cmwap时，需要设置代理
//	    if(Tools.isCMWAPMobileNet(mContext)){
//            HttpHost proxy = new HttpHost("10.0.0.172", 80, "http");
//            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
//        }
	    return request;
	}
	
	public HttpRequest createGetRecommAppRequest(){
		HttpRequest request = new HttpRequest(URL_RECOMM_APP);
	    return request;
	}
	
	public static byte[] encodeCharacters(String from) {
			try {
				return from.getBytes(HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return from.getBytes();
	}
	
	/**
	 * 反馈接口:反馈log
	 * 
	 * @param 
	 * @return
	 */
	public HttpRequest createPostLogReqeust()
	{
		HttpRequest request = new HttpRequest("http://fankui.163.com/ft/file.fb?op=up", HttpRequest.METHOD_POST);
		request.setRequestTimeOut(sTimeout*3);
		File upload = null;
		boolean isZip = PalLog.zipLogFile(Constants.ZIP_LOG_FILE);
		if(isZip){
			upload = PalLog.openAbsoluteFile(Constants.ZIP_LOG_FILE);
			PalLog.i("createPostLogReqeust upload=", upload.toString());
		} else {
			PalLog.i("createPostLogReqeust()", "zip fail");
		}
		
		Part part1 = null;
		try {
			part1 = new FilePart("Filedata",Constants.ZIP_LOG_FILE, upload,"application/zip",null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MultipartEntity entity = new MultipartEntity(new Part[]{part1});
		request.setHttpEntity(entity);
		return request;
		
	}
	/**
	 * 反馈接口:反馈意见
	 * 
	 * @param 
	 * @return
	 */	
	public HttpRequest createFeedBackReqeust(String user,String title,String content,String fileId,String contact,Context context){	
		//HttpRequest request = new HttpRequest("http://220.181.9.96:3102/ft/commentInner.fb?", HttpRequest.METHOD_POST);
		HttpRequest request = new HttpRequest("http://fankui.163.com/ft/commentInner.fb?", HttpRequest.METHOD_POST);
		
		request.addHeaderField("Content-Type", "application/x-www-form-urlencoded");   
		request.setRequestTimeOut(sTimeout);
		
		String resolution = Util.getResolution(context) + ";" + android.os.Build.MODEL + "/android" + android.os.Build.VERSION.RELEASE  + ";" + Constants.APP_NAME + "/" + Util.getNumberVersion(context);
		
		
		List<NameValuePair> list = new ArrayList<NameValuePair>();  
		list.add(new BasicNameValuePair("feedbackId", FEEDBACK_ID)); 
		list.add(new BasicNameValuePair("productId", PRODUCT_ID));   
		list.add(new BasicNameValuePair("userName", user));  
		list.add(new BasicNameValuePair("title", title));  
		list.add(new BasicNameValuePair("content", content));
		list.add(new BasicNameValuePair("resolution", resolution));
		if (fileId != null) {
			list.add(new BasicNameValuePair("fileId", fileId));
			list.add(new BasicNameValuePair("fileName", Constants.ZIP_LOG_FILE));
		}
		if(contact != null){
			list.add(new BasicNameValuePair("contact", contact));
		}
		
		try {
			request.setHttpEntity(new UrlEncodedFormEntity(list, "GB2312"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
		return request;
		
		
	}
}
