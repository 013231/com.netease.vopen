package vopen.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SyncItemInfo {
	public String playid;
	public String storetime;
	
	public SyncItemInfo(String plid, String storetime){
		this.playid = plid;
		this.storetime = storetime;
	}
	
	public SyncItemInfo(JSONObject jso){
		playid = jso.optString("playid");
		storetime = jso.optString("storetime");
	}
	
	public JSONObject toJsonObject(){
		try {
			JSONObject json = new JSONObject();
			json.put("playid", 			playid);
			json.put("storetime", 		storetime);
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<SyncItemInfo> parseSyncResult(JSONArray jsa){
		List<SyncItemInfo> list = new ArrayList<SyncItemInfo>();
		if(jsa == null)
			return list;
		
		for (int i = 0; i < jsa.length(); i++) {

			try {
				JSONObject jso = jsa.getJSONObject(i);
				list.add(new SyncItemInfo(jso));
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		
		return list;
	}
}
