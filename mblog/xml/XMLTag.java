package mblog.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import android.text.TextUtils;

public class XMLTag extends XMLAttribute implements Cloneable {

	int TAGTYPE_TEXT = 1;
	int TAGTYPE_COMMON = 2;

	List<XMLAttribute> mAttributes;

	List<XMLTag> mChildren;
	int mType = TAGTYPE_COMMON;

	public XMLTag(String name) {
		this(name, null);
	}

	public XMLTag(String name, String value) {
		// TODO Auto-generated constructor stub
		super(name, value);
		if (TextUtils.isEmpty(name)) {
			mType = TAGTYPE_TEXT;
		} else {
			mType = TAGTYPE_COMMON;
		}
	}
	
	public XMLTag(XMLTag xmlTag) {
		super(xmlTag.getName(), xmlTag.getValue());
		copy(xmlTag);
	}
	
	public void copy(XMLTag xmlTag) {
		if (xmlTag == null) {
			return ;
		}
		
		setValue(xmlTag.getValue());
		
		
		if (xmlTag.mAttributes != null) {
			if (mAttributes == null) {
				mAttributes = new LinkedList<XMLAttribute>();
			}
			synchronized (mAttributes) {
				mAttributes.clear();
				
				for (XMLAttribute attribute : xmlTag.mAttributes) {
					addAttribute(attribute.getName(), attribute.getValue());
				}
			}
		}
		else {
			mAttributes = null;
		}
		
		if (xmlTag.mChildren != null) {
			if (mChildren == null) {
				mChildren = new LinkedList<XMLTag>();
			}
			synchronized (mChildren) {
				mChildren.clear();
				
				for (XMLTag child : xmlTag.mChildren) {
					XMLTag childtag = createChild(child.getName(), child.getValue());
					if (childtag != null) {
						childtag.copy(child);
					}
				}
			}
		}
		else {
			mChildren = null;
		}
	}
	
	public void removeChild(XMLTag xmlTag) {
		if (mChildren != null && xmlTag != null) {
			synchronized (mChildren) {
				mChildren.remove(xmlTag);
			}
		}
	}
	
	public void removeAllChildren() {
		if (mChildren != null) {
			synchronized (mChildren) {
				mChildren.clear();
			}
			mChildren = null;
		}
	}
	
	public void removeAllAttributes() {
		if (mAttributes != null) {
			mAttributes.clear();
			mAttributes = null;
		}
	}
	
	protected boolean hasSameValue(XMLTag xmltag) {
		if (xmltag == null) {
			return false;
		}
		if (mValue != null) {
			return mValue.equals(xmltag.mValue);
		}
		
		return xmltag.mValue == null;
	}
	
	public static boolean hasSameValue(XMLTag tag1, XMLTag tag2) {
		if (tag1 == null) {
			return tag2 == null;
		}
		
		return tag1.hasSameValue(tag2);
	}
	
	@Override
	public XMLTag clone() {
		XMLTag xmlTag = new XMLTag(getName(), getValue());
		if (mChildren != null) {
			for (XMLTag child : mChildren) {
				xmlTag.addChild((XMLTag)child.clone());
			}
		}

		if (mAttributes != null) {
			for (XMLAttribute attribute : mAttributes) {
				xmlTag.addAttribute(attribute.getName(), attribute.getValue());
			}
		}

		return xmlTag;
	}
	
