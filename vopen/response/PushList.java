
package vopen.response;

import common.util.BaseUtil;
import common.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PushList {
    public List<PushInfo> mPushList;

    public PushList() {
        // TODO Auto-generated constructor stub
        mPushList = new ArrayList<PushInfo>();
    }

    public void parseJson(JSONObject jso) {
        if (null == jso)
            return;
        try {
            String result = jso.getString("pushresult");
            if (!StringUtil.checkStr(result) || "null".equals(result)) {
                return;
            } else {
                JSONArray jsonarray = null;
                jsonarray = jso.getJSONArray("pushresult");
                if (null != jsonarray && jsonarray.length() > 0) {
                    for (int index = 0; index < jsonarray.length(); index++) {
                        JSONObject obj = (JSONObject) jsonarray.get(index);
                        if (null != obj) {
                            PushInfo info = new PushInfo();
                            info.parseJson(obj);
                            mPushList.add(info);
                        }
                    } // 显示push的for结束
                } // 没有push内容
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class PushInfo {
        public String courseid;
        public String content;

        public void parseJson(JSONObject jso) {
            courseid = BaseUtil.nullStr(jso.optString("videoid"));
            content = BaseUtil.nullStr(jso.optString("content"));
        }
    }
}
