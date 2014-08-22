package vopen.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import vopen.db.DBApi.AccountInfo;
import vopen.db.DBApi.CollectInfo;
import vopen.db.DBApi.CoursePlayInfo;
import vopen.db.DBApi.DownLoadInfo;
import vopen.db.DBApi.EDownloadStatus;
import vopen.db.DBApi.VideoPlayInfo;
import vopen.db.ManagerWeiboAccount.WeiboAccountColumn;
import vopen.db.VopenContentProvider.DownloadManagerHelper;
import vopen.db.VopenContentProvider.UserAccountHelper;
import vopen.db.VopenContentProvider.VopenAllDataJsonHelper;
import vopen.db.VopenContentProvider.VopenCoursePlayRecordHeleper;
import vopen.db.VopenContentProvider.VopenDetailHelper;
import vopen.db.VopenContentProvider.VopenMyCollectHelper;
import vopen.download.DownloadPrefHelper;
import vopen.download.DownloadUtils;
import vopen.response.CourseInfo;
import vopen.response.VideoInfo;
import vopen.tools.FileUtils;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.netease.util.PDEEngine;
import com.netease.vopen.pal.Constants;
import common.pal.PalLog;
import common.util.StringUtil;
import common.util.Util;

public class VopenDatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "VideoDatabaseHelper";
	private static VopenDatabaseHelper sDbHelpInstance;
	private static Context mContext;

	synchronized public static VopenDatabaseHelper getInstance(Context ct) {
		if (sDbHelpInstance == null) {
			sDbHelpInstance = new VopenDatabaseHelper(ct);
		}

		return sDbHelpInstance;
	}

	public VopenDatabaseHelper(Context context) {
		super(context, VopenContentProvider.DATABASE_NAME, null,
				VopenContentProvider.DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		PalLog.d(TAG, "creating new video database");

		//        clear(db);

		//所有数据的json表
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_All_DTTA + " ("
				+ "_id INTEGER PRIMARY KEY,"
				+ VopenAllDataJsonHelper.COURSE_CONTENT + " TEXT,"
				+ VopenAllDataJsonHelper.COURSE_TYPE + " TEXT" + ");");

		//课程详细数据表
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_COURSE_DATA + " ("
				+ "_id INTEGER PRIMARY KEY," + VopenDetailHelper.COURSE_PLID
				+ " TEXT," + VopenDetailHelper.COURSE_CONTENT + " TEXT" + ");");

		//我的已经登陆收藏数据表
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_MY_COLLECT + " ("
				+ "_id INTEGER PRIMARY KEY," + VopenMyCollectHelper.COURSE_PLID
				+ " TEXT," + VopenMyCollectHelper.USER_ID + " TEXT,"
				+ VopenMyCollectHelper.COURSE_IMG + " TEXT,"
				+ VopenMyCollectHelper.COURSE_TITLE + " TEXT,"
				+ VopenMyCollectHelper.COURSE_PLAYCOUNT + " INTEGER,"
				+ VopenMyCollectHelper.COURSE_TRANSLATECOUNT + " INTEGER,"
				+ VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM + " INTEGER,"
				+ VopenMyCollectHelper.IS_SYNC + " INTEGER,"
				+ VopenMyCollectHelper.DATA_TIME + " TEXT" +
				//                VopenMyCollectHelper.COURSE_CONTENT + " TEXT" +
				");");

		//我的未登陆收藏数据表
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_NOLOGIN_MY_COLLECT + " ("
				+ "_id INTEGER PRIMARY KEY," + VopenMyCollectHelper.COURSE_PLID
				+ " TEXT," + VopenMyCollectHelper.COURSE_IMG + " TEXT,"
				+ VopenMyCollectHelper.COURSE_TITLE + " TEXT,"
				+ VopenMyCollectHelper.COURSE_PLAYCOUNT + " INTEGER,"
				+ VopenMyCollectHelper.COURSE_TRANSLATECOUNT + " INTEGER,"
				+ VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM + " INTEGER,"
				+ VopenMyCollectHelper.DATA_TIME + " TEXT" +
				//                VopenNoLoginMyCollectHelper.COURSE_CONTENT + " TEXT" +
				");");

		// 用户账号表
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_USER_ACCOUNT + " ("
				+ "_id INTEGER PRIMARY KEY," + UserAccountHelper.USER_ACCOUNT
				+ " TEXT," + UserAccountHelper.USER_PASSWORD + " TEXT,"
				+ UserAccountHelper.USER_NIKENAME + " TEXT,"
				+ UserAccountHelper.IS_LOGIN + " INTEGER,"
				+ UserAccountHelper.USER_COOKIE + " TEXT" + ");");

		// 课程播放表
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_COURSE_PLAY_RECORD + " ("
				+ "_id INTEGER PRIMARY KEY,"
				+ VopenCoursePlayRecordHeleper.COURSE_ID + " TEXT,"
				+ VopenCoursePlayRecordHeleper.IMG_PATH + " TEXT,"
				+ VopenCoursePlayRecordHeleper.TITLE + " TEXT,"
				+ VopenCoursePlayRecordHeleper.VIDEO_INDEX + " INTEGER,"
				+ VopenCoursePlayRecordHeleper.VIDEO_COUNTS + " INTEGER,"
				+ VopenCoursePlayRecordHeleper.PLAY_POSITION + " INTEGER,"
				+ VopenCoursePlayRecordHeleper.VIDEO_LENGTH + " INTEGER,"
				/*2014-5-22 添加播放时间戳*/
				+ VopenCoursePlayRecordHeleper.PLAY_DATE + " LONG);");

		// 视频播放
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_VIDEO_PLAY_RECORD + " ("
				+ "_id INTEGER PRIMARY KEY,"
				+ VopenCoursePlayRecordHeleper.COURSE_ID + " TEXT,"
				+ VopenCoursePlayRecordHeleper.VIDEO_INDEX + " INTEGER,"
				+ VopenCoursePlayRecordHeleper.VIDEO_LENGTH + " INTEGER,"
				+ VopenCoursePlayRecordHeleper.VIDEO_COUNTS + " INTEGER,"
				+ VopenCoursePlayRecordHeleper.PLAY_POSITION + " INTEGER);");

		// 下载管理表
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ VopenContentProvider.TABLE_DOWNLOAD_MANAGER + " ("
				+ "_id INTEGER PRIMARY KEY,"
				+ DownloadManagerHelper.COURSE_PLID + " TEXT,"
				+ DownloadManagerHelper.COURSE_PNUMBER + " INTEGER,"
				+ DownloadManagerHelper.COURSE_NAME + " TEXT,"
				+ DownloadManagerHelper.COURSE_THUMBNAIL + " TEXT,"
				+ DownloadManagerHelper.DOWNLOAD_URL + " TEXT,"
				+ DownloadManagerHelper.DOWNLOAD_STATUS + " INTEGER,"
				+ DownloadManagerHelper.TOTAL_SIZE + " INTEGER,"
				+ DownloadManagerHelper.DOWNLOAD_SIZE + " INTEGER,"
				+ DownloadManagerHelper.SELECT_STATUS + " INTEGER," +
				/*2014-5-22 添加下载优先级字段*/
				DownloadManagerHelper.PRIORITY + " INTEGER);");

		/*      //创建视图
		        db.execSQL("CREATE VIEW "+VopenContentProvider.VIEW_DOWNLOAD_PLID+
		          " AS SELECT "+VopenContentProvider.TABLE_DOWNLOAD_MANAGER+"."+"_id"+" AS _id,"+
		          " "+VopenContentProvider.TABLE_DOWNLOAD_MANAGER+"."+DownloadManagerHelper.COURSE_PLID+""+
		          " FROM "+VopenContentProvider.TABLE_DOWNLOAD_MANAGER + " WHERE "+
		          DownloadManagerHelper.DOWNLOAD_STATUS+"<>"+
		          DownloadGridItemInfo.STATUS_DOWNLOAD_NOT_START+" GROUP BY "+
		          DownloadManagerHelper.COURSE_PLID
		          );*/
		//微博帐号表       手机版v2.0.2 增加    
		createTableWeiboAccount(db);
	}

	//微博帐号表
	void createTableWeiboAccount(SQLiteDatabase db) {
		String sqlSep = " CREATE TABLE IF NOT EXISTS "
				+ WeiboAccountColumn.TABLE_NAME + " ( "
				+ WeiboAccountColumn._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ WeiboAccountColumn.C_ACCOUNT + " TEXT NOT NULL, "
				+ WeiboAccountColumn.C_NAME + " TEXT NOT NULL, "

				+ WeiboAccountColumn.C_LAST_LOGIN + " INTEGER, "

				+ WeiboAccountColumn.C_TYPE_WB + " INTEGER, "
				+ WeiboAccountColumn.C_TOKEN + " TEXT, "
				+ WeiboAccountColumn.C_SECRET + " TEXT, "
				+ WeiboAccountColumn.C_MAINURL + " TEXT, "
				+ WeiboAccountColumn.C_PORTRAIT + " TEXT, "
				+ WeiboAccountColumn.C_OTHER + " TEXT,"

				+ " UNIQUE (" + WeiboAccountColumn.C_NAME + " , "
				+ WeiboAccountColumn.C_ACCOUNT + " , "
				+ WeiboAccountColumn.C_TYPE_WB + ") ON CONFLICT REPLACE"
				+ " ) ";

		db.execSQL(sqlSep);
	}

	private void clear(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_All_DTTA);
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_COURSE_DATA);
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_MY_COLLECT);
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_NOLOGIN_MY_COLLECT);
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_USER_ACCOUNT);
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_COURSE_PLAY_RECORD);
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_VIDEO_PLAY_RECORD);
		db.execSQL("DROP TABLE IF EXISTS "
				+ VopenContentProvider.TABLE_DOWNLOAD_MANAGER);
		db.execSQL("DROP TABLE IF EXISTS " + WeiboAccountColumn.TABLE_NAME);
	}

	private void clearOld(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + "c_course_data");
		db.execSQL("DROP TABLE IF EXISTS " + "tfavority");
		db.execSQL("DROP TABLE IF EXISTS " + "tnetfavority");
		db.execSQL("DROP TABLE IF EXISTS " + "tcourselookrecord");
		db.execSQL("DROP TABLE IF EXISTS " + "tcourseplayrecord");
		db.execSQL("DROP TABLE IF EXISTS " + "course");
		db.execSQL("DROP TABLE IF EXISTS " + "download");
		db.execSQL("DROP TABLE IF EXISTS " + "download_log");
	}

	/**
	 * 把所有帐号的密码加密码
	 * @param db
	 * @return
	 */
	private void encAllPw(SQLiteDatabase db) {

		ContentValues values = new ContentValues();

		// 更新原有account表
		Cursor cursor = db.query(VopenContentProvider.TABLE_USER_ACCOUNT,
				new String[] { "_id", UserAccountHelper.USER_PASSWORD }, null,
				null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String oriPw = cursor.getString(1);
					if (!Util.isStringEmpty(oriPw)) {
						String encPw = PDEEngine.PEncrypt(mContext, oriPw);
						values = new ContentValues();
						values.put(UserAccountHelper.USER_PASSWORD, encPw);
						PalLog.e(TAG, " encAllPw() oriPw = " + oriPw
								+ " encpw = " + encPw);
						db.update(VopenContentProvider.TABLE_USER_ACCOUNT,
								values, "_id =" + cursor.getInt(0), null);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
		return;
	}

	/**
	 * 转换数据库下载表为新版格式
	 * @param db
	 * @return
	 */
	private void transDwonloadTable(SQLiteDatabase db) {
		// 更新原有account表
		PalLog.v(TAG, "transDwonloadTable ");
		long timeStart = System.currentTimeMillis();
		Cursor cursor = db
				.query("download", null, null, null, null, null, null);
		ArrayList<DownLoadInfo> downloadList = new ArrayList<DownLoadInfo>();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					DownLoadInfo dInfo = new DownLoadInfo();
					dInfo.mCourse_id = cursor.getString(cursor
							.getColumnIndex("ccourserid"));
					String pnumber = cursor.getString(cursor
							.getColumnIndex("csectionid"));
					dInfo.mCourse_pnumber = Util.isStringEmpty(pnumber) ? 1
							: Integer.valueOf(pnumber);
					dInfo.mCourse_name = cursor.getString(cursor
							.getColumnIndex("c_name"));
					dInfo.mDownload_url = cursor.getString(cursor
							.getColumnIndex("c_path"));
					dInfo.mDownload_status = toEDownloadStatus(cursor
							.getString(cursor.getColumnIndex("d_status")));
					dInfo.mTotal_size = cursor.getInt(cursor
							.getColumnIndex("c_all_size"));
					if (dInfo.mDownload_status.value() == EDownloadStatus.DOWNLOAD_DONE
							.value()) {
						dInfo.mDownload_size = dInfo.mTotal_size;
					} else {
						dInfo.mDownload_size = cursor.getInt(cursor
								.getColumnIndex("c_d_length"));
					}
					downloadList.add(dInfo);
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}

		try {
			db.beginTransaction();
			DownLoadInfo info = null;
			for (int i = 0; i < downloadList.size(); i++) {
				info = downloadList.get(i);
				if (info == null) {
					db.endTransaction();
					PalLog.e(TAG, "transDwonloadTable info == null");
					return;
				} else {
					if (insertDownLoadStatement(db, info) == false) {
						// 出错结束事务
						db.endTransaction();
						PalLog.e(TAG, "transDwonloadTable info == null");
						return;
					}
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		long timeEnd = System.currentTimeMillis();
		PalLog.e(TAG, "transDwonloadTable downloadList.size() is "
				+ downloadList.size() + " time is " + (timeEnd - timeStart));
		if (downloadList.size() > 0) {
			List<DownLoadInfo> downloadedList = new ArrayList<DownLoadInfo>();
			List<DownLoadInfo> downloadingList = new ArrayList<DownLoadInfo>();
			for (DownLoadInfo dInfo : downloadList) {
				if (dInfo.mDownload_status.value() == EDownloadStatus.DOWNLOAD_DONE
						.value()) {
					downloadedList.add(dInfo);
				} else {
					downloadingList.add(dInfo);
				}
			}
			String oldDownloadPath = Environment.getExternalStorageDirectory()
					+ "/netease/vopen/download/";
			String newDwonloadPath = FileUtils
					.getSavingDownloadFileDir(mContext);
			File fileSaveDir = new File(newDwonloadPath);
			if (!fileSaveDir.exists()) {
				fileSaveDir.mkdirs();
			}
			DownLoadInfo info = null;

			for (DownLoadInfo downloaded : downloadedList) {
				info = downloaded;
				File file = new File(oldDownloadPath + info.mCourse_id + "/"
						+ info.mCourse_pnumber
						+ DownloadUtils.FILE_COMPLETED_POSTFIX);
				if (file.exists()) {
					//转移文件至新的目录及新的命名方式
					long time1 = System.currentTimeMillis();
					boolean filerename = file.renameTo(new File(newDwonloadPath
							+ info.mCourse_id + "_" + info.mCourse_pnumber
							+ DownloadUtils.FILE_COMPLETED_POSTFIX));
					long time2 = System.currentTimeMillis();
					PalLog.e(TAG, "recover Downloaded: filerename = "
							+ filerename + " file is " + file.getPath()
							+ " time is " + (time2 - time1) + " size is "
							+ info.mDownload_size);
				}
			}
			String old_dwonload_preference = "com.netease.vopen.download.log";
			SharedPreferences pref = mContext.getSharedPreferences(
					old_dwonload_preference, Context.MODE_PRIVATE);
			for (DownLoadInfo downloading : downloadingList) {
				info = downloading;
				File file = new File(oldDownloadPath + info.mCourse_id + "/"
						+ info.mCourse_pnumber
						+ DownloadUtils.FILE_COMPLETED_POSTFIX);

				if (file.exists()) {
					//恢复xml中的断点线程记录
					long time1 = System.currentTimeMillis();
					long start = pref.getLong(info.mDownload_url + "_1", 0);
					DownloadPrefHelper.recordDownload(mContext,
							info.mCourse_id, info.mCourse_pnumber, 0,
							(int) start);
					//转移文件至新的目录及新的命名方式  
					boolean filerename = file.renameTo(new File(newDwonloadPath
							+ info.mCourse_id + "_" + info.mCourse_pnumber
							+ DownloadUtils.FILE_NOT_COMPLETED_POSTFIX));
					long time2 = System.currentTimeMillis();
					PalLog.e(TAG, "recover Downloading: filerename = "
							+ filerename + " file is " + file.getPath()
							+ " time is " + (time2 - time1) + " size is "
							+ info.mDownload_size);
				}
			}
			//删除旧版的文件和文件夹
			File prefFile = new File("/data/data/" + mContext.getPackageName()
					+ "/shared_prefs/" + old_dwonload_preference + ".xml");
			if (prefFile.exists()) {
				prefFile.delete();
			}
			File fileSaveDirOld = new File(oldDownloadPath);
			if (fileSaveDirOld.exists()) {
				File[] files = fileSaveDirOld.listFiles();
				for (File f : files) {
					if (f.isDirectory()) {
						File[] filesChild = f.listFiles();
						for (File fc : filesChild) {
							if (fc.isFile()) {
								fc.delete();
							}
						}
						f.delete();
					} else {
						f.delete();
					}
				}
				fileSaveDirOld.delete();
			}
		}
		long timeEnd2 = System.currentTimeMillis();
		PalLog.e(TAG, "transDwonloadTable&files downloadList.size() is "
				+ downloadList.size() + " time is " + (timeEnd2 - timeStart));
		return;
	}

	private static boolean insertDownLoadStatement(SQLiteDatabase db,
			DownLoadInfo info) {
		if (db == null || info == null) {
			return false;
		}

		StringBuilder sql = new StringBuilder(0);
		sql.append(" INSERT OR ROLLBACK INTO ");

		sql.append(VopenContentProvider.TABLE_DOWNLOAD_MANAGER);
		sql.append("(");
		sql.append(DownloadManagerHelper.COURSE_PLID);//1
		sql.append(",");
		sql.append(DownloadManagerHelper.COURSE_PNUMBER);//2
		sql.append(",");
		sql.append(DownloadManagerHelper.COURSE_NAME);//3
		sql.append(",");
		sql.append(DownloadManagerHelper.COURSE_THUMBNAIL);//4
		sql.append(",");
		sql.append(DownloadManagerHelper.DOWNLOAD_URL);//5
		sql.append(",");
		sql.append(DownloadManagerHelper.DOWNLOAD_STATUS);//6
		sql.append(",");
		sql.append(DownloadManagerHelper.TOTAL_SIZE);//7
		sql.append(",");
		sql.append(DownloadManagerHelper.DOWNLOAD_SIZE);//8
		sql.append(",");
		sql.append(DownloadManagerHelper.SELECT_STATUS);//9
		sql.append(")");
		sql.append(" VALUES(");
		sql.append("?,?,?,?,?,?,?,?,?");
		sql.append(");");

		SQLiteStatement statement = null;
		try {
			statement = db.compileStatement(sql.toString());
			statement.bindString(1, null != info.mCourse_id ? info.mCourse_id
					: "");
			statement.bindLong(2, info.mCourse_pnumber);
			statement.bindString(3,
					null != info.mCourse_name ? info.mCourse_name : "");
			statement.bindString(4,
					null != info.mCourse_thumbnail ? info.mCourse_thumbnail
							: "");
			statement.bindString(5,
					null != info.mDownload_url ? info.mDownload_url : "");
			statement.bindLong(6, info.mDownload_status.value());
			statement.bindLong(7, info.mTotal_size);
			statement.bindLong(8, info.mDownload_size);
			statement.bindLong(9, info.mIsSelected ? 1 : 0);
			long insertedRowId = statement.executeInsert();

			if (insertedRowId < 0) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		return true;
	}

	public static EDownloadStatus toEDownloadStatus(String s) {
		/** v2.0.1&v2.0.0 数据 String下载状态 -1下载失败，0等待, 1下载，2 暂停， 3下载完成*/
		EDownloadStatus status = EDownloadStatus.DOWNLOAD_NO;
		if (s.equals("-1")) {
			status = EDownloadStatus.DOWNLOAD_FAILED;
		} else if (s.equals("0")) {
			status = EDownloadStatus.DOWNLOAD_WAITTING;
		} else if (s.equals("1")) {
			status = EDownloadStatus.DOWNLOAD_DOING;
		} else if (s.equals("2")) {
			status = EDownloadStatus.DOWNLOAD_PAUSE;
		} else if (s.equals("3")) {
			status = EDownloadStatus.DOWNLOAD_DONE;
		} else if (s.equals("7")) {
			status = EDownloadStatus.DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME;
		} else if (s.equals("8")) {
			status = EDownloadStatus.DOWNLOAD_FAILED_VIDEO_ERROR;
		}
		return status;
	}

	/**
	 * 更新播放记录表(包括课程播放信息及视频播放信息)
	 * tcourselookrecord --> t_course_play_record
	 * tcourseplayrecord --> t_video_play_record
	 * 
	 * @param db
	 */
	private void transPlayRecord(SQLiteDatabase db) {
		PalLog.d("VoepnDatabaseHelper", "transPlayRecord");
		long timeStart = System.currentTimeMillis();
		String[] oldCoursePlay = {
				//旧表信息
				//				OldDbName.C_ISCHECKED,//废弃不用
				OldDbName.C_CourseJson, OldDbName.C_CourseId, };
		//读取旧表课程播放信息保存至courseInfoMap
		Cursor cousePlayCur = db.query(OldDbName.T_CourseLookRecord,
				oldCoursePlay, null, null, null, null, null);
		Map<String, CourseInfo> courseInfoMap = null;
		if (cousePlayCur == null) {
			//没有播放记录
			return;
		} else {
			courseInfoMap = new HashMap<String, CourseInfo>();
			for (cousePlayCur.moveToFirst(); !cousePlayCur.isAfterLast(); cousePlayCur
					.moveToNext()) {
				String courseId = cousePlayCur.getString(cousePlayCur
						.getColumnIndex(OldDbName.C_CourseId));
				String jsonobjstr = cousePlayCur.getString(cousePlayCur
						.getColumnIndex(OldDbName.C_CourseJson));
				CourseInfo courseInfo = new CourseInfo(jsonobjstr);
				courseInfoMap.put(courseId, courseInfo);
			}
			cousePlayCur.close();
		}

		String[] oldVideoPlayInfo = {
				//旧表信息
				//				OldDbName.C_ISCHECKED,//废弃不用
				OldDbName.C_Datatime, OldDbName.C_CourseId,
				OldDbName.C_SectionId, OldDbName.C_PlayPosition,
				OldDbName.C_playcount };
		//读取旧表视频播放信息
		Cursor videoPlayCur = db.query(OldDbName.T_CoursePlayRecord,
				oldVideoPlayInfo, null, null, null, null, null);
		if (videoPlayCur != null) {
			//构建VideoPlayList
			List<VideoPlayInfo> videoPlayInfoList = new ArrayList<VideoPlayInfo>();
			for (videoPlayCur.moveToFirst(); !videoPlayCur.isAfterLast(); videoPlayCur
					.moveToNext()) {
				VideoPlayInfo info = new VideoPlayInfo();
				//------新表信息------
				info.mCourse_id = videoPlayCur.getString(videoPlayCur
						.getColumnIndex(OldDbName.C_CourseId));
				info.mPlay_position = videoPlayCur.getInt(videoPlayCur
						.getColumnIndex(OldDbName.C_PlayPosition));
				info.mVideo_count = videoPlayCur.getInt(videoPlayCur
						.getColumnIndex(OldDbName.C_playcount));
				info.mVideo_index = videoPlayCur.getInt(videoPlayCur
						.getColumnIndex(OldDbName.C_SectionId));
				//读取length
				CourseInfo courseInfo = courseInfoMap.get(info.mCourse_id);
				if (null != courseInfo) {
					VideoInfo videoInfo = courseInfo.videoList
							.get(info.mVideo_index - 1);
					info.mVideo_length = (int) videoInfo.mlength * 1000;//unkonow
					//--------------------
					videoPlayInfoList.add(info);
				}
			}
			videoPlayCur.close();
			//构建CoursePlayList
			List<CoursePlayInfo> coursePlayInfoList = new ArrayList<CoursePlayInfo>();
			Map<String, Integer> indexMap = new HashMap<String, Integer>();
			//读取VideoPlayList中每课最后观看的视频
			for (int i = 0; i < videoPlayInfoList.size(); i++) {
				VideoPlayInfo videoInfo = videoPlayInfoList.get(i);
				indexMap.put(videoInfo.mCourse_id, i);
			}
			Collection<Integer> c = indexMap.values();
			Iterator<Integer> it = c.iterator();
			while (it.hasNext()) {
				int index = it.next();
				VideoPlayInfo videoPlayInfo = videoPlayInfoList.get(index);
				CoursePlayInfo coursePlayInfo = new CoursePlayInfo();
				coursePlayInfo.mCourse_id = videoPlayInfo.mCourse_id;
				CourseInfo courseInfo = courseInfoMap
						.get(coursePlayInfo.mCourse_id);
				coursePlayInfo.mImg_path = courseInfo.imgpath;
				coursePlayInfo.mPlay_position = videoPlayInfo.mPlay_position;
				coursePlayInfo.mTitle = courseInfo.title;
				coursePlayInfo.mVideo_count = courseInfo.playcount;
				coursePlayInfo.mVideo_index = videoPlayInfo.mVideo_index;
				coursePlayInfo.mVideo_length = videoPlayInfo.mVideo_length;
				coursePlayInfoList.add(coursePlayInfo);
			}
			//insert to table
			if (videoPlayInfoList.size() != 0) {
				doInsertVideoPlayInfoTrasation(db, videoPlayInfoList);
				doInsertCoursePlayInfoTrasation(db, coursePlayInfoList);
			}
			long timeEnd = System.currentTimeMillis();
			PalLog.e(TAG, "transPlayRecord videoPlayInfoList() is "
					+ videoPlayInfoList.size() + "coursePlayInfoList() is "
					+ coursePlayInfoList.size() + " time is "
					+ (timeEnd - timeStart));
		}

	}

	private void doInsertCoursePlayInfoTrasation(SQLiteDatabase db,
			List<CoursePlayInfo> coursePlayInfoList) {
		try {
			db.beginTransaction();
			CoursePlayInfo info = null;
			for (int i = 0; i < coursePlayInfoList.size(); i++) {
				info = coursePlayInfoList.get(i);
				if (info == null) {
					db.endTransaction();
					PalLog.e(TAG, "transDwonloadTable info == null");
					return;
				} else {
					if (insertCoursePlayInfo(db, info) == false) {
						// 出错结束事务
						db.endTransaction();
						PalLog.e(TAG, "transDwonloadTable info == null");
						return;
					}
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private void doInsertVideoPlayInfoTrasation(SQLiteDatabase db,
			List<VideoPlayInfo> videoPlayInfoList) {
		try {
			db.beginTransaction();
			VideoPlayInfo info = null;
			for (int i = 0; i < videoPlayInfoList.size(); i++) {
				info = videoPlayInfoList.get(i);
				if (info == null) {
					db.endTransaction();
					PalLog.e(TAG, "transDwonloadTable info == null");
					return;
				} else {
					if (insertVideoPlayInfo(db, info) == false) {
						// 出错结束事务
						db.endTransaction();
						PalLog.e(TAG, "transDwonloadTable info == null");
						return;
					}
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private boolean insertCoursePlayInfo(SQLiteDatabase db,
			CoursePlayInfo coursePlayInfo) {
		if (db == null || coursePlayInfo == null) {
			return false;
		}
		StringBuilder sql = new StringBuilder(0);
		sql.append(" INSERT OR ROLLBACK INTO ");
		sql.append(VopenContentProvider.TABLE_COURSE_PLAY_RECORD);
		sql.append("(");
		sql.append(VopenCoursePlayRecordHeleper.COURSE_ID);//1
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.PLAY_POSITION);//2
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.VIDEO_COUNTS);//3
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.VIDEO_INDEX);//4
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.VIDEO_LENGTH);//5
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.IMG_PATH);//6
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.TITLE);//7
		sql.append(")");
		sql.append(" VALUES(");
		sql.append("?,?,?,?,?,?,?");
		sql.append(");");

		SQLiteStatement statement = null;
		try {
			statement = db.compileStatement(sql.toString());
			statement.bindString(1, coursePlayInfo.mCourse_id);
			statement.bindLong(2, coursePlayInfo.mPlay_position);
			statement.bindLong(3, coursePlayInfo.mVideo_count);
			statement.bindLong(4, coursePlayInfo.mVideo_index);
			statement.bindLong(5, coursePlayInfo.mVideo_length);
			statement.bindString(6, coursePlayInfo.mImg_path);
			statement.bindString(7, coursePlayInfo.mTitle);
			long insertedRowId = statement.executeInsert();

			if (insertedRowId < 0) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		return true;
	}

	private boolean insertVideoPlayInfo(SQLiteDatabase db,
			VideoPlayInfo videoPlayInfo) {
		if (db == null || videoPlayInfo == null) {
			return false;
		}
		StringBuilder sql = new StringBuilder(0);
		sql.append(" INSERT OR ROLLBACK INTO ");
		sql.append(VopenContentProvider.TABLE_VIDEO_PLAY_RECORD);
		sql.append("(");
		sql.append(VopenCoursePlayRecordHeleper.COURSE_ID);//1
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.PLAY_POSITION);//2
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.VIDEO_COUNTS);//3
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.VIDEO_INDEX);//4
		sql.append(",");
		sql.append(VopenCoursePlayRecordHeleper.VIDEO_LENGTH);//5
		sql.append(")");
		sql.append(" VALUES(");
		sql.append("?,?,?,?,?");
		sql.append(");");

		SQLiteStatement statement = null;
		try {
			statement = db.compileStatement(sql.toString());
			statement.bindString(1, videoPlayInfo.mCourse_id);
			statement.bindLong(2, videoPlayInfo.mPlay_position);
			statement.bindLong(3, videoPlayInfo.mVideo_count);
			statement.bindLong(4, videoPlayInfo.mVideo_index);
			statement.bindLong(5, videoPlayInfo.mVideo_length);
			long insertedRowId = statement.executeInsert();

			if (insertedRowId < 0) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		return true;
	}

	/**
	 * 更新收藏记录表
	 * tafavority --> t_vopen_nologin_my_collect
	 * 
	 * @param db
	 */
	private void transNoLoginCollect(SQLiteDatabase db) {
		PalLog.d("VoepnDatabaseHelper", "transNoLoginCollect");
		long timeStart = System.currentTimeMillis();
		String[] oldCollect = {
				//旧表信息
				//				OldDbName.C_ISCHECKED,//废弃不用
				OldDbName.C_CourseJson, OldDbName.C_CourseId,
				OldDbName.C_Datatime,
		//				OldDbName.C_Translate_num,//旧表里存的全是0,V1.0.0未用该字段
		};
		//读取旧表课程播放信息保存至courseInfoMap
		Cursor collectCur = db.query(OldDbName.T_Favority, oldCollect, null,
				null, null, null, null);
		//		Map<String, CourseInfo> courseInfoMap = null;
		if (collectCur == null) {
			//没有收藏
			return;
		} else {
			List<CollectInfo> collectList = new ArrayList<CollectInfo>();
			for (collectCur.moveToFirst(); !collectCur.isAfterLast(); collectCur
					.moveToNext()) {
				//读取旧表信息
				String courseId = collectCur.getString(collectCur
						.getColumnIndex(OldDbName.C_CourseId));
				String jsonobjstr = collectCur.getString(collectCur
						.getColumnIndex(OldDbName.C_CourseJson));
				String date = collectCur.getString(collectCur
						.getColumnIndex(OldDbName.C_Datatime));
				CourseInfo courseInfo = new CourseInfo(jsonobjstr);
				//构建新表信息
				CollectInfo info = new CollectInfo();
				info.mCourse_id = courseId;
				info.mCourse_img = courseInfo.imgpath;
				info.mCourse_new_translate_num = 0;
				info.mCourse_playcount = courseInfo.playcount;
				info.mCourse_title = courseInfo.title;
				info.mCourse_translatecount = courseInfo.updated_playcount;//旧表里translate_num全部为0，这里从courseJson中读取
				info.mData_time = date;
				//				info.mIs_synchronized//未用
				//				info.mUser_id//未用
				collectList.add(info);
			}
			collectCur.close();
			//insert to table
			if (collectList.size() != 0) {
				doInsertNoLoginCollectInfoTrasation(db, collectList);
			}
			long timeEnd = System.currentTimeMillis();
			PalLog.e(TAG, "transNoLoginCollect collectList.size() is "
					+ collectList.size() + " time is " + (timeEnd - timeStart));
		}
	}

	private void doInsertNoLoginCollectInfoTrasation(SQLiteDatabase db,
			List<CollectInfo> collectInfoList) {
		try {
			db.beginTransaction();
			CollectInfo info = null;
			for (int i = 0; i < collectInfoList.size(); i++) {
				info = collectInfoList.get(i);
				if (info == null) {
					db.endTransaction();
					PalLog.e(TAG, "transDwonloadTable info == null");
					return;
				} else {
					if (insertNoLoginCollectInfo(db, info) == false) {
						// 出错结束事务
						db.endTransaction();
						PalLog.e(TAG, "transDwonloadTable info == null");
						return;
					}
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private boolean insertNoLoginCollectInfo(SQLiteDatabase db,
			CollectInfo collectInfo) {
		if (db == null || collectInfo == null) {
			return false;
		}
		StringBuilder sql = new StringBuilder(0);
		sql.append(" INSERT OR ROLLBACK INTO ");
		sql.append(VopenContentProvider.TABLE_NOLOGIN_MY_COLLECT);
		sql.append("(");
		sql.append(VopenMyCollectHelper.COURSE_PLID);//1
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_IMG);//2
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_TITLE);//3
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_PLAYCOUNT);//4
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_TRANSLATECOUNT);//5
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM);//6
		sql.append(",");
		sql.append(VopenMyCollectHelper.DATA_TIME);//7
		sql.append(")");
		sql.append(" VALUES(");
		sql.append("?,?,?,?,?,?,?");
		sql.append(");");

		SQLiteStatement statement = null;
		try {
			statement = db.compileStatement(sql.toString());
			statement.bindString(1, collectInfo.mCourse_id);
			statement.bindString(2, collectInfo.mCourse_img);
			statement.bindString(3, collectInfo.mCourse_title);
			statement.bindLong(4, collectInfo.mCourse_playcount);
			statement.bindLong(5, collectInfo.mCourse_translatecount);
			statement.bindLong(6, collectInfo.mCourse_new_translate_num);
			statement.bindString(7, collectInfo.mData_time);
			long insertedRowId = statement.executeInsert();
			if (insertedRowId < 0) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		return true;
	}

	/**
	 * 更新已登录收藏记录表
	 * tnetafavority --> t_vopen_my_collect
	 * 
	 * @param db
	 */
	private void transCollect(SQLiteDatabase db, String userId) {
		PalLog.d("VoepnDatabaseHelper", "transCollect");
		long timeStart = System.currentTimeMillis();
		String[] oldCollect = {
				//旧表信息
				//				OldDbName.C_ISCHECKED,//废弃不用
				OldDbName.C_CourseJson, OldDbName.C_CourseId,
				OldDbName.C_Datatime,
		//				OldDbName.C_Translate_num,//旧表里存的全是0,V1.0.0未用该字段
		};
		//读取旧表课程播放信息保存至courseInfoMap
		Cursor collectCur = db.query(OldDbName.T_Net_Favority, oldCollect,
				null, null, null, null, null);
		//		Map<String, CourseInfo> courseInfoMap = null;
		if (collectCur == null) {
			//没有收藏
			return;
		} else {
			List<CollectInfo> collectList = new ArrayList<CollectInfo>();
			for (collectCur.moveToFirst(); !collectCur.isAfterLast(); collectCur
					.moveToNext()) {
				//读取旧表信息
				String courseId = collectCur.getString(collectCur
						.getColumnIndex(OldDbName.C_CourseId));
				String jsonobjstr = collectCur.getString(collectCur
						.getColumnIndex(OldDbName.C_CourseJson));
				String date = collectCur.getString(collectCur
						.getColumnIndex(OldDbName.C_Datatime));
				CourseInfo courseInfo = new CourseInfo(jsonobjstr);
				//构建新表信息
				CollectInfo info = new CollectInfo();
				info.mCourse_id = courseId;
				info.mCourse_img = courseInfo.imgpath;
				info.mCourse_new_translate_num = 0;
				info.mCourse_playcount = courseInfo.playcount;
				info.mCourse_title = courseInfo.title;
				info.mCourse_translatecount = courseInfo.updated_playcount;//旧表里translate_num全部为0，这里从courseJson中读取
				info.mData_time = date;
				//				info.mIs_synchronized//未用
				info.mUser_id = userId;
				collectList.add(info);
			}
			collectCur.close();
			//insert to table
			if (collectList.size() != 0) {
				doInsertCollectInfoTrasation(db, collectList);
			}
			long timeEnd = System.currentTimeMillis();
			PalLog.e(TAG,
					"transCollect collectList.size() is " + collectList.size()
							+ " time is " + (timeEnd - timeStart));
		}
	}

	private void doInsertCollectInfoTrasation(SQLiteDatabase db,
			List<CollectInfo> collectInfoList) {
		try {
			db.beginTransaction();
			CollectInfo info = null;
			for (int i = 0; i < collectInfoList.size(); i++) {
				info = collectInfoList.get(i);
				if (info == null) {
					db.endTransaction();
					PalLog.e(TAG, "transDwonloadTable info == null");
					return;
				} else {
					if (insertCollectInfo(db, info) == false) {
						// 出错结束事务
						db.endTransaction();
						PalLog.e(TAG, "transDwonloadTable info == null");
						return;
					}
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private boolean insertCollectInfo(SQLiteDatabase db, CollectInfo collectInfo) {
		if (db == null || collectInfo == null) {
			return false;
		}
		StringBuilder sql = new StringBuilder(0);
		sql.append(" INSERT OR ROLLBACK INTO ");
		sql.append(VopenContentProvider.TABLE_MY_COLLECT);
		sql.append("(");
		sql.append(VopenMyCollectHelper.COURSE_PLID);//1
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_IMG);//2
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_TITLE);//3
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_PLAYCOUNT);//4
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_TRANSLATECOUNT);//5
		sql.append(",");
		sql.append(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM);//6
		sql.append(",");
		sql.append(VopenMyCollectHelper.DATA_TIME);//7
		sql.append(",");
		sql.append(VopenMyCollectHelper.USER_ID);//8
		sql.append(")");
		sql.append(" VALUES(");
		sql.append("?,?,?,?,?,?,?,?");
		sql.append(");");

		SQLiteStatement statement = null;
		try {
			statement = db.compileStatement(sql.toString());
			statement.bindString(1, collectInfo.mCourse_id);
			statement.bindString(2, collectInfo.mCourse_img);
			statement.bindString(3, collectInfo.mCourse_title);
			statement.bindLong(4, collectInfo.mCourse_playcount);
			statement.bindLong(5, collectInfo.mCourse_translatecount);
			statement.bindLong(6, collectInfo.mCourse_new_translate_num);
			statement.bindString(7, collectInfo.mData_time);
			statement.bindString(8, collectInfo.mUser_id);
			long insertedRowId = statement.executeInsert();
			if (insertedRowId < 0) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		return true;
	}

	/**
	 * 更新账户表
	 * default_pref.xml --> t_vopen_user_account
	 * 
	 * @param db
	 */
	private String transAccount(SQLiteDatabase db) {
		PalLog.d("VoepnDatabaseHelper", "transAccount");
		SharedPreferences default_pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String userAccount = default_pref.getString("account", "");
		String cookie = default_pref.getString("cookie", "");
		if (StringUtil.checkStr(userAccount) && StringUtil.checkStr(cookie)) {
			AccountInfo info = new AccountInfo();
			info.mIs_login = true;
			info.mUser_account = userAccount;
			info.mUser_cookie = cookie;
			//			info.mUser_nikename//无
			//			info.mUser_pwd//无
			doInsertAccountInfo(db, info);
		}
		return userAccount;
	}

	private void doInsertAccountInfo(SQLiteDatabase db, AccountInfo info) {
		ContentValues accountValues = new ContentValues();
		accountValues.put(UserAccountHelper.USER_ACCOUNT, info.mUser_account);
		accountValues.put(UserAccountHelper.USER_COOKIE, info.mUser_cookie);
		accountValues.put(UserAccountHelper.IS_LOGIN, info.mIs_login);
		db.insert(VopenContentProvider.TABLE_USER_ACCOUNT, null, accountValues);
	}

	private void addPlayDateToPlayRecord(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE "
				+ VopenContentProvider.TABLE_COURSE_PLAY_RECORD + " ADD COLUMN "
				+ VopenCoursePlayRecordHeleper.PLAY_DATE + " LONG DEFAULT 0");
	}

	private void addPrioirtyToDownloadManager(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE "
				+ VopenContentProvider.TABLE_DOWNLOAD_MANAGER + " ADD COLUMN "
				+ DownloadManagerHelper.PRIORITY + " INTEGER DEFAULT 0");
	}
	
	/**
	 * 新版本需要重新绑定腾讯微博 
	 * @param context
	 */
	private void removeTencentWBAccount(SQLiteDatabase db){
		String where = WeiboAccountColumn.C_TYPE_WB + " = " + WeiboAccountColumn.WB_TYPE_TENCENT;
		db.delete(WeiboAccountColumn.TABLE_NAME, where, null);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		PalLog.v(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion);
		// TODO ...
		// int version = oldVersion;
		if (oldVersion == newVersion) {
			return;
		}
		if (1 == oldVersion && 16 <= newVersion) {
			StringBuilder sb = new StringBuilder();
			sb.append("alter table ")
					.append(VopenContentProvider.TABLE_USER_ACCOUNT)
					.append(" add ").append(UserAccountHelper.USER_COOKIE)
					.append(" TEXT");
			db.execSQL(sb.toString());
		}
		if (oldVersion > 9 && oldVersion <= 15) {
			oldVersion = 15;
		} else if (oldVersion > 1 && oldVersion <= 9) {
			oldVersion = 9;
		}
		boolean recoverDownload = true;
		switch (oldVersion) {
		case 9:
			recoverDownload = false;
		case 15:
			PalLog.v(TAG, "Upgrading database oldVersion " + oldVersion);
			long timeStart = System.currentTimeMillis();
			onCreate(db);
			if (recoverDownload) {
				transDwonloadTable(db);
			}
			String userAccount = transAccount(db);
			//账户不为空时，转移已登录收藏表
			if (StringUtil.checkStr(userAccount))
				transCollect(db, userAccount);
			transNoLoginCollect(db);
			transPlayRecord(db);
			clearOld(db);
			long timeEnd = System.currentTimeMillis();
			PalLog.e(TAG, "Upgrading database time is  "
					+ (timeEnd - timeStart));
		case 16:
		case 17:
			encAllPw(db);
		case 18:
			//19版本的数据库，主要在下载数据表添加了优先级字段，在播放记录表中添加了播放时间戳字段。
			addPrioirtyToDownloadManager(db);
			addPlayDateToPlayRecord(db);
			removeTencentWBAccount(db);
		default:
			break;
		}
	}

	public class OldDbName {

		// private static final String TAG = "vopenDB";

		public static final String MYID = "myid";
		public static final String C_ISCHECKED = "ischecked"; // 是否被选中
		public static final String T_Favority = "tfavority";// 本地未登录用户收藏表
		public static final String T_Net_Favority = "tnetfavority";// 在线/已登录用户收藏列表
		public static final String C_Translate_num = "translate_num";

		// 课程ID 课程json
		public static final String C_CourseId = "ccourserid";
		public static final String C_CourseJson = "ccourserjson";
		public static final String C_Datatime = "datatime";

		public static final String T_Course = "course";

		public static final String T_CoursePlayRecord = "tcourseplayrecord";// 课程章节播放记录表
																			// 每一课的播放记录
		public static final String T_CourseLookRecord = "tcourselookrecord";// 课程播放记录表
																			// "menu"
																			// 播放记录
		// 课程ID 课节ID 播放位置(0~100)
		public static final String C_SectionId = "csectionid";
		public static final String C_PlayPosition = "cplayposition";
		public static final String C_playcount = "cplaycount";

		// 下载表
		public static final String T_download = "download";
		public static final String C_Name = "c_name"; // 课程名
		public static final String C_path = "c_path"; // 视频路径
		/** 下载状态 -1下载失败，0等待, 1下载，2 暂停， 3下载完成 */
		public static final String C_Download_status = "d_status"; // 视频路径
		/** Y 下载完成 N未下载完成 */
		public static final String IS_Finish = "is_finish"; // 是否下载完成
		/** 视频大小 */
		public static final String C_Allsize = "c_all_size";

		/** 已经下载视频大小 */
		public static final String C_Downloadlength = "c_d_length";

		// 新增课程表 存放所有数据
		public static final String C_Course_allDate_table = "c_course_data";
		// 类型
		public static final String C_type = "is_finish";
	}

}
