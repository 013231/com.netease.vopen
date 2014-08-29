package vopen.db;
/*************************************************************
 * 该类为数据库基本接口
 * @author echo_chen
 * @date 2012-01-08
 *************************************************************/
import java.util.ArrayList;
import java.util.List;

import vopen.db.VopenContentProvider.DownloadManagerHelper;
import vopen.db.VopenContentProvider.UserAccountHelper;
import vopen.db.VopenContentProvider.VopenAllDataJsonHelper;
import vopen.db.VopenContentProvider.VopenCoursePlayRecordHeleper;
import vopen.db.VopenContentProvider.VopenDetailHelper;
import vopen.db.VopenContentProvider.VopenMyCollectHelper;
import vopen.db.VopenContentProvider.VopenNoLoginMyCollectHelper;
import vopen.db.VopenContentProvider.VopenVideoPlayRecordHeleper;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import com.netease.vopen.pal.Constants;
import common.util.StringUtil;
import common.util.Util;


public class DBApi {
	public static final int DB_OPERATION_FAILED = -1;
	/*************************************************************
	 * 公开课所有数据JSON表  
	 * table name :TABLE_All_DTTA = "t_vopen_all_data"
	 *************************************************************/
	//该表数据字段
	public static class CourseInfo{
		public String mType;
		public String mContent;
	}
	/**公开课课程类型*/
    public static class CourseType {
    	public static final String DATA_TYPE_ALL = Constants.DATA_TYPE_ALL;
    	public static final String DATA_TYPE_TOUTU = Constants.DATA_TYPE_TOUTU;
    	public static final String DATA_TYPE_ENJOY = Constants.DATA_TYPE_ENJOY;
    	public static final String DATA_TYPE_XINLI = Constants.DATA_TYPE_XINLI;
    	public static final String DATA_TYPE_SHULI = Constants.DATA_TYPE_SHULI;
    	public static final String DATA_TYPE_ZHEXUE = Constants.DATA_TYPE_ZHEXUE;
    	public static final String DATA_TYPE_RENWEN = Constants.DATA_TYPE_RENWEN;
    	public static final String DATA_TYPE_JINGJI = Constants.DATA_TYPE_JINGJI;
    	public static final String DATA_TYPE_TED = Constants.DATA_TYPE_TED;
    	public static final String DATA_TYPE_QITA = Constants.DATA_TYPE_QITA;
    }
	/**
	 * 向公开课所有数据表中插入一项
	 * @param context
	 * @param CourseInfo mType的定义见CourseType类中的课程类型定义
	 * @return 
	 */
	public static Uri insertCourse(Context context,CourseInfo course) {
		if(null == course) {
			return null;
		}
		ContentValues initialValues = new ContentValues();
		if (null != course.mType) {
			initialValues.put(VopenAllDataJsonHelper.COURSE_TYPE, course.mType);
		}
		if (null != course.mContent) {
			initialValues.put(VopenAllDataJsonHelper.COURSE_CONTENT, course.mContent);
		}
		return context.getContentResolver().insert(VopenAllDataJsonHelper.getUri(), initialValues);
	}
	/**
	 * 更新公开课所有数据表中某一课程类型所对应的课程数据
	 * @param context
	 * @param CourseInfo mType的定义见CourseType类中的课程类型定义
	 * @return 
	 */
	public static int updateCourse (Context context,CourseInfo course) {
		if(null == course) {
			return DB_OPERATION_FAILED;
		}
		ContentValues values = new ContentValues();
		values.clear();
		values.put(VopenAllDataJsonHelper.COURSE_CONTENT, course.mContent);
		String selection = VopenAllDataJsonHelper.COURSE_TYPE + "=?";
		String[] selectionArgs = new String[] {course.mType};		
		return context.getContentResolver().update(VopenAllDataJsonHelper.getUri(), values,selection, selectionArgs);
	}
	/**
	 * 查询公开课所有数据表中的项
	 * @param context
	 * @param selection
	 * @param projection
	 * @param selectionArgs
	 * @return Cursor
	 */
	public static Cursor queryCourse(Context context, String selection,String[] selectionArgs,String[] projection){
		Cursor c = context.getContentResolver().query(VopenAllDataJsonHelper.getUri(),
													  projection,
													  selection,
													  selectionArgs,
													  null);
		return c;
	}
	/**
	 * 根据课程类型查询课程数据
	 * @param context
	 * @param type type的定义见CourseType类中的课程类型定义
	 * @return Cursor
	 */
	public static Cursor queryCourseByType(Context context, String type,String[] projection){
		if( null == type) {
			return null;
		}
		String selection = VopenAllDataJsonHelper.COURSE_TYPE + "=?";
		String[] selectionArgs = new String[] {type};	
		Cursor c = context.getContentResolver().query(VopenAllDataJsonHelper.getUri(),
													  projection,
													  selection,
													  selectionArgs,
													  null);
		return c;
	}
	/**
	 * 根据课程类型删除课程数据
	 * @param context
	 * @param type type的定义见CourseType类中的课程类型定义
	 * @return
	 */
	public static int deleteCourseByType(Context context, String type){
		if(null == type) {
			return DB_OPERATION_FAILED;
		}
		String selection = VopenAllDataJsonHelper.COURSE_TYPE + "=?";
		String[] selectionArgs = new String[] {type};	
		return context.getContentResolver().delete(VopenAllDataJsonHelper.getUri(),
													  selection,
													  selectionArgs);
	}
	/**
	 * 删除所有课程数据
	 * @param context
	 * @param type type的定义见CourseType类中的课程类型定义
	 * @return 
	 */
	public static int deleteAllCourse(Context context){
		return context.getContentResolver().delete(VopenAllDataJsonHelper.getUri(),null,null);
	}
	/***
	 * 所有课程数据表， 删除原来数据，保存新数据,批量操作方式
	 * @param db
	 * @param Map<type,courseJson>
	 * @return
	 * @deprecated
	 */
	public static boolean saveNewAllCourse(Context context, ArrayList<CourseInfo> data) {
		if (data == null ) {
			return false;
		}
		deleteAllCourse(context);
		VopenDatabaseHelper databaseHelper = VopenDatabaseHelper.getInstance(context);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
			db.beginTransaction();
			CourseInfo course = null;
			for(int i=0; i< data.size(); i++ ) {
				course = data.get(i);
				if (course == null) {
					db.endTransaction();
					return false;
				} else {
					if (insertCourseStatement(db, course.mContent,course.mType) == false) {
						// 出错结束事务
						db.endTransaction();
						return false;
					}
				}
			} 
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return true;
		


	}
	private static boolean insertCourseStatement(SQLiteDatabase db, String courseJson, String type) {
		if (db == null || courseJson == null || type == null) {
			return false;
		}
//		db.delete(AppSQLiteOpenHelper.C_Course_allDate_table, AppSQLiteOpenHelper.C_type + " = ? ", new String[]{type});
		
		StringBuilder sql = new StringBuilder(0);
		sql.append(" INSERT OR ROLLBACK INTO ");
		
		sql.append(VopenContentProvider.TABLE_All_DTTA);
		sql.append("(");
		sql.append(VopenAllDataJsonHelper.COURSE_TYPE);//1
		sql.append(",");
		sql.append(VopenAllDataJsonHelper.COURSE_CONTENT);//2
        sql.append(")");
        sql.append(" VALUES(");
        sql.append("?,?");
        sql.append(");");
        
        SQLiteStatement statement = null;
        try {
        	statement = db.compileStatement(sql.toString());
	    	statement.bindString(1, null != type ? type:"");
	    	statement.bindString(2, null != courseJson ? courseJson:"");
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
	
	/*************************************************************
	 * 公开课课程详情数据JSON表  
	 * table name :TABLE_COURSE_DATA = "t_vopen_detail_data"
	 *************************************************************/
	//该表数据字段
	public static class CourseDetailInfo{
		public String mCourse_id;
		public String mContent;
	}
	/**
	 * 向公开课课程详情数据表中插入一项
	 * @param context
	 * @param CourseDetailInfo
	 * @return 
	 */
	public static Uri insertCourseDetail(Context context,CourseDetailInfo course) {
		if(null == course) {
			return null;
		}
		ContentValues initialValues = new ContentValues();
		if (null != course.mCourse_id) {
			initialValues.put(VopenDetailHelper.COURSE_PLID, course.mCourse_id);
		}
		if (null != course.mContent) {
			initialValues.put(VopenDetailHelper.COURSE_CONTENT, course.mContent);
		}
	    return context.getContentResolver().insert(VopenDetailHelper.getUri(), initialValues);
	}
	/**
	 * 更新公开课课程详情数据表中某一课程id所对应的课程详情数据
	 * @param context
	 * @param CourseDetailInfo
	 * @return 
	 */
	public static int updateCourseDetail(Context context,CourseDetailInfo course) {
		if(null == course) {
			return DB_OPERATION_FAILED;
		}
		ContentValues values = new ContentValues();
		values.clear();
		values.put(VopenDetailHelper.COURSE_CONTENT, course.mContent);
		String selection = VopenDetailHelper.COURSE_PLID + "=?";
		String[] selectionArgs = new String[] {course.mCourse_id};		
		return context.getContentResolver().update(VopenDetailHelper.getUri(), values,selection, selectionArgs);
	}
	/**
	 * 查询公开课课程详情数据表中的项
	 * @param context
	 * @param selection
	 * @param projection
	 * @param selectionArgs
	 * @return Cursor
	 */
	public static Cursor queryCourseDetail(Context context, String selection,String[] selectionArgs,String[] projection){
		Cursor c = context.getContentResolver().query(VopenDetailHelper.getUri(),
													  projection,
													  selection,
													  selectionArgs,
													  null);
		return c;
	}
	/**
	 * 根据课程id查询课程详情
	 * @param context
	 * @param course_id 
	 * @return Cursor
	 */
	public static Cursor queryCourseDetailByCourseID(Context context,String course_id, String[] projection){
		if( null == course_id) {
			return null;
		}
		String selection = VopenDetailHelper.COURSE_PLID + "=?";
		String[] selectionArgs = new String[] {course_id};	
		Cursor c = context.getContentResolver().query(VopenDetailHelper.getUri(),
													  projection,
													  selection,
													  selectionArgs,
													  null);
		return c;
	}
	/**
	 * 根据课程id删除课程详情
	 * @param context
	 * @param course_id
	 * @return
	 */
	public static int deleteCourseDetailByType(Context context, String course_id){
		if(null == course_id) {
			return DB_OPERATION_FAILED;
		}
		String selection = VopenDetailHelper.COURSE_PLID + "=?";
		String[] selectionArgs = new String[] {course_id};	
		return context.getContentResolver().delete(VopenDetailHelper.getUri(),
													  selection,
													  selectionArgs);
	}
	/**
	 * 删除所有课程详情数据
	 * @param context
	 * @return 
	 */
	public static int deleteAllCourseDetail(Context context){
		return context.getContentResolver().delete(VopenDetailHelper.getUri(),null,null);
	}
	
	/*************************************************************
	 * 公开课已经登录的收藏表  
	 * table name :TABLE_MY_COLLECT = "t_vopen_my_collect"
	 *************************************************************/
		//该表数据字段
		public static class CollectInfo{
			public String mCourse_id;
			public String mCourse_title;
			public String mCourse_img;
	    	public int mCourse_playcount;
	    	public int mCourse_translatecount;
	    	public int mCourse_new_translate_num;//新翻译集数
	    	public String mUser_id;
	    	public boolean mIs_synchronized;//Integer类型 ,已经同步为1 未同步为0
	    	public String mData_time;//收藏时间
		}
		/**
		 * 向公开课已经登录的收藏表中插入一项
		 * @param context
		 * @param collectInfo
		 * @return 
		 */
		public static Uri insertCollect(Context context,CollectInfo collectInfo) {
			if(null == collectInfo) {
				return null;
			}
			ContentValues initialValues = new ContentValues();
			if (null != collectInfo.mCourse_id) {
				initialValues.put(VopenMyCollectHelper.COURSE_PLID, collectInfo.mCourse_id);
			}
			if (null != collectInfo.mCourse_title) {
				initialValues.put(VopenMyCollectHelper.COURSE_TITLE, collectInfo.mCourse_title);
			}
			if (null != collectInfo.mCourse_img) {
				initialValues.put(VopenMyCollectHelper.COURSE_IMG, collectInfo.mCourse_img);
			}
			initialValues.put(VopenMyCollectHelper.COURSE_PLAYCOUNT, collectInfo.mCourse_playcount);
			initialValues.put(VopenMyCollectHelper.COURSE_TRANSLATECOUNT, collectInfo.mCourse_translatecount);
			initialValues.put(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM, collectInfo.mCourse_new_translate_num);
			if (null != collectInfo.mUser_id) {
				initialValues.put(VopenMyCollectHelper.USER_ID, collectInfo.mUser_id);
			}
			initialValues.put(VopenMyCollectHelper.IS_SYNC, collectInfo.mIs_synchronized?1:0);
			if (null != collectInfo.mData_time) {
				initialValues.put(VopenMyCollectHelper.DATA_TIME, collectInfo.mData_time);
			}
		    return context.getContentResolver().insert(VopenMyCollectHelper.getUri(), initialValues);
		}
		
		/**
		 * 批量插入收藏数据 
		 * @param context
		 * @param collectInfos
		 */
		public static void bulkInsertCollect(Context context,
				List<CollectInfo> collectInfos) {
			if (null == collectInfos || collectInfos.size() == 0) {
				return;
			}
			List<ContentValues> initialValueList = new ArrayList<ContentValues>();
			for (CollectInfo collectInfo : collectInfos) {
				ContentValues initialValues = new ContentValues();
				if (null != collectInfo.mCourse_id) {
					initialValues.put(VopenMyCollectHelper.COURSE_PLID,
							collectInfo.mCourse_id);
				}
				if (null != collectInfo.mCourse_title) {
					initialValues.put(VopenMyCollectHelper.COURSE_TITLE,
							collectInfo.mCourse_title);
				}
				if (null != collectInfo.mCourse_img) {
					initialValues.put(VopenMyCollectHelper.COURSE_IMG,
							collectInfo.mCourse_img);
				}
				initialValues.put(VopenMyCollectHelper.COURSE_PLAYCOUNT,
						collectInfo.mCourse_playcount);
				initialValues.put(VopenMyCollectHelper.COURSE_TRANSLATECOUNT,
						collectInfo.mCourse_translatecount);
				initialValues.put(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM,
						collectInfo.mCourse_new_translate_num);
				if (null != collectInfo.mUser_id) {
					initialValues.put(VopenMyCollectHelper.USER_ID,
							collectInfo.mUser_id);
				}
				initialValues.put(VopenMyCollectHelper.IS_SYNC,
						collectInfo.mIs_synchronized ? 1 : 0);
				if (null != collectInfo.mData_time) {
					initialValues.put(VopenMyCollectHelper.DATA_TIME,
							collectInfo.mData_time);
				}
				initialValueList.add(initialValues);
			}
			context.getContentResolver().bulkInsert(VopenMyCollectHelper.getUri(),
					initialValueList.toArray(new ContentValues[0]));
		}

		
		/**
		 * 更新公开课已经登录的收藏表中某一课程id所对应的数据
		 * @param context
		 * @param collectInfo
		 * @return 
		 */
		public static int updateCollectByCourseID(Context context,CollectInfo collectInfo) {
			if(null == collectInfo) {
				return DB_OPERATION_FAILED;
			}
			ContentValues values = new ContentValues();
			values.clear();
			if (null != collectInfo.mCourse_title) {
				values.put(VopenMyCollectHelper.COURSE_TITLE, collectInfo.mCourse_title);
			}
			if (null != collectInfo.mCourse_img) {
				values.put(VopenMyCollectHelper.COURSE_IMG, collectInfo.mCourse_img);
			}
			values.put(VopenMyCollectHelper.COURSE_PLAYCOUNT, collectInfo.mCourse_playcount);
			values.put(VopenMyCollectHelper.COURSE_TRANSLATECOUNT, collectInfo.mCourse_translatecount);
			values.put(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM, collectInfo.mCourse_new_translate_num);
			if (null != collectInfo.mUser_id) {
				values.put(VopenMyCollectHelper.USER_ID, collectInfo.mUser_id);
			}
			values.put(VopenMyCollectHelper.IS_SYNC, collectInfo.mIs_synchronized?1:0);
			if (null != collectInfo.mData_time) {
				values.put(VopenMyCollectHelper.DATA_TIME, collectInfo.mData_time);
			}
			String selection = VopenMyCollectHelper.COURSE_PLID + "=?";
			String[] selectionArgs = new String[] {collectInfo.mCourse_id};		
			return context.getContentResolver().update(VopenMyCollectHelper.getUri(), values,selection, selectionArgs);
		}
		
		public static void updateNewTranslateNum(Context context, String userId, String plid, int newTranslateNum) {
			Uri uri = Util.isStringEmpty(userId) ? VopenNoLoginMyCollectHelper.getUri() : VopenMyCollectHelper.getUri();
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM, newTranslateNum);
			if(Util.isStringEmpty(userId)){
				resolver.update(uri, values, VopenMyCollectHelper.COURSE_PLID + " =? ", new String[]{plid});
			}else{
				resolver.update(uri, values, VopenMyCollectHelper.COURSE_PLID + " =? and " + VopenMyCollectHelper.USER_ID + " =? ", new String[]{plid, userId});
			}
		}
		/**
		 * 更新该课程的翻译数
		 * @param context
		 * @param bLogin
		 * @param plid
		 * @param translateNum
		 */
		public static void updateTranslateNum(Context context, boolean bLogin, String plid, int translateNum){
			ContentValues values = new ContentValues();
			values.put(VopenMyCollectHelper.COURSE_TRANSLATECOUNT, translateNum);
			
			if(bLogin){
				context.getContentResolver().update(VopenMyCollectHelper.getUri(), values, 
						VopenMyCollectHelper.COURSE_PLID + " =? ", new String[]{plid});
			}else{
				context.getContentResolver().update(VopenNoLoginMyCollectHelper.getUri(), values, 
						VopenMyCollectHelper.COURSE_PLID + " =? ", new String[]{plid});
			}
		}
		
		/**
		 * 查询公开课已经登录的收藏表中的项
		 * @param context
		 * @param selection
		 * @param projection
		 * @param selectionArgs
		 * @return Cursor
		 */
		public static Cursor queryCollect(Context context, String selection,String[] projection,String[] selectionArgs){
			Cursor c = context.getContentResolver().query(VopenMyCollectHelper.getUri(),
														  projection,
														  selection,
														  selectionArgs,
														  null);
			return c;
		}
		/**
		 * 查询公开课已经登录的收藏表中的所有项
		 * @param context
		 * @param sort
		 * @return Cursor
		 */
		public static Cursor queryCollectAll(Context context,String sort,String[] projection){
			if(null == sort) {
				sort = VopenMyCollectHelper.DATA_TIME + " DESC";
			}
			Cursor c = context.getContentResolver().query(VopenMyCollectHelper.getUri(),
														  projection,
														  null,
														  null,
														  sort);
			return c;
		}
		/**
		 * 查询公开课已经登录的收藏表中对应某一帐号的所有项
		 * @param context
		 * @param user_id
		 * @param sort
		 * @return Cursor
		 */
		public static Cursor queryCollectAllByUser(Context context, String user_id,String sort,String[] projection){
			if(null == user_id) {
				return null;
			}
			String selection = VopenMyCollectHelper.USER_ID + "=?";
			String[] selectionArgs = new String[] {user_id};
			Cursor c = context.getContentResolver().query(VopenMyCollectHelper.getUri(),
														  projection,
														  selection,
														  selectionArgs,
														  sort);
			return c;
		}
		/**
		 * 根据课程id和登录帐号查询公开课已经登录的收藏表中对应的数据项
		 * @param context
		 * @param course_id 
		 * @param user_id
		 * @return Cursor
		 */
		public static Cursor queryCollectByCourseID(Context context, String course_id,String user_id){
			if( null == course_id) {
				return null;
			}
//			String selection = VopenMyCollectHelper.COURSE_PLID + " =? " + " AND " +VopenMyCollectHelper.USER_ID + "=?";
//			String[] selectionArgs = new String[] {course_id,user_id};	
			ArrayList<String> selectionArgs = new ArrayList<String>();
			String selection = VopenMyCollectHelper.COURSE_PLID + "=?";
			selectionArgs.add(course_id);
			if(!Util.isStringEmpty(user_id)){
				selection += " AND " + VopenMyCollectHelper.USER_ID + "=?";
				selectionArgs.add(user_id);
			}
			String[] sa = new String[selectionArgs.size()];
			selectionArgs.toArray(sa);
			Cursor c = context.getContentResolver().query(VopenMyCollectHelper.getUri(),
														  null,
														  selection,
														  sa,
														  null);
			return c;
		}
		/**
		 * 根据课程id删除收藏列表项
		 * @param context
		 * @param course_id
		 * @return
		 */
		public static int deleteCollectByCourseID(Context context, String userId, String course_id){
			if( null == course_id) {
				return DB_OPERATION_FAILED;
			}
			ArrayList<String> selectionArgs = new ArrayList<String>();
			String selection = VopenMyCollectHelper.COURSE_PLID + "=?";
			selectionArgs.add(course_id);
			if(!Util.isStringEmpty(userId)){
				selection += " AND " + VopenMyCollectHelper.USER_ID + "=?";
				selectionArgs.add(userId);
			}
			String[] sa = new String[selectionArgs.size()];
			selectionArgs.toArray(sa);
			return context.getContentResolver().delete(VopenMyCollectHelper.getUri(),
														  selection,
														  sa);
		}
		/**
		 * 删除所有收藏表
		 * @param context
		 * @return 
		 */
		public static int deleteAllCollect(Context context, String userId){
			ArrayList<String> selectionArgs = null;
			String selection = null;
			if(!Util.isStringEmpty(userId)){
				selectionArgs = new ArrayList<String>();
				selection = VopenMyCollectHelper.USER_ID + "=?";
				selectionArgs.add(userId);
			}
			String[] sa = new String[selectionArgs.size()];
			selectionArgs.toArray(sa);
			return context.getContentResolver().delete(VopenMyCollectHelper.getUri(),selection,sa);
		}
		
	/*************************************************************
	 * 公开课未登录的收藏表  
	 * table name :TABLE_NOLOGIN_MY_COLLECT = "t_vopen_nologin_my_collect"
	 * 说明与已登录的表字段基本相同，少了user_id，is_synchronized两个字段
	 * 数据结构共用CollectInfo mUser_id，mIs_synchronized可不填
	 *************************************************************/
		
		/**
		 * 向公开课未登录的收藏表中插入一项
		 * @param context
		 * @param collectInfo
		 * @return 
		 */
		public static Uri insertCollectNoLogin(Context context,CollectInfo collectInfo) {
			if(null == collectInfo) {
				return null;
			}
			ContentValues initialValues = new ContentValues();
			if (null != collectInfo.mCourse_id) {
				initialValues.put(VopenMyCollectHelper.COURSE_PLID, collectInfo.mCourse_id);
			}
			if (null != collectInfo.mCourse_title) {
				initialValues.put(VopenMyCollectHelper.COURSE_TITLE, collectInfo.mCourse_title);
			}
			if (null != collectInfo.mCourse_img) {
				initialValues.put(VopenMyCollectHelper.COURSE_IMG, collectInfo.mCourse_img);
			}
			initialValues.put(VopenMyCollectHelper.COURSE_PLAYCOUNT, collectInfo.mCourse_playcount);
			initialValues.put(VopenMyCollectHelper.COURSE_TRANSLATECOUNT, collectInfo.mCourse_translatecount);
			initialValues.put(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM, collectInfo.mCourse_new_translate_num);
			if (null != collectInfo.mData_time) {
				initialValues.put(VopenMyCollectHelper.DATA_TIME, collectInfo.mData_time);
			}
		    return context.getContentResolver().insert(VopenNoLoginMyCollectHelper.getUri(), initialValues);
		}
		/**
		 * 更新公开课未登录的收藏表中某一课程id所对应的数据
		 * @param context
		 * @param collectInfo
		 * @return 
		 */
		public static int updateCollectByCourseIDNoLogin(Context context,CollectInfo collectInfo) {
			if(null == collectInfo) {
				return DB_OPERATION_FAILED;
			}
			ContentValues values = new ContentValues();
			values.clear();
			if (null != collectInfo.mCourse_title) {
				values.put(VopenMyCollectHelper.COURSE_TITLE, collectInfo.mCourse_title);
			}
			if (null != collectInfo.mCourse_img) {
				values.put(VopenMyCollectHelper.COURSE_IMG, collectInfo.mCourse_img);
			}
			values.put(VopenMyCollectHelper.COURSE_PLAYCOUNT, collectInfo.mCourse_playcount);
			values.put(VopenMyCollectHelper.COURSE_TRANSLATECOUNT, collectInfo.mCourse_translatecount);
			values.put(VopenMyCollectHelper.COURSE_NEW_TRANSLATE_NUM, collectInfo.mCourse_new_translate_num);
			if (null != collectInfo.mData_time) {
				values.put(VopenMyCollectHelper.DATA_TIME, collectInfo.mData_time);
			}
			String selection = VopenMyCollectHelper.COURSE_PLID + "=?";
			String[] selectionArgs = new String[] {collectInfo.mCourse_id};		
			return context.getContentResolver().update(VopenNoLoginMyCollectHelper.getUri(), values,selection, selectionArgs);
		}
		/**
		 * 查询公开课未登录的收藏表中的项
		 * @param context
		 * @param selection
		 * @param projection
		 * @param selectionArgs
		 * @return Cursor
		 */
		public static Cursor queryCollectNoLogin(Context context, String selection,String[] projection,String[] selectionArgs){
			Cursor c = context.getContentResolver().query(VopenNoLoginMyCollectHelper.getUri(),
														  projection,
														  selection,
														  selectionArgs,
														  null);
			return c;
		}
		/**
		 * 查询公开课未登录的收藏表中的所有项
		 * @param context
		 * @param sort
		 * @return Cursor
		 */
		public static Cursor queryCollectAllNoLogin(Context context,String sort){
			if(null == sort) {
				sort = VopenMyCollectHelper.DATA_TIME + " DESC";
			}
			Cursor c = context.getContentResolver().query(VopenNoLoginMyCollectHelper.getUri(),
														  null,
														  null,
														  null,
														  sort);
			return c;
		}
		
		/**
		 * 根据课程id查询公开课未登录的收藏表中对应的数据项
		 * @param context
		 * @param course_id 
		 * @return Cursor
		 */
		public static Cursor queryCollectByCourseIDNoLogin(Context context, String course_id){
			if(null == course_id) {
				return null;
			}
			String selection = VopenMyCollectHelper.COURSE_PLID + "=?" ;
			String[] selectionArgs = new String[] {course_id};	
			Cursor c = context.getContentResolver().query(VopenNoLoginMyCollectHelper.getUri(),
														  null,
														  selection,
														  selectionArgs,
														  null);
			return c;
		}
		/**
		 * 根据课程id删除未登录收藏列表项
		 * @param context
		 * @param course_id
		 * @return
		 */
		public static int deleteCollectByCourseIDNoLogin(Context context, String course_id){
			if(null == course_id) {
				return DB_OPERATION_FAILED;
			}
			String selection = VopenMyCollectHelper.COURSE_PLID + "=?";
			String[] selectionArgs = new String[] {course_id};	
			return context.getContentResolver().delete(VopenNoLoginMyCollectHelper.getUri(),
														  selection,
														  selectionArgs);
		}
		/**
		 * 删除未登录所有收藏表
		 * @param context
		 * @return 
		 */
		public static int deleteAllCollectNoLogin(Context context){
			return context.getContentResolver().delete(VopenNoLoginMyCollectHelper.getUri(),null,null);
		}
	/*************************************************************
	 * 公开课用户帐号表  
	 * table name :TABLE_USER_ACCOUNT = "t_vopen_user_account"
	 *************************************************************/
		//该表数据字段
		public static class AccountInfo{
			public String mUser_account;
			public String mUser_pwd;
			public String mUser_nikename;
	    	public boolean mIs_login;//true 为登录
	    	public String mUser_cookie;
		}
		/**
		 * 向公开课用户帐号表 中插入一项
		 * @param context
		 * @param AccountInfo
		 * @return 
		 */
		public static Uri insertAccount(Context context,AccountInfo user) {
			if(null == user) {
				return null;
			}
			ContentValues initialValues = new ContentValues();
			if (null != user.mUser_account) {
				initialValues.put(UserAccountHelper.USER_ACCOUNT, user.mUser_account);
			}
			if (null != user.mUser_pwd) {
				initialValues.put(UserAccountHelper.USER_PASSWORD, user.mUser_pwd);
			}
			if (null != user.mUser_nikename) {
				initialValues.put(UserAccountHelper.USER_NIKENAME, user.mUser_nikename);
			}
			if (null != user.mUser_cookie) {
				initialValues.put(UserAccountHelper.USER_COOKIE, user.mUser_cookie);
			}
			initialValues.put(UserAccountHelper.IS_LOGIN, user.mIs_login?1:0);
			
		    return context.getContentResolver().insert(UserAccountHelper.getUri(), initialValues);
		}
		/**
		 * 更新公开课用户帐号表中某一user_account所对应的数据项
		 * @param context
		 * @param AccountInfo
		 * @return 
		 */
		public static int updateAccountByUserAccount(Context context,AccountInfo user) {
			if(null == user) {
				return DB_OPERATION_FAILED;
			}
			ContentValues values = new ContentValues();
			values.clear();
			if (null != user.mUser_pwd) {
				values.put(UserAccountHelper.USER_PASSWORD, user.mUser_pwd);
			}
			if (null != user.mUser_nikename) {
				values.put(UserAccountHelper.USER_NIKENAME, user.mUser_nikename);
			}
			if (null != user.mUser_cookie) {
				values.put(UserAccountHelper.USER_COOKIE, user.mUser_cookie);
			}
			values.put(UserAccountHelper.IS_LOGIN, user.mIs_login?1:0);
			
			String selection = UserAccountHelper.USER_ACCOUNT + "=?";
			String[] selectionArgs = new String[] {user.mUser_account};		
			return context.getContentResolver().update(UserAccountHelper.getUri(), values,selection, selectionArgs);
		}
		/**
		 * 查询公开课用户帐号表中的项
		 * @param context
		 * @param selection
		 * @param projection
		 * @param selectionArgs
		 * @return Cursor
		 */
		public static Cursor queryAccount(Context context, String selection,String[] projection,String[] selectionArgs){
			Cursor c = context.getContentResolver().query(UserAccountHelper.getUri(),
														  projection,
														  selection,
														  selectionArgs,
														  null);
			return c;
		}
		/**
		 * 查询公开课用户帐号表中的所有帐号
		 * @param context
		 * @param sort
		 * @return Cursor
		 */
		public static Cursor queryAllAccount(Context context,String sort,String[] projection){
			
			Cursor c = context.getContentResolver().query(UserAccountHelper.getUri(),
														  projection,
														  null,
														  null,
														  sort);
			return c;
		}
		/**
		 * 查询公开课帐号表中已经登录的帐号
		 * @param context
		 * @return Cursor
		 */
		public static Cursor queryLoginAccount(Context context,String[] projection){
			String selection = UserAccountHelper.IS_LOGIN + "=?";
			String[] selectionArgs = new String[] {String.valueOf(1)};
			Cursor c = context.getContentResolver().query(UserAccountHelper.getUri(),
														  projection,
														  selection,
														  selectionArgs,
														  null);
			return c;
		}
		/**
		 * 清除当前登录状态
		 * @param context
		 * @return Cursor
		 */
		public static int cleartLoginAccount(Context context){
			ContentValues values = new ContentValues();
	    	values.put(UserAccountHelper.IS_LOGIN, "0");
	    	values.put(UserAccountHelper.USER_COOKIE,"");
	    	String selection = UserAccountHelper.IS_LOGIN + "=?";
			String[] selectionArgs = new String[] {String.valueOf(1)};
			return context.getContentResolver().update(UserAccountHelper.getUri(), values, selection, selectionArgs);
		}
		/**
		 * 根据帐号表中的用户名查找帐号
		 * @param context
		 * @param user_account
		 * @return
		 */
		public static Cursor queryAccountByUserAccount(Context context, String user_account,String[] projection){
			if(null == user_account) {
				return null;
			}
			String selection = UserAccountHelper.USER_ACCOUNT + "=?";
			String[] selectionArgs = new String[] {user_account};	
			return context.getContentResolver().query(UserAccountHelper.getUri(),
													  projection,
													  selection,
													  selectionArgs,
													  null);
		}
		/**
		 * 根据帐号表中的用户名删除帐号
		 * @param context
		 * @param user_account
		 * @return
		 */
		public static int deleteAccountByUserAccount(Context context, String user_account){
			if(null == user_account) {
				return DB_OPERATION_FAILED;
			}
			String selection = UserAccountHelper.USER_ACCOUNT + "=?";
			String[] selectionArgs = new String[] {user_account};	
			return context.getContentResolver().delete(UserAccountHelper.getUri(),
														  selection,
														  selectionArgs);
		}
		/**
		 * 删除所有帐号
		 * @param context
		 * @return 
		 */
		public static int deleteAllAccount(Context context){
			return context.getContentResolver().delete(UserAccountHelper.getUri(),null,null);
		}
		/*************************************************************
		 * 公开课课程播放表  
		 * table name :TABLE_COURSE_PLAY_RECORD = "t_course_play_record"
		 *************************************************************/
		//该表数据字段
		public static class CoursePlayInfo{
	    	public  String mImg_path;//海报路径
	    	public  String mTitle;//标题
	    	public  String mCourse_id;//课程ID
	    	public int mVideo_index;//视频 索引
	    	public int mVideo_count;//视频 总集数
	    	public int mPlay_position;//观看进度
	    	public int mVideo_length;//视频总长度
	    	public void readFromCursor(Cursor cursor) {
	    		// TODO Auto-generated method stub
	    		int index = -1;
	    		if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.IMG_PATH)) != -1) {
		            	mImg_path = cursor.getString(index);
		                index = -1;
		            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.TITLE)) != -1) {
	            	mTitle = cursor.getString(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.COURSE_ID)) != -1) {
	            	mCourse_id = cursor.getString(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_INDEX)) != -1) {
	            	mVideo_index = cursor.getInt(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS)) != -1) {
	            	mVideo_count = cursor.getInt(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION)) != -1) {
	            	mPlay_position = cursor.getInt(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_LENGTH)) != -1) {
	            	mVideo_length = cursor.getInt(index);
	                index = -1;
	            }
	    	}
		}
			/**
			 * 向公开课课程播放表 中插入一项
			 * @param context
			 * @param CoursePlayInfo
			 * @return 
			 */
			public static Uri insertCoursePlayInfo(Context context,CoursePlayInfo info) {
				if(null == info) {
					return null;
				}
				ContentValues initialValues = new ContentValues();
				if (null != info.mImg_path) {
					initialValues.put(VopenCoursePlayRecordHeleper.IMG_PATH, info.mImg_path);
				}
				if (null != info.mTitle) {
					initialValues.put(VopenCoursePlayRecordHeleper.TITLE, info.mTitle);
				}
				if (null != info.mCourse_id) {
					initialValues.put(VopenCoursePlayRecordHeleper.COURSE_ID, info.mCourse_id);
				}
				initialValues.put(VopenCoursePlayRecordHeleper.VIDEO_INDEX, info.mVideo_index);
				initialValues.put(VopenCoursePlayRecordHeleper.VIDEO_COUNTS, info.mVideo_count);
				initialValues.put(VopenCoursePlayRecordHeleper.PLAY_POSITION, info.mPlay_position);
				initialValues.put(VopenCoursePlayRecordHeleper.VIDEO_LENGTH, info.mVideo_length);
				initialValues.put(VopenCoursePlayRecordHeleper.PLAY_DATE, System.currentTimeMillis());
			    return context.getContentResolver().insert(VopenCoursePlayRecordHeleper.getUri(), initialValues);
			}
			/**
			 * 更新公开课课程播放表中某一课程id所对应的数据
			 * @param context
			 * @param CoursePlayInfo
			 * @return 
			 */
			public static int updateCoursePlayInfo(Context context,CoursePlayInfo info) {
				if(null == info) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.clear();
				if (null != info.mImg_path) {
					values.put(VopenCoursePlayRecordHeleper.IMG_PATH, info.mImg_path);
				}
				if (null != info.mTitle) {
					values.put(VopenCoursePlayRecordHeleper.TITLE, info.mTitle);
				}
				values.put(VopenCoursePlayRecordHeleper.VIDEO_INDEX, info.mVideo_index);
				values.put(VopenCoursePlayRecordHeleper.VIDEO_COUNTS, info.mVideo_count);
				values.put(VopenCoursePlayRecordHeleper.PLAY_POSITION, info.mPlay_position);
				values.put(VopenCoursePlayRecordHeleper.VIDEO_LENGTH, info.mVideo_length);
				values.put(VopenCoursePlayRecordHeleper.PLAY_DATE, System.currentTimeMillis());
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID + "=?";
				String[] selectionArgs = new String[] {info.mCourse_id};		
				return context.getContentResolver().update(VopenCoursePlayRecordHeleper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 查询课程播放表中的项
			 * @param context
			 * @param selection
			 * @param projection
			 * @param selectionArgs
			 * @return Cursor
			 */
			public static Cursor queryCoursePlayInfo(Context context, String selection,String[] projection,String[] selectionArgs){
				Cursor c = context.getContentResolver().query(VopenCoursePlayRecordHeleper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 查询课程播放表中的所有项
			 * @param context
			 * @param sort
			 * @return Cursor
			 */
			public static Cursor queryCoursePlayAll(Context context,String sort,String[] projection){
				Cursor c = context.getContentResolver().query(VopenCoursePlayRecordHeleper.getUri(),
															  projection,
															  null,
															  null,
															  sort);
				return c;
			}
			
			/**
			 * 根据课程id查询课程播放表中对应的数据项
			 * @param context
			 * @param course_id 
			 * @return Cursor
			 */
			public static Cursor queryCoursePlayByCourseID(Context context,String course_id, String[] projection){
				if(null == course_id) {
					return null;
				}
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID + "=?" ;
				String[] selectionArgs = new String[] {course_id};	
				Cursor c = context.getContentResolver().query(VopenCoursePlayRecordHeleper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 根据课程id删除课程播放表项
			 * @param context
			 * @param course_id
			 * @return
			 */
			public static int deleteCoursePlayCourseID(Context context, String course_id){
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID + "=?";
				String[] selectionArgs = new String[] {course_id};	
				return context.getContentResolver().delete(VopenCoursePlayRecordHeleper.getUri(),
															  selection,
															  selectionArgs);
			}
			/**
			 * 删除所有课程播放列表项
			 * @param context
			 * @return 
			 */
			public static int deleteAllCoursePlay(Context context){
				return context.getContentResolver().delete(VopenCoursePlayRecordHeleper.getUri(),null,null);
			}
		/*************************************************************
		 * 公开课视频播放表  
		 * table name :TABLE_VIDEO_PLAY_RECORD = "t_video_play_record"
		 *************************************************************/
		//该表数据字段
		public static class VideoPlayInfo{
	    	public  String mCourse_id;//课程ID
	    	public int mVideo_index;//视频 索引,第几课
	    	public int mVideo_count;//视频 总集数
	    	public int mPlay_position;//观看进度,存储大为:毫秒
	    	public int mVideo_length;//视频总长度,存储单位:秒
	    	public void readFromCursor(Cursor cursor) {
	    		// TODO Auto-generated method stub
	    		int index = -1;
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.COURSE_ID)) != -1) {
	            	mCourse_id = cursor.getString(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_INDEX)) != -1) {
	            	mVideo_index = cursor.getInt(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_COUNTS)) != -1) {
	            	mVideo_count = cursor.getInt(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.PLAY_POSITION)) != -1) {
	            	mPlay_position = cursor.getInt(index);
	                index = -1;
	            }
	            if ((index = cursor.getColumnIndex(VopenCoursePlayRecordHeleper.VIDEO_LENGTH)) != -1) {
	            	mVideo_length = cursor.getInt(index);
	                index = -1;
	            }
	    	}
		}
			/**
			 * 向公开课视频播放表 中插入一项
			 * @param context
			 * @param VideoPlayInfo
			 * @return 
			 */
			public static Uri insertVideoPlayInfo(Context context,VideoPlayInfo info) {
				if(null == info) {
					return null;
				}
				ContentValues initialValues = new ContentValues();
				
				if (null != info.mCourse_id) {
					initialValues.put(VopenCoursePlayRecordHeleper.COURSE_ID, info.mCourse_id);
				}
				initialValues.put(VopenCoursePlayRecordHeleper.VIDEO_INDEX, info.mVideo_index);
				initialValues.put(VopenCoursePlayRecordHeleper.VIDEO_COUNTS, info.mVideo_count);
				initialValues.put(VopenCoursePlayRecordHeleper.PLAY_POSITION, info.mPlay_position);
				initialValues.put(VopenCoursePlayRecordHeleper.VIDEO_LENGTH, info.mVideo_length);
				
			    return context.getContentResolver().insert(VopenVideoPlayRecordHeleper.getUri(), initialValues);
			}
			/**
			 * 更新公开课视频播放表中某一课程id和视频id所对应的数据
			 * @param context
			 * @param VideoPlayInfo
			 * @return 
			 */
			public static int updateVideoPlayInfo(Context context,VideoPlayInfo info) {
				if(null == info || null == info.mCourse_id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(VopenCoursePlayRecordHeleper.VIDEO_COUNTS, info.mVideo_count);
				values.put(VopenCoursePlayRecordHeleper.PLAY_POSITION, info.mPlay_position);
				values.put(VopenCoursePlayRecordHeleper.VIDEO_LENGTH, info.mVideo_length);
				
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID +  " =? " + " AND " +VopenCoursePlayRecordHeleper.VIDEO_INDEX + "=?" ;
				String[] selectionArgs = new String[] {info.mCourse_id,String.valueOf(info.mVideo_index)};	
				return context.getContentResolver().update(VopenVideoPlayRecordHeleper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 查询视频播放表中的项
			 * @param context
			 * @param selection
			 * @param projection
			 * @param selectionArgs
			 * @return Cursor
			 */
			public static Cursor queryVideoPlayInfo(Context context, String selection,String[] projection,String[] selectionArgs){
				Cursor c = context.getContentResolver().query(VopenVideoPlayRecordHeleper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 查询视频播放表中的所有项
			 * @param context
			 * @param sort
			 * @return Cursor
			 */
			public static Cursor queryVideoPlayAll(Context context,String sort,String[] projection){
				Cursor c = context.getContentResolver().query(VopenVideoPlayRecordHeleper.getUri(),
															  projection,
															  null,
															  null,
															  sort);
				return c;
			}
			
			/**
			 * 根据课程id查询视频播放表中对应的数据项
			 * @param context
			 * @param course_id 
			 * @return Cursor
			 */
			public static Cursor queryVideoPlayByCourseID(Context context, String course_id,String[] projection){
				if(null == course_id) {
					return null;
				}
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID + "=?" ;
				String[] selectionArgs = new String[] {course_id};	
				Cursor c = context.getContentResolver().query(VopenVideoPlayRecordHeleper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 根据课程id和视频id查询视频播放表中对应的数据项
			 * @param context
			 * @param course_id 
			 * @param video_id 
			 * @return Cursor
			 */
			public static Cursor queryVideoPlayByCourseIDAndVideoID(Context context, String[] projection,String course_id,int video_id){
				if(null == course_id) {
					return null;
				}
				
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID +  " =? " + " AND " +VopenCoursePlayRecordHeleper.VIDEO_INDEX + "=?" ;
				String[] selectionArgs = new String[] {course_id,String.valueOf(video_id)};	
				Cursor c = context.getContentResolver().query(VopenVideoPlayRecordHeleper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 根据课程id和视频id删除视频播放表项
			 * @param context
			 * @param course_id
			 * @param video_id 
			 * @return
			 */
			public static int deleteVideoPlayCourseIDAndVideoID(Context context, String course_id,int video_id){
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID +  " =? " + " AND " +VopenCoursePlayRecordHeleper.VIDEO_INDEX + "=?" ;
				String[] selectionArgs = new String[] {course_id,String.valueOf(video_id)};	
				return context.getContentResolver().delete(VopenVideoPlayRecordHeleper.getUri(),
															  selection,
															  selectionArgs);
			}
			/**
			 * 根据课程id删除视频播放表项
			 * @param context
			 * @param course_id
			 * @return
			 */
			public static int deleteVideoPlayCourseID(Context context, String course_id){
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				String selection = VopenCoursePlayRecordHeleper.COURSE_ID + "=?";
				String[] selectionArgs = new String[] {course_id};	
				return context.getContentResolver().delete(VopenVideoPlayRecordHeleper.getUri(),
															  selection,
															  selectionArgs);
			}
			/**
			 * 删除所有视频播放列表项
			 * @param context
			 * @return 
			 */
			public static int deleteAllVideoPlay(Context context){
				return context.getContentResolver().delete(VopenVideoPlayRecordHeleper.getUri(),null,null);
			}
		/*************************************************************
		 * 公开课下载管理表
		 * table name :TABLE_DOWNLOAD_MANAGER = "t_vopen_download_manager"
		 *************************************************************/
		//该表数据字段
		public static class DownLoadInfo{
			public String m_id;
	    	public String mCourse_id;//课程ID
	    	public int mCourse_pnumber;//课程第几集
	    	public String mDownload_url;//课程下载链接   	
	    	public String mCourse_name;//课程名字
	    	public String mCourse_thumbnail;//课程截图地址
	    	public EDownloadStatus mDownload_status;//课程下载状态 :  见EDownloadStatus定义
	    	public long mTotal_size;//课程总大小,单位byte
	    	public long mDownload_size;//课程已下载大小
	    	public boolean mIsSelected;//课程是否被选中: 未选中false，被选中true 
		}
		 public enum EDownloadStatus
		  {
	  		DOWNLOAD_DONE(1),//下载完成
	  		DOWNLOAD_WAITTING(2),//等待
	  		DOWNLOAD_DOING(3),//正在下载
	  		DOWNLOAD_FAILED(4),//网络错误
	  		DOWNLOAD_NO(5),//未下载
	  		DOWNLOAD_PAUSE(6),//暂停
	  		DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME(7),//SD卡不足
	  		DOWNLOAD_FAILED_VIDEO_ERROR(8);//视频错误
	  		
	  		private final int value;

			private EDownloadStatus(int value)
			{
				this.value = value;
			}

		    public int value()
		    {
		        return this.value;
		    }
		    
		    public static EDownloadStatus fromValue(int value){
		    	for (EDownloadStatus status : EDownloadStatus.values()){
		    		if (status.value == value){
		    			return status;
		    		}
		    	}
		    	return null;
		    }
		 
		  }
			/**
			 * 向公开课下载管理表 中插入一项
			 * @param context
			 * @param VideoPlayInfo
			 * @return 
			 */
			public static Uri insertDownloadInfo(Context context,DownLoadInfo info) {
				if(null == info) {
					return null;
				}
				ContentValues initialValues = new ContentValues();
				
				if (null != info.mCourse_id) {
					initialValues.put(DownloadManagerHelper.COURSE_PLID, info.mCourse_id);
				}
				initialValues.put(DownloadManagerHelper.COURSE_PNUMBER, info.mCourse_pnumber);
				if (null != info.mDownload_url) {
					initialValues.put(DownloadManagerHelper.DOWNLOAD_URL, info.mDownload_url);
				}
				if (null != info.mCourse_name) {
					initialValues.put(DownloadManagerHelper.COURSE_NAME, info.mCourse_name);
				}
				if (null != info.mCourse_thumbnail) {
					initialValues.put(DownloadManagerHelper.COURSE_THUMBNAIL, info.mCourse_thumbnail);
				}
				if(null != info.mDownload_status) {
					initialValues.put(DownloadManagerHelper.DOWNLOAD_STATUS, info.mDownload_status.value);
				}else{
					initialValues.put(DownloadManagerHelper.DOWNLOAD_STATUS, 2);
				}
				initialValues.put(DownloadManagerHelper.TOTAL_SIZE, info.mTotal_size);
				initialValues.put(DownloadManagerHelper.DOWNLOAD_SIZE, info.mDownload_size);
				initialValues.put(DownloadManagerHelper.SELECT_STATUS, info.mIsSelected?1:0);
				
			    return context.getContentResolver().insert(DownloadManagerHelper.getUri(), initialValues);
			}
			public static int insertAllItems(Context context, ContentValues[] values){
				return context.getContentResolver().bulkInsert(DownloadManagerHelper.getUri(), values);
			}
			/**
			 * 系统退出后，更新所有的下载状态为停止状态
			 * @param context
			 * @return
			 */
			public static int updateAllUpdate(Context context){
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, EDownloadStatus.DOWNLOAD_PAUSE.value());
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS + "<>?";
				String[] selectionArgs = new String[]{ String.valueOf(EDownloadStatus.DOWNLOAD_DONE.value)};
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * pad版系统退出后，更新正在下载及等待的为暂停
			 * @param context
			 * @return
			 */
			public static int updateDodingAndWaittingUpdate(Context context){
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, EDownloadStatus.DOWNLOAD_PAUSE.value());
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS + "=?" + " OR " + DownloadManagerHelper.DOWNLOAD_STATUS  + "=?";
				String[] selectionArgs = new String[]{ String.valueOf(EDownloadStatus.DOWNLOAD_DOING.value), String.valueOf(EDownloadStatus.DOWNLOAD_WAITTING.value)};
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 更新公开课下载管理表中某一课程id所对应的数据
			 * @param context
			 * @param DownloadInfo
			 * @return 
			 */
			public static int updateDownloadInfo(Context context,DownLoadInfo info) {
				if(null == info) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				if(info.mCourse_pnumber > 0){
					values.put(DownloadManagerHelper.COURSE_PNUMBER, info.mCourse_pnumber);
				}
				if (StringUtil.checkStr(info.mDownload_url)) {
					values.put(DownloadManagerHelper.DOWNLOAD_URL, info.mDownload_url);
				}
				if (StringUtil.checkStr(info.mCourse_name)) {
					values.put(DownloadManagerHelper.COURSE_NAME, info.mCourse_name);
				}
				if (StringUtil.checkStr(info.mCourse_thumbnail)) {
					values.put(DownloadManagerHelper.COURSE_THUMBNAIL, info.mCourse_thumbnail);
				}
				if(null != info.mDownload_status) {
					Log.v("DBApi", "info.mDownload_status.value = " + info.mDownload_status);
					values.put(DownloadManagerHelper.DOWNLOAD_STATUS, info.mDownload_status.value);
				}else{
					values.put(DownloadManagerHelper.DOWNLOAD_STATUS, EDownloadStatus.DOWNLOAD_WAITTING.value);
				}
				
				if(info.mTotal_size != 0){
					values.put(DownloadManagerHelper.TOTAL_SIZE, info.mTotal_size);
				}
				values.put(DownloadManagerHelper.DOWNLOAD_SIZE, info.mDownload_size);
				values.put(DownloadManagerHelper.SELECT_STATUS, info.mIsSelected?1:0);
				
//				String selection = DownloadManagerHelper.COURSE_PLID + "=?";
//				String[] selectionArgs = new String[] {info.mCourse_id};	
				String selection = DownloadManagerHelper.Download_ID + "=?";
				String[] selectionArgs = new String[] {info.m_id};
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 适用于pad版，删除下载后，更新数据库中的下载状态和已下载大小
			 * @param context
			 * @param info
			 * @return
			 */
			public static int updateDeleteDownloadInfo(Context context) {
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.SELECT_STATUS, 0);
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, EDownloadStatus.DOWNLOAD_NO.value());
				values.put(DownloadManagerHelper.DOWNLOAD_SIZE, 0);
				
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values, null, null);
			}
			
			/**
			 * 根据plid/pnum，更新数据库中需要下载的列表的状态
			 * @param context
			 * @param plid
			 * @param pnum
			 * @param flag
			 * @return
			 */
			public static int updateInitState(Context context, String plid, int pnum, boolean flag){
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, EDownloadStatus.DOWNLOAD_WAITTING.value());		
				values.put(DownloadManagerHelper.SELECT_STATUS, 0);
				if(flag) {
					//清空已下载的size
					values.put(DownloadManagerHelper.DOWNLOAD_SIZE, 0);
				}
		    	
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values, 
		    			DownloadManagerHelper.COURSE_PNUMBER+"=? and "+DownloadManagerHelper.COURSE_PLID+"=?",  
						new String[]{String.valueOf(pnum), plid});   

			}
			
			public static int updateInitStateBySelectedState(Context context, String plid, boolean flag){
				ContentValues value = new ContentValues();
				value.put(DownloadManagerHelper.DOWNLOAD_STATUS,
						EDownloadStatus.DOWNLOAD_WAITTING.value());
				value.put(DownloadManagerHelper.SELECT_STATUS, 0);
				// 清空已下载的size
				if(flag){
					value.put(DownloadManagerHelper.DOWNLOAD_SIZE, 0);
				}
				return context.getContentResolver()
						.update(DownloadManagerHelper.getUri(),value,
								DownloadManagerHelper.DOWNLOAD_STATUS + "="+ EDownloadStatus.DOWNLOAD_NO.value()
										+ " and "+ DownloadManagerHelper.SELECT_STATUS +"= 1" 
										+ " and " + DownloadManagerHelper.COURSE_PLID + "=?",
										new String[] { plid });
			}
			/**
			 * 根据课程id和集数更新下载状态
			 * @param context
			  *@param course_id
			 * @param pnumber
			 * @param EDownloadStatus
			 * @return 
			 */
			public static int updateDownloadStatus(Context context,String course_id,int pnumber,EDownloadStatus status) {
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, status.value);
				String selection = DownloadManagerHelper.COURSE_PLID + " =? " + " AND " +DownloadManagerHelper.COURSE_PNUMBER + "=?";
				String[] selectionArgs = new String[] {course_id,String.valueOf(pnumber)};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 根据id更新下载状态
			 * @param context
			  *@param course_id
			 * @param pnumber
			 * @param EDownloadStatus
			 * @return 
			 */
			public static int updateDownloadStatus(Context context,String id,EDownloadStatus status) {
				if(null == id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, status.value);
				String selection = DownloadManagerHelper.Download_ID + " =? ";
				String[] selectionArgs = new String[] {id};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 根据id更新下载大小
			 * @param context
			 * @param course_id
			 * @param pnumber
			 * @param down_size
			 * @return 
			 */
			public static int updateDownloadSize(Context context,String id,int down_size) {
				if(null == id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_SIZE, down_size);
				String selection = DownloadManagerHelper.Download_ID + " =? ";
				String[] selectionArgs = new String[] {id};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 根据id更新文件大小
			 * @param context
			 * @param course_id
			 * @param pnumber
			 * @param down_size
			 * @return 
			 */
			public static int updateTotalSize(Context context,String id,int total_size) {
				if(null == id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.TOTAL_SIZE, total_size);
				String selection = DownloadManagerHelper.Download_ID + " =? ";
				String[] selectionArgs = new String[] {id};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 根据课程id，更新下载状态及当前下载量
			 * @param context
			 * @param id
			 * @param down_Size
			 * @param status
			 * @return
			 */
			public static int updateFinishDownload(Context context, String id, int down_Size, EDownloadStatus status){
				if(null == id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_SIZE, down_Size);
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, status.value);
				String selection = DownloadManagerHelper.Download_ID + " =? ";
				String[] selectionArgs = new String[] {id};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			
			/**
			 * 更新下载任务的优先级为某个值。
			 * @param context
			 * @param id
			 * @param priority
			 * @return
			 */
			public static int updateDownloadPriorityById(Context context, String id, int priority){
				if (priority < 0 || id == null){
					return 0;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.PRIORITY, priority);
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, EDownloadStatus.DOWNLOAD_WAITTING.value);
				String selection = DownloadManagerHelper.Download_ID + " =? ";
				String[] selectionArgs = new String[] {id};
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			
			
			/**
			 * 根据课程id和集数更新选择状态
			 * @param context
			 * @param course_id
			 * @param pnumber
			 * @param select
			 * @return 
			 */
			public static int updateDownloadSelect(Context context,String course_id,int pnumber,boolean select) {
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.SELECT_STATUS, select?1:0);
				String selection = DownloadManagerHelper.COURSE_PLID + " =? " + " AND " +DownloadManagerHelper.COURSE_PNUMBER + "=?";
				String[] selectionArgs = new String[] {course_id,String.valueOf(pnumber)};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 根据id更新选择状态
			 * @param context
			 * @param id
			 * @param select
			 * @return
			 */
			public static int updateDownloadSelectById(Context context,String id,boolean select) {
				if(null == id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.SELECT_STATUS, select?1:0);
				String selection = DownloadManagerHelper.Download_ID + " =? " + " AND " + DownloadManagerHelper.DOWNLOAD_STATUS+"="+ + EDownloadStatus.DOWNLOAD_NO.value();
				String[] selectionArgs = new String[] {id};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 根据下载状态更新选择状态
			 * @param context
			 * @param id
			 * @param select
			 * @return
			 */
			public static int updateSelectedByDownloadStatus(Context context, boolean selected){
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.SELECT_STATUS, selected?1:0);
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values, 
			    		DownloadManagerHelper.DOWNLOAD_STATUS+"<>"+EDownloadStatus.DOWNLOAD_NO.value(), null);
			}
			/**
			 * 根据id更新选择和下载状态
			 * @param context
			 * @param id
			 * @param select
			 * @return
			 */
			public static int updateDownloadSelectandStatusById(Context context,String id,boolean confirm) {
				if(null == id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				
				values = new ContentValues();
				values.put(DownloadManagerHelper.DOWNLOAD_STATUS, EDownloadStatus.DOWNLOAD_WAITTING.value());		
				values.put(DownloadManagerHelper.SELECT_STATUS, 0);
				//清空已下载的size
				values.put(DownloadManagerHelper.DOWNLOAD_SIZE, 0);
				int itemp = confirm?1:0;
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS+"="+EDownloadStatus.DOWNLOAD_NO.value()
					+" and "+DownloadManagerHelper.SELECT_STATUS+"="+ itemp
					+" and "+DownloadManagerHelper.COURSE_PLID+"=?";
				String[] selectionArgs = new String[] {id};		
				return context.getContentResolver().update(DownloadManagerHelper.getUri(), values,selection, selectionArgs);
			}
			/**
			 * 根据课程id更新选择状态
			 * @param context
			 * @param id
			 * @param select
			 * @return
			 */
			public static int updateDownloadSelectByCourse(Context context,String course_id,boolean select) {
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				ContentValues values = new ContentValues();
				values.put(DownloadManagerHelper.SELECT_STATUS, select?1:0);
				return context.getContentResolver().update(DownloadManagerHelper.getUri(),
						values, 
		        		DownloadManagerHelper.DOWNLOAD_STATUS+"<>"+EDownloadStatus.DOWNLOAD_NO.value()
		        		+" and "+DownloadManagerHelper.COURSE_PLID+"=?", new String[]{course_id});
			}
			/**
			 * 查询下载管理表中的项
			 * @param context
			 * @param selection
			 * @param projection
			 * @param selectionArgs
			 * @return Cursor
			 */
			public static Cursor queryDownloadInfo(Context context, String selection,String[] projection,String[] selectionArgs){
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 查询下载管理表中的项,用户可以指定顺序
			 * @param context
			 * @param selection
			 * @param projection
			 * @param selectionArgs
			 * @return Cursor
			 */
			public static Cursor queryDownloadInfoByAsc(Context context, String selection,String[] projection,String[] selectionArgs, String strAsc){
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  strAsc);
				return c;
			}
			/**
			 * 查询下载管理表中的所有项
			 * @param context
			 * @param sort
			 * @return Cursor
			 */
			public static Cursor queryDownloadAll(Context context,String sort,String[] projection){
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
															  projection,
															  null,
															  null,
															  sort);
				return c;
			}
			
			/**
			 * 根据课程id查询下载管理表中对应的数据项
			 * @param context
			 * @param course_id 
			 * @return Cursor
			 */
			public static Cursor queryDownloadByCourseID(Context context, String course_id,String[] projection){
				if( null == course_id) {
					return null;
				}
				String selection = DownloadManagerHelper.COURSE_PLID + "=?" ;
				String[] selectionArgs = new String[] {course_id};	
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 根据课程id和集数查询下载管理表中对应的数据项
			 * @param context
			 * @param course_id 
			 * @param pnumber 
			 * @return Cursor
			 */
			public static Cursor queryDownloadByCourseIDAndPnumber(Context context, String course_id,int pnumber,String[] projection){
				if( null == course_id) {
					return null;
				}
				String selection = DownloadManagerHelper.COURSE_PLID + " =? " + " AND " +DownloadManagerHelper.COURSE_PNUMBER + "=?" ;
				String[] selectionArgs = new String[] {course_id,String.valueOf(pnumber)};	
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 根据下载id查询下载管理表中对应的数据项
			 * @param context
			 * @param plid 
			 * @param pnumber 
			 * @return Cursor
			 */
			public static Cursor queryDownloadById(Context context, String downloadId,String[] projection){
				if( null == downloadId) {
					return null;
				}
				String selection = DownloadManagerHelper.Download_ID + " =? ";
				String[] selectionArgs = new String[] {downloadId};	
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}
			/**
			 * 根据课程id删除下载管理表项
			 * @param context
			 * @param course_id
			 * @return
			 */
			public static int deleteDownloadCourseID(Context context, String course_id){
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				String selection = DownloadManagerHelper.COURSE_PLID + "=?";
				String[] selectionArgs = new String[] {course_id};	
				return context.getContentResolver().delete(DownloadManagerHelper.getUri(),
															  selection,
															  selectionArgs);
			}
			/**
			 * 根据课程id和集数删除下载管理表项
			 * @param context
			 * @param course_id
			 * @param pnumber 
			 * @return
			 */
			public static int deleteDownloadCourseIDAndPnumber(Context context, String course_id,int pnumber){
				if(null == course_id) {
					return DB_OPERATION_FAILED;
				}
				String selection = DownloadManagerHelper.COURSE_PLID + " =? " + " AND " +DownloadManagerHelper.COURSE_PNUMBER + "=?" ;
				String[] selectionArgs = new String[] {course_id,String.valueOf(pnumber)};
				return context.getContentResolver().delete(DownloadManagerHelper.getUri(),
															  selection,
															  selectionArgs);
			}
			/**
			 * 删除所有下载管理表项
			 * @param context
			 * @return 
			 */
			public static int deleteAllDownload(Context context){
				return context.getContentResolver().delete(DownloadManagerHelper.getUri(),null,null);
			}
			
			/**
			 * 删除数据库中指定课程的空记录
			 * @param context
			 * @param course_id
			 * @return
			 */
			public static int deleteDownloadEmptyRow(Context context, String course_id) {
				String selection = 
						DownloadManagerHelper.DOWNLOAD_STATUS + "="+ EDownloadStatus.DOWNLOAD_NO.value() 
						+ " and " + DownloadManagerHelper.COURSE_PLID + "= ?";
				String[] selectionArgs = new String[] { course_id };
				return context.getContentResolver().delete(
						DownloadManagerHelper.getUri(), selection, selectionArgs);
			}
			
//			public static Cursor queryDistinctDownloadedCourseId(Context context){
//			    VopenDatabaseHelper databaseHelper = VopenDatabaseHelper.getInstance(context);
//		        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//		        String sql = "select "+DownloadManagerHelper.COURSE_PLID + " from " + VopenContentProvider.TABLE_DOWNLOAD_MANAGER + " where download_status = 1 group by " + DownloadManagerHelper.COURSE_PLID + " order by _id desc";
//		        return  db.rawQuery(sql, null);
//			}
//			
//			public static Cursor queryDistinctDownloadingCourseId(Context context){
//                VopenDatabaseHelper databaseHelper = VopenDatabaseHelper.getInstance(context);
//                SQLiteDatabase db = databaseHelper.getWritableDatabase();
//                String sql = "select DISTINCT "+DownloadManagerHelper.COURSE_PLID + " from " + VopenContentProvider.TABLE_DOWNLOAD_MANAGER + " where download_status <> 1";
//                return  db.rawQuery(sql, null);
//            }
			
			/**
			 * 获得下载完成的视频
			 * @param context
			 * @return
			 */
			public static Cursor queryDownloadedAllCourse(Context context,String[] projection){
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS + " =? ";
				String[] selectionArgs = new String[] {String.valueOf(EDownloadStatus.DOWNLOAD_DONE.value)};
				String order = DownloadManagerHelper.Download_ID + " DESC";
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
						  projection,
						  selection,
						  selectionArgs,
						  order);
				return c;
			}
			/**
			 * 获得正在下载的视频
			 * @param context
			 * @return
			 */
			public static Cursor queryDownloadingAllCourse(Context context,String[] projection){
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS + " <>?";
				String[] selectionArgs = new String[] {String.valueOf(EDownloadStatus.DOWNLOAD_DONE.value)};
				String order = DownloadManagerHelper.Download_ID + " ASC";
				
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
						  projection,
						  selection,
						  selectionArgs,
						  order);
				return c;
			}
			
			/**
			 * 提供给下载服务来使用，查询数据库中一条状态是等待、优先级最高的记录。
			 * @param context
			 * @param projection
			 * @return
			 */
			public static Cursor queryDownloadNextTask(Context context,
					String[] projection) {
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS + " = ?";
				String[] selectionArgs = new String[] { String
						.valueOf(EDownloadStatus.DOWNLOAD_WAITTING.value) };
				String order = DownloadManagerHelper.PRIORITY + " DESC,"
						+ DownloadManagerHelper.Download_ID + " ASC"
						+ " LIMIT 1";
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
						  projection,
						  selection,
						  selectionArgs,
						  order);
				return c;
			}
			
			/**
			 * 获取当前下载任务的优先级 
			 * @param context
			 * @return
			 */
			public static Cursor queryDownloadCurrentTaskPriority(Context context){
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS + " = ?";
				String[] selectionArgs = new String[] { String
						.valueOf(EDownloadStatus.DOWNLOAD_DOING.value) };
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
						  new String[]{DownloadManagerHelper.PRIORITY},
						  selection,
						  selectionArgs,
						  null);
				return c;
			}
			
			
			/**
			 * pad版查询需要下载的视频
			 * @param context
			 * @return
			 */
			public static Cursor queryDodingAndWaittingVideo(Context context){
				String selection = DownloadManagerHelper.DOWNLOAD_STATUS + "=?" + " OR " + DownloadManagerHelper.DOWNLOAD_STATUS  + "=?";
				String[] selectionArgs = new String[]{ String.valueOf(EDownloadStatus.DOWNLOAD_DOING.value), String.valueOf(EDownloadStatus.DOWNLOAD_WAITTING.value)};
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(), null,selection, selectionArgs,null);
				return c;
			}
			
			/**
			 * 根据下载id查询下载管理表中对应的数据项
			 * 只选择为开始的数据
			 * @param context
			 * @param plid 
			 * @param pnumber 
			 * @return Cursor
			 */
			public static Cursor queryDownloadNoStartById(Context context, String downloadId,String[] projection){
				if( null == downloadId) {
					return null;
				}
				String selection = DownloadManagerHelper.Download_ID + " =? " + " And " + DownloadManagerHelper.DOWNLOAD_STATUS+"="+ + EDownloadStatus.DOWNLOAD_NO.value();
				String[] selectionArgs = new String[] {downloadId};	
				Cursor c = context.getContentResolver().query(DownloadManagerHelper.getUri(),
															  projection,
															  selection,
															  selectionArgs,
															  null);
				return c;
			}

}
