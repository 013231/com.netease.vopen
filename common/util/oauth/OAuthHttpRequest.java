package common.util.oauth;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HTTP;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;

public class OAuthHttpRequest extends HttpRequest {
	public static final int SPECIAL_NON = 0;
	public static final int SPECIAL_SOHU = 1;
	
	protected static Random mNoceRandom = new Random();
	
	protected OAuthClient mOAuthClient;
	protected String mOAuthUrl;
	boolean mOAuthDataPost;
	boolean mOAuthDataQuery;
	int mSpecial = SPECIAL_NON;
	
	Hashtable mParameters;
	
	public OAuthHttpRequest(String url, OAuthClient client) {
		this(url, THttpMethod.GET, client);
	}
	
	public OAuthHttpRequest(String url, THttpMethod type, OAuthClient client) {
		super(url, type.name());
		mOAuthClient = client;
		addHeaderField("Accept-Encoding","gzip,deflate");
	}
	
	public void setOAuthDataPost(boolean value) {
		mOAuthDataPost = value;
	}
	
	public void setOauthdataQuery(boolean value){
		mOAuthDataQuery = value;
	}
	
	public void setSpecial(int value){
		mSpecial = value;
	}
	public void addParameter(String key, String value) {
		if (key == null || value == null) {
			return ;
		}
		if(mParameters == null){
			mParameters = new Hashtable();
		}
		mParameters.put(key, value);
	}
	protected Hashtable getHttpParamters() {
		return mParameters;
	}
	@Override
	public void doBefore() {
//		super.doBefore();
		List<OAuthNameValuePair> list = new ArrayList<OAuthNameValuePair>();
	
		Hashtable paramters = getHttpParamters();
		if (paramters != null && paramters.size() > 0)
		{
			Enumeration enu = paramters.keys();
			while (enu.hasMoreElements())
			{
				String key = (String) enu.nextElement();
				String value = (String) paramters.get(key);
				list.add(new OAuthNameValuePair(key, value));
			}
		}
		
		if (mOAuthClient.mTokenKey != null) {
			list.add(new OAuthNameValuePair(OAuth.OAUTH_TOKEN, mOAuthClient.mTokenKey));
		}
		list.add(new OAuthNameValuePair(OAuth.OAUTH_CONSUMER_KEY, 
				mOAuthClient.mConsumerKey));
		list.add(new OAuthNameValuePair(OAuth.OAUTH_SIGNATURE_METHOD, 
				mOAuthClient.getSignatureMethod()));
		list.add(new OAuthNameValuePair(OAuth.OAUTH_VERSION, 
				OAuth.VERSION_1_0));
		list.add(new OAuthNameValuePair(OAuth.OAUTH_TIMESTAMP, String.valueOf((System
				.currentTimeMillis() / 1000))));
		list.add(new OAuthNameValuePair(OAuth.OAUTH_NONCE, String.valueOf((System
				.currentTimeMillis()) + Math.abs(mNoceRandom.nextLong()))));
		
		String baseString = getBaseString(getMethod().toString(), super.getUrl(), list);
		String signature = mOAuthClient.getHMAC_SHA1().getSignature(baseString);
		list.add(new OAuthNameValuePair(OAuth.OAUTH_SIGNATURE, signature));
		
		String method = getMethod();
		if (method.endsWith(THttpMethod.POST.name())
				|| method.endsWith(THttpMethod.DELETE.name())
				|| method.endsWith(THttpMethod.PUT.name())) {
			if (mOAuthDataPost && getHttpEntity() == null) {
				String content = normalizeParameters(list);
				byte[] data = null; 
				try {
					data = content.getBytes("utf-8");
				} catch (UnsupportedEncodingException e1) {
					data = content.getBytes();
				}
				addHeaderField(HTTP.CONTENT_TYPE, 
						"application/x-www-form-urlencoded");
				setHttpEntity(new ByteArrayEntity(data));
				list.clear();
			}
//			else if(mOAuthDataQuery){
//				mOAuthUrl = super.getUrl() + "?" + URLEncodedUtils.format(list, "UTF-8");
//			}
			else if(!mOAuthDataQuery){
				addHeaderField(OAuth.Authorization, getAuthorization(list));
				list.clear();
			}
			
			
		}
		
		if (list.size() > 0) {
			mOAuthUrl = super.getUrl() + "?" + URLEncodedUtils.format(list, "UTF-8");
		}
		else {
			mOAuthUrl = super.getUrl();
		}
	}
	
	private String getAuthorization(List<OAuthNameValuePair> list) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(OAuth.AUTH_SCHEME);
		if(SPECIAL_SOHU != mSpecial){
			String realm = OAuth.normalizeUrl(super.getUrl());
			if (realm != null) {
				builder.append(" realm=\"").append(OAuth.percentEncode(realm)).append(
						'"');
				builder.append(",");
			}
		}
		List<OAuthNameValuePair> authList = new LinkedList<OAuthNameValuePair>();
//		for (OAuthNameValuePair entry : list) {
		for(int i = 0; i < list.size(); i++){
			OAuthNameValuePair entry = list.get(i);
			String name = entry.getName();
			if (name.startsWith("oauth_")) {
				if (i > 0)
					builder.append(",");
				builder.append(" ");
				builder.append(OAuth.percentEncode(name)).append("=\"");
				builder.append(OAuth.percentEncode(entry.getValue()));
				builder.append('"');
				
				authList.add(entry);
			}
		}
		
		list.removeAll(authList);
		
		return builder.toString();
	}

	public String getUrl() {
		return mOAuthUrl;
	}
	
	protected static String getBaseString(String method, String url, List<OAuthNameValuePair> list) {
		return OAuth.percentEncode(method.toUpperCase()) + '&'
				+ OAuth.percentEncode(OAuth.normalizeUrl(url)) + '&'
				+ OAuth.percentEncode(normalizeParameters(list));
	}

	@SuppressWarnings("unchecked")
	private static String normalizeParameters(List<OAuthNameValuePair> list) {
		Collections.sort(list);
		
		StringBuilder builder = new StringBuilder();
		for (NameValuePair kv : list) {
			builder.append(OAuth.percentEncode(kv.getName()));
			builder.append("=");
			builder.append(OAuth.percentEncode(kv.getValue()));
			builder.append('&');
		}
		builder.deleteCharAt(builder.length() - 1);
		
		return builder.toString();
	}
	/**
	 * 
	 * 为了不加Authorization，User-Agent等处理
	 */
	@Override
	public  void buildHttpRequest() {
		
	}
}
