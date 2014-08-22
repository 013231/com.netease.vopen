package mblog.Douban;

import java.io.StringReader;

import mblog.base.BaseTransaction;
import mblog.base.ErrDescrip;
import mblog.base.SendBlogResult;
import mblog.xml.XMLTag;
import mblog.xml.XMLTagConstant;

import org.apache.http.entity.ByteArrayEntity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import vopen.app.BaseApplication;
import vopen.db.ManagerWeiboAccount;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;

import common.framework.http.HttpRequest;
import common.framework.http.THttpMethod;
import common.framework.task.AsyncTransaction;
import common.pal.PalLog;
import common.util.Util;


/**
 * 主要负责以下几大协议部份:
 * 		微博  StatuesInfo）
 * 		收藏  
 * 
 * @author Roy
 * 
 * 
 *
 */
public class BlogTransaction extends BaseTransaction {
	//发新微博
	private static final String HOST_MINIBLOG_SAYING = "http://api.douban.com/miniblog/saying";
	
	

	private String mContent;
	private String mImagePath;

	private String mName;
	private HttpRequest mHttpRequest;
	
	protected BlogTransaction(int type) {
		super(type);
	}
	
	public static AsyncTransaction createSendBlogTransaction(
			String name, String content, String imagePath) {
		int type = TRANSACTION_TYPE_BLOGSEND;
		if(!Util.isStringEmpty(imagePath)){
			type = TRANSACTION_TYPE_UPLOAD;
		}
		BlogTransaction bt = new BlogTransaction(type);
		bt.mImagePath = imagePath;
		bt.mContent = content;

		
		bt.mName = name;
		return bt;
	}
	
	
	
	private void setLogin(String tokenKey, String tokenSecret) {
		DoubanService.getInstance().setOauthToken(tokenKey, tokenSecret);
	}
	
	private boolean isLogin(){
		String[] tokens = ManagerWeiboAccount.getWBAccountToken(BaseApplication.getAppInstance(),mName, WeiboAccountColumn.WB_TYPE_DOUBAN);
		if(tokens!= null && tokens.length == 3){
			if(!Util.isStringEmpty(tokens[0]) && !Util.isStringEmpty(tokens[1])){
				setLogin(tokens[0], tokens[1]);
				return true;
			}
		}
		return false;
	}
	
	public void onTransact() {
		if(!isLogin())
		{
			notifyError(ErrDescrip.ERR_AUTH_FAIL, null);
			PalLog.e("***********", "Error  withou token & secret to send blog");
			return;
		}
		switch (getType()) {
		case TRANSACTION_TYPE_UPLOAD:
		case TRANSACTION_TYPE_BLOGSEND:
				mHttpRequest = createBlogSend(mContent, mImagePath);
			break;
			
					
		default:
			break;
		}
		
		if (mHttpRequest != null && !isCancel()) {
			sendRequest(mHttpRequest);
		}
		else {
			notifyError(ErrDescrip.ERR_COMMAND_FAIL, null);
			doEnd();
		}
	}
	
	public void onResponseSuccess(String response) {
		
		SendBlogResult results = null;

		switch (getType()) {
		case TRANSACTION_TYPE_BLOGSEND:
		case TRANSACTION_TYPE_UPLOAD:
			results = parsDoubanXml(response);
			break;
		
		}
		
		if (!isCancel()) {
			if (results != null) {
//				NTLog.i("BlogTransaction", results.getId());
				notifyMessage(ErrDescrip.SUCCESS,results);
			}
			else {
				notifyError(ErrDescrip.ERR_PARSE, null);
			}
		}
	}

	@Override
	public void onResponseError(int errCode, String errStr) {
		ErrDescrip error = DoubanService.getInstance().parseError(errCode, errStr);		
		notifyError(error.errCode, error);
	}
	
//	private SendBlogResult parseJson(String json){
//		SendBlogResult result = new SendBlogResult();
//		NTLog.i("Douban saying", json);
//		result.setId(json);
//			
//		return result;
//	}
	
	private SendBlogResult parsDoubanXml(String xml) {
		SendBlogResult result = new SendBlogResult(WeiboAccountColumn.WB_TYPE_DOUBAN);
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(xml));
			int eventType = parser.getEventType();

			// 当没有解析的文档的末尾的时候，一直执行
			loop: while (eventType != XmlPullParser.END_DOCUMENT) {

				// switch 解析的位置
				switch (eventType) {

				// 开始解析文档的时候，初始化对象集合
				case XmlPullParser.START_DOCUMENT:
					break;

				// 开始解析标签的时候，根据标签的不同名称。做不同操作
				case XmlPullParser.START_TAG:
					if ("id".equals(parser.getName())) {
						result.setId(parser.nextText());
					}
					break;

				// 当解析到标签结束的时候执行
				case XmlPullParser.END_TAG:
					break;
				}

				// 当前解析位置结束，指向下一个位置
				eventType = parser.next();
			}

		} catch (Exception e) {
			result = null;
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 发布一条微博。
	 * 
	 * @param status
	 *            必选参数，微博内容，不得超过163个字符；
	 * @param in_reply_to_status_id
	 *            可选参数，当评论指定微博时需带上此参数，值为被回复的微博ID；
	 * @param 
	 * @return
	 */
	private HttpRequest createBlogSend(String status, String imagePath)
	{
		String url = HOST_MINIBLOG_SAYING;
		String postData = null;
	
//		String saying = "<?xml version='1.0' encoding='UTF-8'?>"
//			+ "<entry xmlns:ns0=\"http://www.w3.org/2005/Atom\" xmlns:db=\"http://www.douban.com/xmlns/\">"
//			+ "<content>%s</content></entry>";
//		postData = String.format(saying, status);
		
		XMLTag xml = new XMLTag("entry");
		xml.addAttribute("xmlns:ns0", "http://www.w3.org/2005/Atom");
		xml.addAttribute("xmlns:db", "http://www.douban.com/xmlns/");
		XMLTag content = xml.createChild("content");
		content.setValue(status);
		
		postData = XMLTagConstant.XML_BANNER + xml.toString();
		
		ByteArrayEntity entity = new ByteArrayEntity(postData.getBytes());
		
		HttpRequest request = DoubanService.getInstance().getOauthClient().request(THttpMethod.POST, url, null, entity);
		request.addHeaderField("Content-Type", "application/atom+xml");
		return request;
	}

	
}
