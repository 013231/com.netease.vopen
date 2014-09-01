package vopen.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import vopen.app.BaseApplication;
import vopen.db.DBApi.EDownloadStatus;
import vopen.tools.FileUtils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.netease.vopen.pal.Constants;
import common.framework.http.AndroidHttpClient;
import common.framework.http.HttpUtils;
import common.pal.PalLog;
import common.util.SystemUtils;

public class DownloadUtils {
	public final static String Download_STATUS = "Download_STATUS";
	public final static String Download_ID = "Download_ID";
	public final static String Download_DOWN_SIZE = "Download_DOWN_SIZE";
	public final static String Download_TOTAL_SIZE = "Download_TOTAL_SIZE";
	public final static String Download_COMPLETED = "Download_COMPLETED";
	public final static String Download_ADD = "Download_ADD";
	public final static int Download_Thread_Count = 3;

	/**
	 * 下载完成后的文件后缀
	 */
	public static final String FILE_COMPLETED_POSTFIX = ".mp4";
	/**
	 * 未下载完成时的文件后缀
	 */
	public static final String FILE_NOT_COMPLETED_POSTFIX = ".mp4.nc";

	/**
	 * 下载完成后的字幕后缀
	 */
	public static final String SUBFILE_COMPLETED_POSTFIX = ".srt";
	/**
	 * 未下载完成时的字幕后缀
	 */
	public static final String SUBFILE_NOT_COMPLETED_POSTFIX = ".srt.nc";

	private static final int BUFFER_SIZE = 8 * 1024;

	/**
	 * 修改已下载完成的文件的后缀
	 * 
	 * @param plid
	 * @param pnum
	 * @throws InterruptedException 
	 */
	public static void renameCompletedFile(Context context, String plid,
			int pnum) throws InterruptedException {
		String oldName = FileUtils.getSavedDownloadVideoPath(context, plid,
				pnum, false);
		if (oldName != null && oldName.length() > 3) {
			String newName = oldName.substring(0, oldName.length() - 3);
			FileUtils.renameFile(newName, new File(oldName));
		}
	}

