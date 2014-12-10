package vopen.protocol;

import java.util.List;

import vopen.response.AboutInfo;
import vopen.response.BindAccountInfo;
import vopen.response.CheckVersionInfo;
import vopen.response.CommentBuildingListInfo;
import vopen.response.CommentCount;
import vopen.response.CourseAdInfo;
import vopen.response.CourseInfo;
import vopen.response.HeadAd;
import vopen.response.HotWordInfo;
import vopen.response.OneWholeCommentBuildingInfo;
import vopen.response.PostCommentStatus;
import vopen.response.PushList;
import vopen.response.RecommAppInfo;
import vopen.response.RecommendColumn;
import vopen.response.RecommendInfo;
import vopen.response.RecommendItem;
import vopen.response.SyncItemInfo;
import vopen.response.UiEventTransport;
import vopen.transactions.BaseTransaction;
import android.os.Handler;
import android.os.Message;

public class VopenCallBack {	

	/*************************************************************
	 * success
	 *************************************************************/
	 /**
     * 获取视频列表返回成功
     */
//	Object[2];[0]: 进度, 0-100, [1]成功的数据, AllCourseInfo
	public void onGetVideoList(int transactionId, Object[] res){}
	/**
	 * 获取视频详情成功
	 */
	public void onGetVideoDetail(int transactionId, CourseInfo videoDetail){}
	/**
	 * 获取版本信息成功
	 */
	public void onGetVersionInfo(int transactionId, CheckVersionInfo versionInfo){}
	/**
     * 登录成功
     */
    public void onLogin(int transactionId, String cookie){}
    /**
     * 注册成功
     */
    public void onRegister(int transactionId){}
    /**
     * 同步收藏成功
     */
    public void onSyncFavorite(int transactionId,List<SyncItemInfo> list){}
    /**
     * 反馈成功
     */
    public void onFeedBack(int transactionId){}
    
    /**
     * 获取关于信息成功
     */
    public void onGetAboutInfo(int transactionId, AboutInfo aboutInfo){}
    /**
     * UI通知事件成功
     */
    public void onUiEvent(int transactionId,UiEventTransport event){}
    
    /**
     * 添加收藏成功
     */
    public void onAddStore(int transactionId, String storeInfo){}
    
    /**
     * 删除收藏成功
     */
    public void onDelStore(int transactionId, String storeInfo){}
    
    /**
     * 获取推送课程成功 
     */
    public void onGetPushCourse(int transactionId, PushList pushList){}

    /**
     * 同步新翻译数量成功 obj = null; TRANSACTION_TYPE_SYNC_TRANSLATE_NUM
     */
	public void onSynctranslateNum(int transactionId, Object obj){}
	 /**
     * 上传日志成功
     */
	public void onPostLog(int transactionId,String fileId){}
	 /**
     * 上传反馈意见成功
     */
	public void onFeedBackNew(int transactionId){}
	/**
     * 获取推荐成功
     */
	public void onGetRecomm(int transactionId, List<RecommAppInfo> list){}

	/**
	 * 获取热词成功 
	 */
	public void onGetHotWords(int transactionId, List<HotWordInfo> list){}
	
	/**
	 * 获取用户推荐成功
	 */
	public void onGetRecommends(int transactionId, List<RecommendItem> list){}
	
	/**
	 * 获取push service用户绑定信息成功 
	 */
	public void onGetUserBindInfo(int transactionId, BindAccountInfo info){};
	
	/**
	 * 获取头图广告信息成功
	 */
	public void onGetHeadAdInfo(int transactionId, HeadAd[] ads){}
	
	/**
	 * 获取首页的信息成功
	 */
	public void onGetHomeRecommendInfos(int transactionId, Object[] infos){}
	
	/**
	 * 获取课程的广告成功
	 */
	public void onGetCourseAdInfo(int transactionId, CourseAdInfo info){}
	
	/*************************************************************
	 * error
	 *************************************************************/
	/**
     * 获取视频列表返回失败
     */
	public void onGetVideoListError(int transactionId, int errCode, String err){}
	/**
     * 获取视频详情失败
     */
	public void onGetVideoDetailError(int transactionId, int errCode, String err){}
	/**
     * 获取版本信息失败
     */
	public void onGetVersionInfoError(int transactionId, int errCode, String err){}
	/**
	 * 登录失败
	 */
	public void onLoginError(int transactionId, int errCode, String err){}
	/**
     * 注册失败
     */
    public void onRegisterError(int transactionId, int errCode, String err){}
    /**
     * 同步收藏失败
     */
    public void onSyncFavoriteError(int transactionId, int errCode, String err){}
    
    /**
     * 反馈失败
     */
    public void onFeedBackError(int transactionId, int errCode, String err){}
    
    /**
     * 获取关于信息失败
     */
    public void onGetAboutInfoError(int transactionId, int errCode, String err){}
    
    /**
     * 添加收藏失败
     */
    public void onAddStoreError(int transactionId, int errCode, String err){}
    
    /**
     * 删除收藏失败
     */
    public void onDelStoreError(int transactionId, int errCode, String err){}
    /**
     * 获取推送课程失败 
     */
    public void onGetPushCourseError(int transactionId, int errCode, String err){}
    
