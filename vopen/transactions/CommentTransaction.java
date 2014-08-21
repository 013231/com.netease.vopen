package vopen.transactions;

import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.CommentProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CommentBuildingListInfo;
import vopen.response.CommentCount;
import vopen.response.OneWholeCommentBuildingInfo;
import vopen.response.PostCommentStatus;
import android.util.Log;

import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.util.NameValuePair;
import common.util.Util;

public class CommentTransaction extends BaseTransaction {
	private String mReplyId;
	private String mPlid;
	private int mCommonStart;
	private int mLatestEnd;
	
	
	//private String board //版面ID
	private String mThreadid; //文章ID
	private String mQoute; //回复的贴子ID(可无)
	private String mBody; //跟贴内容
	private String mUserid; //用户通行证
	private String mNickname; //昵称
//	private String mIp; //发贴的IP地址
	private boolean mHidename; //值：false，是否匿名发贴
	
	private String mDocId;
	private String mPostId;
	
	private String mUrl;
    public CommentTransaction(TransactionEngine transMgr, int type) {
        super(transMgr, type);
    }

    public static CommentTransaction createGetPicShortUrl(TransactionEngine transMgr, String picUrl){    		
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_GET_COMMENT_PIC_SHORT_URL);
    	t.mUrl = picUrl;
    	return t;
    }
    public static CommentTransaction createGetCommentShortUrl(TransactionEngine transMgr, String commentUrl){    		
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_GET_COMMENT_SHORT_URL);
    	t.mUrl = commentUrl;
    	return t;
    }
    
    
    public static CommentTransaction createPostCommentTran(TransactionEngine transMgr,
    		String threadid, String qoute, String body, String userId, String nickName, boolean hideName){
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_POST_COMMENT);
    	t.mThreadid = threadid;
    	t.mQoute = qoute;
    	t.mBody = body;
    	t.mUserid = userId;
    	t.mNickname = nickName;
    	t.mHidename = hideName;
    	return t;
    }
    public static CommentTransaction createVoteCommentTran(TransactionEngine transMgr, String docId, String postId){
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_VOTE_COMMENT);
    	t.mDocId = docId;
    	t.mPostId = postId;
    	return t;
    }
    
    public static CommentTransaction createGetHotCommentTransaction(TransactionEngine transMgr,
    		String replyId, int start, int end){
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_GET_HOT_COMMENT);
    	t.mReplyId = replyId;
    	t.mCommonStart = start;
    	t.mLatestEnd = end;
    	return t;
    }
    
    public static CommentTransaction createGetLatestCommentTransaction(TransactionEngine transMgr,
    		String replyId, int start, int end){
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_GET_LATEST_COMMENT);
    	t.mReplyId = replyId;
    	t.mCommonStart = start;
    	t.mLatestEnd = end;
    	return t;
    }
    
    public static CommentTransaction createGetWholeCommentTran(TransactionEngine transMgr,
    		String replyId, String plid){
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_GET_WHOLE_COMMENT);
    	t.mReplyId = replyId;
    	t.mPlid = plid;
    	return t;
    }
    public static CommentTransaction createGetCommentCountTran(TransactionEngine transMgr,
    		String replyId){
    	CommentTransaction t = new CommentTransaction(transMgr, TRANSACTION_GET_COMMENT_COUNT);
    	t.mReplyId = replyId;
    	return t;
    }
    
    
    @Override
    public void onTransact() {
    	HttpRequest request = null;
    	switch (getType()) {
		case TRANSACTION_GET_HOT_COMMENT:
		case TRANSACTION_GET_LATEST_COMMENT:
			request = CommentProtocol.createGetCommentRequest(mType, mReplyId, mCommonStart, mLatestEnd);
			break;
		case TRANSACTION_GET_WHOLE_COMMENT:
			request = CommentProtocol.createGetWholeCommentRequest(mReplyId, mPlid);
			break;
		case TRANSACTION_POST_COMMENT:
			request = CommentProtocol.createPostCommentRequest(mThreadid, mQoute,
					mBody, mUserid, mNickname, mHidename);
			break;
		case TRANSACTION_VOTE_COMMENT:
			request = CommentProtocol.createVoteCommentRequest(mDocId, mPostId);
			break;
		case TRANSACTION_GET_COMMENT_PIC_SHORT_URL:
			request = CommentProtocol.createGetPicShortUrlRequest(mUrl);
			break;
		case TRANSACTION_GET_COMMENT_SHORT_URL:
			request = CommentProtocol.createGetCommentShortUrlRequest(mUrl);
			break;
		case TRANSACTION_GET_COMMENT_COUNT:
			request = CommentProtocol.createGetCommentCountRequest(mReplyId);
			break;
			
		default:
			break;
		}
		
		if (request != null && !isCancel()) {
			sendRequest(request);
		}
		else{
			getTransactionEngine().endTransaction(this);
		}
    }

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
//		Log.d("CommentTransaction", response);
		switch (getType()) {
		case TRANSACTION_GET_HOT_COMMENT:
		case TRANSACTION_GET_LATEST_COMMENT:
			try {
				JSONObject jobj = new JSONObject(response);
				if (null != jobj) {
					CommentBuildingListInfo info = new CommentBuildingListInfo();
					info.readFromJSONObject(jobj);
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, info);
				}
			} catch (JSONException e) {
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
				e.printStackTrace();
			}
			break;
		
		case TRANSACTION_GET_WHOLE_COMMENT:
			try {
				JSONObject jobj = new JSONObject(response);
				if (null != jobj) {
					OneWholeCommentBuildingInfo info = new OneWholeCommentBuildingInfo();
					info.readFromJSONObject(jobj);
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, info);
				}
			} catch (JSONException e) {
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
				e.printStackTrace();
			}
			break;
		case TRANSACTION_POST_COMMENT:
			try {
				JSONObject jobj = new JSONObject(response);
				if (null != jobj) {
					PostCommentStatus info = new PostCommentStatus(jobj);
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, info);
				}
			} catch (JSONException e) {
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
				e.printStackTrace();
			}
			break;
			
		case TRANSACTION_VOTE_COMMENT:
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, null);
			break;
		case TRANSACTION_GET_COMMENT_PIC_SHORT_URL:
			if(!Util.isStringEmpty(response)){
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, response);
			}
			else{
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
			}
			break;
		case TRANSACTION_GET_COMMENT_SHORT_URL:
			try {
				JSONObject jobj = new JSONObject(response);
				String commentUrl = jobj.getString("shortURL");
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, commentUrl);
			} catch (JSONException e) {
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
				e.printStackTrace();
			}
			break;
		case TRANSACTION_GET_COMMENT_COUNT:
			try {
				JSONObject jobj = new JSONObject(response);
				if (null != jobj) {
					CommentCount countInfo = new CommentCount(jobj);
					notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, countInfo);
				}
			} catch (JSONException e) {
				notifyError(VopenServiceCode.ERR_DATA_PARSE,
						ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
}


