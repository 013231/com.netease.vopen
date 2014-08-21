package common.pal;

import java.io.IOException;

/**
 * 平台系统功能接口类.
 * 本接口只做各平台定方静态方法用，
 * 因为静态方法不能接口化或抽像化,所以该接口并不做实现.只做方法定义。
 * 
 * @author roy.wong1984@gmail.com
 *
 */
public interface IPlatform {

	/**
	 * 判断手机是否支持Qwerty键盘
	 * @return
	 */
	public boolean isQwertyKeyboard();
	

	
	/**
	 * 判断手机是否支持触摸屏
	 * @return
	 */
	public boolean isSupportTouch();
	
	
	/**
	 * 返回手机IMEI 串号
	 * @return
	 */
	public String getPhoneIMEI();
	
	
	/**
	 * 返回手机操作系统
	 * @return
	 */
	public String getPhoneOS();
	
	/**
	 * 返回手机操作系统版本号
	 * @return
	 */
	public String getPhonoeOSVersion();
	

	
	/**
	 * 
	 * @return
	 */
	public String getPhoneModels();
	
	/****************************************************
	// 以下Google地理位置相关数据获取方式
	// 参考  http://wap.anttna.com/index-wap2.php?p=847
	*******************************************************/
	
	/**
	 * 通过GPS获取经纬点不到时返回null
	 * @return （String[2]{latitude, longitude} (纬度, 经度)）
	 */
	public String[] getCurrentLocation() ;
	
	
	
	public boolean isCurrentCDMA() ;
	
	// GSM
	public int getCellid() ;
	
	// GSM
	public int getMNC() ;
	
	// GSM & CDMA
	public int getMCC() ;
	
	// GSM
	public int getLAC() ;
	
	// CDMA
	public int getSID() ;
	
	// CDMA
	public int getNID() ;
	
	// CDMA
	public int getBID() ;
	
	
	
	/************** 日期类方法 ********************/
	
	
	public long timeString2Long(String timeStr);
	
	
	/**************GZIP 相关压缩、解压方法********************/
	/**
	 * GZIP 压缩
	 * @throws IOException
	 */
	public byte[] gzipCompress(byte[] data) throws IOException ;
	
	/**
	 * GZIP 解压
	 * @throws IOException 
	 */
	public byte[] gzipDecompress(byte[] data) throws IOException ;
}
