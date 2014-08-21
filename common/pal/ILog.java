package common.pal;

/**
 * 日志功能类.
 * 
 * 本接口只做各平台定方静态方法用，
 * 因为静态方法不能接口化或抽像化,所以该接口并不做实现.只做方法定义。
 * 
 * @author roy.wong1984@gmail.com
 *
 */
public interface ILog {

	// Field descriptor #8 I
	public static final int VERBOSE = 2;

	// Field descriptor #8 I
	public static final int DEBUG = 3;

	// Field descriptor #8 I
	public static final int INFO = 4;

	// Field descriptor #8 I
	public static final int WARN = 5;

	// Field descriptor #8 I
	public static final int ERROR = 6;

	// Field descriptor #8 I
	public static final int ASSERT = 7;
	
	/**
	 * Send a DEBUG log message.
	 * 
	 * @param tag
	 * @param msg
	 * @param outdest
	 */
	public  void d(String tag, String msg) ;

	/**
	 * Send a VERBOSE log message.
	 * 
	 * @param tag
	 * @param msg
	 * @param outdest
	 */
	public  void v(String tag, String msg) ;

	/**
	 * Send an ERROR log message.
	 * 
	 * @param tag
	 * @param msg
	 * @param outdest
	 */
	public  void e(String tag, String msg) ;

	/**
	 * Send an INFO log message.
	 * 
	 * @param tag
	 * @param msg
	 * @param outdest
	 */
	public void i(String tag, String msg) ;

	/**
	 * Send a WARN log message.
	 * 
	 * @param tag
	 * @param msg
	 * @param outdest
	 */
	public  void w(String tag, String msg) ;
	
	
}
