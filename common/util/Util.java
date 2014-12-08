package common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Vector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import common.pal.IHttp;
import common.pal.PalLog;
import common.pal.PalPlatform;

public class Util {
	
	public static String nullStr(String str){
		if(TextUtils.isEmpty(str) || str.equalsIgnoreCase("null"))
			return null;
		
		return str;
		
	}

	//follow j2me function
	
	static final String URLCharTable = "!#$%&'()*+,-./:;=?@[\\]^_`{|}~";
	
	public static String getHttpLink(String str, int offset) {
		int len = 0;
		if (Util.startsWithIgnoreCase(str, offset, "http://")) {
			len = "http://".length();
		}
		else if (Util.startsWithIgnoreCase(str, offset, "www.")) {
			len = "www.".length();
		}
		else if (Util.startsWithIgnoreCase(str, offset, "wap.")) {
			len = "wap.".length();
		}
		else if (Util.startsWithIgnoreCase(str, offset, "https://")) {
			len = "https://".length();
		}
		else {
			return null;
		}
			
		int strLen = str.length();

		while (offset + len < strLen) {
			char c = str.charAt(offset + len);
			if ((c >= 'A' && c <= 'Z') // 'a' - 'z'
					|| (c >= 'a' && c <= 'z') // 'A' - 'Z'
					|| (c >= '0' && c <= '9')) { // '0' - '9'
				len++;
			} else {
				if (URLCharTable.indexOf(c) >= 0) {
					len++;
				} else {
					break;
				}
			}
		}
		
		return str.substring(offset, offset + len);
	}
	
	public static boolean startsWithIgnoreCase(String str,int offset,String anObject){

		int length = anObject.length();
		
		//待比较字串,大于 str字串
		if(offset +  length > str.length() ){
			return false;
		}
		
		int idx = 0;
	
		while (idx < length) {
			char c = str.charAt(offset + idx);
			if(c >= 'A' && c <= 'Z'){		//大写字母
				c += 32;
			}
			if(c != anObject.charAt(idx)){
				break;
			}else{
				idx ++;
			}
		}
		if(idx == length && idx > 0){
			return true;
		}
		return false;
	}
	
	public static String toString(String str)
	{
		return str == null ? "" : str;
	}
	
	public static boolean isStringEmpty(String v)
	{
		if(v == null || v.length() == 0 ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * replace old string part with new ones
	 * 
	 * @param str
	 * @param oldStr
	 * @param newStr
	 * @return the replaced String
	 */
	public static String replace(String str, String oldStr, String newStr) {
		return str.replace(oldStr, newStr);
	}
	
	/**
	 * 将指定字符串.根据splitStr来切割字符
	 * 
	 * @param txt
	 * @param splitStr
	 * @return
	 */
	public static Vector split(String txt, String splitStr) {
		if (txt == null || txt.length() <= 0 || splitStr == null
				|| splitStr.length() <= 0) {

			return null;
		}

		String[] strArr = txt.split(splitStr);
		if(strArr.length > 0){
			Vector<String> strings = new Vector<String>();
			for(int i = 0; i < strArr.length; i++)
				strings.addElement(strArr[i]);
			return strings;
		}

		return null;
	}
	
	
	
	/**
	 * 复制Hashtable
	 * 
	 * @param table
	 * @return
	 */
	public static Hashtable cloneHashtable(Hashtable table)
	{
		
		if (table != null)
		{
			return (Hashtable) table.clone();
		} else
		{
			return null;
		}
	}
	
	
	/**
	 * Tuning parameter: list size at or below which insertion sort will be used
	 * in preference to mergesort or quicksort.
	 */
	private static final int INSERTIONSORT_THRESHOLD = 7;
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static void sort(Vector list)
	{
		if (list != null && list.size() > 0)
		{
			Object[] src = new Object[list.size()];
			Object[] dest = new Object[list.size()];
			list.copyInto(src);
			list.copyInto(dest);
			mergeSort(src, dest, 0, src.length, 0);
			for (int i = 0; i < dest.length; i++)
			{
				list.setElementAt(dest[i], i);
			}
		}
	}
	
	private static void mergeSort(Object[] src, Object[] dest, int low,
								  int high, int off) {
		int length = high - low;

		// Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD) {
			for (int i = low; i < high; i++)
				for (int j = i; j > low
						&& ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
					swap(dest, j, j - 1);
			return;
		}

		// Recursively sort halves of dest into src
		int destLow = low;
		int destHigh = high;
		low += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest. This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid
					&& ((Comparable) src[p]).compareTo(src[q]) <= 0)
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
		}
	}

