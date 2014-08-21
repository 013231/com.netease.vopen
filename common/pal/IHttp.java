package common.pal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import common.util.NameValuePair;

public interface IHttp
{
	
	public final static String MULTIPART_FORM_DATA = "multipart/form-data";
	public final static String POST_FORM_DATA = "application/x-www-form-urlencoded"; 
	public static final String TRANSFER_ENCODING = "Transfer-Encoding";
	public static final String TRANSFER_ENCODING_CHUNKED = "chunked";
	
	/**
	 * 
	 * @param url
	 */
	public void open(String url);
	
	
	public void close()  throws IOException;
	/**
	 * Sets the general request property.
	 * @param key
	 * @param value
	 */
	public void setRequestProperty(String key, String value);
	
	/**
	 * Set the method for the URL request, one of: GET POST HEAD are legal, subject to protocol restrictions.
	 * @param method
	 */
	public void setRequestMethod(String method);
	
	
//	/**
//	 * set post data: name value pair
//	 * @param name
//	 * @param value
//	 */
//	public void postNameValuePair(Hashtable pair);
	
	
	/**
	 * set post data: file pair
	 * @param fileUrl
	 */
	public void postMultiPart(String fileUrl); 
	public void postMultiPart(String fileUrl, Hashtable param); 
	/**
	 * set post data: byteArray pair
	 * @param byteArray
	 */
	public void postByteArray(byte[] byteArray);
	
	/**
	 * 
	 * @param entity
	 */
	public void setHttpEntity(Object entity);
	/**
	 * 
	 * @return
	 */
	public int execute()  throws IOException;
	
	/**
	 * Returns the HTTP response status code.
	 * @return
	 */
	public int getResponseCode();
	
	/**
	 * Gets the HTTP response message, if any, returned along with the response code from a server.
	 * @return
	 */
	public String getResponseMessage() throws IOException;
	
	/**
	 *  Returns the value of the named header field.
	 * @param name
	 * @return
	 */
	public String getHeaderField(String name)  throws IOException;
	
	/**
	 *  Gets a header field value by index.
	 * @param n
	 * @return
	 */
	public String getHeaderField(int n)  throws IOException; 
	
	/**
	 * Gets a header field key by index.
	 * @param n
	 * @return
	 */
	public String getHeaderFieldKey(int n)  throws IOException;
	
	/**
	 * Gets all header
	 * @return
	 * @throws IOException
	 */
	public NameValuePair[] getHeaderField() throws IOException;
	
	/**
	 * Open and return an input stream for a connection. 
	 * @return
	 */
	public InputStream openInputStream()  throws IOException;
	
	/**
	 * Open and return a data input stream for a connection. 
	 * @return
	 */
	public DataInputStream openDataInputStream()  throws IOException;
	
	/**
	 * Open and return an output stream for a connection. 
	 * @return
	 */
	public OutputStream openOuputStream()  throws IOException;
	
	/**
	 * Open and return a data output stream for a connection. 
	 * @return
	 */
	public DataOutputStream openDataOutputStream()  throws IOException;
	
	/**
	 * Tells the length of the content, if known.
	 * @return
	 */
	public long getContentLength()  throws IOException;
	
	
	public void setTimeout(int timeout) ;
    
	/**
	 * set http params
	 * @param param
	 */
	public void setHttpParams(Hashtable param);
}
