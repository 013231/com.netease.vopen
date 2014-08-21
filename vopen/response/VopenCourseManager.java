package vopen.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.vopen.pal.Constants;

import common.pal.PalLog;
import common.util.Util;


/**
 * 存放所有数据 提供根据课程分类及来源提供查询的接口
 * 
 * @author MR
 * 
 */
public class VopenCourseManager {
	private static final String TAG = "VopenCourseManager";
	List<CourseInfo> mAllCourseList;

	public VopenCourseManager(String allCourseJson) {
		// TODO Auto-generated constructor stub
		mAllCourseList = new ArrayList<CourseInfo>();
		init(allCourseJson);
	}
	
	public VopenCourseManager(List<CourseInfo> allCourseList) {
		// TODO Auto-generated constructor stub
		mAllCourseList = allCourseList;
	}

	/**
	 * 初始化所有课程数据 将JSON String数据格式转化为可用数据结构
	 * 
	 * @param allCourseJson
	 */
	public void init(String allCourseJson) {
		mAllCourseList.clear();
		try {
			JSONArray courseArray = new JSONArray(allCourseJson);
			JSONObject obj = null;
			for (int i = 0; i < courseArray.length(); i++) {
				obj = courseArray.getJSONObject(i);
				vopen.response.CourseInfo info = new vopen.response.CourseInfo(
						obj);
				if (Util.isStringEmpty(info.type))
					continue;
				mAllCourseList.add(info);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			PalLog.e(TAG, e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有课程集合
	 * 
	 * @return
	 */
	public List<CourseInfo> getAllCourses() {
		if (mAllCourseList == null || mAllCourseList.size() == 0) {
			PalLog.e(TAG, "Course Manager not init!");
			return null;
		}
		// List<CourseInfo> allList = new ArrayList<CourseInfo>();
		// Collections.copy(allList, mAllCourseList);
		return mAllCourseList;
	}

	/**
	 * 获取指定课程集合
	 * 传入null表示不过滤该标识
	 * @param tags
	 *            课程分类(新)
	 * @param source
	 *            课程来源
	 * @param type
	 *            课程分类(旧)
	 * @return 筛选后的课程集合
	 */
	public List<CourseInfo> getCoursesBy(String tags, String source, String type) {
		if (mAllCourseList == null || mAllCourseList.size() == 0) {
			PalLog.e(TAG, "Course Manager not init!");
			return null;
		}
		List<CourseInfo> list = new ArrayList<CourseInfo>();
		int length = mAllCourseList.size();
		for (int i = 0; i < length; i++) {
			CourseInfo info = mAllCourseList.get(i);
			if(type == null && info.type.contains(Constants._TAG_hot)) {
				continue;
			}
			if(type == null && info.type.contains(Constants._TAG_head)) {
				continue;
			}
			//进行筛选
			if ((type == null || info.type.contains(type))
					&& (tags == null || info.tags.contains(tags))
					&& (source == null || info.source.contains(source))) {
				list.add(info);
			}
		}
		return list;
	}
	
	/**
	 * 根据课程id来返回课程的信息
	 * @param courseId 
	 * @return
	 */
	public CourseInfo getCourseById(String courseId){
		if (mAllCourseList == null || mAllCourseList.size() == 0) {
			PalLog.e(TAG, "Course Manager not init!");
			return null;
		}
		int length = mAllCourseList.size();
		for (int i = 0; i < length; i++) {
			CourseInfo info = mAllCourseList.get(i);
			if (info.plid.equals(courseId)){
				return info;
			}
		}
		return null;
	}
	
	
	/**
	 * 获取一门课程的相关课程
	 * @param info 源课程
	 * @return
	 */
	public List<CourseInfo> getRelativeCourses(CourseInfo info) {
		List<CourseInfo> relativeCourseList = new ArrayList<CourseInfo>();
		if(info != null) {
			String tags = info.tags;
			String source = info.source;
			String[] temp = tags.split(",");
			//多次检索tags
			for(int i = 0; i < temp.length; i++	){
				relativeCourseList.addAll(getCoursesBy(temp[i], source, null));
			}
			for(CourseInfo cInfo:relativeCourseList) {
				if(cInfo.plid.equals(info.plid)) {
					relativeCourseList.remove(cInfo);
					break;
				}
			}
		}
		return relativeCourseList;
	}
	
	/**
	 * 清除所有课程数据
	 */
	public void clearList(){
		mAllCourseList.clear();
	}

}