	private static void swap(Object[] x, int a, int b) {
		Object t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
	
	public static void delFile(String filePath) {
		File file = new File(filePath);
		try{
			if (file != null && file.exists()) {
				file.delete();
				file = null;
			}
		} catch(Exception e) {
			
		}
	}
	
	public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally  {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }
    
    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
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

	/**
	 * 获取可用空间
	 * @param root
	 * @return
	 */
	public static long getAvailableBytes(File root) {
        StatFs stat = new StatFs(root.getPath());
        // put a bit of margin (in case creating the file grows the system by a few blocks)
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        return stat.getBlockSize() * availableBlocks;
    }
	public static long getAvailableBytes(String dir){
		return getAvailableBytes(new File(dir));
	}
	
	/**
	 * 截取小数点后多少位， 未考虑四舍五入
	 * @param src
	 * @param leave
	 * @return
	 */
	public static String trimDecimalPoint(String src, int leave){
		if(src == null)
			return null;
		
		int dotPos = src.indexOf(".");
		if(dotPos < 0)
			return src;
		
		dotPos++;
		if(dotPos + leave < src.length())
			return src.substring(0, dotPos + leave);
		
		return src;
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	public static String getMd5Hash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);

			while (md5.length() < 32)
				md5 = "0" + md5;

			return md5;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;

		}
	}
	/**
	 * 从邮件地址获取用户名
	 * @param add
	 * 
	 * @return
	 */
	public static String getUserNameFromAddress(String add){
		if(add == null)
			return null;
		
		int pos = add.indexOf("@");
		if(pos <= 0)
			return add;
		String name = add.substring(0, pos);
		return name;
	}
	/**
	 * 流量转为字串
	 * @param flow
	 * 
	 * @return 
	 */
	 public static String flow2String(long flow) {
			StringBuilder str = new StringBuilder();
			if (flow < 1024) {
				str.append(flow);
				str.append("B");
			} else if (flow < 1024 * 1024) {
				String tmp = Long.toString(flow * 10 / 1024);
				str.append(tmp.substring(0, tmp.length() - 1));
				str.append(".");
				str.append(tmp.substring(tmp.length() - 1,tmp.length()));
				str.append("K");
			} else {
				String tmp = Long.toString(flow * 10 / (1024 * 1024));
				str.append(tmp.substring(0, tmp.length() - 1));
				str.append(".");
				str.append(tmp.substring(tmp.length() - 1,tmp.length()));
				str.append("M");
			}
			return str.toString();
		}
	 	    
	    public static String getNetworkType(Context context){  
	        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	        NetworkInfo networkinfo = connManager.getActiveNetworkInfo();  
	        String networkType = "";  
	        if(networkinfo != null){  
	            networkType = networkinfo.getTypeName();              
	        }  
	          
	        return networkType;  
	    } 
	    /**检测网络是否可用*/
		public static boolean CheckNetwork(Context context) {
		    
		    boolean flag = false;
		    ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    if (cwjManager.getActiveNetworkInfo() != null)
		        flag = cwjManager.getActiveNetworkInfo().isAvailable();
		    
		    return flag;
		}
		 
	//判断邮件地址是否合法
		public static boolean isValidAddress(String address) {
		        // Note: Some email provider may violate the standard, so here we only check that
		        // address consists of two part that are separated by '@', and domain part contains
		        // at least one '.'.
		        int len = address.length();
		        int firstAt = address.indexOf('@');
		        int lastAt = address.lastIndexOf('@');
		        int firstDot = address.indexOf('.', lastAt + 1);
		        int lastDot = address.lastIndexOf('.');
		        return firstAt > 0 && firstAt == lastAt && lastAt + 1 < firstDot
		            && firstDot <= lastDot && lastDot < len - 1;
		    }
		
		//获取编码格式
		public static String getCharset(String ContentType){
		    if(ContentType == null){
		        return "UTF-8";
		    }else{
		        int i = ContentType.indexOf("=");
	            String charset = null;
	            if(i<0){
	                charset = "UTF-8";  
	            }else{
	                charset = ContentType.substring(i+1);
	            }           
	            return charset;     
		    }
		}
		/**
		 * byte[]数组经过gzip解压，字符集处理后转化为String
		 * @param byte[],http,String 
		 * 
		 * @return  String
		 */
       public static String processByteDataToString(byte[] data,IHttp http,String charset) throws IOException{
    	    String strData = null;
    	    if(null == data || http == null) {
    	    	return strData;
    	    }
   			String encoding = http.getHeaderField("Content-Encoding");
   			if (encoding != null && encoding.equals("gzip")) {
   				data = PalPlatform.gzipDecompress(data);
   			}
   			try {
   				strData = new String(data, charset);
			} catch (UnsupportedEncodingException e) {
				strData = new String(data);
			}
   			return strData;
       }
       
       
	public static String swVersion = null;
