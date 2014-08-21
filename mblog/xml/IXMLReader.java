package mblog.xml;

import java.util.Map;

import org.xml.sax.Attributes;

public interface IXMLReader {
	
	public void startDocumentHandler();

	public void endDocumentHandler();
	
	public void setPrefixMap(Map<String, String> prefixMap);

	public boolean startElementHandler(String uri, String prefix, String tag, Attributes attributes);

	public boolean endElementHandler(String uri, String prefix, String tag);

	public void characterDataHandler(char[] ch, int start, int length);

}
