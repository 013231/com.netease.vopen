package common.pal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


public class PalPlatform {
	private final static String TAG = "PalPlatform";
	
	
	/******************************************************
	 * 平台提供获取地理位置数据, 目前为GPS, WIFI
	 * 
	 *****************************************************/
	// （String[2]{latitude, longitude} (纬度, 经度)）
	// 获取不到时返回null
//	public static String[] getCurrentLocation() {
//		GeoManager locProvider = new GeoManager(WbApp.getAppInstance().getApplicationContext());
//		return locProvider.getCurrentLocation();
//	}
	
	/****************************************************
	// 以下Google地理位置相关数据获取方式
	// 参考  http://wap.anttna.com/index-wap2.php?p=847
	*******************************************************/
	
//	public static boolean isCurrentCDMA() {
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		if (telephonyManager != null) {
//			return telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_GSM;
//		}
//		
//		return false;
//	}
//	
//	// GSM
//	public static int getCellid() {
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		if (telephonyManager != null) {
//			GsmCellLocation sm = null;
//			try {
//				sm = ((GsmCellLocation) telephonyManager.getCellLocation());
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			if (sm != null) {
//				return  sm.getCid();
//			}
//		}
//		
//		return -1;
//	}
//	
//	// GSM
//	public static int getMNC() {
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		if (telephonyManager != null) {
//			return Integer.valueOf(telephonyManager.getNetworkOperator().substring(3, 5));
//		}
//		
//		return -1;
//	}
//	
//	// GSM & CDMA -1
//	public static int getMCC() {
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		if (telephonyManager != null) {
//			return Integer.valueOf(telephonyManager.getNetworkOperator().substring(0, 3));
//		}
//		
//		return -1;
//	}
//	
//	// GSM
//	public static int getLAC() {
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		if (telephonyManager != null) {
//			GsmCellLocation sm = null;
//			try {
//				sm = ((GsmCellLocation) telephonyManager.getCellLocation());
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			if (sm != null) {
//				return  sm.getLac();
//			}
//		}
//		
//		return -1;
//	}
//	
//	//下面三个函数载低版本API里编译无法通过, 我改了下, LJB check一下
//	// CDMA
//	public static int getSID() {
//		if (!isCurrentCDMA()) {
//			return -1;
//		}
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		CellLocation location = telephonyManager.getCellLocation();
//		Method method = null;
//		try{
//			method = location.getClass().getMethod("getSystemId",String.class);
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		if (method != null) {
//			try {
//				Integer ret = (Integer) method.invoke(location);
//				return ret.intValue();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return -1;
//	}
//	
//	// CDMA
//	public static int getNID() {
//		if (!isCurrentCDMA()) {
//			return -1;
//		}
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
////		CdmaCellLocation location = (CdmaCellLocation) telephonyManager.getCellLocation();
////
////		return location.getNetworkId();
//		
//		CellLocation location = telephonyManager.getCellLocation();
//		Method method = null;
//		try{
//			method = location.getClass().getMethod("getNetworkId",String.class);
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		if (method != null) {
//			try {
//				Integer ret = (Integer) method.invoke(location);
//				return ret.intValue();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return -1;
//	}
//	
//	// CDMA
//	public static int getBID() {
//		if (!isCurrentCDMA()) {
//			return -1;
//		}
//		TelephonyManager telephonyManager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
////		CdmaCellLocation location = (CdmaCellLocation) telephonyManager.getCellLocation();
////
////		return location.getBaseStationId();
//		
//		CellLocation location = telephonyManager.getCellLocation();
//		Method method = null;
//		try{
//			method = location.getClass().getMethod("getBaseStationId",String.class);
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		if (method != null) {
//			try {
//				Integer ret = (Integer) method.invoke(location);
//				return ret.intValue();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return -1;
//	}
//
//
//	public static String getPhoneModels() {
//		return android.os.Build.MODEL;
//	}
//
//	public static String getPhoneIMEI() {
//		TelephonyManager manager = (TelephonyManager)WbApp.getAppInstance().getSystemService(Context.TELEPHONY_SERVICE);
//		if(manager != null)
//			return manager.getDeviceId();
//		
//		return null;
//	}
//
//	public static String getPhoneOS() {
//		return "android";
//	}
//
//	public static String getPhoneOSVersion() {
//		return android.os.Build.VERSION.RELEASE;
//	}
//
//	
//	public static long timeString2Long(String strTime) {
//		return Util.formatDate2MS(strTime);
//	}
//
	public static byte[] gzipDecompress(byte[] data) throws IOException {
//		PalLog.i("TIME-TEST", "gzipDecompress exit:" + System.currentTimeMillis());
		GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int iRead = 0;
		while(0 < (iRead = gzip.read(buffer))){
			try {
				o.write(buffer, 0, iRead);
			}catch (Exception e){
				e.printStackTrace();
				PalLog.i(TAG,"gzipDecompress Exception");
			}
		}
		
//		PalLog.i("PalUtil", "gzipDecompress byte[]");
//		PalLog.i("TIME-TEST", "gzipDecompress exit:" + System.currentTimeMillis());
		return o.toByteArray();
	}
	
	public static InputStream gzipDecompress(InputStream in) throws IOException {
		GZIPInputStream gzip = new GZIPInputStream(in);
		PalLog.i("PalUtil", "gzipDecompress InputStream");
		return gzip;
	}
	
	public static byte[] gzipCompress(byte[] data) throws IOException{
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(o);
		gzip.write(data, 0, data.length);
		gzip.finish();
		
		PalLog.i("PalUtil", "gzipCompress");
		return o.toByteArray();
	}
	
	public static int getSDKVersionNumber() {
		int sdkVersion;
		try {
			sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			sdkVersion = 5;
		}
		return sdkVersion;
	}
	/*如果设备imei获取不到，需要使用mac地址作为device id，方法如下*/

	public static String getWifiMacAddress(Context context) {
		  WifiManager wifi = (WifiManager) context.getSystemService(
		    Context.WIFI_SERVICE);
		  WifiInfo info = wifi.getConnectionInfo();
		  return info.getMacAddress();
		 }

}
