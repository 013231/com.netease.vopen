package common.pal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import vopen.app.BaseApplication;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import common.framework.http.HttpRequest;
import common.framework.http.TrustAllSSLSocketFactory;
import common.net.ApnReference;
import common.net.ApnReference.ApnWrapper;
import common.net.sequenceMutiTypeEntity;
import common.util.NameValuePair;
import common.util.Util;



public class PalHttp implements IHttp
{
	private final String TAG = "PAL_HTTP";
	
	static Random mBoundaryRandom = new Random();
	
	String mUrl;
	String mMethod;
	byte[] mPostData;
	String mMultiPartFile;
	Hashtable mMultiParams;
	
	HttpClient mHttpClient;
	Hashtable mRequestProperty;
	
	HttpResponse mHttpResponse;
	int 		mTimeOut;
	
	HttpEntity mHttpEntity;
	
	Hashtable mHttpParams;
	
	public static IHttp createHttp(String url, String method) {
		PalHttp http = new PalHttp();
		http.open(url);
		http.setRequestMethod(method);
		
		return http;
	}
	
	private void buildHttpClient() {
		if (mHttpClient == null) {
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
			HttpConnectionParams.setSoTimeout(params, mTimeOut);
			HttpConnectionParams.setSocketBufferSize(params, 8*1024);
			HttpProtocolParams.setUseExpectContinue(params, true);
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http",
									   PlainSocketFactory.getSocketFactory(),
									   80));
//			schReg.register(new Scheme("https", 
//										SSLSocketFactory.getSocketFactory(),
//										443));
			schReg.register(new Scheme("https", 
								TrustAllSSLSocketFactory.getDefault(), 
								443));
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params,
																			 schReg);
			
			mHttpClient = new DefaultHttpClient(conMgr, params);
