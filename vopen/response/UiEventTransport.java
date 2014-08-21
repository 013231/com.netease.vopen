package vopen.response;

public class UiEventTransport {
	private int mEventType;
	private Object mData;
	public UiEventTransport(int type){
		mEventType = type;
	}
	public void setEventType(int type) {
		mEventType = type;
	}
	public int getEventType() {
		return mEventType;
	}
	public void setData(Object data){
		mData = data;
	}
	public  Object getData() {
		return mData;
	}
	/**
	 * 通知开始下载
	 */
	public static final int UIEVENT_TYPE_START_DOWNLOAD = 0x0 << 8 | 0x01;
	/**
	 * 通知先暂停当前下载再开始下载
	 */
	public static final int UIEVENT_TYPE_PAUSE_START_DOWNLOAD = 0x0 << 8 | 0x02;
	/**
	 * 通知先更新下载列表再开始下载
	 */
	public static final int UIEVENT_TYPE_UPDATE_DOWNLOADLIST = 0x0 << 8 | 0x03;
	/**
	 * 收藏同步取完详情后通知收藏界面刷新
	 */
	public static final int UIEVENT_TYPE_UPDATE_SYNCSTORE = 0x0 << 8 | 0x04;
	/**
	 * 通知手机关机事件
	 */
	public static final int UIEVENT_TYPE_BOOT_SHUTDOWN = 0x0 << 8 | 0x05;
}
