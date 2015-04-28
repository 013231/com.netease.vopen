package vopen.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.vopen.pal.Constants;

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
	public String largeimgurl;	//大图片地址（横版图片）
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
	//新增排序字段
    public long	hits;
    public long	ltime;
    /*2014-12 新增字段*/
    @Deprecated
    public int preAdvSource;
    @Deprecated
    public int midAdvSource;
    @Deprecated
    public int postAdvSource;
    
    /*2015-2-6新增*/
    
    public int adSource;//0表示没有广告，1表示从dj那边取，10表示从广告sdk取
    public String adPreCategory;
    public String adMidCategory;
    public String adPostCategory;
    
    public final static int AD_SOURCE_NONE = 0;
    public final static int AD_SOURCE_DJ = 1;
    public final static int AD_SOURCE_BJ = 10;
    
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

		title = jso.optString("title");
		subtitle = jso.optString("subtitle");
		plid = jso.optString("plid");
		imgpath = jso.optString("imgpath");
		largeimgurl = jso.optString("largeimgurl");
		school = jso.optString("school");
		director = jso.optString("director");
		playcount = jso.optInt("playcount");
		updated_playcount = jso.optInt("updated_playcount");
		type = jso.optString("type");
		description = jso.optString("description");
		include_virtual = jso.optString("include_virtual");
		ccPic = jso.optString("ccPic");
		ccUrl = jso.optString("ccUrl");
		tags = jso.optString("tags");
		source = jso.optString("source");
		hits 		= jso.optInt("hits");
		ltime 		= jso.optLong("ltime");
		preAdvSource = jso.optInt("preAdvSource");
		midAdvSource = jso.optInt("midAdvSource");
		postAdvSource = jso.optInt("postAdvSource");
		JSONObject adinfo = jso.optJSONObject("ipadPlayAdvInfo");
		if (adinfo != null){
			adSource = adinfo.optInt("advSource");
			adPreCategory = adinfo.optString("advPreId");
			adMidCategory = adinfo.optString("advMidId");
			adPostCategory = adinfo.optString("advPosId");
		}
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
			json.put("largeimgurl", largeimgurl);
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
			
			json.put("hits", hits);
			json.put("ltime", ltime);
			
			JSONObject adinfo = new JSONObject();
			adinfo.put("advSource", adSource);
			adinfo.put("advPreId", adPreCategory);
			adinfo.put("advMidId", adMidCategory);
			adinfo.put("advPosId", adPostCategory);
			json.put("ipadPlayAdvInfo", adinfo);
			
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
	
	public String toJsonString(){
		JSONObject jobj = toJsonObject();
		if (jobj != null){
			return jobj.toString();
		}
		return "";
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
		parcel.writeString(largeimgurl);
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
        parcel.writeLong(hits);
        parcel.writeLong(ltime);
	}

	public void readFromParcel(Parcel parcel) {

		title = parcel.readString();
		subtitle = parcel.readString();
		plid = parcel.readString();
		imgpath = parcel.readString();
		largeimgurl = parcel.readString();
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
		hits = parcel.readInt();
    	ltime = parcel.readLong();
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

	
	/**
	 * 判断课程是否国内课程。
	 * 如果是国内课程，文案改成：已更新和未更新
	 * @param info
	 * @return
	 */
	public boolean isDomesticCourse(){
		if (source != null){
			return source.contains(Constants._SOURCE_DOMESTIC);
		}
		return false;
	} 
	
	@Override
	public boolean equals(Object o) {
		if (o == this){
			return true;
		}
		if (!(o instanceof CourseInfo)){
			return false;
		}
		CourseInfo in = (CourseInfo) o;
		if (this.plid == null){
			return in.plid == null;
		}else{
			return plid.equals(in.plid);
		}
	}
}
