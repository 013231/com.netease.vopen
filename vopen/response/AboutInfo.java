package vopen.response;

import org.json.JSONObject;

import common.util.BaseUtil;

public class AboutInfo {
	public String intro; 		//简介
	public String webopenurl;	//网站主页
	public String foreword;		//感谢前言
	public String remark;		//提醒(姓名按拼音排序)
	public String name;			//感谢名单
	
	public AboutInfo(JSONObject jso){
		parseJson(jso);
	}
	
	public void parseJson(JSONObject jso){
		if(jso == null)
			return;
		
		intro 		= BaseUtil.nullStr(jso.optString("intro"));
		webopenurl 	= BaseUtil.nullStr(jso.optString("webopenurl"));
		foreword 	= BaseUtil.nullStr(jso.optString("foreword"));
		remark 		= BaseUtil.nullStr(jso.optString("remark"));
		name 		= BaseUtil.nullStr(jso.optString("name"));
	}
}
