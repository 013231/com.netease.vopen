package vopen.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.apache.http.HttpStatus;

import vopen.protocol.VopenServiceCode;

import common.framework.http.HttpCallBack;
import common.framework.http.HttpEngine;
import common.framework.http.HttpRequest;
import common.framework.task.DataChannel;
import common.framework.task.NotifyTransaction;
import common.framework.task.Transaction;
import common.framework.task.TransactionEngine;
import common.pal.IHttp;
import common.util.NameValuePair;

public class ImageDataChannel extends DataChannel implements HttpCallBack {

	/**
	 * Hashtable<RequestID,TransactionID> 
	 */
	Hashtable mTransactionMap;
	
	/**
	 *  Hashtable<TransactionID,HttpRequest> 
	 */
	Hashtable mRequestMap;
	
	HttpEngine mHttpEngine;
	
	public ImageDataChannel(TransactionEngine engine,HttpEngine httpEngine){
		super(engine);
		mTransactionMap = new Hashtable(); 
		mRequestMap = new Hashtable();
		mHttpEngine = httpEngine; 
	}
	


	public void sendRequest(Object obj, Transaction t) {
		// TODO Auto-generated method stub
		if(obj instanceof HttpRequest){
			sendRequest((HttpRequest)obj,t);
		}else{
			t.onTransactException(new IllegalArgumentException());
		}
	}
	
	
	public void sendRequest(HttpRequest request,Transaction transaction)
	{
		//  GZIP 压缩
//		request.addHeaderField("Accept-Encoding", "gzip");	
		
//		request.addHeaderField("Accept-Charset", "utf-8");
		
		synchronized (mTransactionMap)
		{
			Integer txId = new Integer(transaction.getId());
			Integer requestId = new Integer(request.getRequestID());
			mTransactionMap.put(requestId, txId);
			mRequestMap.put(txId, request);
			request.setHttpCallBack(this);
			mHttpEngine.addRequest(request);
		}
	}

	/**
	 * 出错返回
	 */
	public void onError(int requestId, int errCode, String errStr)
	{
		
//		try {
//			if(errStr != null){
//				String encoding = http.getHeaderField("Content-Encoding");
//				if (encoding != null && encoding.equals("gzip")) {
//					errStr = PalPlatform.gzipDecompress(errStr);
//				}	
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			errCode =  VopenServiceCode.ERR_GZIP_EXCEPTION;
//			errStr = null;
//		}
//		String str = null;
//		if(errStr != null) {
//			try {
//				str = new String(errStr, "utf-8");
//			} catch (UnsupportedEncodingException e) {
//				str = new String(errStr);
//			}
//		}
		
		switch (errCode) {
		case HttpEngine.ERR_NETWORK_CANCEL:
			errCode = VopenServiceCode.ERR_NETWORK_CANCEL;
			break;
		case HttpEngine.ERR_NETWORK_DONT_CONNECT:
			errCode = VopenServiceCode.ERR_NETWORK_DONT_CONNECT;
			break;
		case HttpEngine.ERR_NETWORK_OTHER:
			errCode = VopenServiceCode.ERR_NETWORK_OTHER;
			break;
		case HttpEngine.ERR_NETWORK_SECURITY:
			errCode = VopenServiceCode.ERR_NETWORK_SECURITY;
			break;
		case HttpEngine.ERR_DATA_PARSE:
			errCode =VopenServiceCode.ERR_DATA_PARSE;
			break;
		case HttpEngine.ERR_GZIP_EXCEPTION:
			errCode =VopenServiceCode.ERR_GZIP_EXCEPTION;
			break;
		case HttpStatus.SC_UNAUTHORIZED://401
			errCode = VopenServiceCode.TOKEN_ERR;
			break;
		case HttpStatus.SC_SERVICE_UNAVAILABLE://503
			errCode = VopenServiceCode.SEVER_BEING_MAINTAIN;
			break;	
		default:
			break;
		}	
		// TODO Auto-generated method stub
		synchronized (mTransactionMap)
		{
			Integer tid = (Integer)mTransactionMap.remove(new Integer(requestId));
			if(tid != null){
				mRequestMap.remove(tid);
				mTransactionEngine.notifyError(tid, errCode, errStr);
			}
		}
	}

	/**
	 * 当网络数据接收完成返回
	 */
	public void onReceived(int requestId, String data, IHttp http)
	{
		// TODO Auto-generated method stub
//		try {
//			String encoding = http.getHeaderField("Content-Encoding");
//			if (encoding != null && encoding.equals("gzip")) {
//				data = PalPlatform.gzipDecompress(data);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			onError(requestId, VopenServiceCode.ERR_GZIP_EXCEPTION,null);
//			return ;
//		}
	    NameValuePair[] pairs = null;
	    try {
            pairs = http.getHeaderField();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		synchronized (mTransactionMap)
		{
			
			Integer key = new Integer(requestId);
			if(mTransactionMap.containsKey(key)){
				Integer tid = (Integer)mTransactionMap.remove(key);
				mRequestMap.remove(tid);
				NotifyTransaction notify = new NotifyTransaction(mTransactionEngine, tid, data,pairs);
				mTransactionEngine.beginTransaction(notify);
			}
		}
	}
	
	/**
	 * 取消一个网络连接.
	 * @param transactionId
	 */
	public void cancelRequest(Transaction t){
		synchronized (mTransactionMap){
			Integer key = new Integer(t.getId());
			if(mRequestMap.containsKey(key)){
				HttpRequest request = (HttpRequest)mRequestMap.remove(key);
				request.doCancel();
				mTransactionMap.remove(new Integer(request.getRequestID()));
			}	
		}
	}
	
	public void close(){
		if(mHttpEngine != null){
			mHttpEngine.shutdown();
		}
	}


	public void onReceived(int requestId, InputStream stream, long contentLength, IHttp http) {

	    NameValuePair[] pairs = null;
	    try {
            pairs = http.getHeaderField();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Integer tid = null;
		synchronized (mTransactionMap)
		{
			
			Integer key = new Integer(requestId);
			if(mTransactionMap.containsKey(key)){
				tid = (Integer)mTransactionMap.remove(key);
				mRequestMap.remove(tid);
			}
		}
		
		if(tid != null)
			mTransactionEngine.notifyIncomingPrimitive(tid, stream,pairs);
		
	}

}
