package vopen.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import vopen.db.DBApi.AccountInfo;
import vopen.db.DBApi.CollectInfo;
import vopen.db.DBApi.CourseDetailInfo;
import vopen.db.DBApi.CoursePlayInfo;
import vopen.db.DBApi.CourseType;
import vopen.db.DBApi.DownLoadInfo;
import vopen.db.DBApi.EDownloadStatus;
import vopen.db.DBApi.VideoPlayInfo;
import vopen.db.VopenContentProvider.DownloadManagerHelper;
import vopen.db.VopenContentProvider.UserAccountHelper;
import vopen.db.VopenContentProvider.VopenAllDataJsonHelper;
import vopen.db.VopenContentProvider.VopenCoursePlayRecordHeleper;
import vopen.db.VopenContentProvider.VopenDetailHelper;
import vopen.db.VopenContentProvider.VopenMyCollectHelper;
import vopen.download.DownloadPrefHelper;
import vopen.response.CollecinfoItem;
import vopen.response.CourseInfo;
import vopen.tools.FileUtils;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.netease.util.PDEEngine;
import com.netease.vopen.pal.Constants;
import common.pal.PalLog;
import common.util.StringUtil;
import common.util.Util;

/*************************************************************
 * 该类用来处理数据库操作后数据结构的转换及封装
 * @author echo_chen
 * @date 2012-01-08
 *************************************************************/

public class DBUtils {
	private static final String TAG = "DBUtils";

