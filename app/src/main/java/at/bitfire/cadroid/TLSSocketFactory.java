package at.bitfire.cadroid;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSSocketFactory extends SSLSocketFactory {
	private final static String TAG = "CAdroid.SocketFactory";

	private final static String[] PREFERRED_CIPHER_SUITES = new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA" };

	private final SSLSocketFactory sslSocketFactory;

	public TLSSocketFactory(SSLSocketFactory delegate) {
		sslSocketFactory = delegate;
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return getPreferredDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return getPreferredSupportedCipherSuites();
	}

	@Override
	public Socket createSocket() throws IOException {
		Socket socket = sslSocketFactory.createSocket();
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException {
		Socket socket = sslSocketFactory.createSocket(arg0, arg1);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		Socket socket = sslSocketFactory.createSocket(arg0, arg1);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3) throws IOException {
		Socket socket = sslSocketFactory.createSocket(arg0, arg1, arg2, arg3);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
		Socket socket = sslSocketFactory.createSocket(arg0, arg1, arg2, arg3);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
		Socket socket = sslSocketFactory.createSocket(arg0, arg1, arg2, arg3);
		return configureSocket(socket);
	}

	private SSLSocket configureSocket(Socket socket) {
		SSLSocket sslSocket = (SSLSocket)socket;
		sslSocket.setEnabledProtocols(getPreferredProtocols(sslSocket));
		sslSocket.setEnabledCipherSuites(getPreferredDefaultCipherSuites());
		log("Enabled protocols", sslSocket.getEnabledProtocols());
		log("Enabled cipher suites", sslSocket.getEnabledCipherSuites());
		return sslSocket;
	}

	private String[] getPreferredDefaultCipherSuites() {
		String[] defaultCipherSuites = sslSocketFactory.getDefaultCipherSuites();
		return getArrayOrderedByPreference(defaultCipherSuites, PREFERRED_CIPHER_SUITES);
	}

	private String[] getPreferredSupportedCipherSuites() {
		String[] supportedCipherSuites = sslSocketFactory.getSupportedCipherSuites();
		return getArrayOrderedByPreference(supportedCipherSuites, PREFERRED_CIPHER_SUITES);
	}

	private static String[] getPreferredProtocols(SSLSocket sslSocket) {
		// Just reverse the order of the supported protocols
		// btw: Android versions below 4.1 do not support TLSv1.2 and TLS1.1
		List<String> preferredProtocols = Arrays.asList(sslSocket.getSupportedProtocols());
		Collections.reverse(preferredProtocols);
		return preferredProtocols.toArray(new String[0]);
	}

´	private static String[] getArrayOrderedByPreference(String[] array, String[] preferences) {
		// Move preferences to the start of the array
		List<String> orderedList = new ArrayList<String>(Arrays.asList(array));

		for (int i = preferences.length - 1; i >= 0; --i) {
			String currentPreference = preferences[i];

			if (orderedList.contains(currentPreference)) {
				orderedList.remove(currentPreference);
				orderedList.add(0, currentPreference);
			}
		}

		return orderedList.toArray(new String[0]);
	}

	private static void log(String title, String[] array) {
		String logMessage = title + ": ";
		logMessage += Arrays.toString(array).replace('[', ' ').replace(']', ' ').trim();
		Log.i(TAG, logMessage);
	}
}
