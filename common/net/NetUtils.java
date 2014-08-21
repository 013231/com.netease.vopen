package common.net;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import common.pal.PalLog;

/**
 * <br/>网络工具类.
 * <br/>主要用于检测当前网络是否可用，检测当前网络连接类型
 * @author wjying
 *
 */
public class NetUtils {
    public static final String TAG = "NetUtils";
    public static final String NETWORK_TYPE_CMWAP = "cmwap";
    
    /**
     * 检测是否漫游连网.
     * @return
     */
    public static boolean isNetworkRoaming(Context context) {
        ConnectivityManager connectivityManager = getConnectivityManager(context);
        TelephonyManager telephonyManager = getTelephonyManager(context);
        if (connectivityManager != null && telephonyManager != null) {

            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            return (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE)
                    && telephonyManager.isNetworkRoaming();
        }
        return false;
    }

    /**
     * 检测是否可以连网.
     * @param context
     * @return
     */
    public static boolean isAvailable(Context context) {
        ConnectivityManager connectivityManager = getConnectivityManager(context);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
        }
        return false;
    }
    
    
    /**
     * 检测是否是cmwap连网.
     * @param context
     * @return
     */
    public static boolean isCMWAP(Context context) {
        // 判断是否是cmwap网络
        ConnectivityManager connectivityManager = getConnectivityManager(context);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                    && NETWORK_TYPE_CMWAP.equals(networkInfo.getExtraInfo())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测是否是wifi连网.
     * @param context
     * @return
     */
    public static boolean isWIFI(Context context) {
        ConnectivityManager connectivityManager = getConnectivityManager(context);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * 检测wifi是否已开.
     * @param context
     * @return
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            return wifiManager.isWifiEnabled();
        }
        return false;
    }
    
    /**
     * 返回ConnectivityManager.
     * @param context
     * @return
     */
    private static ConnectivityManager getConnectivityManager(Context context) {
        ConnectivityManager connectivity =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            PalLog.w(TAG, "couldn't get connectivity manager");
        }
        return connectivity;
    }
    
    /**
     * 返回TelephonyManager.
     * @param context
     * @return
     */
    private static TelephonyManager getTelephonyManager(Context context) {
        TelephonyManager telephone = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephone == null) {
        	PalLog.w(TAG, "couldn't get telephone manager");
        }
        return telephone;
    }

}
