package vopen.subtitle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;

import android.util.Log;

public class AssTool {
	/*
	 * 解析ass,ssa字幕文件
	 * 
	 * @param srtPath 字幕路径
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
			br = new BufferedReader(new InputStreamReader(inputStream, charset));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return srt_map;
		}
		String line = null;
		srt_map = new TreeMap<Integer, SRT>();
		StringBuffer sb = new StringBuffer(charset);
		int key = 0;

		int startIndex = -1, endIndex = -1, textIndex = -1;
		boolean eventStart = false;
		try {
			while ((line = br.readLine()) != null) {
				// 首先查找到event事件开始
				if (!eventStart) {
					if (!line.equals("[Events]")) {
						continue;// 继续下一行
					} else {
						eventStart = true;
						;
					}
				}
				// 查找Format记录Index
				if (line.startsWith("Format:")) {
					// 查找Start, End, Text的位置
					line = line.substring("Format:".length());// 去掉段首的Format:
					String[] parseStrs = line.split(",");
					for (int i = 0; i < parseStrs.length; i++) {
						if (parseStrs[i].trim().equals("Start")) {
							startIndex = i;
						} else if (parseStrs[i].trim().equals("End")) {
							endIndex = i;
						} else if (parseStrs[i].trim().equals("Text")) {
							textIndex = i;
						}
					}
					Log.e("test", "startIndex:" + startIndex + "|endIndex:"
							+ endIndex + "|textIndex" + textIndex);
				} else if (line.startsWith("Dialogue:")) {
					line = line.substring("Dialogue:".length());// 去掉段首的Dialogue
					String[] parseStrs = line.split(",");
					String start = parseStrs[startIndex];
					String end = parseStrs[endIndex];
					// text之前的逗号为最后一个分隔用逗号，所以text中可能包含逗号
					StringBuffer sbText = new StringBuffer();
					for (int i = textIndex; i < parseStrs.length; i++) {
						sbText.append(parseStrs[i]);
					}
					String text = sbText.toString();
					SRT srt = new SRT();
					// 解析开始时间和结束事件。格式为0:00:00:00(时:分:秒:百分数), 注意小时只有一位数
					int begin_hour = Integer.parseInt(start.substring(0, 1));
					int begin_mintue = Integer.parseInt(start.substring(2, 4));
					int begin_scend = Integer.parseInt(start.substring(5, 7));
					int begin_milli = Integer.parseInt(start.substring(8, 10));
					int beginTime = (begin_hour * 3600 + begin_mintue * 60 + begin_scend)
							* 1000 + begin_milli * 10;
					int end_hour = Integer.parseInt(end.substring(0, 1));
					int end_mintue = Integer.parseInt(end.substring(2, 4));
					int end_scend = Integer.parseInt(end.substring(5, 7));
					int end_milli = Integer.parseInt(end.substring(8, 10));
					int endTime = (end_hour * 3600 + end_mintue * 60 + end_scend)
							* 1000 + end_milli * 10;
					// 解析字幕文本
					text = text.replaceAll("\\{([^\\}])*\\}", "");//去掉所有{}内的内容
					String[] texts = text.split("\\\\N");
					// 设置SRT
					if (texts.length >= 1)
						srt.setSrtBodyCh(texts[0]);
					else {
						srt.setSrtBodyCh("");
					}
					if (texts.length >= 2)
						srt.setSrtBodyEn(texts[1]);
					else {
						srt.setSrtBodyEn("");
					}
					srt.setBeginTime(beginTime);
					srt.setEndTime(endTime);
					// // 插入队列
					srt_map.put(key, srt);
					key++;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return srt_map;
	}

	static String get_charset(String file) {
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		try {
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1) {
				return charset;
			}
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE";
			} else if (first3Bytes[0] == (byte) 0xFE
					&& first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE";
			} else if (first3Bytes[0] == (byte) 0xEF
					&& first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8";
			}
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return charset;
	}
}
