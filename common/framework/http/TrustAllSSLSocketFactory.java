package common.framework.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;

import android.os.Build;

public class TrustAllSSLSocketFactory extends SSLSocketFactory {
	private javax.net.ssl.SSLSocketFactory factory;
	static TrustAllSSLSocketFactory instance = null;

	public TrustAllSSLSocketFactory() throws KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException,
			UnrecoverableKeyException {
		super(null);
		
		// Install the default-trusting trust manager
		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[] { getDefaultX509TrustManager() },
					null);
			factory = sslcontext.getSocketFactory();
			setHostnameVerifier(new StrictHostnameVerifier());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SocketFactory getDefault() {
		if (instance == null) {
			try {
				instance = new TrustAllSSLSocketFactory();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return instance;
	}
	
	private X509TrustManager getDefaultX509TrustManager() {
		X509TrustManager x509TrustManager = null;
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream("trustedCerts"), "passphrase".toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
			tmf.init(ks);

			for (TrustManager tm : tmf.getTrustManagers()) {
				if (tm instanceof X509TrustManager) {
					x509TrustManager = (X509TrustManager) tm;
					break;
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
		
		try {
			if (x509TrustManager == null) {
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init((KeyStore) null);
				
				for (TrustManager tm : tmf.getTrustManagers()) {
					if (tm instanceof X509TrustManager) {
						x509TrustManager = (X509TrustManager) tm;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return x509TrustManager;
	}

	@Override
	public Socket createSocket() throws IOException {
		return factory.createSocket();
	}

	@Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag)
			throws IOException {
		if (Build.VERSION.SDK_INT < 11) { // 3.0
			injectHostname(socket, s);
		}
		return factory.createSocket(socket, s, i, flag);
	}

	private void injectHostname(Socket socket, String host) {
		try {
			Field field = InetAddress.class.getDeclaredField("hostName");
			field.setAccessible(true);
			field.set(socket.getInetAddress(), host);
		} catch (Exception ignored) {
		}
	}
}