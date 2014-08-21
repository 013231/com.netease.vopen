package common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import vopen.tools.FileUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import common.pal.PalLog;

/**
 * <br/>
 * 系统工具类. <br/>
 * 主要用于获取系统信息,如设备ID、操作系统版本等
 * 
 * @author wjying
 */
public class SystemUtils {

    /**
     * 获取设备ID.
     * 
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String id = tm.getDeviceId();
        return id;
    }

    /**
     * 获取设备名称.
     * 
     * @return
     */
    public static String getBuildModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取设备SDK版本号.
     * 
     * @return
     */
    public static int getBuildVersionSDK() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取设备系统版本号.
     * 
     * @return
     */
    public static String getBuildVersionRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 判断SD卡是否插入 即是否有SD卡
     */
    public static boolean isSDCardMounted() {
        return android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
                .getExternalStorageState());
    }

    /**
     * 是否：已经挂载,但只拥有可读权限
     */
    public static boolean isSDCardMountedReadOnly() {
        return android.os.Environment.MEDIA_MOUNTED_READ_ONLY.equals(android.os.Environment
                .getExternalStorageState());
    }

    /**
     * 获取android当前可用内存大小
     */
    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存

        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * 获得当前总内存大小
     */
    public static String getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
            	PalLog.i("SystemUtils","getTotalMemory " +str2 + num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }
    
    /**
     * 获取屏幕的亮度
     */
    public static int getScreenBrightness(Activity activity) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
    
    /**
     * 获取当前时间
     */
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
         // 显示为12小时格式
        SimpleDateFormat simpleDF = new SimpleDateFormat("HH:mm");
        String logTime = simpleDF.format(calendar.getTime());
        return logTime;
    }
    
    /**
     * 获取当前时间
     * @param format yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentTime(String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
         // 显示为12小时格式
        SimpleDateFormat simpleDF = new SimpleDateFormat(format);
        String logTime = simpleDF.format(calendar.getTime());
        return logTime;
    }
    
	/**
	 *  获得支持ACTION_SEND的应用列表 
	 */
	public static  List<ResolveInfo> getShareTargets(Context context,String type){
		Intent intent=new Intent(Intent.ACTION_SEND);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType(type);
		PackageManager pm = context.getPackageManager();
	
		return pm.queryIntentActivities(intent,
		PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
	}
	
	/***
	 * 获取屏幕宽度
	 * @param context
	 * @return
	 */
	public static int getDeviceWidth(Context context){
		DisplayMetrics metrics = new DisplayMetrics();
		metrics = context.getResources().getDisplayMetrics();		
		
		int width = metrics.widthPixels;
		
		return width;
	}
	
	/***
	 * 获取屏幕高度
	 * @param context
	 * @return
	 */
	public static int getDeviceHeight(Context context){
		DisplayMetrics metrics = new DisplayMetrics();
		metrics = context.getResources().getDisplayMetrics();		
		
		int height = metrics.heightPixels;
		
		return height;
	}
	
	/***
	 * 获取屏幕尺寸
	 * @param context
	 * @return width x heigth
	 */
	public static String getDeviceSize(Context context){
		return getDeviceWidth(context) + "x" + getDeviceHeight(context);
	}
	
	/**
	 * 获取某张sdcard剩余空间
	 * 单位byte
	 * 
	*/
	public static long readSDCardRemainSize(String filePath){ 
		long remain = 0;
		try {
			if(!TextUtils.isEmpty(filePath)){
				StatFs sf = new StatFs(filePath);
				long blockSize = sf.getBlockSize(); 
//				long blockCount = sf.getBlockCount(); 
				long availCount = sf.getAvailableBlocks(); 
//				Log.d("", "block大小:"+ blockSize+",block数目:"+ blockCount+",总大小:"+blockSize*blockCount/1024+"KB"); 
//				Log.d("", "可用的block数目：:"+ availCount+",剩余空间:"+ availCount*blockSize/1024+"KB"); 
				remain= availCount*blockSize;
			}
		}catch(Exception e){
			remain = 0;
		}
		return remain;
	}
	
	/**
	 * 获取所有sdcard剩余空间 单位byte
	 * 
	 */
	public static long readAllSDCardRemainSize(Context context) {
		long remain = 0;
		List<File> list = FileUtils.getWritableSDs(context);
		for (File f : list) {
			remain += readSDCardRemainSize(f.getAbsolutePath());
		}
		return remain;
	}
	
	/**
	 * 获取sdcard总空间
	 * @param filePath
	 * @return
	 */
	public static long readSDCardAllSize(String filePath) {
		long all = 0;
		if (!TextUtils.isEmpty(filePath)) {
			StatFs sf = new StatFs(filePath);
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			all = blockCount * blockSize;
		}
		return all;
	}
	
	
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

/**
 * 将byte进行转换，并附上单位“B，KB，MB，GB，TB”等
 * @param size 大小
 * @param num 小数点位数
 * @param unit 单位
 * @return
 */
	public static String getSizeStr(long size, int num, int unit) {
		StringBuffer sb = new StringBuffer(size < 0 ? "-" : "");
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
	
	/**
	 * 将byte进行转换，附上单位“B，K，M，G，T”等
	 * @param size 大小
	 * @param num 小数点位数
	 * @param unit 单位
	 * @return
	 */
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
				sb.append(s).append("M");
			} else if (size < SIZE_TB_X) {
				sb.append(s).append("G"); 
			} else {
				sb.append(s).append("T"); 
			}
		}		
		
		return sb.toString();
	}
	
	/**
	 * 将byte进行转换
	 * @param size 大小
	 * @param num 小数点位数
	 * @param unit 单位
	 * @return
	 */
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
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。舍入模式采用用户指定舍入模式
	 * 
	 * @param v1
	 * @param v2
	 * @param scale 表示需要精确到小数点以后几位
	 * @param round_mode 表示用户指定的舍入模式
	 * @return 两个参数的商
	 */
	// 默认除法运算精度
	private static final int DEFAULT_DIV_SCALE = 10;

	public static double divide(double v1, double v2) {
		return divide(v1, v2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_HALF_EVEN);
	}
	
	public static double divide(double v1, double v2, int scale, int mode) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, mode).doubleValue();
	}
}
