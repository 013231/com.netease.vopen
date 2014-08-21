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
 * 视频信息
 * @author Panjf
 * @date   2012-1-9
 */
public class VideoInfo implements Parcelable{
	public String 	title; //视频标题
    //2013-9-16 添加广告
    public String 	mAdVideoUrl;//广告视频地址
    public String	mAdLink;
    
	public String 	repovideourl; //发布地址
	public int	 	mlength; //视频长度
	public long 	mp4size;  //压制字幕视频的size
	public String 	imgpath; //图片地址
	public int 		pnumber; //第几集
	public String 	commentid; //评论ID
	public String 	subtitle; //子标题
	public String 	subtitle_language; //字幕语言
	public String 	weburl;//web url
    public int 		mCourseProgress;//课程已看进程 ,仅本地使用
    
    /**
	 * 表示接口协议的版本， null或1表示使用旧版本的字幕压制进视频， 2表示字幕分离
	 */
    public Integer protoVersion = 1;
	
    public String repovideourlOrigin = null;
    public String repoMP3urlOrigin = null;	
    public long   mp4SizeOrigin;  //原始视频的size
    public List<VideoSubTitleInfo> subList = null;
	
    public VideoInfo(){
		
	}
	public VideoInfo(String json){
		try {
			JSONObject jso = new JSONObject(json);
			parseJson(jso);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public VideoInfo(JSONObject jso){
		parseJson(jso);
	}
	
	private void parseJson(JSONObject jso){
		if(jso == null)
			return;
		
		title 			= BaseUtil.nullStr(jso.optString("title"));
		mAdVideoUrl 	= BaseUtil.nullStr(jso.optString("adv"));
		mAdLink			= BaseUtil.nullStr(jso.optString("advlink"));
		repovideourl 	= BaseUtil.nullStr(jso.optString("repovideourl"));
		mlength 		= jso.optInt("mlength");
		mp4size 		= jso.optLong("mp4size");
		imgpath 		= BaseUtil.nullStr(jso.optString("imgpath"));
		pnumber 		= jso.optInt("pnumber");
		commentid 		= BaseUtil.nullStr(jso.optString("commentid"));
		subtitle 		= BaseUtil.nullStr(jso.optString("subtitle"));
		subtitle_language = BaseUtil.nullStr(jso.optString("subtitle_language"));
		weburl          = BaseUtil.nullStr(jso.optString("weburl"));
		protoVersion 	= jso.optInt("protoVersion");
		repovideourlOrigin 		= BaseUtil.nullStr(jso.optString("repovideourlOrigin"));
		repoMP3urlOrigin = BaseUtil.nullStr(jso.optString("repoMP3urlOrigin"));
		mp4SizeOrigin 		= jso.optLong("mp4SizeOrigin");
		JSONArray jsa = jso.optJSONArray("subList");
		try {
			if (jsa != null && jsa.length() > 0) {
				if(subList == null){
					subList = new ArrayList<VideoSubTitleInfo>();
				}
				for (int i = 0; i < jsa.length(); i++) {
					subList.add(new VideoSubTitleInfo(jsa.getJSONObject(i)));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJsonObject(){
		try {
			JSONObject json = new JSONObject();
			json.put("title", 			title);
			json.put("adv", 			mAdVideoUrl);
			json.put("advlink", 		mAdLink);
			json.put("repovideourl", 	repovideourl);
			json.put("mlength", 		mlength);
			json.put("mp4size", 		mp4size);
			json.put("imgpath", 		imgpath);
			json.put("pnumber", 		pnumber);
			json.put("commentid", 		commentid);
			json.put("subtitle", 		subtitle);
			json.put("subtitle_language", subtitle_language);
			json.put("weburl", weburl);
			json.put("protoVersion", 		protoVersion);
			json.put("repovideourlOrigin", repovideourlOrigin);
			json.put("repoMP3urlOrigin", repoMP3urlOrigin);
			json.put("mp4SizeOrigin", 		mp4SizeOrigin);
			if (subList != null) {
				JSONArray jsonArray = new JSONArray();
				for (int i = 0; i < subList.size(); i++) {
					jsonArray.put(subList.get(i).toJsonObject());
				}
				json.put("subList", jsonArray);
			}
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	 @Override
	    public void writeToParcel(Parcel parcel, int paramInt) {	        
	        parcel.writeString(title);
	        parcel.writeString(mAdVideoUrl);
	        parcel.writeString(mAdLink);
	        parcel.writeString(repovideourl);
	        parcel.writeInt(mlength);
	        parcel.writeLong(mp4size);
	        parcel.writeString(imgpath);
	        parcel.writeInt(pnumber);
	        parcel.writeString(commentid);
	        parcel.writeString(subtitle);
	        parcel.writeString(subtitle_language);
	        parcel.writeString(weburl);
	        parcel.writeInt(mCourseProgress);
	        parcel.writeInt(protoVersion);
	        parcel.writeString(repovideourlOrigin);
	        parcel.writeString(repoMP3urlOrigin);
	        parcel.writeLong(mp4SizeOrigin);
	        parcel.writeList(subList);
	        
	    }  
	    public void readFromParcel(Parcel source) {
	        
	    	title 		= source.readString();
	    	mAdVideoUrl = source.readString();
	    	mAdLink		= source.readString();
	    	repovideourl 	= source.readString();
	    	mlength 	= source.readInt();
	    	mp4size     = source.readLong();
	    	imgpath     = source.readString();
	    	pnumber		= source.readInt();
	    	commentid   = source.readString();
	    	subtitle	= source.readString();
	    	subtitle_language  = source.readString();
	    	weburl 	= source.readString();
	    	mCourseProgress =source.readInt();
	    	protoVersion 	= source.readInt();
	    	repovideourlOrigin   = source.readString();
	    	repoMP3urlOrigin	= source.readString();
	    	mp4SizeOrigin     = source.readLong();
	    	ClassLoader cl = VideoSubTitleInfo.class.getClassLoader();
	    	subList = source.readArrayList(cl);
	    }

	    public static final Parcelable.Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
	        public VideoInfo createFromParcel(Parcel source) {
	        	VideoInfo info = new VideoInfo();
	            info.readFromParcel(source);
	            return info;
	        }

	        public VideoInfo[] newArray(int size) {
	            return new VideoInfo[size];
	        }
	    };
}
