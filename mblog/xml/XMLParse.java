package mblog.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XMLParse {

	InputStream mInputSteam;
	IXMLReader mReader;

	public XMLParse(IXMLReader reader, InputStream stream, int bufferSize) {
		// TODO Auto-generated constructor stub
		mReader = reader;
		mInputSteam = stream;
	}

	public void doIt() throws XMLParseException, IOException {
		// TODO Auto-generated method stub
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		SAXParser sp;
		try {
			sp = spf.newSAXParser();
			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader */
			XMLHandler myExampleHandler = new XMLHandler(mReader);
			xr.setContentHandler(myExampleHandler);

			/* Parse the xml-data from our URL. */
			InputSource inputSource = new InputSource(mInputSteam);
//			inputSource.setEncoding("UTF-8");
			xr.parse(inputSource);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new XMLParseException(e.getMessage());
		}
	}

	public void finish() {
		// TODO Auto-generated method stub

	}
}
