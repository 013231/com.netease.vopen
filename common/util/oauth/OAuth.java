package common.util.oauth;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;

import common.framework.http.THttpMethod;
import common.util.URLDecoder;
import common.util.URLEncoder;

public class OAuth {
	
	private final static String POST_FORM_DATA = "application/x-www-form-urlencoded"; 
	public static final String VERSION_1_0 = "1.0";

	/** The encoding used to represent characters as bytes. */
	public static final String ENCODING = "utf-8";
	public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	public static final String OAUTH_TOKEN = "oauth_token";
	public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
	public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	public static final String OAUTH_SIGNATURE = "oauth_signature";
	public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	public static final String OAUTH_NONCE = "oauth_nonce";
	public static final String OAUTH_VERSION = "oauth_version";
	public static final String OAUTH_CALLBACK = "oauth_callback";
	public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	public static final String OAUTH_VERIFIER = "oauth_verifier";
	public static final String Authorization = "Authorization";

	public static final String HMAC_SHA1 = "HMAC-SHA1";
	public static final String AUTH_SCHEME = "OAuth";
	// public static final String REALM = "http://api.t.163.com/";

	private static String characterEncoding = ENCODING;
	
	public static OAuthHttpRequest request(THttpMethod method, String url,
			Hashtable<String, String> parameter, OAuthClient client) {
		return request(method, url, parameter, null, client);
	}
	
	public static OAuthHttpRequest request(THttpMethod method, String url,
			Hashtable<String, String> parameter, HttpEntity entity, OAuthClient client) {
		if (method == null) {
			method = THttpMethod.GET;
		}
		
		OAuthHttpRequest request = new OAuthHttpRequest(url, method, client);
		if (parameter != null) {
			for (Entry<String, String> entry : parameter.entrySet()) {
				request.addParameter(entry.getKey(), entry.getValue());
			}
		}
		
		request.setHttpEntity(entity);
		
		return request;
	}

//	/**
//	 * Construct a WWW-Authenticate or Authentication header value, containing
//	 * the given realm plus all the parameters whose names begin with "oauth_".
//	 */
//	public static Vector getAuthorizationHeader(String realm, Vector parameters,StringBuffer into) {
//		//StringBuffer into = new StringBuffer();
//		into.setLength(0);
//		into.append(AUTH_SCHEME);
//		if (realm != null) {
//			into.append(" realm=\"").append(OAuth.percentEncode(realm)).append('"');
//		}
//		Vector last = new Vector();
//		if (parameters != null && parameters.size() > 0) {
//			int num = parameters.size();
//			for (int i = 0; i < num; i++) {
//				ParameterEntry entry = (ParameterEntry) parameters.elementAt(i);
//				String name = entry.getName();
//				if (name.startsWith("oauth_")) {
//					if (into.length() > 0)
//						into.append(",");
//					into.append(" ");
//					into.append(percentEncode(name)).append("=\"");
//					into.append(percentEncode(entry.getValue()));
//					into.append('"');
//				}else{
//					last.addElement(entry);
//				}
//			}
//		}
//		return last;
//	}

	
//	protected static void addRequiredParameters(Hashtable<String, String> parameter,
//			OAuthClient client) {
//		if (!parameter.containsKey(OAuth.OAUTH_TOKEN)
//				&& client.mTokenKey != null) {
//			addParameter(parameter, OAuth.OAUTH_TOKEN, client.mTokenKey);
//		}
//
//		if (!parameter.containsKey(OAuth.OAUTH_CONSUMER_KEY)) {
//			addParameter(parameter, OAuth.OAUTH_CONSUMER_KEY,
//					client.mConsumerKey);
//		}
//
//		if (!parameter.containsKey(OAuth.OAUTH_SIGNATURE_METHOD)) {
//			String signatureMethod = client.getSignatureMethod();
//			addParameter(parameter, OAuth.OAUTH_SIGNATURE_METHOD,
//					signatureMethod);
//		}
//
//		if (!parameter.containsKey(OAuth.OAUTH_VERSION)) {
//			addParameter(parameter, OAuth.OAUTH_VERSION, OAuth.VERSION_1_0);
//		}
//	}
	
