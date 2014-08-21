package mblog.xml;

public interface IXMLWriter {

	XMLTag addTag(String name, String value);

	boolean addTag(XMLTag tag);

	boolean addChildTag(XMLTag tag);

	XMLTag addChildTag(String name, String value);

	boolean addEmptyTag(String value);

	boolean addEmptyTag(String name, String value);

	boolean addAttribute(String name, String value);

	// void closeTag();

	void closeAllTag();

}
