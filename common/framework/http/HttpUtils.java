
package common.framework.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import common.net.NetUtils;
import common.pal.PalLog;


/**
 * <br/>
 * Http工具类. <br/>
 * 用于返回同步http请求结果 <br/>
 * 用于返回异步http <br/>
 * TODO :目前还没有增加断点下载方法
 * 
 * @author wjying
 */
public class HttpUtils {
    private static final String TAG = "HttpUtils";

    public static final String GET = HttpGet.METHOD_NAME;

    public static final String POST = HttpPost.METHOD_NAME;

    public static final int TIMEOUT = 3000;

    public static final String USER_AGENT = "NTES Android";

    public static final String WAP_PROXY_URL = "10.0.0.172";

    public static final int WAP_PROXY_PORT = 80;

    public static final String DEFAULT_SCHEME_NAME = "http";

    /**
     * 返回http JSONArray请求结果.
     * 
     * @param params
     * @param url
     * @param method 请求类型GET或者POST
     * @param context
     * @return
     */
    public static JSONArray getHttpJSONArrayResult(String url, List<NameValuePair> params,
            String method, Context context) {
        String str = getHttpStringResult(url, params, method, context);

        if (!TextUtils.isEmpty(str)) {
            try {
                return new JSONArray(str);
            } catch (Exception e) {
            	PalLog.e(TAG, "getHttpJSONObjectResult error!!!"+ e);
            }
        }
        return null;
    }

    /**
     * <br/>
     * 返回http JSONArray请求结果. <br/>
     * 注意使用这个方法时应注意关闭HttpClient {@literal xxx}
     * 
     * @param httpclient
     * @param params
     * @param url
     * @param method 请求类型GET或者POST
     * @param context
     * @return
     */
    public static JSONArray getHttpJSONArrayResult(HttpClient httpclient, String url,
            List<NameValuePair> params, String method) {
        String str = getHttpStringResult(httpclient, url, params, method);

        if (!TextUtils.isEmpty(str)) {
            try {
            	PalLog.i(TAG,"getHttpJSONArrayResult " + str);
                return new JSONArray(str);
            } catch (Exception e) {
            	PalLog.e(TAG, str + " getHttpJSONObjectResult error!!!"+ e);
            }
        }
        return null;
    }

    /**
     * 同getHttpJSONArrayResult(HttpClient httpclient, String url,
     * List<NameValuePair> params, String method)
     * 
     * @deprecated
     * @param httpclient
     * @param url
     * @param params
     * @param method
     * @param context
     * @return
     */
    public static JSONArray getHttpJSONArrayResult(HttpClient httpclient, String url,
            List<NameValuePair> params, String method, Context context) {
        return getHttpJSONArrayResult(httpclient, url, params, method);
    }

    /**
     * 返回http JSONObject请求结果.
     * 
     * @param params
     * @param url
     * @param method 请求类型GET或者POST
     * @param context
     * @return
     */
    public static JSONObject getHttpJSONObjectResult(String url, List<NameValuePair> params,
            String method, Context context) {
        String str = getHttpStringResult(url, params, method, context);

        if (!TextUtils.isEmpty(str)) {
            try {
                return new JSONObject(str);
            } catch (Exception e) {
            	PalLog.e(TAG, str + " getHttpJSONObjectResult error!!!"+ e);
            }
        }
        return null;
    }

    /**
     * <br/>
     * 返回http JSONObject请求结果. <br/>
     * 注意使用这个方法时应注意关闭HttpClient
     * 
     * @param httpclient
     * @param params
     * @param url
     * @param method 请求类型GET或者POST
     * @param context
     * @return
     */
    public static JSONObject getHttpJSONObjectResult(HttpClient httpclient, String url,
            List<NameValuePair> params, String method) {
        String str = getHttpStringResult(httpclient, url, params, method);

        if (!TextUtils.isEmpty(str)) {
            try {
                return new JSONObject(str);
            } catch (Exception e) {
            	PalLog.e(TAG, str + " getHttpJSONObjectResult error!!!"+ e);
            }
        }
        return null;
    }

    /**
     * <br/>
     * 返回http JSONObject请求结果. <br/>
     * 注意使用这个方法时应注意关闭HttpClient
     * 
     * @param httpclient
     * @param params
     * @param url
     * @param method 请求类型GET或者POST
     * @param cookie
     * @return
     */
    public static JSONObject getHttpJSONObjectResult(HttpClient httpclient, String url,
            List<NameValuePair> params, String method, String cookie) {
        String str = getHttpStringResult(httpclient, url, params, method, cookie);

        if (!TextUtils.isEmpty(str)) {
            try {
                return new JSONObject(str);
            } catch (Exception e) {
            	PalLog.e(TAG, str + " getHttpJSONObjectResult error!!!"+ e);
            }
        }
        return null;
    }
    
