package mblog.base;

public class FriendshipResult {
    private int mBlogType;
    private boolean mFollowing;//是否关注
    private boolean mFollowedBy;//是否被关注
    
    public FriendshipResult(int blogType) {
        mBlogType = blogType;
    }
    
    public int getBlogType(){
        return mBlogType;
    }

    public boolean isFollowing() {
        return mFollowing;
    }

    public void setFollowing(boolean mFollowing) {
        this.mFollowing = mFollowing;
    }

    public boolean isFollowedBy() {
        return mFollowedBy;
    }

    public void setFollowedBy(boolean mFollowedBy) {
        this.mFollowedBy = mFollowedBy;
    }
    
}
