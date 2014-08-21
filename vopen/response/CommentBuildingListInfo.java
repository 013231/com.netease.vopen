package vopen.response;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentBuildingListInfo{
	public static final String PRCOUNT_TAG = "prcount";
	public static final String HOTPOSTS_TAG = "hotPosts";
	public static final String NEWPOSTS_TAG = "newPosts";
	public static final String CODE_TAG = "code";
	public static final String PTCOUNT_TAG = "ptcount";	
	public static final String CODEMSG_TAG = "codeMsg";
	
	public ArrayList<CommentBuildingInfo> postsList;
	
	public int prcount;
	public int ptcount;
	public int code;
	public String codeMsg = "";
	
    /***
     * 从JSONObject中读取到Info中
     */
    public void readFromJSONObject(JSONObject o) throws JSONException{
    	
        if (o != null) {
        	
        	CommentBuildingInfo commentBuildingInfo = null;
        	JSONArray joArray = null;
    		if(!o.isNull(HOTPOSTS_TAG)) {
    			joArray = o.getJSONArray(HOTPOSTS_TAG);

    		}else if(!o.isNull(NEWPOSTS_TAG)) {
    			joArray = o.getJSONArray(NEWPOSTS_TAG);
    		}		
    		if(joArray != null) {
    			postsList = new ArrayList<CommentBuildingInfo>();
    			int len = joArray.length();
    			for(int i=0; i<len; i++) {
        			commentBuildingInfo = new CommentBuildingInfo();       			
        			commentBuildingInfo.readFromJSONObject(joArray.getJSONObject(i));
        			postsList.add(commentBuildingInfo);
    			}
    		}
	
        	if(!o.isNull(PRCOUNT_TAG)) prcount = o.getInt(PRCOUNT_TAG);
        	if(!o.isNull(PTCOUNT_TAG)) ptcount = o.getInt(PTCOUNT_TAG);
        	if(!o.isNull(CODE_TAG)) code = o.getInt(CODE_TAG);
        	if(!o.isNull(CODEMSG_TAG)) codeMsg = o.getString(CODEMSG_TAG);

        }
    }
}