	protected static void addParameter(Hashtable<String, String> map, 
			String key, String value) {
		map.put(key, value);
	}

//	protected static void sign(String method, String url, Hashtable parameter,
//			OAuthClient client) {
//		String baseString = getBaseString(method, url, parameter);
//		String signString = client.getHMAC_SHA1().getSignature(baseString);
//		addParameter(parameter, OAUTH_SIGNATURE, signString);
//	}

//	public static String getBaseString(String method, String url,
//			Hashtable parameter) {
//		Hashtable map = null;
//		int q = url.indexOf('?');
//		if (q < 0) {
//			map = parameter;
//		} else {
//			// Combine the URL query string with the other parameters:
//			map = BaseUtil.cloneHashtable(parameter);
//			String variable = url.substring(q + 1);
//			addVariable(map, variable);
//			url = url.substring(0, q);
//		}
//
//		if (map.containsKey(OAuth.OAUTH_SIGNATURE)) {
//			map.remove(OAuth.OAUTH_SIGNATURE);
//		}
//		if (map != null && map.size() > 0) {
//
//			Vector array = BaseUtil.getMapValues(map);
//			return OAuth.percentEncode(method.toUpperCase()) + '&'
//					+ OAuth.percentEncode(normalizeUrl(url)) + '&'
//					+ OAuth.percentEncode(normalizeParameters(array));
//		} else {
//			return "";
//		}
//	}
//	
//	private static void addVariable(Hashtable table, String variable) {
//
//		if (variable != null && variable.length() > 0) {
//			Vector array = BaseUtil.split(variable, "&");
//			for (int i = 0; i < array.size(); i++) {
//				String nvp = (String) array.elementAt(i);
//				int equals = nvp.indexOf('=');
//				String name;
//				String value;
//				if (equals < 0) {
//					name = decodePercent(nvp);
//					value = null;
//				} else {
//					name = decodePercent(nvp.substring(0, equals));
//					value = decodePercent(nvp.substring(equals + 1));
//				}
//				table.put(name, new ParameterEntry(name, value));
//			}
//		}
//	}
//
//	/**
//	 * Write a form-urlencoded document into the given stream, containing the
//	 * given sequence of name/value pairs.
//	 * 
//	 * @throws IOException
//	 */
//	public static void formEncode(Vector parameters, StringBuffer into) {
//		if (parameters != null) {
//			boolean first = true;
//			for (int i = 0; i < parameters.size(); i++) {
//				ParameterEntry p = (ParameterEntry) parameters.elementAt(i);
//				if (first) {
//					first = false;
//				} else {
//					into.append('&');
//					// into.write('&');
//				}
//				into.append(percentEncode(p.getName()));
//				into.append("=");
//				into.append(percentEncode(p.getValue()));
//				// into.write(encodeCharacters(percentEncode(p.getKey())));
//				// into.write('=');
//				// into.write(encodeCharacters(percentEncode(p.getValue())));
//			}
//		}
//	}

	public static String percentEncode(String s) {
		if (s == null) {
			return "";
		}

		return URLEncoder.percentEncode(s, ENCODING);
	}

	public static String decodePercent(String s) {
		try {
			return URLDecoder.decode(s, ENCODING);
			// This implements http://oauth.pbwiki.com/FlexibleDecoding
		} catch (java.io.UnsupportedEncodingException wow) {
			throw new RuntimeException(wow.getMessage());
		}
	}

	public static byte[] encodeCharacters(String from) {
		if (characterEncoding != null) {
			try {
				return from.getBytes(characterEncoding);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return from.getBytes();
	}

	protected static String normalizeUrl(String url) {
		String scheme = null;
		String authority = null;
		String path = null;
		int schemeIdx = url.indexOf(":");
		if (schemeIdx > 0) {
			scheme = url.substring(0, schemeIdx);
			int authorityStart = url.indexOf("//");
			if (authorityStart > 0) {
				authorityStart += "//".length();
				int authorityEnd = url.indexOf("/", authorityStart);
				if (authorityEnd > 0) {
					authority = url.substring(authorityStart, authorityEnd);
					int pathEnd = url.indexOf("?", authorityEnd);
					if (pathEnd > 0) {
						path = url.substring(authorityEnd, pathEnd);
					} else {
						path = url.substring(authorityEnd);
					}
				} else {
					authority = url.substring(authorityStart);
				}
			}
		}

		if (scheme != null && authority != null) {
			int portIdx = authority.indexOf(":");
			if (portIdx > 0) {
				String port = authority.substring(portIdx + 1);
				if ((scheme.equals("http") && port.equals("80"))
						|| (scheme.equals("https") && port.equals("443"))) {
					authority = authority.substring(0, portIdx);
				}
			}
			if (path == null || path.length() == 0) {
				path = "/";
			}
			return scheme + "://" + authority + path;

		}
		return null;
	}
//
//	protected static String normalizeParameters(Vector parameters) {
//		if (parameters == null || parameters.size() == 0) {
//			return "";
//		}
//		BaseUtil.sort(parameters);
//		StringBuffer sb = new StringBuffer();
//		formEncode(parameters, sb);
//		return sb.toString();
//	}

}