package common.util;

import java.util.Enumeration;
import java.util.Hashtable;


public class HtmlConverter {


	/**
	 * main function to get text from html node
	 */
	public static String html2text(String html) {
		StringBuffer sb = new StringBuffer(html.length());
		char[] data = html.toCharArray();
		int start = 0;
		boolean previousIsPre = false;
		Token token = null;

		boolean isBodyStarted = true;
		for (;;) {
			token = parse(data, start, previousIsPre);
			if (token == null  )
				break;

			previousIsPre = token.isPreTag();
			if(isBodyStarted){
				sb.append(token.getText());
			}

			// if one paragraph ends, append '\n'
			if (token.toString().startsWith("</p")
					|| token.toString().startsWith("</br")
					|| token.toString().startsWith("</div")) {
				sb = sb.append("\n");
			}

			start += token.getLength();
		}
		return sb.toString();
	}
	
	/**
	 * convert html special characters
	 */
	public static String convertHtmlSpecialCharacters(String body) {
		Enumeration keys = specialCharTable.keys();

		while (keys.hasMoreElements()) {
			String strSpecial = (String) keys.nextElement();
			String newStr = (String)specialCharTable.get(strSpecial);
			body = BaseUtil.replace(body,strSpecial,newStr);
//			int index = 0;
//			while ((index = body.indexOf(strSpecial)) != -1) {
//				
//				body = body.substring(0, index)
//						+ specialCharTable.get(strSpecial)
//						+ body.substring(index + strSpecial.length());
//			}
		}

		return body;
	}

	public static final Hashtable specialCharTable = new Hashtable();

	static {
		specialCharTable.put("&lt;", "<");
		specialCharTable.put("&gt;", ">");
		specialCharTable.put("&amp;", "&");
		specialCharTable.put("&ldquo;", "\"");
		specialCharTable.put("&rdquo;", "\"");
		specialCharTable.put("&quot;", "\"");
		specialCharTable.put("&nbsp;", " ");
		specialCharTable.put("&emsp;", " ");
		specialCharTable.put("&ensp;", " ");
		specialCharTable.put("&times;", "×");
		specialCharTable.put("&divide;", "÷");
		specialCharTable.put("&copy;", "©");
		specialCharTable.put("&reg;", "®");
		specialCharTable.put("™", "™");
	};


	private static Token parse(char[] data, int start, boolean previousIsPre) {
		if (start >= data.length)
			return null;
		// try to read next char:
		char c = data[start];
		if (c == '<') {
			// this is a tag or comment or script:
			int end_index = indexOf(data, start + 1, '>');
			if (end_index == (-1)) {
				// the left is all text!
				return new Token(Token.TOKEN_TEXT, data, start, data.length,
						previousIsPre);
			}
			String s = new String(data, start, end_index - start + 1);
			// now we got s="<...>":
			if (s.startsWith("<!--")) { // this is a comment!
				int end_comment_index = indexOf(data, start + 1, "-->");
				if (end_comment_index == (-1)) {
					// illegal end, but treat as comment:
					return new Token(Token.TOKEN_COMMENT, data, start,
							data.length, previousIsPre);
				} else
					return new Token(Token.TOKEN_COMMENT, data, start,
							end_comment_index + 3, previousIsPre);
			}
			String s_lowerCase = s.toLowerCase();
			if(s_lowerCase.startsWith("<style")){
				
				int end_script_index = indexOf(data, start + 1, "</style>");
				if (end_script_index == (-1))
					// illegal end, but treat as script:
					return new Token(Token.TOKEN_STYPE, data, start,
							data.length, previousIsPre);
				else
					return new Token(Token.TOKEN_STYPE, data, start,
							end_script_index + 9, previousIsPre);
				
			}else if (s_lowerCase.startsWith("<script")) { // this is a script:
				int end_script_index = indexOf(data, start + 1, "</script>");
				if (end_script_index == (-1))
					// illegal end, but treat as script:
					return new Token(Token.TOKEN_SCRIPT, data, start,
							data.length, previousIsPre);
				else
					return new Token(Token.TOKEN_SCRIPT, data, start,
							end_script_index + 9, previousIsPre);
			} else { // this is a tag:
				return new Token(Token.TOKEN_TAG, data, start, start
						+ s.length(), previousIsPre);
			}
		}
		// this is a text:
		int next_tag_index = indexOf(data, start + 1, '<');
		if (next_tag_index == (-1))
			return new Token(Token.TOKEN_TEXT, data, start, data.length,
					previousIsPre);
		return new Token(Token.TOKEN_TEXT, data, start, next_tag_index,
				previousIsPre);
	}

