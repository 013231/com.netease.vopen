package mblog.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.xml.sax.Attributes;

public abstract class XMLReaderAdapter implements IXMLReader {

	public boolean read(File file) throws XMLParseException, IOException {
		return XMLProcessor.read(this, file);
	}

	public boolean read(InputStream stream) throws XMLParseException, IOException {
		return XMLProcessor.read(this, stream, 65536);
	}

	@Override
	public abstract void characterDataHandler(char[] ch, int start, int length);

	@Override
	public void endDocumentHandler() {

	}
	
	@Override
	public void startDocumentHandler() {
		
	}

	@Override
	public void setPrefixMap(Map<String, String> prefixMap) {
		
	}
	
	@Override
	public abstract boolean endElementHandler(String uri, String prefix, String tag);

	@Override
	public abstract boolean startElementHandler(String uri, String prefix,  String tag,
			Attributes attributes);


}
