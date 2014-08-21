package vopen.subtitle;

import java.util.Iterator;
import java.util.TreeMap;

import android.util.Log;

/**
 * 字幕解析及管理类，目前支持的格式包括srt, ass/ssa
 * @author MR
 *
 */
public class SubtitleManager {
	private static final String TAG = "SubtitleManager";
	private static final boolean DEBUG = false;
	//格式类型
	private static final int UNKNOW_FORMAT = -1;
	private static final int SRT_FORMAT = 0;
	private static final int ASS_FORMAT = 1;
	
	private TreeMap<Integer, SRT> mSrtMap;//保存字幕
	private int mSubFormat = UNKNOW_FORMAT;//当前字幕格式
	
	/**
	 * 构建字幕管理器，根据传入的字幕文件进行解析
	 * @param subFileSrc 字幕文件路径
	 */
	public SubtitleManager(String subFileSrc) {
		// TODO Auto-generated constructor stub
		long t1 = System.currentTimeMillis();
		if(subFileSrc == null)throw new NullPointerException("subFileSrc can not be null!");
		//解析格式
		String[] tempStrs = subFileSrc.split("\\.");
		if(tempStrs.length > 0){
			String formatStr = tempStrs[tempStrs.length - 1];
			if(DEBUG)Log.d(TAG, "subtitle format:" + formatStr);
			if(formatStr.equals("srt")){
				mSubFormat = SRT_FORMAT;
			}else if(formatStr.equals("ass")){
				mSubFormat = ASS_FORMAT;
			}
		}
		if(mSubFormat == UNKNOW_FORMAT)throw new RuntimeException("Unknow format subtitle!");
		//解析字幕
		switch(mSubFormat){
		case SRT_FORMAT:
			mSrtMap = SrtTool.parseSrt(subFileSrc);
			break;
		case ASS_FORMAT:
			mSrtMap = AssTool.parseSrt(subFileSrc);
			break;
		}
		long t2 = System.currentTimeMillis();
		if(DEBUG)Log.d(TAG, "Parser time:" + (t2 - t1));
		if(mSrtMap == null)throw new RuntimeException("Subtitle parse failed!");
	}
	
	/**
	 * 根据当前进度获得字幕
	 * @param currentPosition 当前进度，millSeconds
	 * @return
	 */
	public SRT getCurrentSubtitle(long currentPosition){
		long t1 = System.currentTimeMillis();
		SRT srt = null;
		Iterator<Integer> keys = mSrtMap.keySet().iterator();
		//通过while循环遍历比较
		while (keys.hasNext()) {
			Integer key = keys.next();
			SRT srtbean = mSrtMap.get(key);
			if (currentPosition >= srtbean.getBeginTime()
			&& currentPosition <= srtbean.getEndTime()) {
				srt = srtbean;
				break;//找到后就没必要继续遍历下去，节约资源
			}
		}
		long t2 = System.currentTimeMillis();
		if(DEBUG){
			Log.d(TAG, "Search subtitle time:" + (t2 - t1));
			if(srt != null)Log.d(TAG, "SRT:" + srt.getSrtBodyCh() +"|"+ srt.getSrtBodyEn());
		}
		return srt;
	}
	
	/**
	 * 获取当前字幕格式
	 * @return
	 */
	public String getSubFormatStr(){
		switch(mSubFormat){
		case SRT_FORMAT:
			return "Srt Format";
		case ASS_FORMAT:
			return "Ass Format";
		}
		return "Unknow Format";
	}
}