    /**
     * 返回http 字符串请求结果. <br/>
     * 
     * @param url
     * @param params
     * @param method
     * @param context
     * @return
     */
    public static String getHttpStringResult(String url, List<NameValuePair> params, String method,
            Context context) {
        AndroidHttpClient httpclient = getAndroidHttpClient(context);
        try {
            return getHttpStringResult(httpclient, url, params, method);
        } finally {
            if (null != httpclient) {
                httpclient.close();
            }
        }
    }

    /**
     * 返回http 字符串请求结果. <br/>
     * 注意使用这个方法时应注意关闭HttpClient
     * 
     * @param httpClient
     * @param url
     * @param params
     * @param method
     * @return
     */
    public static String getHttpStringResult(HttpClient httpClient, String url,
            List<NameValuePair> params, String method) {
        return getHttpStringResult(httpClient, url, params, null, method, null);
    }
    
    /**
     * 返回http 字符串请求结果. <br/>
     * 注意使用这个方法时应注意关闭HttpClient
     * 
     * @param httpClient
     * @param url
     * @param params
     * @param method
     * @param cookie
     * @return
     */
    public static String getHttpStringResult(HttpClient httpClient, String url,
            List<NameValuePair> params, String method, String cookie) {
    	 Header[] headers = null;
    	 if(!TextUtils.isEmpty(cookie)){
    		 headers = new Header[]{new BasicHeader("Cookie", cookie)};
    	 }
        return getHttpStringResult(httpClient, url, params, headers, method, null);
    }

