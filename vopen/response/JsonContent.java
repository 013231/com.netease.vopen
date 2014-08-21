package vopen.response;
/* 返回的json数据相应字段  chenping 2011-12-30
 * 
 * */
public class JsonContent {
	/**获取视频节目信息返回的json数据相应字段  */
	public static String ccPic = "ccPic";//CC图片地址
	public static String ccUrl = "ccUrl";//CC链接地址
	public static String description = "description";//节目描述
	public static String director = "director";//导演
	public static String imgpath = "imgpath";//图片地址
	public static String include_virtual = "include_virtual";//精华贴地址
	public static String playcount = "playcount";//总集数
	public static String plid = "plid";//节目ID
	public static String school = "school";//学校
	public static String subtitle = "subtitle";//剧集的英文标题
	public static String title = "title";//标题
	public static String type = "type";//节目类别，六大分类
	public static String updated_playcount = "updated_playcount";//已翻译集数
	public static String videoList = "videoList";//视频集合

	public static String storetime = "storetime";
	
	public static String updatenum = "updatenum";
	public static String lookRecord = "lookrecord";
	public static String lookRecordtag = "lookRecordtag";
	
	public static String coursejson = "coursejson";
	
	/**每个videoList中的json字段  */
	public static String videoTitle = "title";//视频标题
	public static String videoUrl = "repovideourl";//发布地址
	public static String videoLen = "mlength";//视频长度
	public static String imgPath = "imgpath";//图片地址
	public static String videoNum = "pnumber";//第几集
	public static String commentId = "commentid";//评论ID
	public static String subTitle  = "subtitle";//子标题
	public static String subtitleLanguage  = "subtitle_language";//字幕语言
	public static String videoSize = "mp4size";//??文档未定义	
	
	//----------关于信息的json字段----------
	//----------{"remark":"（姓名按拼音排序）","webopenurl":"http://v.163.com/open/","foreword":
	//"真挚感谢以下为“网易公开课”公益事业做出贡献的同事：","name":"蔡迎东、陈德进...",
	//"intro":"网易公开课客户端，是网易为爱学习的网友打造的“随时随地上名校公开课”的免费课程平台。
	//它以哈佛、耶鲁、牛津、剑桥等“全球名校视频公开课”为内容资源，结合稳重的界面设计和人性化的操作，
	//实现您与名校真正零距离接触，无成本、无障碍地了解世界前沿的新知、新思。"}----------
	public static String aboutIntro = "intro";//简介
	public static String aboutWebUrl = "webopenurl";//网站主页
	public static String aboutForeword = "foreword";//感谢前言
	public static String aboutRemark = "remark";//提醒(姓名按拼音排序)
	public static String aboutThanksName = "name";//感谢名单
	//----------EndOf关于信息的json字段----------
	

}
