package vopen.protocol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

import vopen.transactions.BaseTransaction;

import common.framework.http.HttpRequest;
import common.util.Util;

public class CommentProtocol {
	/**热门跟贴URL*/
	public static final String URL_HOT_COMMENTS_URL = "http://comment.api.163.com/api/json/post/list/hot/%s/%s/%s/%s/%s/%s/%s";
	/**最新跟贴URL*/
    public static final String URL_LATEST_COMMENTS_URL = "http://comment.api.163.com/api/json/post/list/normal/%s/%s/desc/%s/%s/%s/%s/%s";
    
    /**一栋楼的URL*/
    public static final String URL_ONEBUILDDING_URL = "http://comment.api.163.com/api/json/post/load/%s/%s_%s";
    
    /**一栋楼的分享URL*/
    public static final String URL_SHARE_ONEBUILDDING_URL = "http://comment.api.163.com/%s/%s/%s.html";
    /**一栋楼图片快照的分享URL*/
    public static final String URL_SHARE_ONEBUILDDING_PIC_URL = "http://webshot.ws.126.net/%s/%s/%s.html";
    /**生成短地址URL*/
//    public static final String URL_GEN_SHORT_URL = "http://c.3g.163.com/nc/shorturl/getShort/www/%s.html";
    public static final String URL_GEN_SHORT_URL = "http://c.3g.163.com/nc/shorturl/getShort/wwwo/%s.html";
   
    
    /**查看某篇文章的跟帖数*/
    public static final String URL_GET_COMMENT_COUNT = "http://comment.api.163.com/api/json/thread/total/%s/%s";
    
    
    /**每次显示更贴个数*/
    public static final String REPLY_BOARD = "video_bbs";
	
	
	/** =====================跟贴信息========================*/
	/**隐藏楼层的条件数、、到达次数隐藏楼层*/
    public static final int HIDE_CONDITION_NUMS = 10;
	/**热门跟贴显示个数*/
    public static final int HOT_COMMNET_NUMS = 5;
	/**最新跟贴显示个数*/
    public static final int LATEST_COMMNET_NUMS = 10;
	/**隐藏楼层上部跟贴数*/
    public static final int HIDE_TOP_NUM = 2;
	/**隐藏楼层下部跟贴数*/
    public static final int HIDE_BOTTOM_NUM = 2;
    /**每次显示更贴个数*/
    public static final int PERTIME_SHOW_COMMENT_NUMS = 20;


    /**发表跟贴URL*/
    public static final String URL_POST_COMMENT = "http://comment.api.163.com/api/jsonp/post/insert";
    
    /**顶贴URL*/
    public static final String URL_VOTE_COMMENT_UP = "http://comment.news.163.com/reply/upvote/";
    
	/**版块ID*/
	private final static String REPLY_BOARDID = "board";
	/**THREADID【文章ID】*/
	private final static String REPLY_ID = "threadid";
	/**引用内容*/
	private final static String QUOTE_TAG = "quote";
	/**发帖内容*/
	private final static String BODY_TAG = "body";
	/**用户账号*/
	private final static String USERID_TAG = "userid";
	/**应用名称*/
	private final static String NICKNAME_TAG = "nickname";
	/**发帖IP*/
	private final static String IP_TAG = "ip";
	/**隐藏名称*/
	private final static String HIDDENAME_TAG = "hidename";
	
    
    public static HttpRequest createGetCommentRequest(int type, String replyId, int start, int end){
    	String url = null;
    	if(type == BaseTransaction.TRANSACTION_GET_HOT_COMMENT)
    		url = URL_HOT_COMMENTS_URL;
    	else
    		url = URL_LATEST_COMMENTS_URL;
    	
    	url = String.format(url, REPLY_BOARD, replyId,
    				start, end, HIDE_CONDITION_NUMS,
    				HIDE_TOP_NUM, HIDE_BOTTOM_NUM);

    	HttpRequest request = new HttpRequest(url);
    	request.addHttpParams(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
    	return request;
    }
    
    public static HttpRequest createGetWholeCommentRequest(String replyId, String plid){
    	String url = String.format(URL_ONEBUILDDING_URL, REPLY_BOARD, replyId,plid);

    	HttpRequest request = new HttpRequest(url);
    	request.addHttpParams(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
    	return request;
    }
    
    public static HttpRequest createPostCommentRequest(String threadid, String qoute,
    		String body, String userId, String nickName, boolean hideName){
    	
    	List<NameValuePair> postParams = new ArrayList<NameValuePair>();
    	postParams.add(new BasicNameValuePair(REPLY_BOARDID, REPLY_BOARD));
    	postParams.add(new BasicNameValuePair(REPLY_ID, threadid));
    	
    	if(!Util.isStringEmpty(qoute))
    		postParams.add(new BasicNameValuePair(QUOTE_TAG, qoute));
    	if(!Util.isStringEmpty(body))
    		postParams.add(new BasicNameValuePair(BODY_TAG, body));
    	if(!Util.isStringEmpty(userId))
    		postParams.add(new BasicNameValuePair(USERID_TAG, userId));
    	if(!Util.isStringEmpty(nickName))
    		postParams.add(new BasicNameValuePair(NICKNAME_TAG, nickName));
    	postParams.add(new BasicNameValuePair(IP_TAG, "0.0.0.0"));
    	postParams.add(new BasicNameValuePair(HIDDENAME_TAG, Boolean.toString(hideName)));
  
    	HttpRequest request = new HttpRequest(URL_POST_COMMENT);
		request.setRequestMethod(HttpRequest.METHOD_POST);
		//不设置会有417错误, 其他API没有错误, 但原来的都设置了这个, 偶也先设上
		request.addHttpParams(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return request;
        
    }

	public static HttpRequest createVoteCommentRequest(String docId, String postId) {
		String url;        		
		if(postId.contains(docId)) {
			url = String.format("%s%s%s%s", URL_VOTE_COMMENT_UP, REPLY_BOARD, "/" , postId);
		} else {
			url = String.format("%s%s%s%s%s%s", URL_VOTE_COMMENT_UP, REPLY_BOARD, "/", docId, "_", postId);
		}
		
		HttpRequest request = new HttpRequest(url);
		request.setRequestMethod(HttpRequest.METHOD_POST);
		request.addHeaderField("referer", "http://v.163.com");
		request.addHttpParams(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
		return request;
	}
	
	public static HttpRequest createGetPicShortUrlRequest(String picUrl) {
		HttpRequest request = new HttpRequest(picUrl);
		return request;
	}
	
	public static HttpRequest createGetCommentShortUrlRequest(String commentUrl) {
		String url = String.format(CommentProtocol.URL_GEN_SHORT_URL, commentUrl);
		HttpRequest request = new HttpRequest(url);
		return request;
	}
	
	public static HttpRequest createGetCommentCountRequest(String docId) {
		String url = String.format(CommentProtocol.URL_GET_COMMENT_COUNT, REPLY_BOARD,docId);
		HttpRequest request = new HttpRequest(url);
		return request;
	}
}
