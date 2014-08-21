package vopen.response;

import org.json.JSONException;
import org.json.JSONObject;
import common.util.BaseUtil;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * 字幕信息
 * 
 * @author echo_chen
 * @date 2012-11-21
 */

public class VideoSubTitleInfo implements Parcelable {
	
	public String subName = null;//字幕语言
	public String subUrl = null;//字幕下载地址
	public long subSize = 0;//字幕size,暂未使用

	public VideoSubTitleInfo() {

	}

	public VideoSubTitleInfo(String str) {
		try {
			JSONObject jso = new JSONObject(str);
			parseJson(jso);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public VideoSubTitleInfo(JSONObject jso) {
		parseJson(jso);
	}

	public void parseJson(JSONObject jso) {
		if (jso == null)
			return;
		subName = BaseUtil.nullStr(jso.optString("subName"));
		subUrl = BaseUtil.nullStr(jso.optString("subUrl"));
		subSize = jso.optLong("subSize");;
	}

	public JSONObject toJsonObject() {
		try {
			JSONObject json = new JSONObject();
			json.put("subName", subName);
			json.put("subUrl", subUrl);	
			json.put("subSize", subSize);		
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
		parcel.writeString(subName);
		parcel.writeString(subUrl);
		parcel.writeLong(subSize);
	}

	public void readFromParcel(Parcel parcel) {
		subName = parcel.readString();
		subUrl = parcel.readString();
		subSize = parcel.readLong();
	}

	public static final Parcelable.Creator<VideoSubTitleInfo> CREATOR = new Creator<VideoSubTitleInfo>() {
		public VideoSubTitleInfo createFromParcel(Parcel parcel) {
			VideoSubTitleInfo info = new VideoSubTitleInfo();
			info.readFromParcel(parcel);
			return info;
		}

		public VideoSubTitleInfo[] newArray(int size) {
			return new VideoSubTitleInfo[size];
		}
	};

}
