package common.util;

public interface IHandler {

	public void onHandle(int mid,Message msg);
	
	public void onError(int mid,Message err);
}
