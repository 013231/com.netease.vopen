package common.pal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.os.Environment;
import android.util.Log;

import common.util.Util;

/**
 * NeteaseLog 日志工具 1.添加日志输出选项.控制日志输出位置 
 * 2.添加文件日志功能.(因进程问题.现UI与Service只能打到不同的文件中)
 * 3.控制单个日志文件最大限制.由LOG_MAXSIZE常量控制,保留两个最新日志文件
 *  4.文件日志输出目标
 * /data/data/%packetname%/files/
 * 
 * @author wangrongqiang@163.com
 * @data 2010-5-26
 */
public class PalLog {
	public static final String TAG = "NeteaseLog";

	public static boolean DEBUG = true;

	public static final int TO_CONSOLE = 0x1;
	public static final int TO_SCREEN = 0x10;
	public static final int TO_FILE = 0x100;
	public static final int FROM_LOGCAT = 0x1000;

	public static final int DEBUG_ALL = TO_CONSOLE | TO_SCREEN | TO_FILE /*| FROM_LOGCAT*/;

	private static final String LOG_TEMP_FILE = "netease_log.temp";
	private static final String LOG_LAST_FILE = "netease_log_last.txt";
	private static final String LOG_NOW_FILE = "netease_log_now.txt";

	private static final int LOG_LEVEL = Log.VERBOSE;

	private static final int LOG_MAXSIZE = 1024 * 1024; // double the size
														// temporarily

	static String mAppPath;
	static String mLogFilePrefix;

	private static Object lockObj = new Object();

	public static final int CHATSESSION_FORM = 1;

	public static final int CHATSESSION_TO = 2;

	/**
	 * log文件路径
	 */
	private static final String LOG_PATH = "/data/data/%packetname%/files/";

	public static PaintLogThread mPaintLogThread = null;

	public static void NeteaseLogIni(String packetName, String filePrefix) {
		if (getAppPath() == null)
			iniAppPath(packetName, filePrefix);
	}

	public static String getAppPath() {
		if (mAppPath != null) {
			PalLog.d("mAppPath lockObj:",
						 "" + lockObj.hashCode());
			PalLog.d("mAppPath hashCode:",
						 "" + mAppPath.hashCode());
		}
		return mAppPath;
	}

	public static void iniAppPath(String packetName, String filePrefix) {
		synchronized (lockObj) {
			mAppPath = LOG_PATH.replaceFirst("%packetname%", packetName);
			mLogFilePrefix = filePrefix;
			Log.d("iniAppPath", mAppPath);

			File dir = new File(mAppPath);
			if (!dir.exists()) {
				dir.mkdir();
			}
		}

	}

	/**
	 * 获得log文件
	 * 
	 * @return
	 */
	public static String[] getLogFiles() {

		if (mAppPath != null && mAppPath.length() > 0) {

			Vector<String> v = new Vector<String>();

			File file = openAbsoluteFile(LOG_LAST_FILE);
			if (file.exists()) {
				v.addElement(file.getAbsolutePath());
			}

			file = openAbsoluteFile(LOG_NOW_FILE);
			if (file.exists()) {
				v.addElement(file.getAbsolutePath());
			}
			if (v.size() > 0) {
				String[] paths = new String[v.size()];
				v.copyInto(paths);
				return paths;
			}
		}
		return null;
	}

	public static void log(String msg) {
		d("CloudAlbum", msg);
	}

	public static void d(String tag, String msg) {
		log(tag, msg, DEBUG_ALL, Log.DEBUG);
	}

	public static void v(String tag, String msg) {
		log(tag, msg, DEBUG_ALL, Log.VERBOSE);
	}

	public static void e(String tag, String msg) {
		log(tag, msg, DEBUG_ALL, Log.ERROR);
	}

	public static void i(String tag, String msg) {
		log(tag, msg, DEBUG_ALL, Log.INFO);
	}

