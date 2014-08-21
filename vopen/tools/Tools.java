package vopen.tools;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;

public class Tools {
	
//	/**
//	 * 视频大小
//	 * @param length 视频大小，单位为byte
//	 * @param num 小数点后几位
//	 * @return String 000.000MB
//	 */
//	public static String calculateVideoSizeMB(long length, int num){
//		
//		float f = calculateVideoSize2float(length, num);
//		
//		return f + "MB";
//	}
//	
//	/**
//	 * 视频大小
//	 * @param length 视频大小，单位为byte
//	 * @param num 小数点后几位
//	 * @return String 000.000MB
//	 */
//	public static String calculateVideoSizeM(long length, int num){
//		
//		float f = calculateVideoSize2float(length, num);
//		
//		return f + "M";
//	}
//
//	/**
//	 * 视频大小
//	 * @param length 视频大小，单位为byte
//	 * @param num 小数点后几位
//	 * @return float 000.000
//	 */
//	public static float calculateVideoSize2float(long length, int num){
//		double vlaue = (double)length;
//		float f = formatDoubleNum(vlaue/1024/1024, num);
//		return f;
//	}
	
	
	/**
	 * 返回 format 格式的时间字符串
	 * 时间格式为 yyyy-MM-dd HH:mm:ss
	 * yyyy 返回4位年份
	 * MM 返回2位月份
	 * dd 返回2位日
	 * 时间类同
	 * @return 相应日期类型的字符串
	 */
	public static String getCurrnetDate(String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date()).toString();
	}
	
	public static int getScreenWidth(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getApplicationContext().getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	/**
	 *  返回屏幕的规格
	 *  0 为其他尺寸 
	 *  1 为 320*240
	 *  2 为 480*320
	 *  3 为 480*800
	 *  4 为 480*8540
	 *  5 为 640*960
	 * @param c
	 * @return
	 */
	public static int getScreenMetrics(Context c){
		int type = 0;
		DisplayMetrics metrics = new DisplayMetrics();
		metrics = c.getApplicationContext().getResources().getDisplayMetrics();		
		
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		
		if(width==240 && height == 320)
			type = 1;
		else if(width==320 && height == 480)
			type = 2;
		else if(width==480 && height == 800)
			type = 3;
		else if(width==480 && height == 854)
			type = 4;
		else if(width==640 && height == 960)
			type = 5;

		return type;
	}
	
	/**检测网络是否可用*/
	public static boolean CheckNetwork(Context context) {
	    
	    boolean flag = false;
	    ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (cwjManager.getActiveNetworkInfo() != null)
	        flag = cwjManager.getActiveNetworkInfo().isAvailable();
	    
	    return flag;
	}
	
	/**检测是否是WIFI网络*/
	public static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext  
		             .getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
		if (activeNetInfo != null  
		             && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {  
			return true;  
		}  
		return false; 
	}
	
	/**检查是否是cmpwap网络*/
	public static boolean isCMWAPMobileNet(Context mContext){
		
		if(isWifi(mContext)){
			return false;
		}else{
			ConnectivityManager mag = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			if(null!=mag){
				NetworkInfo mobInfo = mag.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if(null!=mobInfo){
					
					if("cmwap".equals(mobInfo.getExtraInfo())){
						return true;
					}else{
						return false;
					}
					
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
		
	}
	
	/**byte*/
//	public static long readSDCardRemainSize() { 
//		
//		long remain = 0;
//		String state = Environment.getExternalStorageState(); 
//		if(Environment.MEDIA_MOUNTED.equals(state)) { 
//			File sdcardDir = Environment.getExternalStorageDirectory(); 
//			StatFs sf = new StatFs(sdcardDir.getPath()); 
//			long blockSize = sf.getBlockSize(); 
////			long blockCount = sf.getBlockCount(); 
//			long availCount = sf.getAvailableBlocks(); 
//			
////			Log.d("", "block大小:"+ blockSize+",block数目:"+ blockCount+",总大小:"+blockSize*blockCount/1024+"KB"); 
////			Log.d("", "可用的block数目：:"+ availCount+",剩余空间:"+ availCount*blockSize/1024+"KB"); 
//			remain= availCount*blockSize;
//			
//		}
//		return remain;
//	}
	
	
	private final static long SIZE_KB = 1024;
	private final static long SIZE_MB = SIZE_KB * 1024;
	private final static long SIZE_GB = SIZE_MB * 1024;
	private final static long SIZE_TB = SIZE_GB * 1024;
	
	private final static long SIZE_KB_X = 1000;
	private final static long SIZE_MB_X = SIZE_KB * 1000;
	private final static long SIZE_GB_X = SIZE_MB * 1000;
	private final static long SIZE_TB_X = SIZE_GB * 1000;
	
	public final static int SIZE_UINT_DEFAULT = -1;
	public final static int SIZE_UINT_B = 0;
	public final static int SIZE_UINT_KB = 1;
	public final static int SIZE_UINT_MB = 2;
	public final static int SIZE_UINT_GB = 3;
	public final static int SIZE_UINT_TB = 4;


	public static String getSizeStr(long size, int num, int unit) {
	    if(size <= 0)return "0B";
		StringBuffer sb = new StringBuffer("");
		final Float s = getSize(size, num, unit);
		if (unit == SIZE_UINT_B) {
			sb.append(s.intValue()).append("B");
		} else if (unit == SIZE_UINT_KB) {
			sb.append(s.intValue()).append("KB");
		} else if (unit == SIZE_UINT_MB) {
			sb.append(s).append("MB");
		} else if (unit == SIZE_UINT_GB) {
			sb.append(s).append("GB");
		} else if (unit == SIZE_UINT_TB) {
			sb.append(s).append("TB");
		} else {
			if (size < SIZE_KB_X) {
				sb.append(s.intValue()).append("B"); 
			} else if (size < SIZE_MB_X) {
				sb.append(s.intValue()).append("KB");
			} else if (size < SIZE_GB_X) {
				sb.append(s.intValue()).append("MB");
			} else if (size < SIZE_TB_X) {
				sb.append(s).append("GB"); 
			} else {
				sb.append(s).append("TB"); 
			}
		}		
		
		return sb.toString();
	}
	
	public static String getSizeStrNoB(long size, int num, int unit) {
		StringBuffer sb = new StringBuffer(size < 0 ? "-" : "");
		final Float s = getSize(size, num, unit);
		
		if (unit == SIZE_UINT_B) {
			sb.append(s.intValue()).append("B");
		} else if (unit == SIZE_UINT_KB) {
			sb.append(s.intValue()).append("K");
		} else if (unit == SIZE_UINT_MB) {
			sb.append(s).append("M");
		} else if (unit == SIZE_UINT_GB) {
			sb.append(s).append("G");
		} else if (unit == SIZE_UINT_TB) {
			sb.append(s).append("T");
		} else {
			if (size < SIZE_KB_X) {
				sb.append(s.intValue()).append("B"); 
			} else if (size < SIZE_MB_X) {
				sb.append(s.intValue()).append("K");
			} else if (size < SIZE_GB_X) {
				sb.append(s.intValue()).append("M");
			} else if (size < SIZE_TB_X) {
				sb.append(s).append("G"); 
			} else {
				sb.append(s).append("T"); 
			}
		}		
		
		return sb.toString();
	}
	
	public static float getSize(long size, int num, int unit) {
		double s = size;
		if (unit == SIZE_UINT_B) {
			return size;
		} else if (unit == SIZE_UINT_KB) {
			return formatDoubleNum((s /= SIZE_KB), num);
		} else if (unit == SIZE_UINT_MB) {
			return formatDoubleNum((s /= SIZE_MB), num);
		} else if (unit == SIZE_UINT_GB) {
			return formatDoubleNum((s /= SIZE_GB), num);
		} else if (unit == SIZE_UINT_TB) {
			return formatDoubleNum((s /= SIZE_TB), num);
		} else {
			if (size < SIZE_KB_X) {
				return size;
			} else if (size < SIZE_MB_X) {
				return formatDoubleNum((s /= SIZE_KB), num);
			} else if (size < SIZE_GB_X) {
				return formatDoubleNum((s /= SIZE_MB), num);
			} else if (size < SIZE_TB_X) {
				return formatDoubleNum((s /= SIZE_GB), num);
			} else {
				return formatDoubleNum((s /= SIZE_TB), num);
			}
		}
	}
	
	/**
	 * 获取小数点位数
	 * @param d 原来数据
	 * @param num 小数点位数
	 * @return 
	 */
	public static Float formatDoubleNum(double d, int num){
		BigDecimal bd = new BigDecimal(d); 
		bd = bd.setScale (num, BigDecimal.ROUND_HALF_UP); 
		return bd.floatValue();
	}
	
	/**
     * 将时间信息（毫秒）转化为日期 形如"yyyy-MM-dd HH:mm:ss"
     * 
     * @param ms
     * @return
     */
    public static String msToDate(Long ms) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(ms)).toString();
    }

    /**
     * 将形如"yyyy-MM-dd HH:mm:ss"的日期信息转换为毫秒
     * 
     * @param date
     * @return
     */
    public static long dateToMs(String date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return format.parse(date).getTime();

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * 根据包名启动一个外部App。
     * 若该app已经安装且启动成功返回true，否则返回false。
     * @param context
     * @param packageName App包名
     * @return
     */
    public static boolean startOtherApp(Context context, String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES);
			if(pi == null)return false;
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);
			List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(
					resolveIntent, 0);
			ResolveInfo ri = apps.iterator().next();
			if(ri == null)return false;
			String className = ri.activityInfo.name;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName cn = new ComponentName(packageName, className);
			intent.setComponent(cn);
			context.startActivity(intent);
			return true;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
    
    /**
     * 检查是否包含该APP
     * @param context
     * @param packageName App包名
     * @return
     */
    public static boolean hasApp(Context context, String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES);
			if(pi == null)return false;
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);
			List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(
					resolveIntent, 0);
			ResolveInfo ri = apps.iterator().next();
			if(ri == null)return false;
			return true;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
}
