package vopen.response;

import org.json.JSONObject;

/**
 * 首页的轮播图以及除了猜你喜欢之外，其它栏目中的grid使用的数据结构。
 * @author netease
 */
public class RecommendInfo {

	// #################Type常量#################
	// 头图
	public static final int TYPE_MOBILE_TOP_PICTURE = 0;
	// 小编推荐
	public static final int TYPE_MOBILE_EDITOR_RECOMMEND = 1;
	// 赏课
	public static final int TYPE_MOBILE_BBC_RECOMMEND = 2;
	// TED
	public static final int TYPE_MOBILE_TED_RECOMMEND = 3;
	// 讲座
	public static final int TYPE_MOBILE_TALK_RECOMMEND = 4;

	// #################Content type常量#################
	public static final int CONTENT_TYPE_PLAY = 0;
	public static final int CONTENT_TYPE_MOVIE = 1;
	public static final int CONTENT_TYPE_SPECIAL = 2;
	public static final int CONTENT_TYPE_LINK = 3;

	public String id;
	public long gmtCreate;
	public long gmtModified;
	public int type; // type常量
	public int contentType; // CONTENT_TYPE常量
	public String contentUrl;
	public String contentId;
	public String title;
	public String picUrl;
	public String tag;
	public String tagColor;
	public String tagColorBg;
	public String top;

	public RecommendInfo(JSONObject jsonObj) {
		parseJson(jsonObj);
	}

	private void parseJson(JSONObject jso) {
		if (jso == null)
			return;
		id = jso.optString("id");
		gmtCreate = jso.optLong("gmtCreate");
		gmtModified = jso.optLong("gmtModified");
		type = jso.optInt("type", 0);
		contentType = jso.optInt("contentType");
		contentUrl = jso.optString("contentUrl");
		contentId = jso.optString("contentId");
		title = jso.optString("title");
		picUrl = jso.optString("picUrl");
		tag = jso.optString("tag");
		tagColor = jso.optString("tagColor");
		tagColorBg = jso.optString("tagColorBg");
		top = jso.optString("top");
	}
}