	public static void w(String tag, String msg) {
		log(tag, msg, DEBUG_ALL, Log.WARN);
	}

	protected static void log(String tag, String msg, int outdest, int level) {
		if (tag == null)
			tag = "TAG_NULL";
		if (msg == null)
			msg = "MSG_NULL";

		if (level >= LOG_LEVEL) {

			if ((outdest & TO_CONSOLE) != 0) {
				LogToConsole(tag, msg, level);
			}

			if ((outdest & TO_SCREEN) != 0) {
				LogToScreen(tag, msg, level);
			}

			if ((outdest & FROM_LOGCAT) != 0) {

				 if(mPaintLogThread == null){
									
    				 PalLog log = new PalLog();
    				 mPaintLogThread = log.new PaintLogThread();
    				 mPaintLogThread.start();
				 }

			}

			if ((outdest & TO_FILE) != 0) {
				LogToFile(tag, msg, level);
			}
		}

	}

	static Calendar mDate = Calendar.getInstance();
	static StringBuffer mBuffer = new StringBuffer();

	/**
	 * 组成Log字符串.添加时间信息.
	 * 
	 * @param tag
	 * @param msg
	 * @return
	 */
	private static String getLogStr(String tag, String msg) {

		mDate.setTimeInMillis(System.currentTimeMillis());

		mBuffer.setLength(0);
		mBuffer.append("[");
		mBuffer.append(tag);
		mBuffer.append(" : ");
		mBuffer.append(mDate.get(Calendar.MONTH) + 1);
		mBuffer.append("-");
		mBuffer.append(mDate.get(Calendar.DATE));
		mBuffer.append(" ");
		mBuffer.append(mDate.get(Calendar.HOUR_OF_DAY));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.MINUTE));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.SECOND));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.MILLISECOND));
		mBuffer.append("] ");
		mBuffer.append(msg);

		return mBuffer.toString();
	}

	/**
	 * 将log打到控制台
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 */
	private static void LogToConsole(String tag, String msg, int level) {
		switch (level) {
		case Log.DEBUG:
			Log.d(tag, msg);
			break;
		case Log.ERROR:
			Log.e(tag, msg);
			break;
		case Log.INFO:
			Log.i(tag, msg);
			break;
		case Log.VERBOSE:
			Log.v(tag, msg);
			break;
		case Log.WARN:
			Log.w(tag, msg);
			break;
		default:
			break;
		}
	}

	/**
	 * 将log打到文件日志
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 */
	private static void LogToFile(String tag, String msg, int level) {
		synchronized (lockObj) {
			OutputStream outStream = openLogFileOutStream();

			if (outStream != null) {
				try {
					byte[] d = getLogStr(tag, msg).getBytes("utf-8");
					// byte[] d = msg.getBytes("utf-8");
					if (mFileSize < LOG_MAXSIZE) {
						outStream.write(d);
						outStream.write("\r\n".getBytes());
						outStream.flush();
						mFileSize += d.length;

					} else {
						closeLogFileOutStream();
						renameLogFile();
						LogToFile(tag, msg, level);
					}

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

			} else {
				Log.w("NeteaseLog", "Log File open fail: [AppPath]=" + mAppPath
						+ ",[LogName]:" + mLogFilePrefix);
			}
		}
	}

	private static void LogToScreen(String tag, String msg, int level) {

	}

	/**
	 * back now log file
	 */
	public static void backLogFile() {
		synchronized (lockObj) {
			try {
				closeLogFileOutStream();

				File destFile = openAbsoluteFile(LOG_NOW_FILE);

				if (destFile.exists()) {
					destFile.delete();
				}

				try {
					destFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}

				File srcFile1 = openAbsoluteFile(LOG_LAST_FILE);
				File srcFile2 = openAbsoluteFile(LOG_TEMP_FILE);

				copyFile(srcFile1, srcFile2, destFile, true);
				openLogFileOutStream();

			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
				Log.w("NeteaseLog", "backLogFile fail:" + e.toString());
			}
		}
	}

	static OutputStream mLogStream;
	static long mFileSize;

	/**
	 * 获取日志临时文件输入流
	 * 
	 * @return
	 */
	private static OutputStream openLogFileOutStream() {
		if (mLogStream == null) {
			try {
				if (mAppPath == null || mAppPath.length() == 0) {
					return null;
					// mAppPath="/data/data/com.netease.rpmms/files/";
				}

				File file = openAbsoluteFile(LOG_TEMP_FILE);

				if (file == null) {
					return null;
				}

				if (file.exists()) {
					mLogStream = new FileOutputStream(file, true);
					mFileSize = file.length();
				} else {
					// file.createNewFile();
					mLogStream = new FileOutputStream(file);
					mFileSize = 0;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return mLogStream;
	}

	public static File openAbsoluteFile(String name) {

		// Context context = AndroidSystemService.getInstance()
		// .getContext();
		// File file = context.getFileStreamPath(LOG_TEMP_FILE);

		if (mAppPath == null || mAppPath.length() == 0) {
			i("PalLog", "openAbsoluteFile mAppPath invalid");
			return null;
		} else {
			File file = new File(mAppPath + mLogFilePrefix + "_" + name);
			return file;
		}
	}
	
	public static File copyAbsoluteFileForShare(String name) {
		File sourceFile = openAbsoluteFile(name);
		if(sourceFile == null)
			return null;
			
		File destFile;
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File directory = Environment.getExternalStorageDirectory();
			destFile = new File(directory, name);
		}
		else{
			destFile = new File("/system/" + mLogFilePrefix + "_" + name );
		}
		
		if(Util.copyFile(sourceFile, destFile))
			return destFile;
		else
			return null;
	}

	/**
	 * 关闭日志输出流
	 */
	private static void closeLogFileOutStream() {
		try {
			if (mLogStream != null) {
				mLogStream.close();
				mLogStream = null;
				mFileSize = 0;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 文件复制
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	private static void copyFile(File src, File dest, boolean destAppend)
			throws IOException {

		if (dest.exists()) {
			dest.delete();
		}
		long total = src.length();
		FileOutputStream out = new FileOutputStream(dest, destAppend);
		FileInputStream in = new FileInputStream(src);
		long count = 0;
		byte[] temp = new byte[1024 * 10];
		while (count < total) {
			int size = in.read(temp);
			out.write(temp, 0, size);
			count += size;
		}
		in.close();
		in = null;
		out.close();
		out = null;

	}

	private static void copyFile(File src1, File src2, File dest,
								 boolean destAppend) throws IOException {

		if (dest.exists()) {
			dest.delete();
		}
		long total = 0;
		long count = 0;
		FileInputStream in = null;
		FileOutputStream out = new FileOutputStream(dest);
		byte[] temp = new byte[1024 * 10];

		if (src1.exists()) {
			total = src1.length();
			in = new FileInputStream(src1);
			while (count < total) {
				int size = in.read(temp);
				out.write(temp, 0, size);
				count += size;
			}
			in.close();
		}

		if (src2.exists()) {
			count = 0;
			total = src2.length();
			in = new FileInputStream(src2);
			while (count < total) {
				int size = in.read(temp);
				out.write(temp, 0, size);
				count += size;
			}
			in.close();
		}

		in = null;
		out.close();
		out = null;

	}

	/**
	 * rename log file
	 */
	private static void renameLogFile() {

		synchronized (lockObj) {

			File file = openAbsoluteFile(LOG_TEMP_FILE);
			File destFile = openAbsoluteFile(LOG_LAST_FILE);

			if (destFile.exists()) {
				destFile.delete();
			}
			file.renameTo(destFile);
		}
	}

	public static boolean zipLogFile(String zipFileName) {
		// backup ui log file
		backLogFile();

		File destFile = openAbsoluteFile(zipFileName);
		if (destFile.exists()) {
			destFile.delete();
		}

		try {
			destFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		File srcFile = openAbsoluteFile(LOG_NOW_FILE);
		boolean ret = zip(srcFile, destFile);

		// File srcFile = openAbsoluteFile("test.txt");
		// AndroidUtil.unZip(destFile, srcFile);

		destFile = null;
		if (null != srcFile && srcFile.exists()) {
			srcFile.delete();
		}
		srcFile = null;
		return ret;
	}

	public static void start() {
		synchronized (lockObj) {
			if (mPaintLogThread == null) {

				PalLog log = new PalLog();
				mPaintLogThread = log.new PaintLogThread();
				mPaintLogThread.start();
			}
		}
	}

	public static void close() {

		if (mPaintLogThread != null) {
			mPaintLogThread.shutdown();
			mPaintLogThread = null;
		}
	}

	class PaintLogThread extends Thread {

		Process mProcess;
		boolean mStop = false;

		public void shutdown() {
			PalLog.i("PaintLogThread:", "shutdown");
			mStop = true;
			if (mProcess != null) {
				mProcess.destroy();
				mProcess = null;
			}
		}

		public void run() {
			// TODO Auto-generated method stub
			try {

				PalLog.i("PaintLogThread:", "start");
				ArrayList<String> commandLine = new ArrayList<String>();
				commandLine.add("logcat");
				// commandLine.add( "-d");

				commandLine.add("-v");
				commandLine.add("time");

				// commandLine.add( "-s");
				// commandLine.add( "tag:W");
				// commandLine.add( "-f");
				// commandLine.add("/sdcard/log.txt");

				mProcess = Runtime.getRuntime()
								  .exec(commandLine.toArray(new String[commandLine.size()]));

				BufferedReader bufferedReader = new BufferedReader
				// ( new InputStreamReader(mProcess.getInputStream()), 1024);
				(new InputStreamReader(mProcess.getInputStream()));

				String line = null;
				while (!mStop) {
					line = bufferedReader.readLine();
					if (line != null && mAppPath != null) {
						LogToFile("SysLog", line, Log.VERBOSE);
					} else {
						if (line == null) {
							Log.i("PaintLogThread:", "readLine==null");
							break;
							// Log.i("PaintLogThread:","PaintLogThread sleep 1000second"
							// );
							// Thread.sleep(1000);
						}
						// Thread.sleep(1000);

					}
				}

				bufferedReader.close();
				if (mProcess != null)
					mProcess.destroy();
				mProcess = null;
				mPaintLogThread = null;
				Log.i("PaintLogThread:", "end PaintLogThread:");

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				Log.d("NeteaseLog", "logcatToFile Exception:" + e.toString());
			}
		}
	}

	private static boolean zip(File unZip, File zip) {
		if (!unZip.exists())
			return false;
		if (!zip.getParentFile().exists())
			zip.getParentFile().mkdir();

		try {
			FileInputStream in = new FileInputStream(unZip);
			FileOutputStream out = new FileOutputStream(zip);

			ZipOutputStream zipOut = new ZipOutputStream(out);

			// for buffer
			byte[] buf = new byte[1024];

			int readCnt = 0;

			zipOut.putNextEntry(new ZipEntry(unZip.getName()));
			while ((readCnt = in.read(buf)) > 0) {
				zipOut.write(buf, 0, readCnt);
			}
			zipOut.closeEntry();

			in.close();
			zipOut.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

/******************************************************************
 * for service log function
 */
	public static final boolean DEBUG_TO_SCREEN = false;
	public static final int DEBUG_LEVEL_3 = 3;
	public static final int DEBUG_LEVEL_2 = 2;
	public static final int DEBUG_LEVEL_1 = 1;
	public static final int debug_level = 0;
	public static final int DEBUG_LEVEL_PROTOCOL = 0;
	
	public static void append(String log){
		log(log);
	}
	
	public static void append(String log, int level){
		log(log);
	}
}
