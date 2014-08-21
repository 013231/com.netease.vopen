package mblog.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XMLProcessor {

	public static boolean read(IXMLReader reader, InputStream stream,
			int bufferSize) throws XMLParseException, IOException {
		XMLParse parser = null;

		parser = new XMLParse(reader, stream, bufferSize);
		reader.startDocumentHandler();
		parser.doIt();
		reader.endDocumentHandler();

		if (parser != null)
			parser.finish();

		return true;
	}

	public static boolean read(IXMLReader xmlReader, File file) 
			throws XMLParseException, IOException {
		return read(xmlReader, file, 65536);
	}

	public static boolean read(IXMLReader xmlReader, File file, int bufferSize) 
			throws XMLParseException, IOException {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (IOException e) {
		}
		if (stream == null) {
			return false;
		}
		boolean code = read(xmlReader, stream, bufferSize);
		try {
			stream.close();
		} catch (IOException e) {
		}
		return code;
	}
}
