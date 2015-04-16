package vopen.db;

import android.net.Uri;

import com.netease.vopen.pal.Constants;

public class VopenContentProvider extends BaseContentProvider {

    public static final int DATABASE_VERSION = Constants.DATABASE_VERSION;

    public static final String DATABASE_NAME = "netease_vopen.db";

    public static final String AUTHORITY = Constants.DATABASE_AUTHORITY;

    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    /**公开课所有数据JSON表*/
    public static final String TABLE_All_DTTA = "t_vopen_all_data";
    
    /**公开课单个课程*/
    public static final String TABLE_COURSE_DATA = "t_vopen_detail_data";

    /**公开课已经登陆我的收藏*/
    public static final String TABLE_MY_COLLECT = "t_vopen_my_collect";
    
    /**公开课未登陆我的收藏*/
    public static final String TABLE_NOLOGIN_MY_COLLECT = "t_vopen_nologin_my_collect";

    /**用户帐号表*/
    public static final String TABLE_USER_ACCOUNT = "t_vopen_user_account";

    /**课程播放记录表*/
    public static final String TABLE_COURSE_PLAY_RECORD = "t_course_play_record";

    /**视频播放记录*/
    public static final String TABLE_VIDEO_PLAY_RECORD = "t_video_play_record";
    
    /**下载管理表*/
    public static final String TABLE_DOWNLOAD_MANAGER = "t_vopen_download_manager";
    
/*    *//**下载管理的Plid视图*//*
    public static final String VIEW_DOWNLOAD_PLID = "v_vopen_download_plid";
    */
    @Override
    public boolean onCreate() {
        setSQLiteOpenHelper(new VopenDatabaseHelper(getContext()));
        return true;
    }
    
