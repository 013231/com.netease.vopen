package common.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.vopen.pal.Constants;

public class StringUtil {
	
	public static boolean isEqual(String str1, String str2){
		if (isEmpty(str1)){
			return isEmpty(str2);
		}else{
			return str1.equals(str2);
		}
	}
	
	public static boolean isEmpty(String str){
		return str == null || str.length() == 0;
	}
	
	public static String makeSafe(String paramString)
	  {
	    if (paramString == null);
	    for (String str = ""; ; str = paramString)
	      return str;
	  }
	
	public static String getPlidUrl(String plid){
		
		return Constants.url_plist_head+plid+Constants.url_plist_end;
		
	}
	
	/**
	 * 替换HTML字符.
	 */
	public static String htmlDecoder(String src) throws Exception {
		
		if (src == null || src.equals("")) {
			return "";
		}

		String dst = src;
		dst = replaceAll(dst, "&lt;", "<");
		dst = replaceAll(dst, "&rt;", ">");
		dst = replaceAll(dst, "&quot;", "\"");
		dst = replaceAll(dst, "&039;", "'");
		dst = replaceAll(dst, "&nbsp;", " ");
		dst = replaceAll(dst, "&nbsp", " ");
		dst = replaceAll(dst, "<br>", "\n");
		dst = replaceAll(dst, "\r\n", "\n");
		dst = replaceAll(dst, "&#8826;", "??");
		dst = replaceAll(dst, "&#8226;", "??");
		dst = replaceAll(dst, "&#9642;", "??");
		return dst;
	}
	
	public static String replaceAll(String src, String fnd, String rep)throws Exception {
		
		if (src == null || src.equals("")) {
			return "";
		}
		
		String dst = src;
		
		int idx = dst.indexOf(fnd);
		
		while (idx >= 0) {
			dst = dst.substring(0, idx) + rep
					+ dst.substring(idx + fnd.length(), dst.length());
			idx = dst.indexOf(fnd, idx + rep.length());
		}
		
		return dst;
	}
	
	public static boolean checkStr(String str){
		
		boolean _is = false;
		
		if(null!=str && !"".equals(str)){
			_is = true;
		}
		
		return _is;
		
	}
	
	public static boolean checkObj(Object obj){
		boolean _is = false;
		
		if(null!=obj){
			_is = true;
		}
		
		return _is;
	}
	
	public static String JsonToString(JSONObject json){
		
		if(null!=json){
			
			return json.toString();
		}else{
			
			return "";
		}
		
	}
    
    public static JSONObject StringToJson(String str){
		
		if(null!=str){
			try {
				JSONObject json = new JSONObject(str);
				return json;
			} catch (JSONException e) {
				return null;
			}
		}else{
			
			return null;
		}
	}
	
	public static JSONArray StringToJsonArray(String str){
		
		if(null!=str){
			try {
				 JSONArray jsonArray = new JSONArray(str);
				return jsonArray;
			} catch (JSONException e) {
				return null;
			}
		}else{
			
			return null;
		}
	}
	
	public static String addZerotoFront2(int src){
		String rst = "";
		
		if(src>9){
			rst = ""+src;
		}else{
			rst = "0"+src;
		}
		
		return rst;
	}

	 public static String getNameFromPath(String path) {
	        if (!TextUtils.isEmpty(path)) {
	            return path.substring(path.lastIndexOf("/") + 1);
	        }
	        return null;
	    }
}
