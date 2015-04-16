package vopen.transactions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vopen.db.DBApi;
import vopen.db.DBApi.DBCourseInfo;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.CourseInfo;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.netease.vopen.app.VopenApp;
import com.netease.vopen.pal.Constants;
import com.netease.vopen.pal.ErrorToString;
import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;

public class GetVideoListTransaction extends BaseTransaction {
	private static final String TAG = "GetVideoListTransaction";

	private long startTime;
	private String cacheFile;

	public GetVideoListTransaction(TransactionEngine transMgr, String cacheFile) {
		super(transMgr, TRANSACTION_TYPE_GET_VIDEO_LIST);
		startTime = System.currentTimeMillis();
		this.cacheFile = cacheFile;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		PalLog.d(TAG, "返回数据成功");
		long requestEnd = System.currentTimeMillis();
		PalLog.d(TAG, "数据返回共花费时间:" + (requestEnd - startTime));
		JsonReader reader = null;
		try {
			reader = parseAndSaveData();
			notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, null);
		} catch (Exception e) {
			PalLog.e(TAG, e.getMessage());
			notifyResponseError(VopenServiceCode.ERR_DATA_PARSE,
					ErrorToString.getString(VopenServiceCode.ERR_DATA_PARSE));
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		long endTime = System.currentTimeMillis();
		PalLog.d(TAG, "请求总共花费的时间:" + (endTime - startTime));
	}

	@Override
	public void onTransact() {
		if (!isCancel()) {
			HttpRequest httpRequest = VopenProtocol.getInstance()
					.createGetVListRequest(cacheFile);
			sendRequest(httpRequest);
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

	/**
	 * 解析并且保存数据
	 * 
	 * @return
	 * @throws Exception
	 */
	private JsonReader parseAndSaveData() throws Exception {
		JsonReader reader;
		List<DBCourseInfo> resultList = new ArrayList<DBCourseInfo>();
		long start = System.currentTimeMillis();
		File file = VopenApp.getAppInstance().getCacheListFilePath();
		FileReader fReader = new FileReader(file);
		reader = new JsonReader(fReader);
		reader.beginArray();
		DBApi.deleteAllCourse(VopenApp.getAppInstance());
		while (reader.hasNext()) {
//			PalLog.d(TAG, "读取一个课程信息");
			DBCourseInfo info = parseCourse(reader);
			if (info != null) {
				resultList.add(info);
			}
			// 500条一批插入数据库
			if (resultList.size() > 500) {
//				PalLog.d(TAG, "批量插入数据");
				DBApi.bulkInsertCourse(VopenApp.getAppInstance(), resultList);
				resultList.clear();
			}
		}
		if (resultList.size() > 0) {
//			PalLog.d(TAG, "批量插入数据");
			DBApi.bulkInsertCourse(VopenApp.getAppInstance(), resultList);
		}
		reader.endArray();
		PalLog.d(TAG, "解析和保存数据所用时间：" + (System.currentTimeMillis() - start));
		return reader;
	}

	/**
	 * 解析一个课程的数据
	 * 
	 * @param reader
	 * @return
	 */
	private DBCourseInfo parseCourse(JsonReader reader) {
		DBCourseInfo dInfo = null;
		try {
			reader.beginObject();
			CourseInfo cInfo = new CourseInfo();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("type")) {
					cInfo.type = reader.nextString();
				} else if (name.equals("ccPic")) {
					cInfo.ccPic = reader.nextString();
				} else if (name.equals("ccUrl")) {
					cInfo.ccUrl = reader.nextString();
				} else if (name.equals("description")) {
					cInfo.description = reader.nextString();
				} else if (name.equals("director")) {
					cInfo.director = reader.nextString();
				} else if (name.equals("hits")) {
					cInfo.hits = reader.nextLong();
				} else if (name.equals("include_virtual")) {
					cInfo.include_virtual = reader.nextString();
				} else if (name.equals("largeimgurl")) {
					cInfo.largeimgurl = reader.nextString();
				} else if (name.equals("ltime")) {
					cInfo.ltime = reader.nextLong();
				} else if (name.equals("playcount")) {
					cInfo.playcount = reader.nextInt();
				} else if (name.equals("plid")) {
					cInfo.plid = reader.nextString();
				} else if (name.equals("school")) {
					cInfo.school = reader.nextString();
				} else if (name.equals("source")) {
					cInfo.source = reader.nextString();
				} else if (name.equals("subtitle")) {
					cInfo.subtitle = reader.nextString();
				} else if (name.equals("tags")) {
					cInfo.tags = reader.nextString();
				} else if (name.equals("title")) {
					cInfo.title = reader.nextString();
				} else if (name.equals("type")) {
					cInfo.type = reader.nextString();
				} else if (name.equals("updated_playcount")) {
					cInfo.updated_playcount = reader.nextInt();
				} else if (name.equals("ipadPlayAdvInfo")) {
					JsonToken peek = reader.peek();
					if (peek == JsonToken.NULL) {
						reader.skipValue();
					} else {
						reader.beginObject();
						while (reader.hasNext()) {
							String n = reader.nextName();
							if (n.equals("advSource")) {
								cInfo.adSource = reader.nextInt();
							} else if (n.equals("advPreId")) {
								cInfo.adPreCategory = reader.nextString();
							} else if (n.equals("advMidId")) {
								cInfo.adMidCategory = reader.nextString();
							} else if (n.equals("advPosId")) {
								cInfo.adPostCategory = reader.nextString();
							} else {
								reader.skipValue();
							}
						}
						reader.endObject();
					}
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			if (Constants._TAG_head.equals(cInfo.type)
					|| Constants._TAG_hot.equals(cInfo.type)
					|| Constants._TAG_tuijian.equals(cInfo.type)) {
				dInfo = null;
			} else {
				dInfo = new DBCourseInfo();
				dInfo.mCourseId = cInfo.plid;
				dInfo.mCourseName = cInfo.title;
				dInfo.mCourseTag = cInfo.tags;
				dInfo.mCourseSrc = cInfo.source;
				dInfo.mCourseHitCount = cInfo.hits;
				dInfo.mCourseUpdateTime = cInfo.ltime;
				dInfo.mContent = cInfo.toJsonString();
			}
		} catch (Exception e) {
			PalLog.e(TAG, e.getMessage());
		}
		return dInfo;
	}

}