    /**公开课所有数据JSON表helper类*/
    public static class VopenAllDataJsonHelper {
    	/**===================数据库字段==================*/
    	/**课程id*/
    	public static final String COURSE_ID = "course_id";
    	/**课程名称*/
    	public static final String COURSE_NAME = "course_name";
    	/**课程tag*/
    	public static final String COURSE_TAG = "course_tag";
    	/**课程来源*/
    	public static final String COURSE_SOURCE = "course_src";
    	/**课程热度*/
    	public static final String COURSE_HIT_COUNT = "course_hit_count";
    	/**课程更新时间*/
    	public static final String COURSE_UPDATE_TIME = "course_update_time";
    	/**课程的JSON数据*/
    	public static final String COURSE_CONTENT = "course_content";
    	/**type*/
    	public static final String COURSE_TYPE = "update_type";
    	/**===================数据库字段==================*/
   	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
                    + TABLE_All_DTTA);
        }
    }
    
    /**公开课课程详细数据JSON表helper类*/
    public static class VopenDetailHelper {
    	/**===================数据库字段==================*/
    	/**Content*/
    	public static final String COURSE_CONTENT = "course_content";
    	/**ID*/
    	public static final String COURSE_PLID = "course_plid";
    	/**===================数据库字段==================*/
    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
                    + TABLE_COURSE_DATA);
        }
    }
    
    /**公开课已经登陆我的收藏helper类*/
    public static class VopenMyCollectHelper {
    	/**===================数据库字段==================*/
    	/**Content*/
//    	public static final String COURSE_CONTENT = "course_content";//未用到
    	/**ID*/
    	public static final String COURSE_PLID = "course_plid";
    	/**Title*/
    	public static final String COURSE_TITLE = "course_title";
    	/**img*/
    	public static final String COURSE_IMG = "course_img";
    	/**play count*/
    	public static final String COURSE_PLAYCOUNT = "course_playcount";
    	/**translate count*/
    	public static final String COURSE_TRANSLATECOUNT = "course_translatecount";
    	/**新翻译集数*/
    	public static final String COURSE_NEW_TRANSLATE_NUM = "course_new_translate_num";
    	/**User ID*/
    	public static final String USER_ID = "user_id";
    	/**is synchronized Integer 类型 已经同步为1 未同步为0*/
    	public static final String IS_SYNC = "is_synchronized";
    	/**收藏时间*/
    	public static final String DATA_TIME = "data_time";
    	/**===================数据库字段==================*/
    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
                    + TABLE_MY_COLLECT);
        }
    }
    
    /**公开课未登陆我的收藏helper类
     * 与已登录的表字段名称共用，少了user_id，is_synchronized两个字段
     **/
    public static class VopenNoLoginMyCollectHelper {    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
                    + TABLE_NOLOGIN_MY_COLLECT);
        }
    }
    
    /**用户帐号表*/
    public static class UserAccountHelper {
    	/**===================数据库字段==================*/
    	/**帐号*/
    	public static final String USER_ACCOUNT = "user_account";
    	/**密码*/
    	public static final String USER_PASSWORD = "user_pwd";
    	/**昵称*/
    	public static final String USER_NIKENAME = "user_nikename";
    	/**是否登录 1为登录，其他为未登录*/
    	public static final String IS_LOGIN = "is_login";
    	/**cookie*/
    	public static final String USER_COOKIE = "user_cookie";
    	
    	/**===================数据库字段==================*/
    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
            		+ TABLE_USER_ACCOUNT);
        }
    }
    
    /**课程播放表*/
    public static class VopenCoursePlayRecordHeleper {
    	/**===================数据库字段==================*/
    	/**海报路径*/
    	public static final String IMG_PATH = "img_path";
    	/**标题*/
    	public static final String TITLE = "c_title";
    	/**课程ID*/
    	public static final String COURSE_ID = "course_id";
    	/**视频  索引*/
    	public static final String VIDEO_INDEX = "void_index";
    	/**视频 总集数*/
    	public static final String VIDEO_COUNTS = "void_count";
    	/**观看进度*/
    	public static final String PLAY_POSITION = "play_position";
    	/**视频总长度*/
    	public static final String VIDEO_LENGTH = "video_length";
    	/**观看时间（2014-5-22添加）*/
    	public static final String PLAY_DATE = "play_date";
    	
    	/**===================数据库字段==================*/
    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
            		+ TABLE_COURSE_PLAY_RECORD);
        }
    }
    
    /**视频播放表
    * 与课程播放表字段名称共用，少了img_path，c_title两个字段
    */
    public static class VopenVideoPlayRecordHeleper {
    	    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
            		+ TABLE_VIDEO_PLAY_RECORD);
        }
    }
    
    /**下载管理表*/
    public static class DownloadManagerHelper {
    	/**===================数据库字段==================*/
    	/**数据库中id*/
    	public static final String Download_ID = "_id";
    	/**课程plid*/
    	public static final String COURSE_PLID = "course_plid";
    	/**课程第几集*/
    	public static final String COURSE_PNUMBER = "course_pnumber";
    	/**课程下载链接*/
    	public static final String DOWNLOAD_URL = "download_url";   	
    	/**课程名字*/
    	public static final String COURSE_NAME = "course_name";
    	/**课程截图地址*/
    	public static final String COURSE_THUMBNAIL = "course_thumbnail";
    	/**课程下载状态 :  下载状态 4下载失败，2等待, 3下载，6 暂停， 1下载完成, 5未下载*/
    	public static final String DOWNLOAD_STATUS = "download_status";
    	/**课程总大小*/
    	public static final String TOTAL_SIZE = "total_size";
    	/**课程已下载大小*/
    	public static final String DOWNLOAD_SIZE = "download_size";
    	/**课程是否被选中: 未选中0，被选中1 */
    	public static final String SELECT_STATUS = "select_status";
    	/**下载任务的优先级，优先级大的先下载！（2014-5-22添加）*/
    	public static final String PRIORITY = "download_priority";
    	
    	/**===================数据库字段==================*/
    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
            		+ TABLE_DOWNLOAD_MANAGER);
        }
    }
    
	public static final String[] PROJECTION = { "_id",
		DownloadManagerHelper.COURSE_PLID,
		DownloadManagerHelper.COURSE_PNUMBER,
		DownloadManagerHelper.DOWNLOAD_STATUS,
		DownloadManagerHelper.COURSE_NAME,
		DownloadManagerHelper.COURSE_THUMBNAIL,
		DownloadManagerHelper.DOWNLOAD_SIZE,
		DownloadManagerHelper.DOWNLOAD_URL,
		DownloadManagerHelper.TOTAL_SIZE,
		DownloadManagerHelper.SELECT_STATUS,
		DownloadManagerHelper.PRIORITY
		};

    public static final int INDEX_ID = 0;
    public static final int INDEX_PLID = INDEX_ID + 1;
    public static final int INDEX_PNUMBER = INDEX_PLID + 1;
    public static final int INDEX_STATUS = INDEX_PNUMBER + 1;
    public static final int INDEX_NAME = INDEX_STATUS + 1;
    public static final int INDEX_IMG = INDEX_NAME + 1;
    public static final int INDEX_DOWN_SIZE = INDEX_IMG + 1;
    public static final int INDEX_URL = INDEX_DOWN_SIZE + 1;
    public static final int INDEX_TOTAL_SIZE = INDEX_URL + 1;
    public static final int INDEX_SELECT_STATUS = INDEX_TOTAL_SIZE + 1;
    public static final int INDEX_PRIORITY = INDEX_SELECT_STATUS + 1;
/*    
    *//**下载管理的Plid视图*//*
    public static class DownloadManagerPlidHelper {
    	*//**===================数据库字段==================*//*
    	*//**课程plid*//*
    	public static final String COURSE_PLID = "course_plid";
    	
    	*//**===================数据库字段==================*//*
    	
        public static Uri getUri() {
            return Uri.parse("content://" + VopenContentProvider.AUTHORITY + "/"
            		+ VIEW_DOWNLOAD_PLID);
        }
    }*/
}
