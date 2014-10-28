package at.bitfire.cadroid;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;

public class InfoHostnameVerifier implements HostnameVerifier {
	private static HostnameVerifier defaultVerifier = new BrowserCompatHostnameVerifier();
	
	ConnectionInfo info;
	
	
	InfoHostnameVerifier(ConnectionInfo info) {
		super();
		this.info = info;
	}

	@Override
	public boolean verify(String hostName, SSLSession session) {
		info.setHostName(hostName);
		info.setHostNameMatching(defaultVerifier.verify(hostName, session));
		return true;
	}
	
}