	private static int indexOf(char[] data, int start, String s) {
		char[] ss = s.toCharArray();
		// TODO: performance can improve!
		for (int i = start; i < (data.length - ss.length); i++) {
			// compare from data[i] with ss[0]:
			boolean match = true;
			for (int j = 0; j < ss.length; j++) {
				if (data[i + j] != ss[j]) {
					match = false;
					break;
				}
			}
			if (match)
				return i;
		}
		return (-1);
	}

	private static int indexOf(char[] data, int start, char c) {
		for (int i = start; i < data.length; i++) {
			if (data[i] == c)
				return i;
		}
		return (-1);
	}
}

class Token {

	public static final int TOKEN_TEXT = 0; // html text.
	public static final int TOKEN_COMMENT = 1; // comment like <!--
	// comments... -->
	public static final int TOKEN_TAG = 2; // tag like <pre>, <font>,
	// etc.
	public static final int TOKEN_SCRIPT = 3;
	
	public static final int TOKEN_STYPE = 4;
	
	private static final char[] TAG_BR = "<br".toCharArray();
	private static final char[] TAG_P = "<p".toCharArray();
	private static final char[] TAG_LI = "<li".toCharArray();
	private static final char[] TAG_PRE = "<pre".toCharArray();
	private static final char[] TAG_HR = "<hr".toCharArray();
	private static final char[] END_TAG_TD = "</td>".toCharArray();
	private static final char[] END_TAG_TR = "</tr>".toCharArray();
	private static final char[] END_TAG_LI = "</li>".toCharArray();
	private static final Hashtable SPECIAL_CHARS = new Hashtable();
	private int type;
	private String html; // original html
	private String text = null; // text!
	private int length = 0; // html length
	private boolean isPre = false; // isPre tag?
	static {

		// SPECIAL_CHARS.put(""", "\"");
		SPECIAL_CHARS.put("<", "<");
		SPECIAL_CHARS.put(">", ">");
		SPECIAL_CHARS.put("&", "&");
		SPECIAL_CHARS.put("?", "(r)");
		SPECIAL_CHARS.put("?", "(c)");
		SPECIAL_CHARS.put(" ", " ");
		SPECIAL_CHARS.put("￡", "?");
	}

	public Token(int type, char[] data, int start, int end,
			boolean previousIsPre) {
		this.type = type;
		this.length = end - start;
		this.html = new String(data, start, length);
//		 System.out.println("[Token] html=" + html + ".");
		parseText(previousIsPre);
//		 System.out.println("[Token] text=" + text + ".");
	}

	public int getLength() {
		return length;
	}

	public boolean isPreTag() {
		return isPre;
	}

	private void parseText(boolean previousIsPre) {
		if (type == TOKEN_TAG) {
			char[] cs = html.toCharArray();
			if (compareTag(TAG_BR, cs) || compareTag(TAG_P, cs))
				text = "\n";
			else if (compareTag(TAG_LI, cs))
				text = "\n* ";
			else if (compareTag(TAG_PRE, cs))
				isPre = true;
			else if (compareTag(TAG_HR, cs))
				text = "\n--------\n";
			else if (compareString(END_TAG_TD, cs))
				text = "\t";
			else if (compareString(END_TAG_TR, cs)
					|| compareString(END_TAG_LI, cs))
				text = "\n";
		}
		// text token:
		else if (type == TOKEN_TEXT) {
			text = toText(html, previousIsPre);
		}
	}

