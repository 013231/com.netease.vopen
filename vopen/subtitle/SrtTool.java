package vopen.subtitle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;

public class SrtTool {
	/* 解析SRT字幕文件
	* @param srtPath
	* 字幕路径
	*/
	public static TreeMap<Integer, SRT> parseSrt(String srtPath) {
		TreeMap srt_map = null;
		String charset = get_charset(srtPath);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(srtPath);
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
			return srt_map;// 有异常，就没必要继续下去了
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream,charset));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return srt_map;
		}
		String line = null;
		srt_map = new TreeMap<Integer, SRT>();
		StringBuffer sb = new StringBuffer(charset);
		int key = 0;
		try {
			while ((line = br.readLine()) != null) {
				if (!line.equals("")) {
					sb.append(line).append("@");
					continue;
				}
				
				String[] parseStrs = sb.toString().split("@");
				// 该if为了适应一开始就有空行以及其他不符格式的空行情况
				if (parseStrs.length < 3) {
					sb.delete(0, sb.length());// 清空，否则影响下一个字幕元素的解析
					continue;
				}
				SRT srt = new SRT();
				// 解析开始和结束时间
				String timeTotime = parseStrs[1];
				String[] timestamps = timeTotime.split("-->");
				if (timestamps.length != 2) {//
					sb.delete(0, sb.length());// 清空，否则影响下一个字幕元素的解析
					continue;
				}
				int beginTime = getTimeWithMilliScnd(timestamps[0]);
				int endTime = getTimeWithMilliScnd(timestamps[1]);
				// 解析字幕文字
				String srtBody = "";
				if(parseStrs.length >= 3){
					srtBody = parseStrs[2];
					srt.setSrtBodyCh(srtBody);
				} else {
					srt.setSrtBodyCh("");
				}
				if(parseStrs.length >= 4){
					srtBody = parseStrs[3];
					srt.setSrtBodyEn(srtBody);
				} else {
					srt.setSrtBodyEn("");
				}
			
				// 设置SRT
				srt.setBeginTime(beginTime);
				srt.setEndTime(endTime);
			
			// 插入队列
				srt_map.put(key, srt);
				key++;
				sb.delete(0, sb.length());// 清空，否则影响下一个字幕元素的解析
			}
		} catch (Exception e) {
		// TODO: handle exception
			e.printStackTrace();
		}
		return srt_map;
	}
	
	static final int getTimeWithMilliScnd(String timeStr){
		int milli = 0, other = 0;
		int p = timeStr.indexOf(","); 
		if (p != -1){//包含逗号，正常情况
			String[] tokens = timeStr.split(",");
			if (tokens.length == 1){
				if (p == 0){//只包含毫秒数
					milli = parseIntNoException(tokens[0]);
				}
				else if (p == timeStr.length() -1){//不包含毫秒数
					other = getTimeWithoutMilliScnd(tokens[0]);
				}
			}
			else if (tokens.length >= 2){//正常情况
				other = getTimeWithoutMilliScnd(tokens[0]);
				milli = parseIntNoException(tokens[1]);
			}
		} else {//不包含逗号，不正常情况
			if(timeStr.contains(":")){//如果有分号，则认为是时间部分
				other = getTimeWithMilliScnd(timeStr);
			} else {//否则，认为是毫秒数
				milli = parseIntNoException(timeStr);
			}
		}
		return  other + milli;
	}
	
	static final int getTimeWithoutMilliScnd(String timeStr){
		int hour = 0, mintue = 0, scend = 0;
		String[] tokens = timeStr.split(":");
		for (int i = 0; i < tokens.length; i++) {
			if (i == 0) {
				hour = parseIntNoException(tokens[0]);
			} else if (i == 1) {
				mintue = parseIntNoException(tokens[1]);
			} else if (i == 2) {
				scend = parseIntNoException(tokens[2]);
			}
		}
		return (hour * 3600 + mintue * 60 + scend)* 1000;
	}
	
	
	static final int parseIntNoException(String s){
		int result = 0;
		try{
			s = s.trim();
			result = Integer.parseInt(s);
		}catch(NumberFormatException e){
			e.printStackTrace();
			return 0;
		}
		return result;
	}
	
	static String get_charset(String file) {
			String charset = "GBK";
			byte [] first3Bytes = new byte[3];
			try{
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
				int read = bis.read(first3Bytes, 0, 3);
				if (read == -1) {
					return charset;
				}
				if (first3Bytes[0] == (byte)0xFF && first3Bytes[1] == (byte)0xFE) {
					charset = "UTF-16LE";
				} else if(first3Bytes[0] == (byte)0xFE && first3Bytes[1] == (byte)0xFF) {
					charset = "UTF-16BE";
				} else if(first3Bytes[0] == (byte)0xEF && first3Bytes[1] == (byte)0xBB && first3Bytes[2] == (byte)0xBF) {
					charset = "UTF-8";
				}
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return charset;
		}
}
