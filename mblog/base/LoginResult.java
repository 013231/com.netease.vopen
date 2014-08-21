package mblog.base;

public class LoginResult{
//	获取未授权的Request Token(oauth/request_token)
//	请求用户授权Token(oauth/authenticate)
//	通过Request Token换取Access Token(oauth/access_token)
	public static final int LOGIN_REQUEST = 1;
	public static final int LOGIN_AUTHENTICATE = 2;
	public static final int LOGIN_ACCESS = 3;
	public static final int LOGIN_GETINFO = 4;
	public static final int LOGIN_DONE = 5;
	
	private int mBlogType;
	private String mUserId;
	private String name;
	private String screen_name; //for screen name or nick name
	private String profile_url;
	private String domain;
	
	private String mAccessToken;
	private String mTokenSecret;
	
	private int loginStep;
	
	private Authenticate mAuthenticate;
	
	public LoginResult(int blogType) {
		mBlogType = blogType;
	}
	
	public int getBlogType(){
		return mBlogType;
	}
	
	public void setId(String id) {
		mUserId = id;
	}
	
	public String getId() {
		return mUserId;
	}
	
	public void setAccessToken(String accessToken) {
		mAccessToken = accessToken;
	}
	
	public String getAccessToken() {
		return mAccessToken;
	}
	
	public void setTokenSecret(String tokenSecret) {
		mTokenSecret = tokenSecret;
	}
	
	public String getTokenSecret() {
		return mTokenSecret;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setScreeName(String screenName) {
		this.screen_name = screenName;
	}

	public void setProfile(String profile) {
		profile_url = profile;
	}
	
	public void setDomain(String domain){
		this.domain = domain;
	}
	
	public String getName() {
		return name;
	}

	public String getScreeName() {
		return screen_name;
	}
	public String getDomain() {
		return domain;
	}
	
	public String getProfile() {
		return profile_url;
	}
	
	public void setLoginStep(int step){
		loginStep = step;
	}
	
	public int getLoginStep(){
		return loginStep;
	}
	
	public void setAuthenticate(Authenticate a){
		mAuthenticate = a;
	}
	
	public Authenticate getAuthenticate(){
		return mAuthenticate;
	}
	
	public abstract class Authenticate{
		public String mAuthenticateUrl;
		public String mCbUrl;
		public Authenticate(String url, String cb){
			mAuthenticateUrl = url;
			mCbUrl = cb;
		}
//		abstract public void onCancel();
		abstract public void onGetToken(String token, String verifier, String uid);
	}
}