//	public static String getSwVersion(Context context) {
//		if (swVersion == null || swVersion.length() == 0)
//			swVersion = swVersionStr(context);
//		return swVersion;
//	}
	public static String getSwVersion(Context context) {
		if (swVersion == null || swVersion.length() == 0)
			swVersion = getApplicationMetaInfo(context, "VERSION");;
		return swVersion;
	}
	public static String getApplicationMetaInfo(Context context, String metaName) {
		PackageManager pm = context.getPackageManager();
		ApplicationInfo info;
		try {
			info = pm.getApplicationInfo(context.getPackageName(), 128);
			if (info != null && info.metaData != null) {
				return info.metaData.getString(metaName);
			} else {
				return null;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		}
		
	}
	
	private static String domain = null;
	public static String getPushDomain(Context context){
		if (domain == null){
			domain = getApplicationMetaInfo(context, "NETEASE_DOMAIN");
		}
		return domain;
	}
	
//	private static String productKey = null;
//	public static String getPushProductKey(Context context){
//		if (productKey == null){
//			productKey = getApplicationMetaInfo(context, "NETEASE_PRODUCT_KEY");
//		}
//		return productKey;
//	}
	
//	private static String productVersion = null;
//	public static String getPushProductVersion(Context context){
//		if (productVersion == null){
//			productVersion = getApplicationMetaInfo(context, "NETEASE_PRODUCT_VERSION");
//		}
//		return productVersion;
//	}
	
	private static String mChannelId = null;
	public static String getAppChannelId(Context context){
		if (TextUtils.isEmpty(mChannelId)){
			mChannelId = getApplicationMetaInfo(context, "Channel");
		}
		return mChannelId;
	}
	
	private static String mDAKey = null;
	public static String getDAKey(Context context){
		if (TextUtils.isEmpty(mDAKey)){
			mDAKey = getApplicationMetaInfo(context, "DAKEY");
		}
		return mDAKey;
	}
	
	public static String getNumberVersion(Context context) {
		if (null == context) {
			PalLog.w("Util", "invalid input when calling getClientVersion()");
			return "";
		}
		String clientVer = "0.0.0";
		try {
			clientVer = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			PalLog.w("Util", "failed to get version");
		}
		if (null != clientVer) {
			clientVer = clientVer.trim();
		}
		return clientVer;
	}

	 /**
     * 获取屏幕分辨率
     * 
     * @return
     */
    public static String getResolution(Context context)
    {
    	DisplayMetrics dm = new DisplayMetrics();
		WindowManager WM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		WM.getDefaultDisplay().getMetrics(dm);
		String widthStr = new Integer(dm.widthPixels).toString();
		String heightStr = new Integer(dm.heightPixels).toString();
		return (widthStr + "x" + heightStr);
    	
    }
    /**
     * 获取反馈标题
     * 
     * @return
     */
    public static String getFeedBackTitle(String content){
    	int len = content.length();
    	if(len<10){
    		return content;
    	}else{
    		return content.substring(0, 10);
    	}	
    }
}
