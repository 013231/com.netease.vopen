package common.framework.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.http.HttpStatus;

import common.pal.IHttp;
import common.pal.PalHttp;
import common.pal.PalLog;
import common.pal.PalPlatform;
import common.util.LinkedBlockingQueue;
import common.util.Util;

public class HttpEngine implements Runnable
{
	
	/**
	 * Net 网络错误值.
	 */
	public static final int NET_CODE = 6000;
	
	// 网络安全异常错误
	public static final int ERR_NETWORK_SECURITY = NET_CODE + 1;
	// 网络无法链接
	public static final int ERR_NETWORK_DONT_CONNECT = NET_CODE + 2;
	// 其它网络错误
	public static final int ERR_NETWORK_OTHER = NET_CODE + 3;
	// 网络请求取消
	public static final int ERR_NETWORK_CANCEL = NET_CODE + 4;
	
	
	/**
	 * token错误
	 */
	public static final int TOKEN_ERR = NET_CODE + 5;
	
	// 服务器正在维护
	public static final int SEVER_BEING_MAINTAIN = NET_CODE + 6;
	
	/**
	 * Net 数据处理错误.
	 */
	public static final int DATA_CODE = 8000;
	public static final int ERR_DATA_PARSE = DATA_CODE + 1;
	public static final int ERR_GZIP_EXCEPTION = DATA_CODE + 2;
	
	
	private static final int MAX_RETRY_COUNT = 3; // 最大重连次数
	private static final int NET_BUFFER = 1 * 1024; // 数据接收buff
//	private static final int NET_TIMEOUT = 1 * 60 * 1000; //2分钟超时
	
	LinkedBlockingQueue mReqeustQueue; // 请求队列
	Thread mThread; // 执行线程
	boolean mStop = false; // 是关停止
	static HttpEngine mEngine;
	static int mReqeustNum = 0;
	
	IHttp mRunningHttp;

	public HttpEngine()
	{
		mReqeustQueue = new LinkedBlockingQueue();
	}


	public void addRequest(HttpRequest request)
	{
		mReqeustQueue.put(request);
		if (mThread == null)
		{
			mThread = new Thread(this);
			mThread.start();
		}
	}

	public void shutdown()
	{
		if (mThread != null)
		{
			mStop = true;
			
			closeRunningHttp();
			
			mReqeustQueue.interrupt();
			mThread.interrupt();
			mThread = null;
		}
	}
	
