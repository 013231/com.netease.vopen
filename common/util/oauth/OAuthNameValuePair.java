package common.util.oauth;

import org.apache.http.NameValuePair;

public class OAuthNameValuePair implements NameValuePair, Comparable {

	String mName;
	String mValue;
	
	public OAuthNameValuePair(String name, String value) {
		mName = name;
		mValue = value;
	}
	
	@Override
	public String getName() {
		return mName;
	}

	@Override
	public String getValue() {
		return mValue;
	}

	@Override
	public int compareTo(Object obj) {
		if (obj != null && obj instanceof OAuthNameValuePair) {
			return mName.compareTo(((OAuthNameValuePair)obj).mName);
		}
		return 0;
	}

}
