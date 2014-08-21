package vopen.response;

import org.json.JSONObject;

import common.util.BaseUtil;
/**
 * 获取push service用于绑定用户的信息 
 */
public class BindAccountInfo {
	public String account; 		//用户名
	public String expireTime;	//过期时间
	public String nonce;		//随机数
	public String signature;	//签名
	
	public BindAccountInfo(JSONObject jso){
		parseJson(jso);
	}
	
	public void parseJson(JSONObject jso){
		if(jso == null)
			return;
		account 	= BaseUtil.nullStr(jso.optString("account"));
		expireTime 	= BaseUtil.nullStr(jso.optString("expire_time"));
		nonce 		= BaseUtil.nullStr(jso.optString("nonce"));
		signature 	= BaseUtil.nullStr(jso.optString("signature"));
	}
}
