package vopen.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import vopen.download.DownloadUtils;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.netease.vopen.pal.Constants;

public class FileUtils {

	public static String DownloadPath = Environment
			.getExternalStorageDirectory() + "/netease/vopen/download/";
	public static final String datafilepath = "/sdcard/netease/vopen/datafile/";
	public static final String datafilename = "V1OPEN6TEMP3";

	public static long getFileSizes(File f) throws Exception {// 取得文件大小
		long s = 0;
		if (f.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(f);
			s = fis.available();
		} else {
			f.createNewFile();
			System.out.println("文件不存！");
		}
		return s;
	}

	// 递归
	public static long getFileSize(File f) throws Exception// 取得文件夹大
	{
		long size = 0;
		if (!f.exists())
			return size;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	public static String FormetFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = "0K";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/** sdcard是否可用 */
	public static boolean isSDCardMounted() {
		String sDStateString = android.os.Environment.getExternalStorageState();
		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/** sdcard是否只读 */
	public static boolean isSDCardMountedReadOnly() {
		String sDStateString = android.os.Environment.getExternalStorageState();
		if (sDStateString
				.equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isFileExit(String filename) {
		boolean s = false;
		try {
			File file = new File(filename);
			s = file.exists();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	public static boolean autoDeleteCacheFile(File file, long expiredDuration) {
		if (isSDCardMounted()) {
			if (file.exists() && file.isDirectory()) {
				try {
					File[] files = file.listFiles();
					for (int i = 0; files != null && i < files.length; i++) {
						if (files[i].isFile()
								&& System.currentTimeMillis()
										- files[i].lastModified() >= expiredDuration) {
							files[i].delete();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				return true;
			} else {
				return true;
			}
		} else
			return false;

	}

	public static boolean createFolder(String filepath) {

		File file = new File(filepath);

		if ((isSDCardMounted() || isSDCardMountedReadOnly()) && file.exists()) {
			return true;
		} else {
			if (isSDCardMounted()) {
				return file.mkdirs();
			} else
				return false;

		}
	}

	public static void delFile(String strFileName) {
		if (isSDCardMounted()) {
			try {
				File myFile = new File(strFileName);
				if (myFile.exists()) {
					myFile.delete();
				}
			} catch (Exception e) {
			}
		}

	}

	public static void writeDatatoFile(String rst) {
		if (isSDCardMounted()) {

			String realfilename = datafilepath + datafilename;

			if (createFolder(datafilepath)) {

				if (isFileExit(realfilename)) {
					delFile(realfilename);
				}

				try {

					BufferedWriter output = new BufferedWriter(new FileWriter(
							realfilename));
					output.write(rst);
					output.close();

					Log.i("FileUtils", "write data to file ");
				} catch (Exception e) {

				}

			}

		}
	}

	public static void writeVDetailtoFile(String rst, String plid) {
		if (isSDCardMounted()) {

			String realfilename = datafilepath + plid;

			if (createFolder(datafilepath)) {

				if (isFileExit(realfilename)) {
					delFile(realfilename);
				}

				try {

					BufferedWriter output = new BufferedWriter(new FileWriter(
							realfilename));
					output.write(rst);
					output.close();

				} catch (Exception e) {

				}

			}

		}
	}

	public static String readDatatoFile(String filepath, String filename) {

		String realfilename = filepath + filename;
		if (isSDCardMounted() || isSDCardMountedReadOnly()) {
			if (isFileExit(realfilename)) {

				StringBuffer sb = new StringBuffer();
				FileInputStream fi = null;
				InputStreamReader isr = null;
				BufferedReader br = null;
				try {
					fi = new FileInputStream(realfilename);
					isr = new InputStreamReader(fi);
					br = new BufferedReader(isr);
					String line = "";
					while ((line = br.readLine()) != null) {

						sb.append(line);
					}

				} catch (Exception e) {

				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {

						}
					}
				}

				return sb.toString();

			} else {
				return "";
			}

		} else {
			return "";
		}
	}

	public static HashMap<String, Long> getSDCardInfo() {

		HashMap<String, Long> map = null;

		String sDcString = android.os.Environment.getExternalStorageState();
		if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 取得sdcard文件路径
			File pathFile = android.os.Environment
					.getExternalStorageDirectory();
			android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
			// 获取SDCard上BLOCK总数
			long nTotalBlocks = statfs.getBlockCount();
			// 获取SDCard上每个block的SIZE
			long nBlocSize = statfs.getBlockSize();
			// 获取可供程序使用的Block的数量
			long nAvailaBlock = statfs.getAvailableBlocks();
			// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
			long nFreeBlock = statfs.getFreeBlocks();
			// 计算SDCard 总容量大小MB
			long nSDTotalSize = nTotalBlocks * nBlocSize / 1024 / 1024;
			// 计算 SDCard 剩余大小MB
			long nSDFreeSize = nAvailaBlock * nBlocSize / 1024 / 1024;
			map = new HashMap<String, Long>();

			map.put("totalsize", nSDTotalSize);
			map.put("freesize", nSDFreeSize);
		}
		return map;

	}

	/**
	 * 删除目录或者文件
	 * 
	 * @param path
	 * @return
	 */
	public static void delete(String path) {
		delete(new File(path));
	}

	/**
	 * 删除目录或者文件
	 * 
	 * @param file
	 */
	public static void delete(File file) {
		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				File[] subs = file.listFiles();
				if (subs != null) {
					for (File sub : subs) {
						delete(sub.toString());
					}
				}
			} else {
				deleteFile(file);
			}
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param path
	 */
	public static void deleteFile(String path) {
		delete(new File(path));
	}

	/**
	 * 删除单个文件
	 * 
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (file != null && file.exists()) {
			file.delete();
		}
	}

	/**
	 * 创建文件<br/>
	 * 如果原来文件存在,先删除
	 * 
	 * @param path
	 */
	public static void createFile(String path) {
		createFile(new File(path));

	}

	/**
	 * 创建文件<br/>
	 * 如果原来文件存在,先删除
	 * 
	 * @param file
	 */
	public static void createFile(File file) {
		if (file.exists()) {
			file.delete();
		}

		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
		}

	}

	/**
	 * 重命名
	 * 
	 * @param newName
	 * @param file
	 * @return
	 */
	public static boolean renameFile(String newName, File file) {
		if (file != null && file.exists()) {
			File newFile = new File(newName);
			if (newFile.exists()) {
				newFile.delete();
			}

			return file.renameTo(newFile);
		}
		return false;
	}

	/**
	 * 拷贝文件
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 */
	public static boolean copyFile(File srcFile, File destFile) {
		boolean result = false;
		try {
			InputStream in = new FileInputStream(srcFile);
			try {
				result = copyToFile(in, destFile);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	/**
	 * 拷贝文件
	 * 
	 * @param inputStream
	 * @param destFile
	 * @return
	 */
	public static boolean copyToFile(InputStream inputStream, File destFile) {
		try {
			if (destFile.exists()) {
				destFile.delete();
			}
			OutputStream out = new FileOutputStream(destFile);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

//	public static int DOWNLOAD_FILE_TYPE_VIDEO = 0;
//	public static int DOWNLOAD_FILE_TYPE_SUB_1 = 1;
//	public static int DOWNLOAD_FILE_TYPE_SUB_2 = 2;

	public static String getSavedDownloadVideoPath(Context context, String courseId,
			int videoIndex, boolean isComplete) {
		List<File> list = getWritableSDs(context);
		String suffix = getVideoSuffix(isComplete);
		for (int i = 0; i < list.size(); i++) {
			File sdRoot = list.get(i);
			String filePath = String.format(sdRoot.getAbsolutePath()
					+ Constants.FILE_APP_DOWNLOAD + "%s_%d" + suffix, courseId,
					videoIndex);
			if (isFileExit(filePath))
				return filePath;
		}
		return "";
	}

	/**
	 * 获取已经下载的字幕文件
	 * 
	 * @param courseId
	 * @param videoIndex
	 * @param subType 字幕1、字幕2
	 * @param url 字幕url地址
	 * @param isComplete
	 * @param fuzzyMatching 模糊匹配，是否返回课程相同，但url地址已改变的字幕
	 * @return
	 */
	public static String getSavedDownloadSubPath(Context context, String courseId,
			int videoIndex, int subType, String url, boolean isComplete, boolean fuzzyMatching) {
		List<File> list = getWritableSDs(context);
		String suffix = getSubSuffix(isComplete);
		int urlHash = url.hashCode();
		for (int i = 0; i < list.size(); i++) {
			File sdRoot = list.get(i);
			String subPathWithOutUrl = String.format("%s_%d_%d", courseId,
					videoIndex, subType);
			String subPath = String.format(sdRoot.getAbsolutePath()
					+ Constants.FILE_APP_DOWNLOAD + "%s_%d_%d_%d" + suffix,
					courseId, videoIndex, subType, urlHash);
			if (isFileExit(subPath)) {
				return subPath;// 直接返回完全匹配的字幕文件
			} else if(fuzzyMatching){
				File downloadDir = new File(sdRoot.getAbsolutePath()
						+ Constants.FILE_APP_DOWNLOAD);
				if (downloadDir != null && downloadDir.exists()
						&& downloadDir.isDirectory()) {
					String[] subs = downloadDir.list(new MySubFilter(
							subPathWithOutUrl, suffix));
					if (subs != null && subs.length != 0) {
						return downloadDir + "/" + subs[0];// 返回url地址不匹配但课程匹配的字幕文件(字幕文件已过期)
					}
				}
			}
		}
		return "";
	}
	
	/**
	 * 删除该课程所有字幕，完成/未完成，包含url/不包含url
	 * @param courseId
	 * @param videoId
	 */
	public static void deleteSubFile(Context context, String courseId, int videoId){
		List<File> list = getWritableSDs(context);
		for (int i = 0; i < list.size(); i++) {
			File sdRoot = list.get(i);
			String filePathWithOutUrl = String.format("%s_%d", courseId,
					videoId);
			File downloadDir = new File(sdRoot.getAbsolutePath()
					+ Constants.FILE_APP_DOWNLOAD);
			if (downloadDir != null && downloadDir.exists()
					&& downloadDir.isDirectory()) {
				String[] subs = downloadDir.list(new MySubFilter(
						filePathWithOutUrl, DownloadUtils.SUBFILE_COMPLETED_POSTFIX));
				if (subs != null) {
					for(String sub : subs){
						FileUtils.delete(downloadDir.getAbsolutePath() + "/" + sub);
					}
				}
				subs = downloadDir.list(new MySubFilter(
						filePathWithOutUrl, DownloadUtils.SUBFILE_NOT_COMPLETED_POSTFIX));
				if (subs != null) {
					for(String sub : subs){
						FileUtils.delete(sub);
					}
				}
			}
		}
	}

	private static class MySubFilter implements FilenameFilter {

		private String mSubName;
		private String mSuffix;

		public MySubFilter(String subNameWithOutUrl, String suffix) {
			mSubName = subNameWithOutUrl;
			mSuffix = suffix;
		}

		@Override
		public boolean accept(File dir, String filename) {
			if (filename.startsWith(mSubName) && filename.endsWith(mSuffix)) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static String getSavingDownloadVideoPath(Context context,
			String courseId, int videoIndex, boolean isComplete) {
		String suffix = getVideoSuffix(isComplete);
		String filePath = String.format(getSavingDownloadFileDir(context)
				+ "%s_%d" + suffix, courseId, videoIndex);
		return filePath;
	}
	
	public static String getSavingDownloadSubPath(Context context,
			String courseId, int videoIndex, int subType, String url,
			boolean isComplete) {
		String suffix = getSubSuffix(isComplete);
		String filePath = String.format(getSavingDownloadFileDir(context)
				+ "%s_%d_%d" + suffix, courseId, videoIndex, url.hashCode());
		return filePath;
	}

	public static String getSavingDownloadFileDir(Context context) {
		return getSavingRoot(context) + Constants.FILE_APP_DOWNLOAD;
	}

	public static String getSavingRoot(Context context) {
		String savingRoot = Constants.getDownloadSaveRoot(context);
		File root = new File(savingRoot);
		if (root.canWrite()) {
			return savingRoot;
		} else {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
	}

	public static boolean isSameDir(File f1, File f2) {
		if (f1 == f2)
			return true;
		if (f1 == null || f2 == null)
			return false;
		if (f1.equals(f2)) {
			return true;
		} else {
			File[] childFiles1 = f1.listFiles();
			File[] childFiles2 = f2.listFiles();
			if (childFiles1 == childFiles2)
				return true;
			if (childFiles1 == null || childFiles2 == null)
				return false;
			if (childFiles1.length == childFiles2.length) {
				for (int i = 0; i < childFiles1.length; i++) {
					if (!childFiles1[i].getName().equals(
							childFiles2[i].getName())) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public static List<File> getWritableSDs(Context context) {
		List<File> writableFiles = new ArrayList<File>();
//		File mnt = new File("/mnt");
//		if (mnt.exists()) {
//			File[] children = mnt.listFiles();
//			if (children != null) {
//				for (int i = 0; i < children.length; i++) {
//					if (children[i].isDirectory() && children[i].canWrite()) {
//						writableFiles.add(children[i]);
//					}
//				}
//			}
//		}
		// 读取所有SD卡信息
		HashSet<String> set = getExternalMounts2(context);
		Iterator<String> it = set.iterator();
		while(it.hasNext()){
			String path = it.next();
			writableFiles.add(new File(path));
		}
		// mnt下没有或者只有一个sd卡
		if (writableFiles.size() == 0 || writableFiles.size() == 1) {
			File f = Environment.getExternalStorageDirectory();
			writableFiles.clear();
			writableFiles.add(f);
		}
		return writableFiles;
	}
	
	
	
	private static boolean addExtraSdcards(Context context, HashSet<String> out, String exclude) {
		boolean ret = false;
		try {
			int version = android.os.Build.VERSION.SDK_INT;
			if (version < 19) {
				return ret;
			}
			
			if (exclude != null && ! exclude.endsWith("/")) {
				exclude += "/";
			}
			
			Method method = Context.class.getMethod("getExternalFilesDirs", String.class);
			File[] list = (File[]) method.invoke(context, new String[]{null});
			if (list != null && list.length > 0) {
				for (File file : list) {
					if (exclude != null && file.getPath().startsWith(exclude)) {
					}
					else {
						String path = null;
						File parent = file.getParentFile();
						if (parent != null && parent.canWrite()) {
							path = parent.getPath();
						}
						else {
							path = file.getPath();
						}
						
						out.add(path);
					}
				}
			}
			
			ret = true;
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	public static HashSet<String> getExternalMounts2(Context context) {
	    final HashSet<String> out = new HashSet<String>();
	    
	    String path = null; 
	    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
		    path = Environment.getExternalStorageDirectory().getPath();
		    out.add(path);
	    }
	    
	    if(addExtraSdcards(context, out, path)){
	    	return out;
	    }
	    
	    String reg = ".* (vfat|ntfs|exfat|fat32|fuse) .*rw.*";
	    try {
	        final Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
	        process.waitFor();
	        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if (!line.toLowerCase(Locale.US).contains("asec") && !line.toLowerCase(Locale.US).contains("obb") && !line.toLowerCase(Locale.US).contains("secure")) {
		            if (line.matches(reg)) {
		                String[] parts = line.split(" ");
		                for (int i = 1; i < parts.length; i++) {
		                	String part = parts[i];
		                    if (part.startsWith("/")) {
		                    	if (new File(part).canWrite()) {
			                    	boolean needAdd = true;//nexus 4的两个目录非常奇怪，可能是内部做了替换 [/storage/emulated/legacy, /storage/emulated/0]
			                    	String tmpFilename = System.currentTimeMillis() + "";
			                    	File f = new File(part, tmpFilename);
			                    	f.createNewFile();
			                    	for (String o : out) {
			                    		if (new File(o, tmpFilename).exists()) {
			                    			needAdd = false;
			                    			break;
			                    		}
			                    	}
			                    	f.delete();
			                    	if (needAdd) {
				                    	out.add(part);
			                    	}
		                    	}
		                    	break;
		                    }
		                }
		            }
		        }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
        return out;
	}

	private static String getVideoSuffix(boolean isComplete) {
		String fileSuffix = DownloadUtils.FILE_COMPLETED_POSTFIX;
		if (isComplete) {
			fileSuffix = DownloadUtils.FILE_COMPLETED_POSTFIX;
		} else {
			fileSuffix = DownloadUtils.FILE_NOT_COMPLETED_POSTFIX;
		}
		return fileSuffix;
	}

	private static String getSubSuffix(boolean isComplete) {
		String fileSuffix = DownloadUtils.SUBFILE_COMPLETED_POSTFIX;
		if (isComplete) {
			fileSuffix = DownloadUtils.SUBFILE_COMPLETED_POSTFIX;
		} else {
			fileSuffix = DownloadUtils.SUBFILE_NOT_COMPLETED_POSTFIX;
		}
		return fileSuffix;
	}

}
