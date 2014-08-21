package common.util.oauth;

import java.util.Hashtable;

import org.apache.http.HttpEntity;

import common.framework.http.THttpMethod;
import common.util.crypto.HMAC_SHA1;

public class OAuthClient
{
	
	public String mConsumerKey, mConsumerSecret;

	public String mRequestToken;
	public String mTokenKey;
	public String mTokenSecret;

	/**
	 * 加密方式
	 */
	//String mSignatureMethod;

	HMAC_SHA1 mHMACSHA1;

	public OAuthClient()
	{
		this.mRequestToken = null;
		this.mTokenKey = null;
		this.mTokenSecret = null;
		this.mConsumerKey = null;
		this.mConsumerSecret = null;
	//	this.mSignatureMethod = OAuth.HMAC_SHA1;
	}


	public void setConsumer(String consumerKey, String consumerSecret)
	{
		mConsumerKey = consumerKey;
		mConsumerSecret = consumerSecret;
	}
	
	public void setToken(String tokenKey,String tokenSecret){
		mTokenKey = tokenKey;
		mTokenSecret = tokenSecret;
	}
	
	public void setHMACSHA1Key(String key){
		byte[] keyByte = OAuth.encodeCharacters(key);
		getHMAC_SHA1().init(keyByte);
	}
	
	/**
	 * 加密方法.
	 * @return
	 */
	public String getSignatureMethod()
	{
		return  OAuth.HMAC_SHA1;
	}

	
	public HMAC_SHA1 getHMAC_SHA1()
	{
		if(mHMACSHA1 == null){
			mHMACSHA1 = new HMAC_SHA1();
		}
		return mHMACSHA1;
	}
	
	public OAuthHttpRequest request(THttpMethod method, String url, Hashtable<String, String> parameter)
	{
		return request(method, url, parameter, null);
	}
	
	public OAuthHttpRequest request(THttpMethod method, String url, Hashtable<String, String> parameter, HttpEntity entity)
	{
		return OAuth.request(method, url, parameter, entity, this);
	}

}
