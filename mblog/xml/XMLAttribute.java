package mblog.xml;

import java.io.IOException;
import java.io.OutputStream;

public class XMLAttribute {

	protected String mName;
	protected String mValue;

	public XMLAttribute(String name, String value) {
		// TODO Auto-generated constructor stub
		mName = name;
		mValue = value;
	}

	/**
	 * @return the mKey
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param mKey
	 *            the mKey to set
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * @return the mValue
	 */
	public String getValue() {
		return mValue;
	}
	
	/**
	 * @param value Default Value
	 * @return
	 */
	public int getValue_Int(int value) {
		if (mValue != null) {
			try {
				return Integer.parseInt(mValue);
			} catch (Exception e) {
			}
		}
		
		return value;
	}
	
	public long getValue_Long(long value) {
		if (mValue != null) {
			try {
				return Long.parseLong(mValue);
			} catch (Exception e) {
			}
		}
		
		return value;
	}
	
	/**
	 * @param value Default Value
	 * @return
	 */
	public float getValue_Float(float value) {
		if (mValue != null) {
			try {
				return Float.parseFloat(mValue);
			} catch (Exception e) {
			}
		}
		
		return value;
	}

	/**
	 * @param value Default Value
	 * @return
	 */
	public double getValue_Double(double value) {
		if (mValue != null) {
			try {
				return Double.parseDouble(mValue);
			} catch (Exception e) {
			}
		}
		
		return value;
	}
	
	/**
	 * @param mValue
	 *            the mValue to set
	 */
	public void setValue(String value) {
		this.mValue = value;
	}

	void writeAttribute(OutputStream output) throws IOException {
		if (mValue == null || mName == null)
			return;
		XMLTagConstant.writeTag(output, XMLTagConstant.TWO_SPACES);
		output.write(mName.getBytes());
		XMLTagConstant.writeTag(output, XMLTagConstant.EQUALS_QUOTE);
		output.write(toXMLString(mValue).getBytes());
		XMLTagConstant.writeTag(output, XMLTagConstant.QUOTE);
		// ZLXMLTagConstant.writeTag(output, ZLXMLTagConstant.TWO_SPACES);
	}
	
	protected static String toXMLString(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			default:
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (o instanceof XMLAttribute) {
			XMLAttribute another = (XMLAttribute) o;
			boolean flagName = mName == null ? another.getName() == null
					: mName.equals(another.getName());
			boolean flagValue = mValue == null ? another.getValue() == null
					: mValue.equals(another.getValue());

			return flagValue && flagName;
		} else {
			return false;
		}

	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
//		return super.toString();
		StringBuilder sb = new StringBuilder();
		
		sb.append(XMLTagConstant.TWO_SPACES);
		sb.append(mName);
		sb.append(XMLTagConstant.EQUALS_QUOTE);
		sb.append(toXMLString(mValue));
		sb.append(XMLTagConstant.QUOTE);
		
		return sb.toString();
	}
}
