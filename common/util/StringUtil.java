package common.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.vopen.pal.Constants;

public class StringUtil {

	public static boolean isEqual(String str1, String str2) {
		if (isEmpty(str1)) {
			return isEmpty(str2);
		} else {
			return str1.equals(str2);
		}
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String makeSafe(String paramString) {
		if (paramString == null)
			;
		for (String str = "";; str = paramString)
			return str;
	}

	public static String getPlidUrl(String plid) {

		return Constants.url_plist_head + plid + Constants.url_plist_end;

	}

	/**
	 * 替换HTML字符.
	 */
	public static String htmlDecoder(String src) throws Exception {

		if (src == null || src.equals("")) {
			return "";
		}

		String dst = src;
		dst = replaceAll(dst, "&lt;", "<");
		dst = replaceAll(dst, "&rt;", ">");
		dst = replaceAll(dst, "&quot;", "\"");
		dst = replaceAll(dst, "&039;", "'");
		dst = replaceAll(dst, "&nbsp;", " ");
		dst = replaceAll(dst, "&nbsp", " ");
		dst = replaceAll(dst, "<br>", "\n");
		dst = replaceAll(dst, "\r\n", "\n");
		dst = replaceAll(dst, "&#8826;", "??");
		dst = replaceAll(dst, "&#8226;", "??");
		dst = replaceAll(dst, "&#9642;", "??");
		return dst;
	}

	public static String replaceAll(String src, String fnd, String rep)
			throws Exception {

		if (src == null || src.equals("")) {
			return "";
		}

		String dst = src;

		int idx = dst.indexOf(fnd);

		while (idx >= 0) {
			dst = dst.substring(0, idx) + rep
					+ dst.substring(idx + fnd.length(), dst.length());
			idx = dst.indexOf(fnd, idx + rep.length());
		}

		return dst;
	}

	public static boolean checkStr(String str) {

		boolean _is = false;

		if (null != str && !"".equals(str)) {
			_is = true;
		}

		return _is;

	}

	public static boolean checkObj(Object obj) {
		boolean _is = false;

		if (null != obj) {
			_is = true;
		}

		return _is;
	}

	public static String JsonToString(JSONObject json) {

		if (null != json) {

			return json.toString();
		} else {

			return "";
		}

	}

	public static JSONObject StringToJson(String str) {

		if (null != str) {
			try {
				JSONObject json = new JSONObject(str);
				return json;
			} catch (JSONException e) {
				return null;
			}
		} else {

			return null;
		}
	}

	public static JSONArray StringToJsonArray(String str) {

		if (null != str) {
			try {
				JSONArray jsonArray = new JSONArray(str);
				return jsonArray;
			} catch (JSONException e) {
				return null;
			}
		} else {

			return null;
		}
	}

	public static String addZerotoFront2(int src) {
		String rst = "";

		if (src > 9) {
			rst = "" + src;
		} else {
			rst = "0" + src;
		}

		return rst;
	}

	public static String getNameFromPath(String path) {
		if (!TextUtils.isEmpty(path)) {
			return path.substring(path.lastIndexOf("/") + 1);
		}
		return null;
	}

	/**
	 * ASCII表中可见字符从!开始，偏移位值为33(Decimal)
	 */
	static final char DBC_CHAR_START = 33; // 半角!

	/**
	 * ASCII表中可见字符到~结束，偏移位值为126(Decimal)
	 */
	static final char DBC_CHAR_END = 126; // 半角~

	/**
	 * 全角对应于ASCII表的可见字符从！开始，偏移值为65281
	 */
	static final char SBC_CHAR_START = 65281; // 全角！

	/**
	 * 全角对应于ASCII表的可见字符到～结束，偏移值为65374
	 */
	static final char SBC_CHAR_END = 65374; // 全角～

	/**
	 * ASCII表中除空格外的可见字符与对应的全角字符的相对偏移
	 */
	static final int CONVERT_STEP = 65248; // 全角半角转换间隔

	/**
	 * 全角空格的值，它没有遵从与ASCII的相对偏移，必须单独处理
	 */
	static final char SBC_SPACE = 12288; // 全角空格 12288

	/**
	 * 半角空格的值，在ASCII中为32(Decimal)
	 */
	static final char DBC_SPACE = ' '; // 半角空格

	/**
	 * 半角转全角字符
	 * 
	 * @param src
	 * @return
	 */
	public static String bj2qj(String src) {
		if (src == null) {
			return src;
		}
		StringBuilder buf = new StringBuilder(src.length());
		char[] ca = src.toCharArray();
		for (int i = 0; i < ca.length; i++) {
			if (ca[i] == DBC_SPACE) { // 如果是半角空格，直接用全角空格替代
				buf.append(SBC_SPACE);
			} else if ((ca[i] >= DBC_CHAR_START) && (ca[i] <= DBC_CHAR_END)) { // 字符是!到~之间的可见字符
				buf.append((char) (ca[i] + CONVERT_STEP));
			} else { // 不对空格以及ascii表中其他可见字符之外的字符做任何处理
				buf.append(ca[i]);
			}
		}
		return buf.toString();
	}

	/**
	 * <PRE>
	 * 全角字符->半角字符转换   
	 * 只处理全角的空格，全角！到全角～之间的字符，忽略其他
	 * </PRE>
	 */
	public static String qj2bj(String src) {
		if (src == null) {
			return src;
		}
		StringBuilder buf = new StringBuilder(src.length());
		char[] ca = src.toCharArray();
		for (int i = 0; i < src.length(); i++) {
			if (ca[i] >= SBC_CHAR_START && ca[i] <= SBC_CHAR_END) { // 如果位于全角！到全角～区间内
				buf.append((char) (ca[i] - CONVERT_STEP));
			} else if (ca[i] == SBC_SPACE) { // 如果是全角空格
				buf.append(DBC_SPACE);
			} else { // 不处理全角空格，全角！到全角～区间外的字符
				buf.append(ca[i]);
			}
		}
		return buf.toString();
	}
}
