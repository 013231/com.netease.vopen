package vopen.response;

import org.json.JSONException;
import org.json.JSONObject;

public class RecommAppInfo {
	private String displayName;
	private String description;
	private String lIconUrl;
	private String hIconUrl;
	private String downloadUrl;
	private String schema;
	
	public RecommAppInfo(String json) {
		// TODO Auto-generated constructor stub
		try {
			JSONObject jso = new JSONObject(json);
			parseJson(jso);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void parseJson(JSONObject jso){
		if(jso == null)
			return;
		displayName = jso.optString("displayName");
		description= jso.optString("description");
		lIconUrl= jso.optString("lIconUrl");
		hIconUrl= jso.optString("hIconUrl");
		downloadUrl= jso.optString("downloadUrl");
		schema= jso.optString("schema");
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getlIconUrl() {
		return lIconUrl;
	}
	public void setlIconUrl(String lIconUrl) {
		this.lIconUrl = lIconUrl;
	}
	public String gethIconUrl() {
		return hIconUrl;
	}
	public void sethIconUrl(String hIconUrl) {
		this.hIconUrl = hIconUrl;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
}
