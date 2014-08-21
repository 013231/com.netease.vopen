package common.util;

import java.io.UnsupportedEncodingException;

public class URLEncoder {

	static final String digits = "0123456789ABCDEF"; //$NON-NLS-1$

	/**
	 * Prevents this class from being instantiated.
	 */
	private URLEncoder() {
	}

	/**
	 * Encodes a given string {@code s} in a x-www-form-urlencoded string using
	 * the specified encoding scheme {@code enc}.
	 * <p>
	 * All characters except letters ('a'..'z', 'A'..'Z') and numbers ('0'..'9')
	 * and characters '.', '-', '*', '_' are converted into their hexadecimal
	 * value prepended by '%'. For example: '#' -> %. In addition, spaces are
	 * substituted by '+'
	 * 
	 * @param s
	 *            the string to be encoded.
	 * @return the encoded string.
	 * 
	 */

	public static String encode(String s) {
		// Guess a bit bigger for encoded form
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9') || ".-*_".indexOf(ch) > -1) { //$NON-NLS-1$
				buf.append(ch);
			} else if (ch == ' ') {
				buf.append("%20");
			} else {
				char[] array = new char[]{ch};
				String b = new String(array);
				byte[] bytes = b.getBytes();
				for (int j = 0; j < bytes.length; j++) {
					buf.append('%');
					buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
					buf.append(digits.charAt(bytes[j] & 0xf));
				}
			}
		}
		return buf.toString();
	}

	/**
	 * Encodes the given string {@code s} in a x-www-form-urlencoded string
	 * using the specified encoding scheme {@code enc}.
	 * <p>
	 * All characters except letters ('a'..'z', 'A'..'Z') and numbers ('0'..'9')
	 * and characters '.', '-', '*', '_' are converted into their hexadecimal
	 * value prepended by '%'. For example: '#' -> %. In addition, spaces are
	 * substituted by '+'
	 * 
	 * @param s
	 *            the string to be encoded.
	 * @param enc
	 *            the encoding scheme to be used.
	 * @return the encoded string.
	 * @throws UnsupportedEncodingException
	 *             if the specified encoding scheme is invalid.
	 */
	public static String encode(String s, String enc)
	{

		if (s == null || enc == null) {
			return null;
		}
		// check for UnsupportedEncodingException
		try {
			"".getBytes(enc); //$NON-NLS-1$	
		} catch (Exception e) {
			// TODO: handle exception
		}

		// Guess a bit bigger for encoded form
		
		StringBuffer buf = new StringBuffer();
		
		int start = -1;
		
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9') || " .-*_".indexOf(ch) > -1) 
			{
				if (start >= 0) {
					convert(s.substring(start, i), buf, enc);
					start = -1;
				}
				if (ch != ' ') {
					buf.append(ch);
				} else {
					buf.append("%20");
				}
			} else {
				if (start < 0) {
					start = i;
				}
			}
		}
		
		if (start >= 0) {
			convert(s.substring(start, s.length()), buf, enc);
		}
		return buf.toString();
	}
	
	/**
	 * <p>
	 * All characters except letters ('a'..'z', 'A'..'Z') and numbers ('0'..'9')
	 * and characters '.', '-', '~', '_' are converted into their hexadecimal
	 * value prepended by '%'. For example: '#' -> %. In addition, spaces are
	 * substituted by '+'
	 * 
	 * @param s
	 *            the string to be encoded.
	 * @param enc
	 *            the encoding scheme to be used.
	 * @return the encoded string.
	 * @throws UnsupportedEncodingException
	 *             if the specified encoding scheme is invalid.
	 */
	public static String percentEncode(String s, String enc) {
		if (s == null || enc == null) {
			return null;
		}
		// check for UnsupportedEncodingException
		try {
			"".getBytes(enc); //$NON-NLS-1$	
		} catch (Exception e) {
			// TODO: handle exception
		}

		// Guess a bit bigger for encoded form
		
		StringBuffer buf = new StringBuffer();
		
		int start = -1;
		
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9') || " .-_~".indexOf(ch) > -1) 
			{
				if (start >= 0) {
					convert(s.substring(start, i), buf, enc);
					start = -1;
				}
				if (ch != ' ') {
					buf.append(ch);
				} else {
					buf.append("%20");
				}
			} else {
				if (start < 0) {
					start = i;
				}
			}
		}
		
		if (start >= 0) {
			convert(s.substring(start, s.length()), buf, enc);
		}
		return buf.toString();
	}

	private static void convert(String s, StringBuffer buf, String enc) {
		try {
			byte[] bytes = s.getBytes(enc);
			for (int j = 0; j < bytes.length; j++) {
				buf.append("%");
				char c1 = digits.charAt((bytes[j] & 0xf0) >> 4);
				char c2 = digits.charAt(bytes[j] & 0xf);
			
				buf.append(c1);
				buf.append(c2);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