    /**
     * 返回http 字符串请求结果. <br/>
     * 注意使用这个方法时应注意关闭HttpClient
     * 
     * @param httpClient
     * @param url
     * @param params
     * @param headers
     * @param method
     * @param encoding
     * @return
     */
    public static String getHttpStringResult(HttpClient httpClient, String url,
            List<NameValuePair> params, Header[] headers, String method, String encoding) {
        try {
            HttpResponse response = doHttpExecute(httpClient, url, params, headers, method,
                    encoding);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String retStr = EntityUtils.toString(response.getEntity());
                PalLog.d(TAG, "getHttpStringResult:" + retStr);
                return retStr;
            }
        } catch (Exception e) {
        	PalLog.e(TAG, "getHttpStringResult error!!!"+ e);
        }
        return null;
    }
    
    /**
     * <br/>
     * 返回http 图片请求结果. <br/>
     * 如果返回为空说明下载没有成功
     * 
     * @param params
     * @param url
     * @param method 请求类型GET或者POST
     * @param context
     * @return
     */
    public static Bitmap getHttpBitmapResult(String url, List<NameValuePair> params,
            Header[] headers, String method, String encoding, Context context) {
        AndroidHttpClient httpClient = getAndroidHttpClient(context);
        try {
            return getHttpBitmapResult(httpClient, url, params, headers, method, encoding, context
                    .getResources());
        } finally {
            if (null != httpClient) {
                httpClient.close();
            }
        }
    }

    /**
     * <br/>
     * 返回http 图片请求结果. <br/>
     * 如果返回为空说明下载没有成功 <br/>
     * 注意使用这个方法时应注意关闭HttpClient
     * 
     * @param httpclient
     * @param params
     * @param url
     * @param method 请求类型GET或者POST
     * @return
     */
    public static Bitmap getHttpBitmapResult(HttpClient httpClient, String url,
            List<NameValuePair> params, Header[] headers, String method, String encoding,
            Resources res) {
        Bitmap bitmap = null;
        try {
            HttpResponse response = doHttpExecute(httpClient, url, params, headers, method,
                    encoding);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream is = entity.getContent();
                    bitmap = readBitmap(res, is);
                }
            }
        } catch (Exception e) {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
            PalLog.e(TAG, "getHttpBitmapResult error!!!"+ e);
        }
        return bitmap;
    }
    /**
     * <br/>
     * 获取图片读取写入时参数. <br/>
     * TODO 暂时全部返回 {@link Bitmap.CompressFormat.PNG}
     * 
     * @return
     */
    private static BitmapFactory.Options getBitmapOptions() {
        // TODO 这个opt需不需要有什么要求
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        return opt;
    }
    
    /**
     * 从文件读取图片.
     * 
     * @param res
     * @param is
     * @return
     */
    private static Bitmap readBitmap(Resources res, InputStream is) {
        return BitmapFactory.decodeResourceStream(res, null, is, null, getBitmapOptions());
    }
    
    /**
     * 执行http请求. <br/>
     * 当需要自己处理状态码时使用这个方法
     * 
     * @param httpClient
     * @param url
     * @param params
     * @param headers
     * @param encoding
     * @return 返回 null 说明联网错误
     */
    public static HttpResponse doHttpExecute(HttpClient httpClient, String url,
            List<NameValuePair> params, Header[] headers, String method, String encoding) {
        HttpUriRequest request = null;
        
        if (POST.equals(method)) {
            HttpPost post = new HttpPost(url);
            if (params != null) {
                try {
                    post.setEntity(new UrlEncodedFormEntity(params,
                            TextUtils.isEmpty(encoding) ? HTTP.UTF_8 : encoding));
                } catch (UnsupportedEncodingException e) {
                	PalLog.e(TAG, "doHttpExecute error!!!"+ e);
                }
            }
            request = post;
        } else if (GET.equals(method)) {
            if (params != null && params.size() > 0) {
                url += ("?" + URLEncodedUtils.format(params,
                        TextUtils.isEmpty(encoding) ? HTTP.UTF_8 : encoding));
            }
            request = new HttpGet(url);
        }
        
        HttpResponse response = null;
        if (request != null) {
            response = doHttpExecute(httpClient, request, headers);
        }
        
//        if (Logger.DEBUG) {
//            new PrintThread(url, params, headers, method, encoding, response).start();
//        }

        return response;
    }
    
    /**
     * 执行Http请求
     * 
     * @param httpClient
     * @param request
     * @param headers
     * @return 返回 null 说明联网错误
     */
    private static HttpResponse doHttpExecute(HttpClient httpClient, HttpUriRequest request,
            Header[] headers) {
        try {
            if (headers != null) {
                for (Header header : headers) {
                	
                    request.addHeader(header);
                }
            }
            return httpClient.execute(request);
        } catch (ClientProtocolException e) {
            PalLog.e(TAG, "doHttpExecute error!!!"+ e);
        } catch (IOException e) {
        	PalLog.e(TAG, "doHttpExecute error!!!"+ e);
        }
        return null;
    }

    /**
     * 返回默认请求参数.
     * 
     * @return
     */
    public static HttpParams getDefaultHttpParams() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT);
        HttpProtocolParams.setUserAgent(httpParameters, USER_AGENT);
        return httpParameters;
    }

    /**
     * 返回HttpClient.
     * 
     * @param context
     * @return
     */
    public static HttpClient getHttpClient(Context context) {
        DefaultHttpClient httpclient = new DefaultHttpClient(getDefaultHttpParams());
        initHttpClient(context, httpclient);
        return httpclient;
    }

    /**
     * 返回AndroidHttpClient.
     * 
     * @param context
     * @return
     */
    public static AndroidHttpClient getAndroidHttpClient(Context context) {
        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(USER_AGENT, context);
        initHttpClient(context, httpclient);
        return httpclient;
    }

    /**
     * 初始化HttpClient. 目前只是增加cmwap代理
     * 
     * @param context
     * @param httpClient
     */
    private static void initHttpClient(Context context, HttpClient httpClient) {
        // 支持cmwap网络
        if (NetUtils.isCMWAP(context)) {
            HttpHost proxy = new HttpHost(WAP_PROXY_URL, WAP_PROXY_PORT, DEFAULT_SCHEME_NAME);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        HttpClientParams.setRedirecting(httpClient.getParams(), true);
    }
    
    private static class PrintThread extends Thread {
        private static final Object sLock = new Object();

        private String url;

        private List<NameValuePair> params;

        private Header[] headers;

        private String method;

        private String encoding;

        private HttpResponse response;

        public PrintThread(String url, List<NameValuePair> params, Header[] headers, String method,
                String encoding, HttpResponse response) {
            this.url = url;
            this.params = params;
            this.headers = headers;
            this.method = method;
            this.encoding = encoding;
            this.response = response;
        }

        @Override
        public void run() {
            synchronized (sLock) {
                print(url, params, headers, method, encoding, response);
            }
        }
    }
    
    private static void print(String url, List<NameValuePair> params, Header[] headers,
            String method, String encoding, HttpResponse response) {
    	PalLog.d(TAG,"***************************************");
    	PalLog.d(TAG, " url=" + url);
    	PalLog.d(TAG, " method=" + method);
    	PalLog.d(TAG, " encoding=" + encoding);
        if (params != null) {
            final int size = params.size();
            PalLog.d(TAG, " params size=" + size);
            for (int i = 0; i < size; i++) {
            	PalLog.d(TAG, " params[" + i + "]=[" + params.get(i).getName() + ","
                        + params.get(i).getValue() + "]");
            }
        } else {
        	PalLog.d(TAG, " params=null");
        }
        if (headers != null) {
            final int size = headers.length;
            PalLog.d(TAG, " headers size=" + size);
            for (int i = 0; i < size; i++) {
            	PalLog.d(TAG, " headers[" + i + "]=[" + headers[i].getName() + ","
                        + headers[i].getValue() + "]");
            }
        } else {
        	PalLog.d(TAG, " headers=null");
        }

        if (response == null) {
        	PalLog.d(TAG, " response=null");
        } else {
        	PalLog.d(TAG, " response status_code="
                    + response.getStatusLine().getStatusCode());
        }
        PalLog.d(TAG, "***************************************");
    }
}