//			mHttpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
//												CookiePolicy.BEST_MATCH);
			
			ApnWrapper aw = ApnReference.getInstance(BaseApplication.getAppInstance()).getCurrApn();
			if (aw != null && !Util.isStringEmpty(aw.proxy)) {
			    // used to access cmwap
			    HttpHost proxy = new HttpHost(aw.proxy, aw.port);
			    mHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
		}
	}
	private void ShuDownHttpClient() {
		if (mHttpClient != null) {
			mHttpClient.getConnectionManager().shutdown();
			mHttpClient = null;
		}
	}
	
	private HttpResponse httpGet(){
		PalLog.i(TAG, "[http get url:]"+mUrl);
		buildHttpClient();
		HttpGet httpGet = new HttpGet(mUrl);
		HttpResponse response = null;
		
		if(mRequestProperty != null && mRequestProperty.size() > 0 ){
			Enumeration e = mRequestProperty.keys();
			while (e.hasMoreElements())
			{
				String key = (String) e.nextElement();
				String value = (String) mRequestProperty.get(key);
				httpGet.setHeader(key, value);
			}
		}
		
		if(mHttpParams != null){
			Enumeration e = mHttpParams.keys();
			while(e.hasMoreElements()){
				String key = (String) e.nextElement();
				Object value = (Object) mHttpParams.get(key);
				httpGet.getParams().setParameter(key, value);
			}
		}
		
		try {
			response = mHttpClient.execute(httpGet);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}
	
	private HttpResponse httpPost(){
		PalLog.i(TAG, "[http post url:]"+mUrl);
		
		buildHttpClient();
		HttpEntity entry = null;
		HttpResponse response = null;
		HttpPost httpPost = new HttpPost(mUrl);
		if(mRequestProperty != null && mRequestProperty.size() > 0 ){
			Enumeration e = mRequestProperty.keys();
			while (e.hasMoreElements())
			{
				String key = (String) e.nextElement();
				String value = (String) mRequestProperty.get(key);
				httpPost.setHeader(key, value);
			}
		}
		if(mHttpEntity != null){
			entry = mHttpEntity;
		}
		else if(mPostData != null){
			entry = new PalByteArrayEntity(mPostData,HTTP.UTF_8);
		}
		else if (mMultiPartFile != null) {
			String boundary = getBoundary();
			
			PalFile file = new PalFile(mMultiPartFile, true);

			StringBuffer header = new StringBuffer();
			header.append("--");
			header.append(boundary);
			header.append("\r\n");
			header.append("Content-Disposition: form-data; ");
			if(mMultiParams != null && mMultiParams.size() > 0){
				Enumeration e = mMultiParams.keys();
				while (e.hasMoreElements())
				{
					String key = (String) e.nextElement();
					String value = (String) mMultiParams.get(key);
					header.append(key + "=" + value + "; ");
				}
			}
			else{
				header.append("name=\"pic\"; ");
			}
			header.append("filename=\"" + file.getName() + "\"\r\n");
			header.append("Content-Type: application/octet-stream;\r\n");
			header.append("Content-Transfer-Encoding: binary\r\n");
			header.append("\r\n");

			try{
    			byte[] head_data = header.toString().getBytes();
    			byte[] end_data = ("\r\n--" + boundary + "--\r\n").getBytes();
    			
    			httpPost.setHeader("Content-Type", 
    					"multipart/form-data; boundary=" + boundary);
//    			httpPost.setHeader("Content-Length", 
//    					String.valueOf((file.getSize() + end_data.length + head_data.length)));
    			
    			InputStream in = file.openInputStream();
//    			ByteArrayOutputStream os = new ByteArrayOutputStream();
//    			
//    			os.write(head_data);
//    			
//    			byte[] data = new byte[1024];
//    			int length = 0;
//    			while((length = in.read(data, 0, data.length)) > 0) {
//    				os.write(data, 0, length);
//    			}
//    			
//    			in.close();
//    			file.close();
//    			os.write(end_data);
//    			os.close();
//    			entry = new ByteArrayEntity(os.toByteArray());
    			sequenceMutiTypeEntity sequenceentry = new sequenceMutiTypeEntity();
    			sequenceentry.addByteArray(head_data, -1);
    			sequenceentry.addInputStream(in, (int)file.getSize());
    			sequenceentry.addByteArray(end_data, -1);
    			entry = sequenceentry;
			}
			catch (IOException e){
				e.printStackTrace();
			}
		} 
		httpPost.setEntity(entry);
		
		if(mHttpParams != null){
			Enumeration e = mHttpParams.keys();
			while(e.hasMoreElements()){
				String key = (String) e.nextElement();
				Object value = (Object) mHttpParams.get(key);
				httpPost.getParams().setParameter(key, value);
			}
		}
		
		try {
			response = mHttpClient.execute(httpPost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	
	public int execute() throws IOException
	{
		buildHttpClient();
		if (mMethod.equals(HttpRequest.METHOD_POST)) {
			mHttpResponse = httpPost();
		} else {
			mHttpResponse = httpGet();
		}

		if(mHttpResponse != null && mHttpResponse.getStatusLine() != null){		
			PalLog.i(TAG, "ResponseCode" + mHttpResponse.getStatusLine().getStatusCode());
			return mHttpResponse.getStatusLine().getStatusCode();
		}
		else{
			PalLog.e(TAG, "mHttpResponse = null");
			return -1;
		}		
	}

	private String getBoundary() {
		return String.valueOf(System.currentTimeMillis()) 
				+ String.valueOf(Math.abs(mBoundaryRandom.nextLong()));
	}
	
	public String getHeaderField(int n) throws IOException
	{
		if(mHttpResponse != null){
			Header head[] = mHttpResponse.getAllHeaders();
			if(head != null && head.length >= n)
				return head[n].getValue();
		}
		
		return null;
	}

	public String getHeaderField(String name) throws IOException
	{
		// TODO Auto-generated method stub
		if(mHttpResponse != null){
			Header head = mHttpResponse.getFirstHeader(name);	
			if(head != null)
				return head.getValue();
		}
		
		return null;
	}

	public String getHeaderFieldKey(int n) throws IOException
	{
		if(mHttpResponse != null){
			Header head[] = mHttpResponse.getAllHeaders();
			if(head != null && head.length >= n)
				return head[n].getName();
		}
		
		return null;
	}
	
    public NameValuePair[] getHeaderField() throws IOException {
        Header[] headers = mHttpResponse.getAllHeaders();
        if(headers != null && headers.length > 0){
            NameValuePair[] pairs = new NameValuePair[headers.length];
            for(int i = 0; i < headers.length; i++){
                pairs[i] = new NameValuePair(headers[i].getName(), headers[i].getValue());
            }
            
            return pairs;
        }
        return null;
    }
	

	public int getResponseCode()
	{
		if(mHttpResponse != null && mHttpResponse.getStatusLine() != null )
			return mHttpResponse.getStatusLine().getStatusCode();
		
		return -1;
	}

	public String getResponseMessage() throws IOException
	{
		if(mHttpResponse != null)
			return EntityUtils.toString(mHttpResponse.getEntity());
		
		return null;
	}

	public void open(String url)
	{
		// TODO Auto-generated method stub
		mUrl = url;
	}

	public DataInputStream openDataInputStream() throws IOException
	{
		return null;
	}

	public DataOutputStream openDataOutputStream() throws IOException
	{
		return null;
	}

	public InputStream openInputStream() throws IOException
	{
		if(mHttpResponse != null && mHttpResponse.getEntity() != null){
			return mHttpResponse.getEntity().getContent();
		}
		else{
			return null;
		}
	}

	public OutputStream openOuputStream() throws IOException
	{
		return null;
	}

	public void postMultiPart(String file)
	{
		postMultiPart(file, null);
	}
	@Override
	public void postMultiPart(String file, Hashtable param) {
		mMultiPartFile = file;
		mMultiParams = param;
	}
	
	public void postByteArray(byte[] byteArray)
	{
		mPostData = byteArray;
	}
	
	public void setHttpEntity(Object entity){
		if(entity instanceof HttpEntity)
			mHttpEntity = (HttpEntity) entity;
	}

	public void setRequestMethod(String method)
	{
		// TODO Auto-generated method stub
		mMethod = method;
	}

	public void setRequestProperty(String key, String value)
	{
		// TODO Auto-generated method stub
		if(mRequestProperty == null){
			mRequestProperty = new Hashtable();
		}
		mRequestProperty.put(key, value);
	}

	public void close() throws IOException
	{
		ShuDownHttpClient();
	}

	public long getContentLength()
	{
		if(mHttpResponse != null){
			HttpEntity entity = mHttpResponse.getEntity();
			if(entity != null)
				return entity.getContentLength();
		}
		
		return 0;
	}

	@Override
	public void setTimeout(int timeout) {
		mTimeOut = timeout;
	}

	static public boolean isWap(Context context)
	{
		ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo nInfo = cm.getActiveNetworkInfo();
		if(nInfo == null || nInfo.getType() != ConnectivityManager.TYPE_MOBILE)
			return false;
		String extraInfo = nInfo.getExtraInfo();
		if(extraInfo == null || extraInfo.length() < 3)
			return false;
		if(extraInfo.toLowerCase().contains("wap"))
			return true;
		return false;
	}

	@Override
	public void setHttpParams(Hashtable param) {
		mHttpParams = param;
	}
}
