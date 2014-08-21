package vopen.response;

import org.json.JSONObject;

public class PostCommentStatus {
	/**发帖标示, 1为发帖成功 ，其他为发帖失败*/
	public int mCode = -1;
	/**发帖返回信息*/
	public String mMessage;
	
	public PostCommentStatus(JSONObject jso) {
		if(jso == null)
			return;
		
		mCode = jso.optInt("code");
		mMessage = jso.optString("errMsg");
	}
}
