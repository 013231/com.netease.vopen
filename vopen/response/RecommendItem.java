package vopen.response;

import org.json.JSONException;
import org.json.JSONObject;

public class RecommendItem {
	
	public String itemId;
	public double value;
	public String title;
	public String url;
	public String imageUrl;
	public String bigImageUrl;
	
	public RecommendItem(String jsonStr) {
		try {
			JSONObject jso = new JSONObject(jsonStr);
			parseJson(jso);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	
	private void parseJson(JSONObject jso) {
		if (jso == null)
			return;
		itemId = jso.optString("itemid");
		value = 0;
		JSONObject itemInfo = null;
		try{
			value = jso.getDouble("value");
			itemInfo = jso.getJSONObject("iteminfo");
		}catch(JSONException e){
			e.printStackTrace();
		}
		if (itemInfo != null){
			title = itemInfo.optString("title");
			url = itemInfo.optString("url");
			imageUrl = itemInfo.optString("imgurl");
			bigImageUrl = itemInfo.optString("bigimgurl");
		}
	}
}
