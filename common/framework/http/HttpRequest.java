package common.framework.http;

import java.util.Hashtable;

import org.apache.http.HttpEntity;

import vopen.app.BaseApplication;

import com.netease.vopen.pal.Constants;

import common.pal.IHttp;
import common.util.Util;
public class HttpRequest
{
	/* 常量定义*/
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	
	/* request id */
	int mRequestID;
	/* cancel flag */
	boolean mIsCancel = false;
	/* http url */
	String mUrl;
	/* http method */
	String mMethod;
	
	Hashtable mHeader;
	
	Hashtable mHttpParams;
	/* 以下二种post方式，不能同存 */
	/* 
	 * 补充一下： 纯粹post时仅有mPostByteArray数据，mutipart（mMultiPartFile）方式下可以有mPostByteArray
	 * 此时表示为MultiPart的数据内容，而发送数据时不再通过mMultiPartFile文件访问的方式进行
	 * */
	byte[] mPostByteArray;
	
	String mMultiPartFile;
	
	/*HttpEntity*/Object mHttpEntity;
	
	HttpCallBack mCallback;
//	Hashtable mPostNameValuePairs;
	
	boolean mStreamCallBack;
	//每个request可自定超时
	int mTimeout = 1 * 60 * 1000; //1分钟超时
	
	boolean mHeaderResponse;
	
	String mCachePath;
	
	public HttpRequest(String url){
		this(url,METHOD_GET);
	}
	
	public HttpRequest(String url,byte[] postData){
		
		this(url,METHOD_POST);
		mPostByteArray = postData;
	}
	
	
	public HttpRequest(String url,String method){
		this.mUrl = url;
		mMethod = method;
		mRequestID = HttpEngine.getNextRequestID();
		mIsCancel = false;
	}


	
	public int getRequestID()
	{
		return mRequestID;
	}
	
	
	/**
	 * 添加头字段
	 * @param key
	 * @param value
	 */
	public void addHeaderField(String key,String value)
	{
		if(mHeader == null){
			mHeader = new Hashtable();
		}
		mHeader.put(key, value);
	}
	
	/**
	 * set Http Header Field
	 * @param table
	 */
	public void setHeaderField(Hashtable table)
	{
		if(table != null){
			mHeader = table;
		}
	}
	
	
	public Hashtable getHeaderField()
	{
		return mHeader;
	}

	
	public String getMethod()
	{
		return mMethod;
	}
	
	public String getUrl()
	{
		return mUrl;	
	}
	
	
	public void postData(byte[] postdata)
	{
		mPostByteArray = postdata;
		if(postdata != null){
			mMethod = METHOD_POST;
		}
	}
	
	public void postMultiPartFile(String file)
	{
		mMultiPartFile = file; 
		if(file != null){
			mMethod = METHOD_POST;
		}
	}
	
	public void postMultiPartFile(String filePath, byte[] data)
	{
		mMultiPartFile = filePath;
		mPostByteArray = data;
		if(filePath != null){
			mMethod = METHOD_POST;
		}
	}
	
	
	public void setHttpEntity(HttpEntity entity){
		mHttpEntity = entity;
	}
	public Object getHttpEntity(){
		return mHttpEntity;
	}
//	public void addNameValuePair(String name,String value)
//	{
//		if(mPostNameValuePairs == null){
//			mPostNameValuePairs = new Hashtable();
//		}
//		mPostNameValuePairs.put(name, value);
//	}
//	
//	
//	public void setNameValuePair(Hashtable nameValuePair)
//	{
//		mPostNameValuePairs = nameValuePair;
//	}
//	
//	
//	public Hashtable getNameValuePair()
//	{
//		return mPostNameValuePairs;
//	}
	
	public void setHttpCallBack(HttpCallBack callback)
	{
		mCallback = callback;
	}
	
	public HttpCallBack getHttpCallBack()
	{
		return mCallback;
	}
	
	public String getMultiPartFile()
	{
		return mMultiPartFile;
	}
	
	public byte[] getMultiPartData() {
		return mPostByteArray;
	}
	
	public void setRequestMethod(String method)
	{
		mMethod = method;
	} 
	
	public void setHeaderResponse(boolean headerResponse){
	    mHeaderResponse = true;
	}
	
	public boolean isHeaderResponse(){
	    return mHeaderResponse;
	}
	
	public byte[] getPostData()
	{
		return mPostByteArray;
	}

	
	public void doCancel(){
		mIsCancel = true;	
	}
	
	public boolean isCancel()
	{
		return mIsCancel;
	}

	public void setStreamCallBack(boolean streamCallBack) {
		mStreamCallBack = streamCallBack;
	}
	
	public boolean isStreamCallBack() {
		return mStreamCallBack;
	}
	public void setRequestTimeOut(int timeOut) {
		mTimeout = timeOut;
	}
	public int getRequestTimeOut() {
		return mTimeout;
	}
	//解决上传图片和上传头像参数不一样的问题
	private Hashtable/*<String, String>*/ mMultiPartParams;
	public void setMultiPartParams(String key, String value){
		if(mMultiPartParams == null)
			mMultiPartParams = new Hashtable/*<String, String>*/();
		
		mMultiPartParams.put(key, value);
	}
	public Hashtable getMultiPartParams(){
		return mMultiPartParams;
	}
	public  void buildHttpRequest(){
		//  GZIP 压缩
//		addHeaderField("Accept-Encoding", "gzip");
//		String token = AlbumProtocol.getInstance().getAUTHORIZED_TOKEN();
//		if(!Util.isStringEmpty(token)) {
//			addHeaderField("Authorization","token="+ "\""+token+"\"");
//		}
		String userAgent = Constants.APP_NAME + "_android" + "/" + Util.getNumberVersion(BaseApplication.getAppInstance());
		if(!Util.isStringEmpty(userAgent)) {
			addHeaderField("User-Agent",userAgent);
		}
	}
	public boolean afterExec(IHttp http) {
//		if(http != null && http.getResponseCode() == HttpStatus.SC_UNAUTHORIZED){
//			return AlbumService.getInstance().doReLogin();
//		}
		return false;
	}	
	public void doBefore() {
		
	}
	
	
	/**
	 * 添加http params
	 * @param name
	 * @param value
	 */
	public void addHttpParams(String name, Object value){
		if(mHttpParams == null)
			mHttpParams = new Hashtable();
		
		mHttpParams.put(name, value);
	}
	
	public Hashtable getHttpParams(){
		return mHttpParams;
	}
	
	public void setCachePath(String value) {
		mCachePath = value;
	}
	
	/**
	 * 返回请求结果缓存的地址
	 * @return
	 */
	public String getCachePath(){
		return mCachePath;
	}
}
