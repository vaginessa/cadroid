package at.bitfire.cadroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ConnectionInfo implements Parcelable {
	private final static String TAG = "cadroid.Fetch";
	
	private ConnectionInfo() { }

	// host name in URL
	@Getter @Setter private String hostName;
	
	// was there an exception while fetching the certificate?
	@Getter private Exception exception;
	ConnectionInfo(Exception exception) { this.exception = exception; }
	
	// certificate details
	@Getter @Setter private X509Certificate[] certificates;
	
	// verification results
	@Getter @Setter private boolean hostNameMatching;


	// methods

	public static ConnectionInfo fetch(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		ConnectionInfo info = new ConnectionInfo();

		SSLContext sc = SSLContext.getInstance("TLS");
		InfoTrustManager tm = new InfoTrustManager(info);
		sc.init(null, new X509TrustManager[] { tm }, null);

		Log.i(TAG, "Connecting to URL: " + url);

		// Reusing http connections are buggy with versions before Android 4.1 (API Level 16)
		// Workaround for poor server configuration:
		if (Build.VERSION.SDK_INT < 16) {
			System.setProperty("http.keepAlive", "false");
		}

		@Cleanup("disconnect") HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
		urlConnection.setConnectTimeout(5000);
		urlConnection.setReadTimeout(20000);
		urlConnection.setInstanceFollowRedirects(false);
		urlConnection.setSSLSocketFactory(sc.getSocketFactory());
		urlConnection.setHostnameVerifier(new InfoHostnameVerifier(info));

		try {
			@Cleanup InputStream in = urlConnection.getInputStream();

			// read one byte to make sure the connection has been established
			//noinspection UnusedAssignment
			int c = in.read();
		} catch(IOException e) {
			String httpStatus = urlConnection.getHeaderField(null);
			if (httpStatus != null) {
				Log.i(TAG, "HTTP error when fetching resource: " + httpStatus + " (ignoring)");
			} else {
				throw e;
			}
		}

		// add an exception if certificates could not be found
		if (info.getCertificates() == null) {
			info.exception = new Exception("No certificates found!");
		}

		return info;
	}

	public boolean isTrusted() throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init((KeyStore)null);
		X509TrustManager tm = (X509TrustManager)tmf.getTrustManagers()[0];
		try {
			tm.checkServerTrusted(certificates, certificates[0].getPublicKey().getAlgorithm());
			return true;
		} catch(CertificateException e) {
			return false;
		}
	}



	// Parcelable
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(hostName);
		dest.writeSerializable(exception);
		dest.writeSerializable(certificates);
		dest.writeByte(hostNameMatching ? (byte)1 : (byte)0);
	}

	public static final Parcelable.Creator<ConnectionInfo> CREATOR = new Parcelable.Creator<ConnectionInfo>() {
		@Override
		public ConnectionInfo createFromParcel(Parcel in) {
			ConnectionInfo info = new ConnectionInfo();
			info.hostName = in.readString();
			info.exception = (Exception)in.readSerializable();
			info.certificates = (X509Certificate[])in.readSerializable();
			info.hostNameMatching = in.readByte() == 1;
			return info;
		}

		@Override
		public ConnectionInfo[] newArray(int size) {
			return new ConnectionInfo[size];
		}
	};
}
