package common.util;



public class MessageQueue {

	LinkedBlockingQueue mQueue;
	
	static MessageQueue m_instance;
	
	private MessageQueue()
	{
		mQueue = new LinkedBlockingQueue();
	}
	
	public static MessageQueue getInstance(){
		if(m_instance == null){
			m_instance = new MessageQueue();
		}
		return m_instance;
	}
	
	
	/**
	 * 添加一个消息
	 * @param message
	 */
	public void postMessage(Message message)
	{
		mQueue.put(message);
	}
	
	public Message takeMessage(){
		
		Object obj =  mQueue.take();
		if(obj != null){
			return (Message)obj;
		}else{
			return null;
		}
	}
	
	public int getMessageCount(){
		return mQueue.getSize();
	}
	
	public void interrupt(){
		mQueue.interrupt();
	}

	
}
