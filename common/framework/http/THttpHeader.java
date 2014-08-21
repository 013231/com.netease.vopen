package common.framework.http;

public class THttpHeader {

	String mKey;
	String mValue;
	
	public THttpHeader(String key, String value) {
		mKey = key;
		mValue = value;
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String key) {
		this.mKey = key;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue = value;
	}
}