	private void closeRunningHttp() {
		if(mRunningHttp != null){
			try {
				mRunningHttp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mRunningHttp = null;
		}
	}

	/**
	 * HttpEngine 执行主线程
	 */
	public void run()
	{
		PalLog.d("HttpEngine","Http Engine start");
		
		while (!mStop)
		{
			Object obj = mReqeustQueue.take();
			
			if (obj != null)
			{
				HttpRequest request = (HttpRequest) obj;
				int times = 0;
				
				while (!mStop && times < MAX_RETRY_COUNT && !request.isCancel())
				{
					long start = System.currentTimeMillis();
					try
					{
						int code = doHttpReqeust(request);
						if(code != -1){
							break;
						}
//						if(code == 200){
//							break;
//						}else if(code != -1){
//							notifyError(request, code,null);
//							break;
//						}
					} catch (IOException e)
					{
						e.printStackTrace();
						PalLog.e("[Error]HttpEngine","Http Engine Catch IOException : "
								+ e.toString());	
						
						if(System.currentTimeMillis() - start >= request.getRequestTimeOut()){
							notifyError(request, ERR_NETWORK_DONT_CONNECT,
									null);
							break;
						}
					}catch (SecurityException e)
					{
						e.printStackTrace();
						PalLog.e("[Error]HttpEngine","Http Engine Catch SecurityException : "
								+ e.toString());
						notifyError(request, ERR_NETWORK_SECURITY,
								null);
						break;
						
					}  catch (Exception e)
					{
						e.printStackTrace();
						PalLog.e("[Error]HttpEngine","Http Engine Catch Exception : "
								+ e.toString());	
						notifyError(request, ERR_NETWORK_OTHER, null);
						break;
					}
					times++;
				}
				
				if(isCancel(request))		//取消
				{
					notifyError(request, ERR_NETWORK_CANCEL, null);
				}else if(times >= MAX_RETRY_COUNT){	//重试失败
					notifyError(request, ERR_NETWORK_DONT_CONNECT,
							null);
				}
			}
			obj = null;
		}
		PalLog.d("HttpEngine","Http Engine End");
	}

	/**
	 * 
	 * @param request
	 * @return	请求的ResponseCode值, 需要重试返回-1.
	 * @throws IOException
	 */
	private int doHttpReqeust(HttpRequest request) throws IOException{
		
		try
		{
			//执行前预处理, 加token
			//执行前预处理 ，加user agent
			request.buildHttpRequest();
			trySend(request);
			//执行后处理,处理token过期重登录
			boolean handled = request.afterExec(mRunningHttp);
			
			if(handled){
				request.buildHttpRequest();
				trySend(request);
			}
			int reponseCode = mRunningHttp.getResponseCode();
			if(isCancel(request))
			{
				return reponseCode;
				
			//}else if (reponseCode == 200){
			}else{
				String type = mRunningHttp.getHeaderField("Content-Type");
				if (type != null
						&& type.indexOf("vnd.wap.wml") >= 0)
				{
					PalLog.d("HttpEngine", "Content-Type:" + type);
					return -1;//重试
				}else{
				    if (request.isStreamCallBack() && reponseCode == HttpStatus.SC_OK) 
					{
						InputStream in = null;
						try {
							in = mRunningHttp.openInputStream();
							notifyReceived(request, in, mRunningHttp.getContentLength(), mRunningHttp);
						}
						finally {
							if (in != null) {
								in.close();
								in = null;
							}
						}
					} else {
						if(reponseCode !=  HttpStatus.SC_OK){
							System.out.println(reponseCode);
						}
//						byte[] data = readData(mRunningHttp, request);
//						if(data != null && !isCancel(request)){
//							String ContentType = mRunningHttp.getHeaderField("Content-Type");
//							String charset = Util.getCharset(ContentType);
//							String strData = null;
//							try {
//								strData = Util.processByteDataToString(data, mRunningHttp, charset);
//								if(reponseCode == HttpStatus.SC_OK) {
//									notifyReceived(request, strData, mRunningHttp);
//								} else {
//									notifyError(request, reponseCode, strData);
//								}
//							} catch (UnsupportedEncodingException e1) {
//								e1.printStackTrace();
//								notifyError(request,ERR_DATA_PARSE, null);
//								
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//								notifyError(request, ERR_GZIP_EXCEPTION, null);
//							}
//						}
						
						InputStream in = null;
						StringBuffer sb = new StringBuffer();
				        String inputLine = "";
						try {
							in = mRunningHttp.openInputStream();
							String ContentType = mRunningHttp.getHeaderField("Content-Type");
                            String charset = Util.getCharset(ContentType);
                            
	                    	
                			String encoding = mRunningHttp.getHeaderField("Content-Encoding");
                			if (encoding != null && encoding.equals("gzip")) {
                				in = PalPlatform.gzipDecompress(in);
                				PalLog.i("HttpEngine", "is gzip.....");
                			}
	                		
                            
							BufferedReader is = new BufferedReader(new InputStreamReader(in, charset));
			                while ((inputLine = is.readLine()) != null) {

			                    sb.append(inputLine);
			                    
			                }
			                
			                if(reponseCode == HttpStatus.SC_OK) {
								notifyReceived(request, sb.toString(), mRunningHttp);
							} else {
								notifyError(request, reponseCode, sb.toString());
							}
						}
						finally {
							if (in != null) {
								in.close();
								in = null;
							}
						}
						
					}
				}
			}			
			return reponseCode;
		} finally 
		{
			closeRunningHttp();
		}
	}
	
	/**
	 * 尝试发送数据
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private IHttp trySend(HttpRequest request) throws IOException
	{
		request.doBefore();//为了兼容绑定微博的处理
		String url = request.getUrl();
		if (!url.startsWith("http://"))
		{
			url = "http://" + url;
		}
		
		mRunningHttp = PalHttp.createHttp(request.getUrl(),request.getMethod());
		mRunningHttp.setTimeout(request.getRequestTimeOut());
		try
		{
		//	http.open(request.getUrl());
		//	http.setRequestMethod(request.getMethod());
			Hashtable header = request.getHeaderField();
			if (header != null && header.size() > 0)
			{
				Enumeration enu = header.keys();
				while (enu.hasMoreElements())
				{
					String key = (String) enu.nextElement();
					String value = (String) header.get(key);
					mRunningHttp.setRequestProperty(key, value);
				}
			}
			
			if(request.getMethod().equals(HttpRequest.METHOD_POST)){
				
				if(request.getHttpEntity() != null){
					mRunningHttp.setHttpEntity(request.getHttpEntity());
				}
				else{
					if(request.getPostData() != null){
						mRunningHttp.postByteArray(request.getPostData());
					}
					if(request.getMultiPartFile() != null){
						String file = request.getMultiPartFile();
						Hashtable t = request.getMultiPartParams();
						mRunningHttp.postMultiPart(file, t);
					}
				}
			}
			
			mRunningHttp.setHttpParams(request.getHttpParams());
			
			mRunningHttp.execute();
			
		} catch (SecurityException e)
		{
			mRunningHttp.close();
			mRunningHttp = null;
			throw e;
		} catch (IOException e)
		{
			e.printStackTrace();
			mRunningHttp.close();
			mRunningHttp = null;
			PalLog.e("[Error]HttpEngine", "HttpEngine trySend IOException");
			throw e;
		}
		
		
		return mRunningHttp;
	}

	/**
	 * 读取Resonpse数据
	 * 
	 * @param http
	 * @param reqeust
	 * @return
	 * @throws IOException
	 */
	private byte[] readData(IHttp http,HttpRequest request) throws IOException
	{   InputStream inStream = null;
		try
		{
			inStream = http.openInputStream();
			
			ByteArrayOutputStream buff = new ByteArrayOutputStream();
			
			if (http.getHeaderField("Content-Length") != null)
			{
				int len = (int)http.getContentLength();
				readData(request, inStream, buff, len);
				return buff.toByteArray();
				
			} else
			{
				int numread = 0;
				long timeout = System.currentTimeMillis();
				
				byte[] tmp = new byte[NET_BUFFER];
				
				while (!isCancel(request))
				{
					numread = inStream.read(tmp, 0, tmp.length);

					long t = System.currentTimeMillis();
					if (numread < 0) {
						break;
					} else if (numread == 0) {
						if (t - timeout > 5000) {
							break;
						}
						sleep(50);
					} else {
						buff.write(tmp, 0, numread);
						timeout = t;
					}
				}
				return buff.toByteArray();
			}
			
		} finally{
			if(inStream != null){
				inStream.close();
				inStream = null;
			}
		}
	}

	private void readData(HttpRequest request, InputStream in, OutputStream out, int preferLength) throws IOException {
		int numread = 0;
		int count = 0;
		byte[] data = new byte[1024];
		while (!isCancel(request) && count < preferLength)
		{
			numread = in.read(data, 0, Math.min(data.length, preferLength - count));

			if (numread == -1)
			{ // 数据长度 与size 大小不匹配.
				throw new IOException();
			} else
			{
				out.write(data, 0, numread);
				count += numread;
			}
			sleep(10);
		}
	}
	
	/**
	 * 返回是否中断
	 * 
	 * @param reqeust
	 * @return
	 */
	private boolean isCancel(HttpRequest request)
	{
		return mStop || request.isCancel();
	}

	/**
	 * http请求.流式数据接收.
	 * 
	 * @param request
	 * @param data
	 * @param size
	 */
	private void notifyReceived(HttpRequest request, String data, IHttp http)
	{
		if(request.getHttpCallBack() != null)
		{
			request.getHttpCallBack().onReceived(request.getRequestID(), data, http);
		}
	}
	
	private void notifyReceived(HttpRequest request, InputStream stream, long contentLength, IHttp http)
	{
		if(request.getHttpCallBack() != null)
		{
			request.getHttpCallBack().onReceived(request.getRequestID(), stream, contentLength, http);
		}
	}

	/**
	 * http请求出错. 请出错消息回调给请求者
	 * 
	 * @param request
	 * @param err
	 */
	private void notifyError(HttpRequest request, int errCode, String msg)
	{
		if(request.getHttpCallBack() != null)
		{
			request.getHttpCallBack().onError(request.getRequestID(), errCode, msg);
		}
	}

	private void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	public static synchronized int getNextRequestID()
	{
		return mReqeustNum++;
	}

}