    /**
     * 同步新翻译数量失败  TRANSACTION_TYPE_SYNC_TRANSLATE_NUM
     */
	public void onSynctranslateNumError(int transactionId, int errCode, String err){}
	 /**
     * 上传日志失败
     */
	public void onPostLogError(int transactionId, int errCode, String err){}
	/**
     * 上传反馈意见失败
     */
	public void onFeedBackNewError(int transactionId, int errCode, String err){}
	/**
     * 获取推荐失败
     */
	public void onGetRecommError(int transactionId, int errCode, String err){}
	
	/**
	 * 获取热词失败
	 */
	public void onGetHotWordsError(int transcationId, int errCode, String err){}
	
	/**
	 * 获取用户推荐失败
	 */
	public void onGetRecommendsError(int transactionId, int errCode, String err){}
	
	/**
	 * 获取用户绑定信息失败 
	 */
	public void onGetUserBindInfoError(int transactionId, int errCode, String err){}
	
	/**
	 * 获取头图广告信息失败
	 */
	public void onGetHeadAdInfoError(int transactionId, int errCode, String err){}
	
	/**
	 * 获取首页推荐信息失败
	 */
	public void onGetHomeRecommendInfosError(int transactionId, int errCode, String err){}
	
	/**
	 * 获取课程的广告失败
	 */
	public void onGetCourseAdInfoError(int transactionId,int errCode, String err){}
	
	/*********************************************************
	 * 评论相关回调
	 ********************************************************/
	public void onGetHotComment(int transactionId, CommentBuildingListInfo info){}
	public void onGetHotCommentError(int transactionId, int errCode, String err){}
	public void onGetLatestComment(int transactionId, CommentBuildingListInfo info){}
	public void onGetLatestCommentError(int transactionId, int errCode, String err){}
	public void onGetWholeComment(int transactionId, OneWholeCommentBuildingInfo info){}
	public void onGetWholeCommentError(int transactionId, int errCode, String err){}
	public void onPostComment(int transactionId, PostCommentStatus stat){}
	public void onPostCommentError(int transactionId, int errCode, String err){}
	public void onGetCommentCount(int transactionId, CommentCount count){}
	public void onGetCommentCountError(int transactionId, int errCode, String err){}
	public void onVoteComment(int transactionId){}
//	public void onVoteCommentError(int transactionId, int errCode, String err){}
	//短链
	public void onGetPicShortUrl(int transactionId, String picUrl){}
	public void onGetPicShortUrlError(int transactionId, int errCode, String err){}
	public void onGetCommentShortUrl(int transactionId, String commentUrl){}
	public void onGetCommentShortUrlError(int transactionId, int errCode, String err){}

