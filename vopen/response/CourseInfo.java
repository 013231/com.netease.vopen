package vopen.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import common.util.BaseUtil;

/**
 * 课程信息
 * 
 * @author Panjf
 * @date 2012-1-9
 */
public class CourseInfo implements Parcelable {
	public String title; // 标题
	public String subtitle; // 剧集的英文标题
	public String plid; // 节目ID
	public String imgpath; // 图片地址
	public String school; // 学校
	public String director; // 导演
	public int playcount; // 总集数
	public int updated_playcount; // 已翻译集数
	public String type; // 节目类别，六大分类
	public String description; // 节目描述
	public String include_virtual; // 精华贴地址
	public String ccPic; // CC图片地址
	public String ccUrl; // CC链接地址
	public List<VideoInfo> videoList = new ArrayList<VideoInfo>(); // 视频集合
	public int mCurrentCourseNum = 1; // 当前是第几课
	public int mCurrentPosition = -1;// 当前课程播放位置
	public String tags;// 2012.11新增,标识分类
	public String source;// 2012.11新增,标识来源

	public CourseInfo() {

	}

	public CourseInfo(String str) {
		try {
			JSONObject jso = new JSONObject(str);
			parseJson(jso);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CourseInfo(JSONObject jso) {
		parseJson(jso);
	}

	public void parseJson(JSONObject jso) {
		if (jso == null)
			return;

		title = BaseUtil.nullStr(jso.optString("title"));
		subtitle = BaseUtil.nullStr(jso.optString("subtitle"));
		plid = BaseUtil.nullStr(jso.optString("plid"));
		imgpath = BaseUtil.nullStr(jso.optString("imgpath"));
		school = BaseUtil.nullStr(jso.optString("school"));
		director = BaseUtil.nullStr(jso.optString("director"));
		playcount = jso.optInt("playcount");
		updated_playcount = jso.optInt("updated_playcount");
		type = BaseUtil.nullStr(jso.optString("type"));
		description = BaseUtil.nullStr(jso.optString("description"));
		include_virtual = BaseUtil.nullStr(jso.optString("include_virtual"));
		ccPic = BaseUtil.nullStr(jso.optString("ccPic"));
		ccUrl = BaseUtil.nullStr(jso.optString("ccUrl"));
		tags = jso.optString("tags");
		source = jso.optString("source");
		JSONArray jsa = jso.optJSONArray("videoList");
		try {
			if (jsa != null && jsa.length() > 0) {
				for (int i = 0; i < jsa.length(); i++) {
					videoList.add(new VideoInfo(jsa.getJSONObject(i)));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JSONObject toJsonObject() {
		try {
			JSONObject json = new JSONObject();
			json.put("title", title);
			json.put("subtitle", subtitle);
			json.put("plid", plid);
			json.put("imgpath", imgpath);
			json.put("school", school);
			json.put("director", director);
			json.put("playcount", playcount);
			json.put("updated_playcount", updated_playcount);
			json.put("type", type);
			json.put("description", description);
			json.put("include_virtual", include_virtual);
			json.put("ccPic", ccPic);
			json.put("ccUrl", ccUrl);
			json.put("tags", tags);
			json.put("source", source);
			if (videoList != null) {
				JSONArray jsonArray = new JSONArray();
				for (int i = 0; i < videoList.size(); i++) {
					jsonArray.put(videoList.get(i).toJsonObject());
				}

				json.put("videoList", jsonArray);
			}

			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int paramInt) {
		parcel.writeString(title);
		parcel.writeString(subtitle);
		parcel.writeString(plid);
		parcel.writeString(imgpath);
		parcel.writeString(school);
		parcel.writeString(director);
		parcel.writeInt(playcount);
		parcel.writeInt(updated_playcount);
		parcel.writeString(type);
		parcel.writeString(description);
		parcel.writeString(include_virtual);
		parcel.writeString(ccPic);
		parcel.writeString(ccUrl);
		parcel.writeList(videoList);
		parcel.writeInt(mCurrentCourseNum);
		parcel.writeInt(mCurrentPosition);
		parcel.writeString(source);
		parcel.writeString(tags);
	}

	public void readFromParcel(Parcel parcel) {

		title = parcel.readString();
		subtitle = parcel.readString();
		plid = parcel.readString();
		imgpath = parcel.readString();
		school = parcel.readString();
		director = parcel.readString();
		playcount = parcel.readInt();
		updated_playcount = parcel.readInt();
		type = parcel.readString();
		description = parcel.readString();
		include_virtual = parcel.readString();
		ccPic = parcel.readString();
		ccUrl = parcel.readString();
		ClassLoader cl = VideoInfo.class.getClassLoader();
		videoList = parcel.readArrayList(cl);
		mCurrentCourseNum = parcel.readInt();
		mCurrentPosition = parcel.readInt();
		source = parcel.readString();
		tags = parcel.readString();
	}

	public static final Parcelable.Creator<CourseInfo> CREATOR = new Creator<CourseInfo>() {
		public CourseInfo createFromParcel(Parcel parcel) {
			CourseInfo info = new CourseInfo();
			info.readFromParcel(parcel);
			return info;
		}

		public CourseInfo[] newArray(int size) {
			return new CourseInfo[size];
		}
	};

}
