package vopen.response;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import common.util.BaseUtil;

public class HotWordInfo {

	public String word;
	public int hotCount;

	public HotWordInfo(String jsonStr) {
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
		word = BaseUtil.nullStr(jso.optString("word"));
		hotCount = jso.optInt("times");
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {

	}

}
