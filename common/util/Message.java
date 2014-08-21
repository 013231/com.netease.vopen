package common.util;



public class Message {

	/**
	 * Message ID
	 */
	int mWhat;
	
	public int arg1,arg2;
	
	public Object arg3;
	
	IHandler m_handler;
	
	boolean isError;
	
	public int getMessageID(){
		return mWhat;
	}
	
	public IHandler getHandler(){
		return m_handler;
	}
	
	
	private Message(int what,IHandler handler){
		mWhat = what;
		m_handler = handler;
	}
	
	public boolean isError()
	{
		return isError;
	}
	
	
	
	public static Message createError(int what ,IHandler handler)
	{
		return createError(what,-1,-1,null,handler);
	}
	
	/**
	 * 创建一个error消息
	 * @param mid
	 * @param error
	 * @param handler
	 * @return
	 */
	public static Message createError(int what,int arg1,int arg2,Object arg3,IHandler handler)
	{
		Message message = new Message(what,handler);
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.arg3 = arg3;
		message.isError = true;
		return message;
	}
	
	
	public static Message createMessage(int what,IHandler handler)
	{
		return createMessage(what,-1,-1,null,handler);
	}
	/**
	 * 创建一个message消息
	 * @param mid
	 * @param message
	 * @param handler
	 * @return
	 */
	public static Message createMessage(int what,int arg1,int arg2,Object arg3,IHandler handler)
	{
		Message message = new Message(what,handler);
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.arg3 = arg3;
		return message;
	}
	
	
	

	
}