	/*************************************************************
	 * 
	 * 
	 *************************************************************/
	Handler mHandler;
	public VopenCallBack(){
		mHandler = new InternalHandler();
	}
	private void onError(int type, int transactionId, int errCode, String errStr) {
		switch (type) {
		case BaseTransaction.TRANSACTION_TYPE_GET_VIDEO_LIST:
			onGetVideoListError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_TYPE_GET_VIDEO_DETAIL:
		    onGetVideoDetailError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_TYPE_GET_VERSION_INFO:
		    onGetVersionInfoError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_LOGIN:
		    onLoginError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_REGISTER:
		    onRegisterError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_SYNC_FAVORITE:
		    onSyncFavoriteError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_FEED_BACK:
            onFeedBackError(transactionId, errCode, errStr);
            break;
		case BaseTransaction.TRANSACTION_TYPE_GET_ABOUT_INFO:
		    onGetAboutInfoError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_ADD_STORE:
		    onAddStoreError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_DEL_STORE:
		    onDelStoreError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_GET_PUSH_COURSE:
		    onGetPushCourseError(transactionId, errCode, errStr);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_SYNC_TRANSLATE_NUM:
			onSynctranslateNumError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_HOT_COMMENT:
			onGetHotCommentError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_LATEST_COMMENT:
			onGetLatestCommentError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_WHOLE_COMMENT:
			onGetWholeCommentError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_POST_COMMENT:
			onPostCommentError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_VOTE_COMMENT:
//			onGetHotCommentError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_COMMENT_PIC_SHORT_URL:
			onGetPicShortUrlError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_COMMENT_SHORT_URL:
			onGetCommentShortUrlError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_COMMENT_COUNT:
			onGetCommentCountError(transactionId, errCode, errStr);
			break;	
		case BaseTransaction.TRANSACTION_TYPE_POSTLOG:
			onPostLogError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_TYPE_FEEDBACK:
			onFeedBackNewError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_RECOMM_APP:
			onGetRecommError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_HOT_WORDS:
			onGetHotWordsError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_RECOMMENDS:
			onGetRecommendsError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_USER_BIND_INFO:
			onGetUserBindInfoError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_HEAD_ADS:
			onGetHeadAdInfoError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_GET_HOME_RECOMMEDN_INFO:
			onGetHomeRecommendInfosError(transactionId, errCode, errStr);
			break;
		case BaseTransaction.TRANSACTION_TYPE_GET_VIDEO_AD:
			onGetCourseAdInfoError(transactionId, errCode, errStr);
			break;
		
		}
	}
	private void onSuccess(int type, int transactionId, Object obj) {
		switch (type) {
		case BaseTransaction.TRANSACTION_TYPE_GET_VIDEO_LIST:
			onGetVideoList(transactionId,(Object[])obj);
			break;
		case BaseTransaction.TRANSACTION_TYPE_GET_VIDEO_DETAIL:
		    onGetVideoDetail(transactionId, (CourseInfo)obj);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_GET_VERSION_INFO:
		    onGetVersionInfo(transactionId, (CheckVersionInfo)obj);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_LOGIN:
		    onLogin(transactionId, (String)obj);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_REGISTER:
		    onRegister(transactionId);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_SYNC_FAVORITE:
            onSyncFavorite(transactionId, (List<SyncItemInfo>)obj);
            break;
		case BaseTransaction.TRANSACTION_TYPE_FEED_BACK:
		    onFeedBack(transactionId);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_GET_ABOUT_INFO:
            onGetAboutInfo(transactionId, (AboutInfo)obj);
            break;
		case BaseTransaction.TRANSACTION_TYPE_UI_EVENT:
			onUiEvent(transactionId, (UiEventTransport)obj);
            break;
		case BaseTransaction.TRANSACTION_TYPE_ADD_STORE:
		    onAddStore(transactionId, (String)obj);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_DEL_STORE:
		    onDelStore(transactionId, (String)obj);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_GET_PUSH_COURSE:
		    onGetPushCourse(transactionId, (PushList)obj);
		    break;
		case BaseTransaction.TRANSACTION_TYPE_SYNC_TRANSLATE_NUM:
			onSynctranslateNum(transactionId, obj);
			break;
		case BaseTransaction.TRANSACTION_GET_HOT_COMMENT:
			onGetHotComment(transactionId, (CommentBuildingListInfo)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_LATEST_COMMENT:
			onGetLatestComment(transactionId, (CommentBuildingListInfo)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_WHOLE_COMMENT:
			onGetWholeComment(transactionId, (OneWholeCommentBuildingInfo)obj);
			break;
		case BaseTransaction.TRANSACTION_POST_COMMENT:
			onPostComment(transactionId, (PostCommentStatus)obj);
			break;
		case BaseTransaction.TRANSACTION_VOTE_COMMENT:
			break;
		case BaseTransaction.TRANSACTION_GET_COMMENT_PIC_SHORT_URL:
			onGetPicShortUrl(transactionId, (String)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_COMMENT_SHORT_URL:
			onGetCommentShortUrl(transactionId, (String)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_COMMENT_COUNT:
			onGetCommentCount(transactionId, (CommentCount)obj);
			break;	
			
		case BaseTransaction.TRANSACTION_TYPE_POSTLOG:
			onPostLog(transactionId,(String)obj);
			break;
		case BaseTransaction.TRANSACTION_TYPE_FEEDBACK:
			onFeedBackNew(transactionId);
			break;
		case BaseTransaction.TRANSACTION_GET_RECOMM_APP:
			onGetRecomm(transactionId,(List<RecommAppInfo>)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_HOT_WORDS:
			onGetHotWords(transactionId, (List<HotWordInfo>)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_RECOMMENDS:
			onGetRecommends(transactionId, (List<RecommendItem>)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_USER_BIND_INFO:
			onGetUserBindInfo(transactionId, (BindAccountInfo)obj);
			break;
		case BaseTransaction.TRANSACTION_GET_HEAD_ADS:
			onGetHeadAdInfo(transactionId, (HeadAd[]) obj);
			break;
		case BaseTransaction.TRANSACTION_GET_HOME_RECOMMEDN_INFO:
			onGetHomeRecommendInfos(transactionId, (Object[])obj);
			break;
		case BaseTransaction.TRANSACTION_TYPE_GET_VIDEO_AD:
			onGetCourseAdInfo(transactionId, (CourseAdInfo)obj);
			break;
		}
	}
	public void callError(int type, int transactionId, int errCode, String errStr){
		mHandler.obtainMessage(TRANSACTION_ERROR, type, transactionId, new Object[]{errStr, Integer.valueOf(errCode)}).sendToTarget();
	}
	
	public void callSuccess(int type, int transactionId, Object obj){
		mHandler.obtainMessage(TRANSACTION_SUCCESS, type, transactionId, obj).sendToTarget();
	}
	
	private static final int TRANSACTION_ERROR = 0x1;
	private static final int TRANSACTION_SUCCESS = 0x2;
	private class InternalHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case TRANSACTION_ERROR:
				onError(msg.arg1, msg.arg2, (Integer)((Object[])msg.obj)[1], (String)((Object[])msg.obj)[0]);
				break;
				
			case TRANSACTION_SUCCESS:
				onSuccess(msg.arg1, msg.arg2, msg.obj);
				break;
				
			}
			super.handleMessage(msg);
		}
	}
	
	
}