	public void readAttributes(Attributes attributes, Map<String, String> prefixMap) {
		if (attributes != null) {
			if (prefixMap != null && prefixMap.size() > 0) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String uri = attributes.getURI(i);
					String prefix = null;
					if (uri != null && uri.length() > 0) {
						prefix = prefixMap.get(uri);
					}
					
					String name = attributes.getLocalName(i);
					if (prefix != null) {
						name = prefix + ':' + name;
					}
					
					addAttribute(name, attributes.getValue(i));
				}
			}
			else {
				for (int i = 0; i < attributes.getLength(); i++) {
					addAttribute(attributes.getLocalName(i), attributes.getValue(i));
				}
			}
		}
	}

	public boolean hasChild() {
		return mChildren != null && mChildren.size() > 0 && mType == TAGTYPE_COMMON;
	}

	public boolean hasAttribute() {
		return mAttributes == null || mAttributes.size() <= 0;
	}

	public boolean isEmptyTag() {
		return mType == TAGTYPE_TEXT;
	}

	public void addAttribute(String key, String value) {
		addAttribute(new XMLAttribute(key, value));
	}
	
	public void replaceAddAttribute(String key, String value) {
		XMLAttribute attribute = getAttribute(key);
		if (attribute != null) {
			attribute.setValue(value);
		}
		else {
			addAttribute(key, value);
		}
	}
	
	public void addAttribute(XMLAttribute attribute) {
		if (mAttributes == null) {
			mAttributes = new LinkedList<XMLAttribute>();
		}
		
		//TODO XML属性唯一性判断
//		for (XMLAttribute attr : mAttributes) {
//			if (attr.getName().equals(attribute.getName())) {
//				attr.setValue(attribute.getValue());
//				return ;
//			}
//		}
		
		synchronized (mAttributes) {
			mAttributes.add(attribute);
		}
	}
	
	public List<XMLAttribute> getAttributes() {
		return mAttributes;
	}
	
	public List<XMLTag> getChildren() {
		return mChildren;
	}
	
	public String getAttribute_String(String key) {
		return getAttribute_String(key, null);
	}
	
	public String getAttribute_String(String key, String value) {
		if (mAttributes != null && key != null) {
			XMLAttribute attribute = getAttribute(key);
			if (attribute != null) {
				return attribute.getValue();
			}
		}
		
		return value;
	}
	
	public XMLAttribute getAttribute(String key) {
		if (mAttributes != null && key != null) {
			synchronized (mAttributes) {
				for (XMLAttribute attribute : mAttributes) {
					if (attribute.getName().equals(key)) {
						return attribute;
					}
				}
			}
		}
		
		return null;
	}
	
	public int getAttribute_Int(String key, int value) {
		if (mAttributes != null && key != null) {
			XMLAttribute attribute = getAttribute(key);
			if (attribute != null) {
				try {
					value = Integer.parseInt(attribute.getValue());
				} catch (Exception e) {
				}
			}
		}
		
		return value;
	}
	
	public long getAttribute_Long(String key, long value) {
		if (mAttributes != null && key != null) {
			XMLAttribute attribute = getAttribute(key);
			if (attribute != null) {
				try {
					value = Long.parseLong(attribute.getValue());
				} catch (Exception e) {
				}
			}
		}
		
		return value;
	}
	
	public float getAttribute_Float(String key, float value) {
		if (mAttributes != null && key != null) {
			XMLAttribute attribute = getAttribute(key);
			if (attribute != null) {
				try {
					value = Float.parseFloat(attribute.getValue());
				} catch (Exception e) {
				}
			}
		}
		
		return value;
	}
	
	public double getAttribute_Double(String key, double value) {
		if (mAttributes != null && key != null) {
			XMLAttribute attribute = getAttribute(key);
			if (attribute != null) {
				try {
					value = Double.parseDouble(attribute.getValue());
				} catch (Exception e) {
				}
			}
		}
		
		return value;
	}
	
	public XMLTag getChild(String key) {
		if (mChildren != null && key != null) {
			synchronized (mChildren) {
				for (XMLTag xmlTag : mChildren) {
					if (key.equals(xmlTag.getName())) {
						return xmlTag;
					}
				}
			}
		}
		
		return null;
	}
	
	public List<XMLTag> getChildren(String key) {
		if (mChildren != null) {
			List<XMLTag> children = new LinkedList<XMLTag>();
			synchronized (mChildren) {
				if (key == null) {
						for (XMLTag xmlTag : mChildren) {
							if (xmlTag.getName() == null 
									|| xmlTag.getName().length() == 0) {
								children.add(xmlTag);
							}
						}
				}
				else {
					for (XMLTag xmlTag : mChildren) {
						if (key.equals(xmlTag.getName())) {
							children.add(xmlTag);
						}
					}
				}
			}
			if (children.size() > 0) {
				return children;
			}
		}
		
		return null;
	}

	public void addData(String data) {
		if (!TextUtils.isEmpty(data)) {
			XMLTag tag = createChild(null, data);
			addChild(tag);
		}
	}
	
	public void endTag() {
		
	}

	private void writeStart(OutputStream stream) throws IOException {

		if (!isEmptyTag()) {
			XMLTagConstant.writeTag(stream, XMLTagConstant.LANGLE);
			stream.write(mName.getBytes());

			if (mAttributes != null) {
				synchronized (mAttributes) {
					for (XMLAttribute attribute : mAttributes) {
						attribute.writeAttribute(stream);
					}
				}
			}

			XMLTagConstant.writeTag(stream, XMLTagConstant.RANGLE);

			if (!TextUtils.isEmpty(mValue)) {
				stream.write(toXMLString(mValue).getBytes());
			}
			
			if (mChildren != null) {
				synchronized (mChildren) {
					for (XMLTag tag : mChildren) {
						tag.writeStart(stream);
						tag.writeEnd(stream);
					}
				}
			} else {
				// ZLXMLTagConstant.writeTag(stream,ZLXMLTagConstant.SLASH);

			}

		} else {
			stream.write(toXMLString(mValue).getBytes());
		}

	}

	private void writeEnd(OutputStream stream) throws IOException {
		if (!isEmptyTag() /* && hasChild() */) {
			XMLTagConstant.writeTag(stream, XMLTagConstant.LANGLE_SLASH);
			stream.write(mName.getBytes());
			XMLTagConstant.writeTag(stream, XMLTagConstant.RANGLE_EOL);
		}
	}

	public XMLTag createChild(String name, String value) {
		// TODO Auto-generated constructor stub
		XMLTag tag = new XMLTag(name, value);
		return addChild(tag);
	}

	public XMLTag createChild(String name) {
		return createChild(name, null);
	}

	public XMLTag addChild(XMLTag tag) {
		if (tag == null) {
			return tag;
		}
		if (mChildren == null) {
			mChildren = new LinkedList<XMLTag>();
		}
		synchronized (mChildren) {
			mChildren.add(tag);
		}
		return tag;
	}

	public boolean write(OutputStream stream) {
		boolean ret = false;
		try {
			writeStart(stream);
			writeEnd(stream);
			ret = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (!isEmptyTag()) {
			sb.append(XMLTagConstant.LANGLE);
			sb.append(mName);

			if (mAttributes != null) {
				synchronized (mAttributes) {
					for (XMLAttribute attribute : mAttributes) {
						sb.append(attribute.toString());
					}
				}
			}

			sb.append(XMLTagConstant.RANGLE);

			if (!TextUtils.isEmpty(mValue)) {
				sb.append(toXMLString(mValue));
			}
			if (mChildren != null) {
				// write child
				synchronized (mChildren) {
					for (XMLTag tag : mChildren) {
						sb.append(tag.toString());
					}
				}
			}

		} else {
			if (!TextUtils.isEmpty(mValue)) {
				sb.append(toXMLString(mValue));
			}
		}
		
		if (!isEmptyTag() /* && hasChild() */) {
			sb.append(XMLTagConstant.LANGLE_SLASH);
			sb.append(mName);
			sb.append(XMLTagConstant.RANGLE_EOL);
		}
		return sb.toString();
	}
}
