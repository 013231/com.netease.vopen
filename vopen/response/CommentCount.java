package vopen.response;

import org.json.JSONObject;

public class CommentCount {
	
	/**查看跟帖数结果，1为成功，其它为错误*/
	public int mCode = -1;
	/**查看跟帖数返回信息*/
	public String mMessage;
	/**发贴总数，不包括删除*/
	public int mPrcount = -1;
	/**发贴总数，包括删除，用于分页显示*/
	public int mPtcount = -1;
	
	public CommentCount(JSONObject jso) {
		if(jso == null)
			return;
		
		mCode = jso.optInt("code");
		mMessage = jso.optString("errMsg");
		mPrcount = jso.optInt("prcount");
		mPtcount = jso.optInt("ptcount");
	}

}
