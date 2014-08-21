package vopen.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vopen.protocol.CommentProtocol;
import android.text.TextUtils;

public class CommentBuildingInfo {
	public static final String NON_TAG = "NON";
	public JSONArray oneCommentJSONArray = new JSONArray();

	public int non = 0;

	/**
	 * 保存每层楼的跟帖信息(json数据)
	 */
	public void readFromJSONObject(JSONObject o) throws JSONException {
		if (o != null) {
			int nonValue = 0;
			String nonString = "";
			if (o.has(NON_TAG))
				nonString = o.getString(NON_TAG);

			if (!TextUtils.isEmpty(nonString)) {
				try {
					non = Integer.parseInt(nonString);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					non = 0;
				}
				if (non > 0)
					nonValue = non - CommentProtocol.HIDE_TOP_NUM
							- CommentProtocol.HIDE_BOTTOM_NUM;
			}

			int len = o.length();
			for (int j = len; j > 0; j--) {
				if (j > CommentProtocol.HIDE_TOP_NUM) {
					if (o.has(String.valueOf(j + nonValue))) {
						oneCommentJSONArray.put(o.getJSONObject(String
								.valueOf(j + nonValue)));
					}
				} else {
					if (o.has(String.valueOf(j))) {
						oneCommentJSONArray.put(o.getJSONObject(String
								.valueOf(j)));
					}
				}

			}

		}
	}

}