	/***
	 * 根据课程类型查询课程数据
	 * @param context
	 * @param type
	 * @return 课程json
	 * @deprecated
	 */
	public static String getLocalCourseByType(Context context, String type) {
		String rst = null;
		Cursor cur = DBApi.queryCourseByType(context, type,null);
		if (cur != null && cur.moveToFirst()) {
			rst = cur.getString(cur.getColumnIndex(VopenAllDataJsonHelper.COURSE_CONTENT));
		}
		if (cur != null) {
			cur.close();
		}
		return rst;
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 * @deprecated
	 */
	public static boolean isAllCourseDataExist(Context context){
		boolean ret = false;
		Cursor cur = DBApi.queryCourseByType(context, CourseType.DATA_TYPE_ALL, null);
		if (cur != null && cur.moveToFirst()){
			ret = true;
		}
		if (cur != null){
			cur.close();
		}
		return ret;
	}
	
	/**保存，修改课程*/
	public static void insertOrUpdateCourse(Context context, String plid,
			String json) {
		Cursor c = DBApi.queryCourseDetailByCourseID(context,plid,null);

		CourseDetailInfo course = new CourseDetailInfo();
		course.mContent = json;
		course.mCourse_id = plid;
		if (c == null || c.getCount() == 0) {
			DBApi.insertCourseDetail(context, course);
		} else {
			DBApi.updateCourseDetail(context, course);
		}

		if (c != null)
			c.close();
	}
	/***
	 * 根据课程id查询课程详情
	 * @param context
	 * @param course_id
	 * @return CourseInfo
	 */
	public static CourseInfo getCourseByPlid(Context context, String course_id){
		CourseInfo course = null;
		Cursor c = DBApi.queryCourseDetailByCourseID(context, course_id,null);
		if(c != null && c.getCount() > 0){
			c.moveToFirst();
			String content = c.getString(c.getColumnIndex(VopenDetailHelper.COURSE_CONTENT));
			if(!Util.isStringEmpty(content))
				course = new CourseInfo(content);
		}
		
		if(c != null)
			c.close();
		
		return course;
	}	
	 /**
     * 查询是已否收藏某一课程
     * 已登录情况下
     * @param context
     * @param courseid 课程id
     * @param userid 
     * @return 存在-true,不存在-false
     */
	public static boolean isCollectExit(Context context, String courseId, String userId){
	    Cursor c = null;
	    boolean bRet = false;
	    if(!Util.isStringEmpty(userId)){
	        c = DBApi.queryCollectByCourseID(context, courseId, userId);
	    }
	    
	    if(c != null && c.moveToFirst()){
	    	bRet = true;
	    }

	    if(c != null)
	    	c.close();
	    
	    return bRet;
	}
	
	/**
	 * 查询是否已收藏某一课程
     * 未登录情况下
	 * @param context
	 * @param courseId
	 * @return
	 */
	public static boolean isCollectExitNoLogin(Context context, String courseId){
	    Cursor c = DBApi.queryCollectByCourseIDNoLogin(context, courseId);
	    boolean bRet = false;
	    
        if(c != null && c.moveToFirst()){
        	bRet = true;
        }

        if(c != null)
        	c.close();
        
        return bRet;
	}
	
	/**
	 * 查询是否已收藏某一课程
	 * @param context
	 * @param courseId
	 * @param userId
	 * @param isLogin
	 * @return
	 */
	public static boolean isCollect(Context context, String courseId, String userId, boolean isLogin){
		if(isLogin)
			return isCollectExit(context, courseId, userId);
		else
			return isCollectExitNoLogin(context, courseId);
	}
	
	/**
	 * 是否有收藏的数据
	 * @param userid
	 * @param isLogin
	 * @return
	 */
	public static boolean isHasCollectData(Context context, String userid, boolean isLogin){
		Cursor c;
		if(isLogin){
			c = DBApi.queryCollectAllByUser(context, userid, null,null);
		}
		else{
			c = DBApi.queryCollectAllNoLogin(context, null);
		}
		
		boolean res = (c != null && c.getCount() > 0);
		
		if(c != null)
			c.close();
		
		return res;
	}
	/**
	 * 获取播放记录的位置,
	 * 无记录返回0
	 * @param context
	 * @param courseId
	 * @param videoId
	 * @return
	 */
	public static VideoPlayInfo getPlayRecordInfo(Context context, String courseId, int videoId){
	    VideoPlayInfo info = null;
	    Cursor c;
        c = DBApi.queryVideoPlayByCourseIDAndVideoID(context, null,courseId, videoId);
        if(c != null){
            if(c.moveToFirst()){
                info = new VideoPlayInfo();
                info.mCourse_id = courseId;
                info.mVideo_index = videoId;
                info.mPlay_position = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION));
                info.mVideo_count = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS));
                info.mVideo_length = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_LENGTH));
            }
            c.close();
        }
        return info;
	}
	
	/**
     * 检查文件是否在本地下载完毕
     * 首先从数据库中读取已下载视频
     * 然后与本地文件对比，若存在则返回文件路径
     * @param db
     * @param cid
     * @param sid
     * @return filename 如果不存在，返回null
	 * @throws InterruptedException 
     */
    public static String isLocalFileExist(Context context, String courseId,int videoId){
        String filename = null;
        Cursor c = DBApi.queryDownloadByCourseIDAndPnumber(context, courseId, videoId,null);
        if(c !=null){
            if(c.moveToFirst()){
                int status = c.getInt(c.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS));
                if(status == 1){
                String path = "";
				try {
					path = FileUtils.getSavedDownloadVideoPath(context, courseId, videoId, true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                Log.i("download_path", path);
                    if(FileUtils.isFileExit(path))
                        filename = path;
                }
            }
            c.close();
        }
        return filename;
    }
	
    /**
     * 删除某一收藏,非登录
     * @param context
     * @param courseid
     */
    public static void removeCollectNoLogin(Context context, String courseid) {
        DBApi.deleteCollectByCourseIDNoLogin(context, courseid);
    }
    
    /**
     * 删除某一收藏,已登录
     * @param context
     * @param courseid
     */
    public static void removeCollect(Context context, String userId, String courseid) {
        DBApi.deleteCollectByCourseID(context, userId, courseid);
    }
    
    /**
     * 删除所有收藏，已登录
     * @param context
     */
    public static void removeCollectAll(Context context, String userId){
        DBApi.deleteAllCollect(context, userId);
    }
    
    /**
     * 添加收藏,未登录
     * @param context
     * @param courseInfo
     */
    public static void addCollectNoLogin(Context context, CourseInfo courseInfo, String dateTime, boolean isSynced){
        CollectInfo collectInfo = new CollectInfo();
        collectInfo.mCourse_id = courseInfo.plid;
        //优先使用横屏的大图片 -- hzsongyuming
        collectInfo.mCourse_img = courseInfo.largeImg;
        if (StringUtil.isEmpty(collectInfo.mCourse_img)){
        	collectInfo.mCourse_img = courseInfo.imgpath;
        }
        collectInfo.mCourse_new_translate_num = 0;
        collectInfo.mCourse_playcount = courseInfo.playcount;
        collectInfo.mCourse_title = courseInfo.title;
        collectInfo.mCourse_translatecount = courseInfo.updated_playcount;
        collectInfo.mData_time = dateTime;
        collectInfo.mIs_synchronized = isSynced;
        DBApi.insertCollectNoLogin(context, collectInfo);
    }
    
    public static List<CollectInfo> getAllCollectNoLogin(Context context){
        List<CollectInfo> list = new ArrayList<CollectInfo>();
        Cursor c = DBApi.queryCollectAllNoLogin(context, null);
        if(c != null){
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                CollectInfo info = new CollectInfo();
                info.mCourse_id = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLID));
                info.mCourse_img = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_IMG));
                info.mCourse_new_translate_num = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM));
                info.mCourse_playcount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLAYCOUNT));
                info.mCourse_title = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_TITLE));
                info.mCourse_translatecount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_TRANSLATECOUNT));
                info.mData_time = c.getString(c.getColumnIndex(VopenMyCollectHelper.DATA_TIME));
                list.add(info);
            }
            c.close();
        }
        return list;
    }
    
    public static List<CollectInfo> getAllCollectLogin(Context context, String userId){
        List<CollectInfo> list = new ArrayList<CollectInfo>();
        Cursor c = DBApi.queryCollectAllByUser(context, userId, null,null);
        if(c != null){
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            	CollectInfo info = new CollectInfo();
                info.mCourse_id = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLID));
                info.mCourse_img = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_IMG));
                info.mCourse_new_translate_num = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM));
                info.mCourse_playcount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLAYCOUNT));
                info.mCourse_title = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_TITLE));
                info.mCourse_translatecount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_TRANSLATECOUNT));
                info.mData_time = c.getString(c.getColumnIndex(VopenMyCollectHelper.DATA_TIME));
                list.add(info);
            }
            c.close();
        }
        return list;
    }
    
    public static List<CollecinfoItem> getAllPlayRecordItem(Context context, List<CourseInfo> courseList){
        List<CollecinfoItem> list = new ArrayList<CollecinfoItem>();
//        Cursor c = DBApi.queryCollectAllByUser(context, null);
        String sort = VopenCoursePlayRecordHeleper.PLAY_DATE + " DESC";
        Cursor c = DBApi.queryCoursePlayAll(context, sort, null);
        if(c != null) {
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                CollecinfoItem infoItem = new CollecinfoItem();
                CollectInfo info = infoItem.mCollectInfo;
                info.mCourse_id = c.getString(c.getColumnIndex(VopenCoursePlayRecordHeleper.COURSE_ID));
                info.mCourse_img = c.getString(c.getColumnIndex(VopenCoursePlayRecordHeleper.IMG_PATH));
                info.mCourse_playcount = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS));
                info.mCourse_title = c.getString(c.getColumnIndex(VopenCoursePlayRecordHeleper.TITLE));
                
                CourseInfo courseInfo = getCourseByPlid(context, info.mCourse_id);
                if(courseInfo == null && courseList != null){
                	for (CourseInfo course : courseList) {
            			if (course.plid.equals(info.mCourse_id)){
            				courseInfo = course;
            					break;
            			}
            		}
                }
                if(courseInfo != null)
                	info.mCourse_translatecount = courseInfo.updated_playcount;
                infoItem.mStrPlayRecord = getCoursePlayRecord2(context, info.mCourse_id);
                list.add(infoItem);
            }
            c.close();
        }
        return list;
    }
    
    /**
     * 该部分包括 课程播放信息
     * @param context
     * @return List<CollecinfoItem>
     */
    public static List<CollecinfoItem> getAllCollectItemLoginByUser(Context context, String user_id){
        List<CollecinfoItem> list = new ArrayList<CollecinfoItem>();
        Cursor c = DBApi.queryCollectAllByUser(context,user_id, null,null);
        if(c != null){
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            	CollecinfoItem infoItem = new CollecinfoItem();
            	CollectInfo info = infoItem.mCollectInfo;
                info.mCourse_id = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLID));
                info.mCourse_img = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_IMG));
                info.mCourse_new_translate_num = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM));
                info.mCourse_playcount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLAYCOUNT));
                info.mCourse_title = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_TITLE));
                info.mCourse_translatecount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_TRANSLATECOUNT));
                info.mData_time = c.getString(c.getColumnIndex(VopenMyCollectHelper.DATA_TIME));
                
                infoItem.mStrPlayRecord = getCoursePlayRecord2(context, info.mCourse_id);
                
                list.add(infoItem);
            }
            c.close();
        }
        return list;
    }
    /**
     * 该部分包括 课程播放信息
     * @param context
     * @return List<CollecinfoItem>
     */
    public static List<CollecinfoItem> getAllCollectItemNoLogin(Context context){
        List<CollecinfoItem> list = new ArrayList<CollecinfoItem>();
        Cursor c = DBApi.queryCollectAllNoLogin(context, null);
        if(c != null){
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            	CollecinfoItem infoItem = new CollecinfoItem();
            	CollectInfo info = infoItem.mCollectInfo;
                info.mCourse_id = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLID));
                info.mCourse_img = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_IMG));
                info.mCourse_new_translate_num = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM));
                info.mCourse_playcount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_PLAYCOUNT));
                info.mCourse_title = c.getString(c.getColumnIndex(VopenMyCollectHelper.COURSE_TITLE));
                info.mCourse_translatecount = c.getInt(c.getColumnIndex(VopenMyCollectHelper.COURSE_TRANSLATECOUNT));
                info.mData_time = c.getString(c.getColumnIndex(VopenMyCollectHelper.DATA_TIME));
                
                infoItem.mStrPlayRecord = getCoursePlayRecord2(context, info.mCourse_id);
                
                list.add(infoItem);
            }
            c.close();
        }
        return list;
    }
	
    /**
     * 添加收藏,已登录
     * @param context
     * @param courseInfo
     */
    public static void addCollect(Context context, CourseInfo courseInfo, String dateTime, boolean isSynced, String userId){
        CollectInfo collectInfo = new CollectInfo();
        collectInfo.mCourse_id = courseInfo.plid;
        //优先使用横屏的大图片 -- hzsongyuming
        collectInfo.mCourse_img = courseInfo.largeImg;
        if (StringUtil.isEmpty(collectInfo.mCourse_img)){
        	collectInfo.mCourse_img = courseInfo.imgpath;
        }
        collectInfo.mCourse_new_translate_num = 0;
        collectInfo.mCourse_playcount = courseInfo.playcount;
        collectInfo.mCourse_title = courseInfo.title;
        collectInfo.mCourse_translatecount = courseInfo.updated_playcount;
        collectInfo.mData_time = dateTime;
        collectInfo.mIs_synchronized = isSynced;
        collectInfo.mUser_id = userId;
        DBApi.insertCollect(context, collectInfo);
    }
    
    /**
     * 获取某课程已下载完成列表
     * @param context
     * @param courseId
     */
    public static List<DownLoadInfo> getDownloadedVideoList(Context context, String courseId){
        List<DownLoadInfo> list = new ArrayList<DownLoadInfo>();
        Cursor c = DBApi.queryDownloadByCourseID(context, courseId,null);
        if(c != null){
        	for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
        		if(c.getInt(c.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS)) != 1)continue;
                
                DownLoadInfo info = new DownLoadInfo();
                info.mCourse_id = courseId;
                info.mCourse_name = c.getString(c.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
                info.mCourse_pnumber = c.getInt(c.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
                info.mDownload_status = EDownloadStatus.DOWNLOAD_DONE;
                String file = "";
                try {
                	file = FileUtils.getSavedDownloadVideoPath(context, info.mCourse_id, info.mCourse_pnumber, true);
                }catch (InterruptedException e){
                	e.printStackTrace();
                }
                boolean exist = FileUtils.isFileExit(file);
                if (exist)
                	list.add(info);
        	}
            c.close();
        }
        return list;
    }
    
    /**
     * 获取某课程所有播放过的视频的播放记录
     * @param context
     * @param courseId
     * @return
     */
    public static List<VideoPlayInfo> getVideoPlayList(Context context, String courseId){
        List<VideoPlayInfo> list = new ArrayList<VideoPlayInfo>();
        Cursor c = DBApi.queryVideoPlayByCourseID(context, courseId,null);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                VideoPlayInfo info = new VideoPlayInfo();
                info.readFromCursor(c);
//                info.mCourse_id = courseId;
//                info.mVideo_index = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_INDEX));
//                info.mPlay_position = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION));
//                info.mVideo_count = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS));
                list.add(info);
            }
            c.close();
        }
        return list;
    }
    /**
     * 
     * @param context
     * @param courseId
     */
    public static void removeCoursePlayByCourseId(Context context, String courseId){
        DBApi.deleteCoursePlayCourseID(context, courseId);
    }
    
    /**
     * 获取最近一次播放记录
     * 从PlayRecord表中取最后一条数据
     * @param context
     * @param courseId
     * @return
     */
    public static VideoPlayInfo getLatestPlayRecord(Context context, String courseId){
        VideoPlayInfo info = null;
        Cursor c = DBApi.queryCoursePlayByCourseID(context, courseId,null);
        if(c != null){
            if(c.moveToLast()){
                info = new VideoPlayInfo();
                info.mCourse_id = courseId;
                info.mVideo_index = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_INDEX));
                info.mPlay_position = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION));
                info.mVideo_count = c.getInt(c.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS));
            }
            c.close();
        }
        return info;
    }
    /***
     * 添加或修改播放记录
     * @param resolver
     * @param info
     */
	public static void insertOrUpdatePlayRecord(Context context, CoursePlayInfo info){
		Cursor cursor = null;
		try {
			// 保存课程播放记录
			cursor = DBApi.queryCoursePlayByCourseID(context,info.mCourse_id, new String[]{BaseColumns._ID});
			if(null!= cursor && cursor.getCount() > 0){
				DBApi.updateCoursePlayInfo(context, info);
			}else{
				DBApi.insertCoursePlayInfo(context, info);
			}
			if(null != cursor){
				cursor.close();
				cursor = null;
			}
			// 保存视频播放记录
			cursor = DBApi.queryVideoPlayByCourseIDAndVideoID(context, new String[]{BaseColumns._ID},info.mCourse_id, info.mVideo_index);
			VideoPlayInfo vInfo = new VideoPlayInfo();
			vInfo.mCourse_id = info.mCourse_id;
			vInfo.mPlay_position = info.mPlay_position;
			vInfo.mVideo_count = info.mVideo_count;
			vInfo.mVideo_index = info.mVideo_index;
			vInfo.mVideo_length = info.mVideo_length;
			
			if(null != cursor && cursor.getCount() > 0){
				DBApi.updateVideoPlayInfo(context, vInfo);
			}else{
				DBApi.insertVideoPlayInfo(context, vInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != cursor){
				cursor.close();
				cursor = null;
			}
		}
	}
	
    /***
	 * 根据课程id和视频id,获取当前视频播放位置
	 * @param context
	 * @param course_id
	 * @param video_id
	 * @return 
	 */
	public static int getVideoPlayPositioin(Context context, String course_id,  int video_id) {
		Cursor cursor = null;
		int position = 0;
		try {
			cursor = DBApi.queryVideoPlayByCourseIDAndVideoID(context, new String[]{VopenCoursePlayRecordHeleper.PLAY_POSITION},course_id, video_id);
			if(null != cursor && cursor.moveToFirst()){
				position = cursor.getInt(cursor.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != cursor) {
				cursor.close();
			}
		}
		return position;
	}
	 /***
	 * 根据课程id,从课程播放表中获取当前课程播放记录
	 * @param context
	 * @param course_id
	 * @return CoursePlayInfo 包括视频id mVideo_index和播放位置mPlay_position
	 */
	public static CoursePlayInfo getCoursePlayRecordsByCourseid(Context context, String course_id) {
		// TODO Auto-generated method stub
		Cursor cursor = null;
		CoursePlayInfo info = null;
		try {
			cursor = DBApi.queryCoursePlayByCourseID(context,course_id, null);
			if(null != cursor && cursor.moveToFirst()){
				info = new CoursePlayInfo();
				info.readFromCursor(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != cursor) {
				cursor.close();
			}
		}
		return info;
	}
	/***
	 * 根据课程id和视频id,从视频播放表中获取当前视频播放记录信息
	 * @param context
	 * @param course_id
	 * @param video_id
	 * @return
	 */
	public static VideoPlayInfo getVideoPlayRecordByCourseIdAndVid(Context context, String course_id, int video_id) {
		// TODO Auto-generated method stub
		VideoPlayInfo info = null;
		Cursor cursor = null;
		try {
			cursor = DBApi.queryVideoPlayByCourseIDAndVideoID(context, null,course_id, video_id);
			if(null != cursor && cursor.moveToFirst()){
				info = new VideoPlayInfo();
				info.readFromCursor(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != cursor) {
				cursor.close();
			}
		}
		return info;
	}
	/***
	 * 课程表中是否有播放记录
	 * @param context
	 * @return
	 */
	public static boolean isHasPlayRecrodData(Context context) {
		// TODO Auto-generated method stub
		boolean result = false;
		Cursor cursor = null;
		try {
			cursor = DBApi.queryCoursePlayInfo(context, null, new String[]{BaseColumns._ID}, null);
			if(null != cursor && cursor.getCount() > 0){
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != cursor) {
				cursor.close();
			}
		}
		return result;
	}
	/**
	 * 删除课程播放表和视频播放表中的对应课程id的播放记录
	 * @param context
	 * @param ids
	 */
	public static void deletePlayRecordsByIds(Context context, String[] ids) {
		// TODO Auto-generated method stub
        int courseCount = 0;
        int videoCount = 0;
        for (String id : ids) {
        	courseCount += DBApi.deleteCoursePlayCourseID(context, id) ;
            videoCount  += DBApi.deleteVideoPlayCourseID(context, id);
        }
        PalLog.d(TAG, " delete course records count=" + courseCount + " delete video records count=" + videoCount);
	}
	/**
	 * 清空播放记录
	 * @param context
	 */
	public static void clearPlayRecords(Context context){
		// TODO Auto-generated method stub
		// 课程记录
		int count = DBApi.deleteAllCoursePlay(context);
		PalLog.d(TAG, " clear course records count=" + count);
		// 视频记录
		count = DBApi.deleteAllVideoPlay(context);
		PalLog.d(TAG, " clear video records count=" + count);
	}
    /**
	 * 添加下载列表到数据库  
	 * @param alreadySelect 已选列表
	 * @param context
	 */
	public static void insertDownloadList(Context context,List<DownLoadInfo> alreadySelect) {
		DownLoadInfo tempInfo = null;
		for(int i=0; i<alreadySelect.size(); i++){
			// 是否已经加入到下载列表中
			tempInfo = alreadySelect.get(i);
			String courseid = tempInfo.mCourse_id;
			int selectid = tempInfo.mCourse_pnumber;
			boolean alreadyDown = DBUtils.isAlreadyDown(context, courseid, selectid);
			if(alreadyDown && !FileUtils.isFileExit(FileUtils.DownloadPath + courseid + "/" + selectid + ".mp4")){ // 存在记录，但是文件和记录关联不上， 则修改下载状态，重新加入到下载列表中
				EDownloadStatus status = EDownloadStatus.DOWNLOAD_WAITTING;
				DBApi.updateDownloadStatus(context,courseid,selectid,status);
			}else if(!alreadyDown){ // 不存在，新插入数据
				DBApi.insertDownloadInfo(context, tempInfo);
			}
		}
		
	}
	/**
	 * 查询一个视频是否已下载  
	 * @param context
	 * @param course_id
	 * @param viedo_id
	 */
	public static boolean isAlreadyDown(Context context, String course_id, int viedo_id) {
		Cursor cur = DBApi.queryDownloadByCourseIDAndPnumber(context, course_id, viedo_id,null);
		boolean exist = false;
		if(null != cur && cur.getCount()>0) {
			exist = true;
		}
		if(null != cur) {
			cur.close();
		}
		return exist;
	}
	/**
	 * 查询选择下载文件的总长度，包含数据库里记录已下载但在本地不存在的文件长度  
	 * @param db
	 * @param type
	 * @return long size
	 */
	public static long getSelectedDownloadSizeFull(Context context, EDownloadStatus... type) {
		
		int len = type.length;
		StringBuilder selection = new StringBuilder();
		String[] selectArgs = new String[len];
		for(int i = 0; i < len; i++){
			if(i==len-1) {
				selection.append(DownloadManagerHelper.DOWNLOAD_STATUS).append("= ?");
			}else {
				selection.append(DownloadManagerHelper.DOWNLOAD_STATUS).append("= ? or ");
			}
			selectArgs[i] = String.valueOf( type[i].value());
		}
		Cursor cur = DBApi.queryDownloadInfo(context, selection.toString(), null, selectArgs);
		long totalsize = 0;
		if(cur != null){
        	for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()){
        		long size = cur.getLong(cur.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
				String course_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
				int video_id = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
				String path = "";
				try {
					path = FileUtils.getSavedDownloadVideoPath(context,course_id, video_id, false);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				totalsize += size; 
				if(FileUtils.isFileExit(path)) {
					File file =  new File(path);
					if(null != file) {
						long downsize = file.length();
						totalsize -= downsize;
					}
				}
        	}
        	cur.close();
        }
		return totalsize;
	}
	/**
	 * 查询选择下载文件的总长度，包括正在下，失败的，等待的，暂停的，未下的
	 * @param db
	 * @param type
	 * @return long size
	 */
	public static long getSelectDownloadSize(Context context){
		long totalsize = 0;
		totalsize = getSelectedDownloadSizeFull(context, new EDownloadStatus[]{EDownloadStatus.DOWNLOAD_DOING, 
				EDownloadStatus.DOWNLOAD_FAILED,EDownloadStatus.DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME,EDownloadStatus.DOWNLOAD_FAILED_VIDEO_ERROR,
				EDownloadStatus.DOWNLOAD_PAUSE, EDownloadStatus.DOWNLOAD_WAITTING});
		return totalsize;
		
	}
	/**
	 * 查询已下载完成文件的总长度  
	 * @param db
	 * @param type
	 * @return long size
	 */
	public static long getDownloadSizeFull(Context context) {
		Cursor cur = DBApi.queryDownloadedAllCourse(context, null);
		long totalsize = 0;
		if(cur != null){
        	for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()){
        		long size = cur.getLong(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
				totalsize += size; 
        	}
        	cur.close();
        }
		return totalsize;
	}
	/**获取下载表中的某一课程的所有video
	 * @param context
	 * @param courseId 课程id
	 *  */
	public static List<DownLoadInfo> getDownloadListByCourseId(Context context, String course_id) {
		Cursor cur = DBApi.queryDownloadByCourseID(context, course_id,null);
		List<DownLoadInfo> list = new ArrayList<DownLoadInfo>();
		if(cur != null){
        	for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()){
				DownLoadInfo info = new DownLoadInfo();
				info.mCourse_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
				info.mCourse_name =  cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
				info.mCourse_pnumber = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
				info.mCourse_thumbnail = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_THUMBNAIL));
				info.mDownload_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
				int status = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS));
				info.mDownload_status =  intToEDownloadStatus(status);
				info.mDownload_url = cur.getString(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_URL));
				info.mIsSelected = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.SELECT_STATUS))==1 ? true:false;
				info.mTotal_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
				list.add(info);
			}
        	cur.close();
		}
		return list;
	}
	
	/**
	 * 获取下载列表中所有video
	 * @param context
	 * @return
	 */
	public static List<DownLoadInfo> getAllDownloadList(Context context){
	    Cursor cur = DBApi.queryDownloadAll(context, null,null);
	    List<DownLoadInfo> list = new ArrayList<DownLoadInfo>();
        if(null != cur && cur.moveToFirst()){
            while(!cur.isAfterLast()){
                DownLoadInfo info = new DownLoadInfo();
                info.mCourse_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
                info.mCourse_name =  cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
                info.mCourse_pnumber = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
                info.mCourse_thumbnail = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_THUMBNAIL));
                info.mDownload_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
                int status = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS));
                info.mDownload_status =  intToEDownloadStatus(status);
                info.mDownload_url = cur.getString(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_URL));
                info.mIsSelected = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.SELECT_STATUS))==1 ? true:false;
                info.mTotal_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
                list.add(info);
                cur.moveToNext();
            }
        }
        
        if(null!=cur ) {
            cur.close();
        }
        return list;
	}
	
    public static EDownloadStatus intToEDownloadStatus(int value) {
    	EDownloadStatus status = EDownloadStatus.DOWNLOAD_NO;
    	switch(value){
	    	case 4:
	    		status = EDownloadStatus.DOWNLOAD_FAILED;
	    		break;
	    	case 1:
	    		status = EDownloadStatus.DOWNLOAD_DONE;
	    		break;
	    	case 3:
	    		status = EDownloadStatus.DOWNLOAD_DOING;
	    		break;
	    	case 6:
	    		status = EDownloadStatus.DOWNLOAD_PAUSE;
	    		break;
	    	case 2:
	    		status = EDownloadStatus.DOWNLOAD_WAITTING;
	    		break;
	    	case 7:
	    		status = EDownloadStatus.DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME;
	    		break;
	    	case 8:
	    		status = EDownloadStatus.DOWNLOAD_FAILED_VIDEO_ERROR;
	    		break;
    	}
    	return status;
    }
    
    
    /**
	 * 根据json的内容，将数据解析为DBGetStoreListResponse的List结构，并用于分页
	 * @param startNum 用于分页使用，该参数表示该页的启示点
	 * @param pageNum 每页的个数，若为-1表示去全部内容
	 * 
	 * @author cxl
	 */
	public static List<CourseInfo> getAllVideoList(String jsonStr, int startNum, int pageNum){
		List<CourseInfo> responseList = new ArrayList<CourseInfo>();
		
		//现获得全部的数据
		List<CourseInfo> allData = getAllVideoList(jsonStr);
		
		//根据分页的情况获得所需的一页返回给客户端
		int length = 0;
		int end = startNum + pageNum;
		if(null != allData){
			length = allData.size();
			
			Log.v("getAllVideoList", "allData.size() = " + allData.size()
					+ " end = " + end);
		}
		
		if(pageNum == -1)
			return allData;

		if(length > startNum){
			if(length < end) {
				end = length;
			}
			if(StringUtil.checkObj(allData)){
				for(int index = startNum; index < end; index++){
					CourseInfo item = allData.get(index);
					responseList.add(item);
				}
			}
		}
		return responseList;
	}
	
	/**
	 * 根据关键词query，完成课程的匹配
	 * @param orglist
	 * @param query
	 * @return
	 */
