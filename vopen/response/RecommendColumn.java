package vopen.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 首页的轮播图以及除了猜你喜欢之外的其它栏目的数据结构
 * 
 * @author netease
 */
public class RecommendColumn {

	public String name;
	public int type;
	public List<RecommendInfo> vos;

	public RecommendColumn(JSONObject jsonObj) {
		parseJson(jsonObj);
	}

	private void parseJson(JSONObject jso) {
		if (jso == null)
			return;
		name = jso.optString("name");
		type = jso.optInt("type", 0);
		vos = new ArrayList<RecommendInfo>();
		JSONArray content;
		try {
			content = jso.getJSONArray("vos");
			for (int i = 0; i < content.length(); i++) {
				RecommendInfo info = new RecommendInfo(content.getJSONObject(i));
				vos.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
