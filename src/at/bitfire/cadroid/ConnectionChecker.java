package at.bitfire.cadroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import lombok.Cleanup;
import android.util.Log;

public class ConnectionChecker {
	private static final String TAG = "cadroid.ConnectionChecker";

	public static ConnectionInfo check(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		ConnectionInfo info = new ConnectionInfo();
		
		SSLContext sc = SSLContext.getInstance("TLS");
		InfoTrustManager tm = new InfoTrustManager(info);
		sc.init(null, new X509TrustManager[] { tm }, null);
		
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(new InfoHostnameVerifier(info));
		
		@Cleanup("disconnect") HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();

		try {
			@Cleanup InputStream in = urlConnection.getInputStream();
			
			// read one byte to make sure the connection has been established
			@SuppressWarnings("unused")
			int c = in.read();
		} catch(IOException e) {
			String httpStatus = urlConnection.getHeaderField(null);
			if (httpStatus != null)
				Log.i(TAG, "HTTP error when fetching resource: " + httpStatus + " (ignoring)");
			else
				throw e;
		}
		
		return info;
	}
	
}