public static List<CourseInfo> searchVideo(List<CourseInfo> orglist,String query){
		
		List<CourseInfo> tagvideo = new ArrayList<CourseInfo>();
		
		if(StringUtil.checkObj(orglist)){
			
			for(int index=0;index<orglist.size();index++){
				
				String type = ""+orglist.get(index).type;
				String title = ""+orglist.get(index).title;
				String subtitle = ""+orglist.get(index).subtitle;
				//Log.i(Constant.TAG, "type:"+type+"|title:"+title+"|subtitle:"+subtitle);
				if( (StringUtil.checkStr(type) && type.contains(query))
						||(StringUtil.checkStr(title) && title.contains(query))
						||(StringUtil.checkStr(subtitle) && subtitle.contains(query))){
					if(Constants._TAG_head.equals(type) || Constants._TAG_hot.equals(type))
						continue;
					tagvideo.add(orglist.get(index));
				}
			}
		}
		
		return tagvideo;
	}
	
	/**
	 * 根据关键词query，完成课程的匹配
	 * 大小写无关，空格间多匹配
	 * @param orglist
	 * @param query
	 * @return
	 */
	public static List<CourseInfo> searchVideos(List<CourseInfo> orglist, String query) {
		List<CourseInfo> tagvideo = new ArrayList<CourseInfo>();
		if(StringUtil.checkObj(orglist) && StringUtil.checkStr(query) && !"".equals(query.trim())){
			//替换所有标点符号为空格
			query = query.replaceAll("\\p{Punct}", " ").trim();
			//按空格分离
			String[] spl = query.split("\\s+");
			tagvideo = orglist;
			for(int i = 0; i < spl.length; i++){
				//嵌套搜索
				tagvideo = doSearch(tagvideo, spl[i]);
			}
		}
		return tagvideo;
	}
	
	/**
	 * 搜索课程的类型、标题、英文标题
	 * @param orglist
	 * @param query
	 * @return
	 */
	private static List<CourseInfo> doSearch(List<CourseInfo> orglist, String query){
		List<CourseInfo> tagvideo = new ArrayList<CourseInfo>();
		for (int index = 0; index < orglist.size(); index++) {
			String type = "" + orglist.get(index).type;
			String title = "" + orglist.get(index).title;
			String subtitle = "" + orglist.get(index).subtitle;
			if (search(type, query) || search(title, query) || search(subtitle, query)) {
				if (Constants._TAG_head.equals(type) || Constants._TAG_hot.equals(type))
					continue;
//				Log.i(TAG, "type:" + type + "|title:" + title + "|subtitle:" + subtitle);
				tagvideo.add(orglist.get(index));
			}
		}
		return tagvideo;
	}
	
	/**
	 * 大小写无关处理
	 * @param source
	 * @param query
	 * @return
	 */
	private static boolean search(String source, String query){
		if(StringUtil.checkStr(source) && StringUtil.checkStr(query)){
			//不区分大小写,去掉首尾空格
			String tSource = source.toLowerCase().trim();
			String tQuery = query.toLowerCase().trim();
			if(tSource.contains(tQuery))return true;
			else return false;
		}else{
			return false;
		}
	}
	
	/**
	 * 将json格式的数据转化为List结构的数据
	 * @param jsonStr
	 * @return
	 */
	public static List<CourseInfo> getAllVideoList(String jsonStr){
		List<CourseInfo> allDataList = new ArrayList<CourseInfo>();
		
		if(TextUtils.isEmpty(jsonStr))
			return null;

		try {
			
			JSONArray jsonarr = new JSONArray(jsonStr);
			int length = jsonarr.length();
			
			if(StringUtil.checkObj(jsonarr)){
				
				for(int index = 0; index < length; index ++){
					CourseInfo response = new CourseInfo(jsonarr.getString(index));
					
					allDataList.add(response);
				}
			}
		} catch (JSONException e) {
			
		}
		
		return allDataList;
	}
	
	public static String getCoursePlayRecord(Context context, String course_id){
		String strRecord = "";
        Cursor c = DBApi.queryCoursePlayByCourseID(context,course_id, null);
        if(c != null && c.moveToNext()){
        	int secitonid = c.getInt(c
					.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_INDEX));
			int position = c.getInt(c
					.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION));
			int totalLength = c.getInt(c
					.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_LENGTH));
			int playcount = c.getInt(c
					.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS));
    			
			if(new Integer(secitonid).intValue() == playcount){
				
				int or = totalLength-position;
				if(or<2){
					strRecord = "最后一课已看完，将重新播放";
				}else{
					strRecord = translatePlayRecord(String.valueOf(secitonid), position, totalLength);
				}
			}else{

				int or = totalLength-position;
				if(or<2){
					strRecord = String.format("第%s课已看完，将播放下一课", String.valueOf(secitonid));
				}else{
					strRecord = translatePlayRecord(String.valueOf(secitonid), position, totalLength);
				}
			}
        }else{
        	strRecord = "未观看，即将播放第1课";
        }
        
        if(c != null){
        	c.close();
        }
        return strRecord;
	}
	
	/**
	 * 获取某个课程的播放记录信息
	 * @param context
	 * @param course_id
	 * @return
	 */
	private static String getCoursePlayRecord2(Context context, String course_id) {
		String strRecord = "";
		Cursor c = DBApi.queryCoursePlayByCourseID(context, course_id, null);
		if (c != null && c.moveToNext()) {
			int secitonid = c.getInt(c
					.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_INDEX));
			int position = c
					.getInt(c
							.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION));//ms
			int totalLength = c.getInt(c
					.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_LENGTH));//s
			int playcount = c.getInt(c
					.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS));
			position = position / 1000;
			if (position > 0) {
				int left = totalLength - position;
				if (left <= 2) {//剩余2s以内,当作看完
					if (secitonid == playcount) {
						strRecord = "已看完";
					} else {
						strRecord = String.format("第%s集已看完",
								String.valueOf(secitonid));
					}
				} else {
					strRecord = String.format("看到第" + secitonid + "集 %s",
							DateUtils.formatElapsedTime(position));
				}
			}
		}
		if (c != null) {
			c.close();
		}
		if (strRecord.length() == 0) {
			strRecord = "未观看";
		}
		return strRecord;
	}
	
	private static String translatePlayRecord(String sectionid, int position,
			int all) {
		String rst = "看到第" + sectionid + "集 %s";
		String timeStr = DateUtils.formatElapsedTime(position);
		return String.format(rst, timeStr);
	}
	
	/**
	 * 获得已下载视频列表
	 * @param db
	 * @return List<List<DBDownloadedResponse>>
	 * @return 数据结构 以课程为单位，每个课程又包含已下载的视频
	 */
	public static List<List<DownLoadInfo>> getDownloadedList(Context context){
		List<DownLoadInfo> list = getAllDownloadedList(context, true);
		
		List<List<DownLoadInfo>> resultList = new ArrayList<List<DownLoadInfo>>();
		StringBuilder courses = new StringBuilder();
		/**
		 * list要存放多个list，每个list是一门课程
		 */
		for(DownLoadInfo response:list){
			if(!courses.toString().contains(response.mCourse_id))
				courses.append(response.mCourse_id).append(",");
		}
		
		String cs[] = courses.toString().split(",");
		
		for(String c:cs){
			
			if("".equals(c) || null==c)
				continue;
			
			List<DownLoadInfo> child = new ArrayList<DownLoadInfo>();
			for(DownLoadInfo response:list){
				if(c.equals(response.mCourse_id)){
					
					child.add(response);
				}
			}
			resultList.add(child);
		}
		
		return resultList;
	}
	/**
	 * 获得需要下载的视频列表
	 * @param db
	 * @return List<List<DBDownloadedResponse>>
	 * @return 数据结构 以课程为单位，每个课程又包含已下载的视频
	 */
	public static List<List<DownLoadInfo>> getNeedDownloadList(Context context){
		List<DownLoadInfo> list = getAllDownloadedList(context, false);
		
		List<List<DownLoadInfo>> resultList = new ArrayList<List<DownLoadInfo>>();
		StringBuilder courses = new StringBuilder();
		/**
		 * list要存放多个list，每个list是一门课程
		 */
		for(DownLoadInfo response:list){
			if(!courses.toString().contains(response.mCourse_id))
				courses.append(response.mCourse_id).append(",");
		}
		
		String cs[] = courses.toString().split(",");
		
		for(String c:cs){
			
			if("".equals(c) || null==c)
				continue;
			
			List<DownLoadInfo> child = new ArrayList<DownLoadInfo>();
			for(DownLoadInfo response:list){
				if(c.equals(response.mCourse_id)){
					
					child.add(response);
				}
			}
			resultList.add(child);
		}
		
		return resultList;
	}

	/**
	 * 获得全部下载列表，供 getDownloadedList 函数使用
	 * @param db
	 * @return
	 */
	public static List<DownLoadInfo> getAllDownloadedList(Context context, boolean downloaded){
		Cursor cur;
		if(downloaded){
			cur = DBApi.queryDownloadedAllCourse(context,null);
		}else{
			cur = DBApi.queryDownloadingAllCourse(context,null);
		}
		
		List<DownLoadInfo> list = new ArrayList<DownLoadInfo>();
		
		if(cur.moveToFirst()){
			while(!cur.isAfterLast()){
				DownLoadInfo response = new DownLoadInfo();
				response.m_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.Download_ID));
				response.mCourse_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
				response.mCourse_name = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
				response.mCourse_pnumber = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
				response.mCourse_thumbnail = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_THUMBNAIL));
				response.mDownload_size = cur.getLong(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
				response.mDownload_url = cur.getString(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_URL));
				response.mIsSelected = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.SELECT_STATUS)) == 0?false:true;
				response.mTotal_size = cur.getLong(cur.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
				
				EDownloadStatus eStatus = DBUtils.intToEDownloadStatus(cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS)));
				response.mDownload_status = eStatus;
				list.add(response);
				cur.moveToNext();
			}
		}
		
		if(null!=cur && !cur.isClosed())
			cur.close();
		
		return list;
	}
	public static int getNeedDownloadVideoCount(Context context) {
		int count = 0;
		Cursor cur = DBApi.queryDodingAndWaittingVideo(context);
		if(null != cur) {
			count = cur.getCount();
			cur.close();
		}
		return count;
	}
	public static DownLoadInfo getDownloadInfoById(Context context, String m_id){
		DownLoadInfo info = new DownLoadInfo();
		Cursor cur = DBApi.queryDownloadById(context, m_id,null);
		
		if(cur != null){
            for(cur.moveToFirst(); !cur.isAfterLast(); ){
            	info.m_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.Download_ID));
            	info.mCourse_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
				info.mCourse_name =  cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
				info.mCourse_pnumber = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
				info.mCourse_thumbnail = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_THUMBNAIL));
				info.mDownload_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
				int status = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS));
				info.mDownload_status =  intToEDownloadStatus(status);
				info.mDownload_url = cur.getString(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_URL));
				info.mIsSelected = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.SELECT_STATUS))==1 ? true:false;
				info.mTotal_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
				
				break;
            }
            cur.close();
        }else{
        	return null;
        }
		
		return info;
	}
	
	public static DownLoadInfo getDownloadNextTaskInfo(Context context) {

		Cursor cur = DBApi.queryDownloadNextTask(context, null);
		if (cur != null && cur.moveToFirst()) {
			DownLoadInfo info = new DownLoadInfo();

			info.m_id = cur.getString(cur
					.getColumnIndex(DownloadManagerHelper.Download_ID));
			info.mCourse_id = cur.getString(cur
					.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
			info.mCourse_name = cur.getString(cur
					.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
			info.mCourse_pnumber = cur.getInt(cur
					.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
			info.mCourse_thumbnail = cur.getString(cur
					.getColumnIndex(DownloadManagerHelper.COURSE_THUMBNAIL));
			info.mDownload_size = cur.getInt(cur
					.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
			int status = cur.getInt(cur
					.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS));
			info.mDownload_status = intToEDownloadStatus(status);
			info.mDownload_url = cur.getString(cur
					.getColumnIndex(DownloadManagerHelper.DOWNLOAD_URL));
			info.mIsSelected = cur.getInt(cur
					.getColumnIndex(DownloadManagerHelper.SELECT_STATUS)) == 1 ? true
					: false;
			info.mTotal_size = cur.getInt(cur
					.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
			cur.close();
			return info;
		}
		return null;
	}
	/**
	 * 获取下载列表中当前任务的最高优先级。
	 * @param context
	 * @return
	 */
	private static int getDownloadingPriority(Context context){
		int priority = -1;
		Cursor cur = DBApi.queryDownloadCurrentTaskPriority(context);
		if(cur != null){
			for(cur.moveToFirst(); !cur.isAfterLast(); ){
				priority = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.PRIORITY));
				break;
            }
        }
		cur.close();
		return priority;
	}

	/**
	 * 将指定id的下载任务的优先级设为最高。
	 * @param context
	 * @param id
	 */
	public static void updateDownloadPromoteTask(Context context, String id){
		int priority = getDownloadingPriority(context);
		DBApi.updateDownloadPriorityById(context, id, priority+1);
	}
	
	/**
	 * 获取当前登录帐号
	 * @param context
	 * @return
	 */
	public static AccountInfo getLoginAccount(Context context){
		
	    	AccountInfo info = null;
	        Cursor cur = DBApi.queryLoginAccount(context,null);

	        if (cur != null) {
	            try {
	                if (cur.moveToFirst()) {
	                	info = new AccountInfo();
	                	info.mUser_account = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_ACCOUNT));
	                	info.mUser_pwd = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_PASSWORD));
	                	info.mUser_nikename = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_NIKENAME));
	                	info.mUser_cookie = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_COOKIE));
	                	info.mIs_login = cur.getInt(cur.getColumnIndex(UserAccountHelper.IS_LOGIN))==1?true:false;
	                	String pwEnc = PDEEngine.PDecrypt(context, info.mUser_pwd);
	                	if(!Util.isStringEmpty(pwEnc)) {
	                		info.mUser_pwd = pwEnc;
	                	}
	                }
	            } finally {
	            	cur.close();
	            }
	        }
		return info;
	}
	/**
	 * 获取帐号
	 * @param context
	 * @return
	 */
	public static AccountInfo getAccount(Context context,String user_name){
	    	AccountInfo info = null;
	    	if(!Util.isStringEmpty(user_name)) {
		        Cursor cur = DBApi.queryAccountByUserAccount(context,user_name,null);
		        if (cur != null) {
		            try {
		                if (cur.moveToFirst()) {
		                	info = new AccountInfo();
		                	info.mUser_account = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_ACCOUNT));
		                	info.mUser_pwd = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_PASSWORD));
		                	info.mUser_nikename = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_NIKENAME));
		                	info.mUser_cookie = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_COOKIE));
		                	info.mIs_login = cur.getInt(cur.getColumnIndex(UserAccountHelper.IS_LOGIN))==1?true:false;
		                	String pwEnc = PDEEngine.PDecrypt(context, info.mUser_pwd);
		                	if(!Util.isStringEmpty(pwEnc)) {
		                		info.mUser_pwd = pwEnc;
		                	}
		                }
		            } finally {
		            	cur.close();
		            }
		        }
	    	}
		return info;
	}
	/**
	 * 设置当前登录帐号
	 * @param context
	 * @return
	 */
	public static void setLoginAccount(Context context,AccountInfo info){
		if(null == info) {
			return;
		}
		AccountInfo oldInfo = null;
	    if(info.mUser_account != null) {
	    	oldInfo = getAccount(context,info.mUser_account);
	    }
	    DBApi.cleartLoginAccount(context);
	    String pwEnc = PDEEngine.PEncrypt(context, info.mUser_pwd);
    	if(!Util.isStringEmpty(pwEnc)) {
    		info.mUser_pwd = pwEnc;
    	}
	    if(null != oldInfo) {
	    	DBApi.updateAccountByUserAccount(context, info);
	    } else{
	    	DBApi.insertAccount(context, info);
	    }
		return ;
	}
	/**
	 * 获取登陆过的所有账号
     * @param context
	 * @return
	 */
	public static List<AccountInfo> getAllUsers(Context context) {
		List<AccountInfo> list = new ArrayList<AccountInfo>();
		Cursor cur = DBApi.queryAllAccount(context, "_id",null);
		if(cur != null){
            for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()){
            	AccountInfo info = new AccountInfo();
            	info.mUser_account = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_ACCOUNT));
            	info.mUser_pwd = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_PASSWORD));
            	info.mUser_nikename = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_NIKENAME));
            	info.mUser_cookie = cur.getString(cur.getColumnIndex(UserAccountHelper.USER_COOKIE));
            	info.mIs_login = cur.getInt(cur.getColumnIndex(UserAccountHelper.IS_LOGIN))==1?true:false;
            	String pwEnc = PDEEngine.PDecrypt(context, info.mUser_pwd);
            	if(!Util.isStringEmpty(pwEnc)) {
            		info.mUser_pwd = pwEnc;
            	}
                list.add(info);
            }
            cur.close();
	     }
		
		return list;
	}
	/**
     * 注销登录
     * @param context
     * @param resolver
     */
	public static void logout(Context context) {
		DBApi.cleartLoginAccount(context);
	}
	
    /**
     * @param context
     * @param courseId 课程ID
     * @param videoIndex 视频 索引,第几课
     * @param videoCount 视频 总集数
     * @param playPosition 观看进度,存储大为:毫秒
     * @param videoLength 视频总长度,存储单位:秒
     */
