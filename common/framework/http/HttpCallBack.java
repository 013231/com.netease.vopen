package common.framework.http;

import java.io.InputStream;

import common.pal.IHttp;

public interface HttpCallBack
{
	
	public void onError(int requestId, int errCode, String errStr);
	
	public void onReceived(int requestId, String data, IHttp http);
	
	public void onReceived(int requestId, InputStream stream, long contentLength, IHttp http);
	
}
