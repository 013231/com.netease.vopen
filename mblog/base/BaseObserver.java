package mblog.base;

import mblog.base.ResultObserver.Observer;


/**
 * 微博回调接口适配器类
 * @author Administrator
 *
 */
public class BaseObserver implements Observer{

    @Override
    public void onLogin(int transactionId, int type, LoginResult result) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLoginError(int transactionId, int type, ErrDescrip obj) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogSend(int transactionId, int type, SendBlogResult result) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogSendError(int transactionId, int type, ErrDescrip obj) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogUpload(int transactionId, int type, SendBlogResult result) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogUploadError(int transactionId, int type, ErrDescrip obj) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogFollow(int transactionId, int type) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogFollowError(int transactionId, int type, ErrDescrip obj) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogShowFriendship(int transactionId, int type, FriendshipResult obj) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBlogShowFriendshipError(int transactionId, int type, ErrDescrip obj) {
        // TODO Auto-generated method stub
        
    }

}
