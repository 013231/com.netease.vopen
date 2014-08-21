package mblog.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class XMLTagConstant {

	static private final String XML_BANNER_S = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	static private final String LANGLE_S = "<";
	static private final String LANGLE_SLASH_S = "</";
	static private final String RANGLE_S = ">";
	static private final String RANGLE_EOL_S = ">";
	static private final String SLASH_S = "/";
	static private final String SPACE_S = " ";
	static private final String TWO_SPACES_S = "  ";
	static private final String QUOTE_S = "\"";
	static private final String EQUALS_QUOTE_S = "=\"";

	public static final String XML_BANNER = XML_BANNER_S;
	public static final String LANGLE = LANGLE_S;
	public static final String LANGLE_SLASH = LANGLE_SLASH_S;
	public static final String RANGLE = RANGLE_S;
	public static final String RANGLE_EOL = RANGLE_EOL_S;
	public static final String SLASH = SLASH_S;
	public static final String SPACE = SPACE_S;
	public static final String TWO_SPACES = TWO_SPACES_S;
	public static final String QUOTE = QUOTE_S;
	public static final String EQUALS_QUOTE = EQUALS_QUOTE_S;

	static final HashMap<Byte, String> mXMLTAGMap = new HashMap<Byte, String>();

	static public void writeTag(OutputStream outputStream, String type) {


		try {
			outputStream.write(type.getBytes());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
