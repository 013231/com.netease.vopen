package vopen.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OneWholeCommentBuildingInfo{
	public static final String VOTE_TAG = "v";
	public static final String USERNAME_TAG = "u";
	public static final String TIME_TAG = "t";
	public static final String B_TAG = "b";
	public static final String P_TAG = "p";
	public static final String CODE_TAG = "code";
	
	public JSONArray wholeCommentArray = new JSONArray();
	
	public int v;
	public String u = "";
	public String t = "";
	public String p = "";
	public int code;
	
  
    public void readFromJSONObject(JSONObject o) throws JSONException{
    	JSONArray jsonArray = null;
    	JSONObject jobjs = null;
        if (o != null) {

        	if(o.has(VOTE_TAG)) v = o.getInt(VOTE_TAG);
        	if(o.has(USERNAME_TAG)) u = o.getString(USERNAME_TAG);
        	if(o.has(TIME_TAG)) t = o.getString(TIME_TAG);
        	if(o.has(P_TAG)) p = o.getString(P_TAG);
        	if(o.has(CODE_TAG)) code = o.getInt(CODE_TAG);
        	if(o.has(B_TAG)) {
        		jsonArray = o.getJSONArray(B_TAG);
        		jobjs = jsonArray.getJSONObject(0);
        		int len = jobjs.length();
        		for(int i = len; i > 0; i--) {
        			wholeCommentArray.put(jobjs.getJSONObject(String.valueOf(i)));
        		}
        	}
        }
    }
}