	/**
	 * 获取下载文件，若存在未完成的下载文件，则返回该未完成的文件
	 * 
	 * @param plid
	 * @param pnum
	 * @return
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static File getSaveFile(Context context, String plid, int pnum,
			boolean hasDownloadThreadInfo) throws IOException, InterruptedException {
		String ncFileStr = FileUtils.getSavedDownloadVideoPath(context, plid,
				pnum, false);
		if (!TextUtils.isEmpty(ncFileStr)) {
			return new File(ncFileStr);
		} else {
			// 未找到已下载文件,且有下载进度时，清空下载进度
			if (hasDownloadThreadInfo) {
				for (int i = 0; i < Constants.Download_Thread_Count; i++) {
					DownloadPrefHelper
							.recordDownload(context, plid, pnum, i, 0);
				}
				BaseApplication.initDownCurSize(0);
			}
			File fileSaveDir = new File(
					FileUtils.getSavingDownloadFileDir(context));
			if (!fileSaveDir.exists()) {
				fileSaveDir.mkdirs();
			}
			File saveFile = new File(fileSaveDir, plid + "_" + pnum
					+ DownloadUtils.FILE_NOT_COMPLETED_POSTFIX);/* 保存文件 */
			if (!saveFile.exists()) {
				saveFile.createNewFile();
			}
			return saveFile;
		}
	}

	/**
	 * 修改已下载完成的字幕的后缀
	 * 
	 * @param plid
	 * @param pnum
	 * @throws InterruptedException
	 */
	public static void renameCompleteSubFile(Context context, String plid,
			int pnum, int subIndex, String url) throws InterruptedException {
		String oldName = FileUtils.getSavedDownloadSubPath(context, plid, pnum,
				subIndex, url, false, false);
		if (oldName != null && oldName.length() > 3) {
			String newName = oldName.substring(0, oldName.length() - 3);
			FileUtils.renameFile(newName, new File(oldName));
		}
	}

	/**
	 * 创建字幕文件
	 * 
	 * @param plid
	 * @param pnum
	 * @return
	 * @throws IOException
	 */
	public static File getSubSaveFile(Context context, String plid, int pnum,
			int subIndex, String url) throws IOException {
		File fileSaveDir = new File(FileUtils.getSavingDownloadFileDir(context));
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdirs();
		}
		File saveFile = new File(fileSaveDir, plid + "_" + pnum + "_"
				+ subIndex + "_" + url.hashCode()
				+ DownloadUtils.SUBFILE_NOT_COMPLETED_POSTFIX);/* 保存文件 */
		if (!saveFile.exists()) {
			saveFile.createNewFile();
		}
		return saveFile;
	}

	/**
	 * 请求http下载
	 * 
	 * */
	public static HttpResponse httpDownload(HttpClient httpClient, String url,
			int startPos, int endPos, List<NameValuePair> params,
			String encoding) {
		Header[] headers = new Header[7];

		headers[0] = new BasicHeader(
				"Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, "
						+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, "
						+ "application/vnd.ms-powerpoint, application/msword, */*");
		headers[1] = new BasicHeader("Accept-Language", "zh-CN");
		headers[2] = new BasicHeader("Referer", url);
		headers[3] = new BasicHeader("Charset", "UTF-8");
		headers[4] = new BasicHeader("Range", "bytes=" + startPos + "-"
				+ endPos);
		headers[5] = new BasicHeader(
				"User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; "
						+ ".NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
		headers[6] = new BasicHeader("Connection", "Keep-Alive");

		return HttpUtils.doHttpExecute(httpClient, url, params, headers,
				HttpUtils.GET, encoding);
	}

	/**
	 * 获取文件大小
	 */
	public static int getHttpFileSize(Context context, String url,
			List<NameValuePair> params, String encoding) {
		int length = 0;
		AndroidHttpClient httpClient = HttpUtils.getAndroidHttpClient(context);
		try {
			Header[] headers = new Header[5];

			headers[0] = new BasicHeader(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, "
							+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, "
							+ "application/vnd.ms-powerpoint, application/msword, */*");
			headers[1] = new BasicHeader("Accept-Language", "zh-CN");
			headers[2] = new BasicHeader("Charset", "UTF-8");
			headers[3] = new BasicHeader(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; "
							+ ".NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			headers[4] = new BasicHeader("Connection", "Keep-Alive");

			HttpResponse response = HttpUtils.doHttpExecute(httpClient, url,
					params, headers, HttpUtils.GET, encoding);
			if (response == null)
				length = -2;// 网络错误
			if (response != null
					&& response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				length = (int) response.getEntity().getContentLength();
				// if(response.getFirstHeader("Content-Type").toString().contains("video")){
				// length = (int) response.getEntity().getContentLength();
				// }else{
				// //返回类型不为mp4，可能是需要认证重定向
				// length = -1;
				// }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			PalLog.i("DownloadUtils",
					"getHttpFileSize Exception " + e.toString());
			e.printStackTrace();
			length = -2;// 网络错误
		} finally {
			if (null != httpClient) {
				httpClient.close();
				httpClient = null;
			}
		}
		return length;
	}

	/**
	 * 下载成功或者失败时发送广播，通知更新下载状态
	 * 
	 * @param context
	 * @param status
	 * @param id
	 * @param down
	 * @param total
	 */
	public static void sendCommpleteBroadcast(Context context, int status,
			int id, int down, int total) {
		Intent completed = new Intent(DownloadUtils.Download_COMPLETED);
		Bundle bundle = new Bundle();
		bundle.putInt(DownloadUtils.Download_STATUS, status);
		bundle.putInt(DownloadUtils.Download_ID, id);
		bundle.putInt(DownloadUtils.Download_DOWN_SIZE, down);
		bundle.putInt(DownloadUtils.Download_TOTAL_SIZE, total);
		completed.putExtras(bundle);
		context.sendBroadcast(completed);
	}

	/**
	 * 请求http下载,不分段,用于下载 字幕
	 * 
	 * */
	public static HttpResponse httpDownload(HttpClient httpClient, String url,
			List<NameValuePair> params, String encoding) {
		Header[] headers = new Header[6];

		headers[0] = new BasicHeader(
				"Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, "
						+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, "
						+ "application/vnd.ms-powerpoint, application/msword, */*");
		headers[1] = new BasicHeader("Accept-Language", "zh-CN");
		headers[2] = new BasicHeader("Referer", url);
		headers[3] = new BasicHeader("Charset", "UTF-8");
		headers[4] = new BasicHeader(
				"User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; "
						+ ".NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
		headers[5] = new BasicHeader("Connection", "Keep-Alive");

		return HttpUtils.doHttpExecute(httpClient, url, params, headers,
				HttpUtils.GET, encoding);
	}

	/**
	 * 用于下载 字幕
	 * 
	 * @param context
	 * @param url
	 *            字幕的url
	 * @param id
	 * */
	public static EDownloadStatus downloadSubFile(Context context, String url,
			String plid, int pnum, int subIndex) {
		EDownloadStatus result = EDownloadStatus.DOWNLOAD_DONE;
		AndroidHttpClient httpClient = HttpUtils.getAndroidHttpClient(context);
		File saveFile = null;
		try {
			saveFile = DownloadUtils.getSubSaveFile(context, plid, pnum,
					subIndex, url);
			PalLog.v("DownloadUtils", "downloadSubFile url= " + url
					+ " saveFile = " + saveFile);
			HttpResponse response = DownloadUtils.httpDownload(httpClient, url,
					null, null);
			if (response != null
					&& response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null && entity.getContentLength() > 0) {
					InputStream inStream = entity.getContent();
					byte[] buffer = new byte[BUFFER_SIZE];
					int offset = 0;
					RandomAccessFile threadfile = new RandomAccessFile(
							saveFile, "rwd");
					threadfile.seek(0);
					while ((offset = inStream.read(buffer)) != -1) {
						threadfile.write(buffer, 0, offset);
						// Thread.sleep(10);
						if(Thread.interrupted()) {//如果被打断，那么直接返回
							result = EDownloadStatus.DOWNLOAD_FAILED;
							break;
						}
					}
					if (null != httpClient) {
						httpClient.close();
						httpClient = null;
					}
					threadfile.close();
					inStream.close();
				} else {
					result = EDownloadStatus.DOWNLOAD_FAILED;
				}
			} else {
				result = EDownloadStatus.DOWNLOAD_FAILED;
			}
			if (result.value() == EDownloadStatus.DOWNLOAD_DONE.value()) {
				DownloadUtils.renameCompleteSubFile(context, plid, pnum,
						subIndex, url);
			}
		} catch (IOException e) {
			PalLog.e("DownloadUtils", e.toString());
			long sdSize = saveFile == null ? 0 : SystemUtils
					.readSDCardRemainSize(saveFile.getAbsolutePath());
			if (sdSize < 10000) {// 小于10K时认为没有足够空间
				// ENOSPC (No space left on device)
				result = EDownloadStatus.DOWNLOAD_FAILED_NOT_ENOUGH_SDCARD_VOLUME;
			} else {
				result = EDownloadStatus.DOWNLOAD_FAILED;
			}
		} catch (InterruptedException e) {
			PalLog.e("DownloadUtils", e.toString());
			result = EDownloadStatus.DOWNLOAD_PAUSE;
		} catch (Exception e) {
			PalLog.e("DownloadUtils", e.toString());
			result = EDownloadStatus.DOWNLOAD_FAILED;
		} finally {
			if (null != httpClient) {
				httpClient.close();
				httpClient = null;
			}
		}
		return result;
	}
}
