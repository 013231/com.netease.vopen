package mblog.xml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;

public class XMLHandler extends DefaultHandler {

	static final String TAG = "XMLHandler";
	IXMLReader mReader;

	// add preFixMap to
	Map<String, String> mPreFixMap = new HashMap<String, String>();

	public XMLHandler(IXMLReader reader) {
		// TODO Auto-generated constructor stub
		mReader = reader;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
		mReader.characterDataHandler(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
		mReader.endDocumentHandler();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
		String mPreFix = mPreFixMap.get(uri);
		mReader.endElementHandler(uri, mPreFix, localName);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		// Log.i(TAG, "endPrefixMapping prefix is:" + prefix);
		super.endPrefixMapping(prefix);

	}

	public void startPrefixMapping(String prefix, String uri) {
		try {
			super.startPrefixMapping(prefix, uri);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPreFixMap.put(uri, prefix);
		mReader.setPrefixMap(mPreFixMap);
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
		mReader.startDocumentHandler();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub

		super.startElement(uri, localName, localName, attributes);
		String mPreFix = mPreFixMap.get(uri);
		mReader.startElementHandler(uri, mPreFix, localName, attributes);
	}

}
