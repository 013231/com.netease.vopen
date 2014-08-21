package mblog.base;

public class SendBlogResult {
	private int mBlogType;
	private String mId;
	
	public SendBlogResult(int blogType) {
		mBlogType = blogType;
	}
	
	public int getBlogType(){
		return mBlogType;
	}
	
	public void setId(String id){
		mId = id;
	}
	public String getId(){
		return mId;
	}
}
