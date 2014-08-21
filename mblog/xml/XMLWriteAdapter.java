package mblog.xml;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLWriteAdapter implements IXMLWriter {

	private OutputStream mStream;
	private XMLTag mCurrentTag = null;
	private List<XMLTag> mTags = new ArrayList<XMLTag>();

	public XMLWriteAdapter(OutputStream stream) {
		// TODO Auto-generated constructor stub
		mStream = stream;
		XMLTagConstant.writeTag(stream, XMLTagConstant.XML_BANNER);
	}

	@Override
	public boolean addAttribute(String name, String value) {
		// TODO Auto-generated method stub
		boolean ret = false;
		if (mCurrentTag != null) {
			mCurrentTag.addAttribute(name, value);
			ret = true;
		}

		return ret;
	}

	@Override
	public boolean addChildTag(XMLTag tag) {
		// TODO Auto-generated method stub
		boolean ret = false;
		if (mCurrentTag != null) {
			mCurrentTag.addChild(tag);
			ret = true;
		}

		return ret;
	}

	@Override
	public XMLTag addChildTag(String name, String value) {
		// TODO Auto-generated method stub

		if (mCurrentTag != null) {
			return mCurrentTag.createChild(name, value);
		}
		return null;
	}

	@Override
	public boolean addEmptyTag(String value) {
		// TODO Auto-generated method stub
		boolean ret = false;
		if (mCurrentTag != null) {
			XMLTag tag = new XMLTag(null, value);
			mCurrentTag.addChild(tag);
			ret = true;
		}
		return ret;
	}

	@Override
	public XMLTag addTag(String name, String value) {
		// TODO Auto-generated method stub
		XMLTag tag = new XMLTag(name, value);
		mCurrentTag = tag;
		mTags.add(mCurrentTag);

		return mCurrentTag;
	}

	@Override
	public boolean addTag(XMLTag tag) {
		// TODO Auto-generated method stub
		mCurrentTag = tag;
		mTags.add(mCurrentTag);
		return true;
	}

	@Override
	public boolean addEmptyTag(String name, String value) {
		// TODO Auto-generated method stub
		mCurrentTag = null;
		mTags.add(new XMLTag(name, value));
		return true;
	}

	public void closeAllTag() {
		int size = mTags.size();
		for (int i = 0; i < size; i++) {
			XMLTag tag = mTags.get(i);
			tag.write(mStream);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
//		return super.toString();
		StringBuilder sb = new StringBuilder();
		
		sb.append(XMLTagConstant.XML_BANNER);
		int size = mTags.size();
		for (int i = 0; i < size; i++) {
			XMLTag tag = mTags.get(i);
			sb.append(tag.toString());
		}
		return sb.toString();
	}
}
