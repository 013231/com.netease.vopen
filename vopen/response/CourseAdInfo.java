package vopen.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 课程的广告信息。
 * 
 * @author netease
 */
public class CourseAdInfo {

	public int midWeight;
	public int preWeight;
	public int postWeight;
	public List<CourseAdItem> postAdvList;
	public List<CourseAdItem> preAdvList;
	public List<CourseAdItem> midAdvList;

	public CourseAdInfo(JSONObject jso) {
		parseJson(jso);
	}

	private void parseJson(JSONObject jso) {
		if (jso == null)
			return;
		midWeight = jso.optInt("midWeight");
		preWeight = jso.optInt("preWeight");
		postWeight = jso.optInt("postWeight");
		// post adv list
		postAdvList = new ArrayList<CourseAdItem>();
		JSONArray postArray = jso.optJSONArray("postAdvList");
		if (postArray != null) {
			for (int i = 0; i < postArray.length(); i++) {
				JSONObject jo = postArray.optJSONObject(i);
				if (jo != null) {
					postAdvList.add(new CourseAdItem(jo));
				}
			}
		}
		// pre adv list
		preAdvList = new ArrayList<CourseAdItem>();
		JSONArray preArray = jso.optJSONArray("preAdvList");
		if (preArray != null) {
			for (int i = 0; i < preArray.length(); i++) {
				JSONObject jo = preArray.optJSONObject(i);
				if (jo != null) {
					preAdvList.add(new CourseAdItem(jo));
				}
			}
		}
		// mid adv list
		midAdvList = new ArrayList<CourseAdItem>();
		JSONArray midArray = jso.optJSONArray("midAdvList");
		if (midArray != null) {
			for (int i = 0; i < midArray.length(); i++) {
				JSONObject jo = midArray.optJSONObject(i);
				if (jo != null) {
					midAdvList.add(new CourseAdItem(jo));
				}
			}
		}
	}

	public static class CourseAdItem {
		public String advLink;
		public String advUrl;
		public int weight;

		public CourseAdItem(JSONObject jso) {
			parseJson(jso);
		}

		private void parseJson(JSONObject jso) {
			if (jso == null) {
				return;
			}
			advLink = jso.optString("advLink");
			advUrl = jso.optString("advUrl");
			weight = jso.optInt("weight");
		}
	}
}