	public String getText() {
		return text == null ? "" : text;
	}

	private String toText(String html, final boolean isPre) {
		char[] cs = html.toCharArray();
		StringBuffer buffer = new StringBuffer(cs.length);
		int start = 0;
		boolean continueSpace = false;
		char current, next;
		for (;;) {
			if (start >= cs.length)
				break;
			current = cs[start]; // read current char
			if (start + 1 < cs.length) // and next char
				next = cs[start + 1];
			else
				next = '\0';
			if (current == ' ') {
				if (isPre || !continueSpace)
					buffer = buffer.append(' ');
				continueSpace = true;
				// continue loop:
				start++;
				continue;
			}
			// not ' ', so:
			if (current == '\r' && next == '\n') {
				if (isPre)
					buffer = buffer.append('\n');
				// continue loop:
				start += 2;
				continue;
			}
			if (current == '\n' || current == '\r') {
				if (isPre)
					buffer = buffer.append('\n');
				// continue loop:
				start++;
				continue;
			}
			// cannot continue space:
			continueSpace = false;
			if (current == '&') {
				// maybe special char:
				int length = readUtil(cs, start, ';', 10);
				if (length == (-1)) { // just '&':
					buffer = buffer.append('&');
					// continue loop:
					start++;
					continue;
				} else { // check if special character:
					String spec = new String(cs, start, length);
					String specChar = (String) SPECIAL_CHARS.get(spec);
					if (specChar != null) { // special chars!
						buffer = buffer.append(specChar);
						// continue loop:
						start += length;
						continue;
					} else { // check if like '?':
						if (next == '#') { // maybe a char
							String num = new String(cs, start + 2, length - 3);
							try {
								int code = Integer.parseInt(num);
								if (code > 0 && code < 65536) { // this is a
									// special char:
									buffer = buffer.append((char) code);
									// continue loop:
									start++;
									continue;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							// just normal char:
							buffer = buffer.append("&#");
							// continue loop:
							start += 2;
							continue;
						} else { // just '&':
							buffer = buffer.append('&');
							// continue loop:
							start++;
							continue;
						}
					}
				}
			} else { // just a normal char!
				buffer = buffer.append(current);
				// continue loop:
				start++;
				continue;
			}
		}

		String result = buffer.toString();

		result = HtmlConverter.convertHtmlSpecialCharacters(result);

		return result;
	} // read from cs[start] util meet the specified char 'util',

	// or null if not found:

	

	private int readUtil(final char[] cs, final int start, final char util,
			final int maxLength) {
		int end = start + maxLength;
		if (end > cs.length)
			end = cs.length;
		for (int i = start; i < end ; i++) {
			if (cs[i] == util) {
				return i - start + 1;
			}
		}
		
		return (-1);
	} // compare standard tag "<input" with tag "<INPUT value=aa>"

	private boolean compareTag(final char[] ori_tag, char[] tag) {
		if (ori_tag.length >= tag.length)
			return false;
		for (int i = 0; i < ori_tag.length; i++) {
			if (Character.toLowerCase(tag[i]) != ori_tag[i])
				return false;
		}
		// the following char should not be a-z:
		if (tag.length > ori_tag.length) {
			char c = Character.toLowerCase(tag[ori_tag.length]);
			if (c < 'a' || c > 'z')
				return true;
			return false;
		}
		return true;
	}

	private boolean compareString(final char[] ori, char[] comp) {
		if (ori.length > comp.length)
			return false;
		for (int i = 0; i < ori.length; i++) {
			if (Character.toLowerCase(comp[i]) != ori[i])
				return false;
		}
		return true;
	}

	public String toString() {
		return html;
	}
}
