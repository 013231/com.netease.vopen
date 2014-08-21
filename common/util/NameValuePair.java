package common.util;

public class NameValuePair implements Comparable {

	String mName;
	String mValue;
	
	public NameValuePair(String name, String value) {
		mName = name;
		mValue = value;
	}
	
	public String getName() {
		return mName;
	}

	public String getValue() {
		return mValue;
	}

	public int compareTo(Object obj) {
		if (obj != null && obj instanceof NameValuePair) {
			return mName.compareTo(((NameValuePair)obj).mName);
		}
		return 0;
	}

}
