package common.util;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class BaseUtil {

	
	/**
	 * 判断字符是否为空
	 * @param str
	 * @return
	 */
	public static boolean isStringEmpty(String v)
	{
		if(v == null || v.length() == 0 ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 *  
	 */
	public static String toString(String str)
	{
		return str == null ? "" : str;
	}
	
	
	public static String nullStr(String str){
		
		return str;
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
	
	/**
	 * replace old string part with new ones
	 * 
	 * @param str
	 * @param oldStr
	 * @param newStr
	 * @return the replaced String
	 */
	public static String replace(String str, String oldStr, String newStr) {

		int preIndex = 0;
		int index = 0;
		StringBuffer buffer = new StringBuffer();
		index = str.indexOf(oldStr, preIndex);
		while (index >= 0) {
			buffer.append(str.substring(preIndex, index));
			buffer.append(newStr);
			preIndex = index + oldStr.length();
			index = str.indexOf(oldStr, preIndex);
		}
		if (preIndex < str.length()) {
			buffer.append(str.substring(preIndex));
		}
		return buffer.toString();
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

		Vector strings = new Vector();
		int lastIndex = 0;
		int currentIndex = 0;
		while ((currentIndex = txt.indexOf(splitStr, lastIndex)) != -1) {
			if (strings == null) {
				strings = new Vector();
			}
			strings.addElement(txt.substring(lastIndex, currentIndex));
			lastIndex = currentIndex + splitStr.length();
		}
		if (lastIndex < txt.length()) {
			strings.addElement(txt.substring(lastIndex));
		}
		return strings;
	}
	
	static final String URLCharTable = "!#$%&'()*+,-./:;=?@[\\]^_`{|}~";

	/**
	 * 获取HttpLink
	 * @return
	 */
	public static String getHttpLink(String str, int offset) {
		int len = 0;
		if (startsWithIgnoreCase(str, offset, "http://")) {
			len = "http://".length();
		}
		else if (startsWithIgnoreCase(str, offset, "www.")) {
			len = "www.".length();
		}
		else if (startsWithIgnoreCase(str, offset, "wap.")) {
			len = "wap.".length();
		}
		else if (startsWithIgnoreCase(str, offset, "https://")) {
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
	
	//======================================================
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
			Hashtable dest = new Hashtable();
			Enumeration keys = table.keys();
			while (keys.hasMoreElements())
			{
				Object obj = keys.nextElement();
				dest.put(obj, table.get(obj));
			}
			return dest;
		} else
		{
			return null;
		}
	}

	/**
	 * 将Map中的值组成Vector数组
	 * 
	 * @param map
	 * @return
	 */
	public static Vector getMapValues(Hashtable map)
	{
		Enumeration e = map.elements();
		Vector array = new Vector();
		while (e.hasMoreElements())
		{
			Object entry = e.nextElement();
			array.addElement(entry);
		}
		return array;
	}

	
	
	
	
	//------------------------------------------------------
	//
	//		Object sort
	//	 
	//------------------------------------------------------
	
	
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
			int high, int off)
	{
		int length = high - low;

		// Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD)
		{
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
		if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0)
		{
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++)
		{
			if (q >= high || p < mid
					&& ((Comparable) src[p]).compareTo(src[q]) <= 0)
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
		}
	}

	private static void swap(Object[] x, int a, int b)
	{
		Object t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
	
//	public static void parseText(String text, int maxWordCount, Vector offsets, Vector attributes) {
//		if (text == null || text.trim().length() == 0) {
//			
//			return;
//		}
//		boolean isFindAt = false;
//		boolean isFindWell = false;
//		int atorWellBeginIndex = 0;
//		int length = 0;
//		offsets.addElement(new Integer(0));
//		for (int i = 0; i < text.length(); i++) {
//			char c = text.charAt(i);
//			if (isFindAt) {
//				if(c >= 0x4e00 && c <= 0x9fff) {	//汉字
//					if (length + 2 > maxWordCount) {
//						if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//							offsets.addElement(new Integer(atorWellBeginIndex));
//							attributes.addElement(new Byte((byte) 0));
//						}
//						offsets.addElement(new Integer(i));
//						attributes.addElement(new Byte((byte) 1));
//						isFindAt = false;
//						length = 0;
//					}
//					else {
//						length += 2;
//					}
//				}
//				else  if ((c >= 'a' && c<='z') 	   //字母数据
//						||( c >='A' && c <= 'Z')
//						|| (c >='0' && c <='9')) {
//					if (length + 1 > maxWordCount) {
//						if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//							offsets.addElement(new Integer(atorWellBeginIndex));
//							attributes.addElement(new Byte((byte) 0));
//						}
//						offsets.addElement(new Integer(i));
//						attributes.addElement(new Byte((byte) 1));
//						isFindAt = false;
//						length = 0;
//					}
//					else {
//						length += 1;
//					}
//				}
//				else {
//					if (length > 0) {
//						if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//							offsets.addElement(new Integer(atorWellBeginIndex));
//							attributes.addElement(new Byte((byte) 0));
//						}
//						offsets.addElement(new Integer(i));
//						attributes.addElement(new Byte((byte) 1));
//						isFindAt = false;
//						length = 0;
//					}
//					if (c == '@') {
//						atorWellBeginIndex = i;
//						isFindAt = true;
//					}
//					else if (c == '#') {
//						atorWellBeginIndex = i;
//						isFindAt = false;
//						isFindWell = true;
//					}
//					else {
//						isFindAt = false;
//					}
//				}
//			}
//			else if (isFindWell) {
//				if(c >= 0x4e00 && c <= 0x9fff) {	//汉字
//					if (length + 1 > maxWordCount) {
//						if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//							offsets.addElement(new Integer(atorWellBeginIndex));
//							attributes.addElement(new Byte((byte) 0));
//						}
//						offsets.addElement(new Integer(i));
//						attributes.addElement(new Byte((byte) 1));
//						isFindWell = false;
//						length = 0;
//					}
//					else {
//						length += 1;
//					}
//				}
//				else  if ((c >= 'a' && c<='z') 	   //字母数据
//						||( c >='A' && c <= 'Z')
//						|| (c >='0' && c <='9')) {
//					if (length + 1 > maxWordCount) {
//						if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//							offsets.addElement(new Integer(atorWellBeginIndex));
//							attributes.addElement(new Byte((byte) 0));
//						}
//						offsets.addElement(new Integer(i));
//						attributes.addElement(new Byte((byte) 1));
//						isFindWell = false;
//						length = 0;
//					}
//					else {
//						length += 1;
//					}
//				}
//				else {
//					if (length > 0) {
//						if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//							offsets.addElement(new Integer(atorWellBeginIndex));
//							attributes.addElement(new Byte((byte) 0));
//						}
//						offsets.addElement(new Integer(i));
//						attributes.addElement(new Byte((byte) 1));
//						isFindWell = false;
//						length = 0;
//					}
//					if (c == '#') {
//						atorWellBeginIndex = i;
//						isFindWell = true;
//					}
//					else if (c == '@') {
//						atorWellBeginIndex = i;
//						isFindWell = false;
//						isFindAt = true;
//					}
//					else {
//						isFindWell = false;
//					}
//				}
//			}
//			else {
//				if (c == '@') {
//					isFindAt = true;
//					atorWellBeginIndex = i;
//				}
//				else if (c == '#') {
//					isFindWell = true;
//					atorWellBeginIndex = i;
//				}
//			}
//		}
//		
//		if (isFindAt && length > 0) {
//			if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//				offsets.addElement(new Integer(atorWellBeginIndex));
//				attributes.addElement(new Byte((byte) 0));
//			}
//			offsets.addElement(new Integer(text.length()));
//			attributes.addElement(new Byte((byte) 1));
//			isFindAt = false;
//			length = 0;
//		}
//		else if (isFindWell && length > 0) {
//			if (!offsets.contains(new Integer(atorWellBeginIndex))) {
//				offsets.addElement(new Integer(atorWellBeginIndex));
//				attributes.addElement(new Byte((byte) 0));
//			}
//			offsets.addElement(new Integer(text.length()));
//			attributes.addElement(new Byte((byte) 1));
//			isFindWell = false;
//			length = 0;
//		}
//		else {
//			offsets.addElement(new Integer(text.length()));
//			attributes.addElement(new Byte((byte) 0));
//		}
//	}
	
	public static void parseText(String text, int maxWordCount, Vector offsets, Vector attributes) {
		if (text == null || text.trim().length() == 0) {
		
			return;
		}
		int totleLength = text.length();
		int offset = 0;
		offsets.addElement(new Integer(0));
		while (offset < totleLength) {
			int len = 0;
			char c = text.charAt(offset);
			if (c == '@') {
				// 进行用户解析
				len = parseUser(text, offset, maxWordCount);
			}
			else if (c == '#') {
				// 进行话题解析
				len = parseTopic(text, offset, maxWordCount);
			}
			else if (c == 'w' || c == 'W' || c == 'h' || c == 'H') {
				// 进行http链接解析
				len = parseHttp(text, offset, maxWordCount);
			}
			
			if (len > 0) {
				if (!offsets.contains(new Integer(offset))) {
					offsets.addElement(new Integer(offset));
					attributes.addElement(new Byte((byte) 0));
				}
				if (c == '@' || c == '#')	{
					offsets.addElement(new Integer(offset + len + 1));
					attributes.addElement(new Byte((byte) 1));
					offset += len + 1;
				}
				else {
					offsets.addElement(new Integer(offset + len));
					attributes.addElement(new Byte((byte) 1));
					offset += len;
				}
			}
			else {
				offset++;
			}
		}
		
		if (!offsets.contains(new Integer(totleLength))) {
			offsets.addElement(new Integer(text.length()));
			attributes.addElement(new Byte((byte) 0));
		}
	}
	
	public static int parseUser(String text, int offset, int maxWordCount) {
		int length = 0;
		int userOffset = offset + 1;
		while (userOffset < text.length()) {
			char c = text.charAt(userOffset);
			if (c == 'w' || c == 'W' || c == 'h' || c == 'H') {
				if((startsWithIgnoreCase(text, userOffset, "http://"))
						|| (startsWithIgnoreCase(text, userOffset, "www."))
						|| (startsWithIgnoreCase(text, userOffset, "wap."))
						|| (startsWithIgnoreCase(text, userOffset, "https://"))) {
					
					return (userOffset - 1 - offset);
				}
			}
			if(c >= 0x4e00 && c <= 0x9fff) {	//汉字
				if (length + 2 > maxWordCount) {
					
					return (userOffset - 1 - offset);
				}
				else {
					length += 2;
					userOffset++;
				}
			}
			else if ((c >= 'a' && c<='z') 	   //字母数据
					||( c >='A' && c <= 'Z')
					|| (c >='0' && c <='9')) {
				if (length + 1 > maxWordCount) {
					
					return (userOffset - 1 - offset);
				}
				else {
					length += 1;
					userOffset++;
				}
			}
			else {
				
				return (userOffset - 1 - offset);
			}
		}
		
		return (userOffset - 1 - offset);
	}
	
	public static int parseTopic(String text, int offset, int maxWordCount) {
		int length = 0;
		int userOffset = offset + 1;
		while (userOffset < text.length()) {
			char c = text.charAt(userOffset);
			if (c == 'w' || c == 'W' || c == 'h' || c == 'H') {
				if((startsWithIgnoreCase(text, userOffset, "http://"))
						|| (startsWithIgnoreCase(text, userOffset, "www."))
						|| (startsWithIgnoreCase(text, userOffset, "wap."))
						|| (startsWithIgnoreCase(text, userOffset, "https://"))) {
					
					return (userOffset - 1 - offset);
				}
			}
			if(c >= 0x4e00 && c <= 0x9fff) {	//汉字
				if (length + 1 > maxWordCount) {
					
					return (userOffset - 1 - offset);
				}
				else {
					length += 1;
					userOffset++;
				}
			}
			else  if ((c >= 'a' && c<='z') 	   //字母数据
					||( c >='A' && c <= 'Z')
					|| (c >='0' && c <='9')) {
				if (length + 1 > maxWordCount) {
					
					return (userOffset - 1 - offset);
				}
				else {
					length += 1;
					userOffset++;
				}
			}
			else {
				
				return (userOffset - 1 - offset);
			}
		}
		
		return (userOffset - 1 - offset);
	}
	
	public static int parseHttp(String text, int offset, int maxWordCount) {
		int len = 0;
		boolean b1 = false, b2 = false, b3 = false, b4 = false;
		if((b1 = startsWithIgnoreCase(text, offset, "http://"))
				|| (b2 = startsWithIgnoreCase(text, offset, "www."))
				|| (b3 = startsWithIgnoreCase(text, offset, "wap."))
				|| (b3 = startsWithIgnoreCase(text, offset, "https://"))){	
			if(b1){
				len = "http://".length();
			} else if (b2) {
				len = "www.".length();
			} else if (b3) {
				len = "wap.".length();
			} else if (b4) {
				len = "https://".length();
			}
			int httpOffset = offset + len;
			while (httpOffset < text.length()) {
				char c = text.charAt(httpOffset);
				if((c >= 'A' && c <= 'Z')
						|| (c >= 'a' && c <= 'z')
						|| (c >= '0' && c <= '9') ) {
					len++;
				} else {
					if(URLCharTable.indexOf(c) >= 0) {
						len++;
					} else {
						break;
					}
				}
				httpOffset++;
			}
		}
		
		return len;
	}
	
}
