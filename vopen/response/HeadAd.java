package vopen.response;

import org.json.JSONObject;

/**
 * 头图的广告信息
 * @author ymsong
 */
public class HeadAd {
	public String title; 		//广告标题
	public String imgURL;		//广告图片地址
	public String adURL;		//广告指向的地址
	public String type;			//类型，目前只有“推广”
	public int position;		//这条广告在头图中的位置, 
	
	public HeadAd(JSONObject jso){
		parseJson(jso);
	}

	private void parseJson(JSONObject jso) {
		if(jso == null)
			return;
		title = jso.optString("title");
		imgURL = jso.optString("imgURL");
		adURL = jso.optString("promotionURL");
		type = jso.optString("type");
		position = jso.optInt("position");
	}
}