/*	public static void insertOrUpdateVideoPlayRecord(Context context, String courseId,int videoIndex, int videoCount,int playPosition,int videoLength){
	    if(courseId == null || videoIndex <= 0|| videoIndex > videoCount){
	        PalLog.e("DBUtil", "insertOrUpdateVideoPlayRecord error!");
	        return;
	    }
	    VideoPlayInfo info = new VideoPlayInfo();
	    info.mCourse_id = courseId;
	    info.mPlay_position = playPosition;
	    info.mVideo_count = videoCount;
	    info.mVideo_index = videoIndex;
	    info.mVideo_length = videoLength;
	    Cursor c = DBApi.queryVideoPlayByCourseIDAndVideoID(context, new String[]{BaseColumns._ID},courseId, videoIndex);
	    boolean isExist;
	    if(c != null){
	        if(c.moveToFirst()){
	          //存在
	          isExist = true;
	        }else{
	          //不存在
	          isExist = false;
	        }
	        c.close();
	    }else{
	      //不存在
	      isExist = false;
	    }
	    if(isExist){
	        //更新
	        DBApi.updateVideoPlayInfo(context, info);
	    }else{
	        //新增
	        DBApi.insertVideoPlayInfo(context, info);
	    }
	        
	}*/
	private static DownLoadInfo getDownloadInfoNotStart(Context context, String id){
		DownLoadInfo info = new DownLoadInfo();
		Cursor cur = DBApi.queryDownloadNoStartById(context, id,null);
		
		if(cur != null){
            for(cur.moveToFirst(); !cur.isAfterLast(); ){
            	info.m_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.Download_ID));
            	info.mCourse_id = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_PLID));
				info.mCourse_name =  cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_NAME));
				info.mCourse_pnumber = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.COURSE_PNUMBER));
				info.mCourse_thumbnail = cur.getString(cur.getColumnIndex(DownloadManagerHelper.COURSE_THUMBNAIL));
				info.mDownload_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_SIZE));
				int status = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_STATUS));
				info.mDownload_status =  intToEDownloadStatus(status);
				info.mDownload_url = cur.getString(cur.getColumnIndex(DownloadManagerHelper.DOWNLOAD_URL));
				info.mIsSelected = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.SELECT_STATUS))==1 ? true:false;
				info.mTotal_size = cur.getInt(cur.getColumnIndex(DownloadManagerHelper.TOTAL_SIZE));
				
				break;
            }
            cur.close();
        }else{
        	return null;
        }
		
		return info;
	}
	/**
	 * 设置对应id项选择状态
	 * @param context
	 * @param id
	 * @param bSelected
	 */
	public static void setDownloadSelectType(Context context, int id, boolean bSelected){
		
		DownLoadInfo info = getDownloadInfoNotStart(context, String.valueOf(id));
		
		if(bSelected){
			if(info.mIsSelected == false){//更新未选择的状态为选择状态
				DBApi.updateDownloadSelectById(context, String.valueOf(id), bSelected);
			}
		}else{
			if(info.mIsSelected == true){//更新选择的状态为未选择状态
				DBApi.updateDownloadSelectById(context, String.valueOf(id), bSelected);
			}
		}
	}
	
	public static void deleteDownloadVideo(Context context, List<String> deleteList,boolean syncDb){
	    if(null != deleteList && deleteList.size()>0){
	        for(int i = 0; i < deleteList.size(); i++){
	            String[] selectedFileString = deleteList.get(i).split("_");//"courseId_videoId"
	            String courseId = selectedFileString[0];
	            int videoId = Integer.valueOf(selectedFileString[1]);
//	            Log.d("okry", "courseId:" + courseId + "|videoId:" + videoId);
	            if(syncDb){//pad版db处理不同，已在ui处理
	            	DBApi.deleteDownloadCourseIDAndPnumber(context, courseId, videoId);
	            }
				try {
					//同时删除文件
					String path = FileUtils.getSavedDownloadVideoPath(context,courseId, videoId, true);
					File f = new File(path);
	                FileUtils.deleteFile(f);
	                f = new File(FileUtils.getSavedDownloadVideoPath(context,courseId, videoId, false));
	                FileUtils.delete(f);
	                //同时删除字幕文件 add by echo_chen 2012-11-22
	            	FileUtils.deleteSubFile(context,courseId, videoId);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                DownloadPrefHelper.removeThreadInfo(context, courseId, Integer.valueOf(selectedFileString[1]));
	        }
	    }
	}

}
