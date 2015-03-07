package at.bitfire.cadroid;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import android.util.Log;
import at.bitfire.cadroid.ConnectionInfo.RootCertificateType;

public class InfoTrustManager implements X509TrustManager {
	private static String TAG = "CAdroid.InfoTrustManager";
	
	ConnectionInfo info;
	
	InfoTrustManager(ConnectionInfo info) {
		this.info = info;
	}
	

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new CertificateException("Client certificates are not supported");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		Log.d(TAG, "checkServerTrusted");
		
		info.setRootCertificateType(chain.length > 1 ? RootCertificateType.HIERARCHY : RootCertificateType.STANDALONE);
		info.setRootCertificate(chain[chain.length - 1]);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		Log.d(TAG, "getAcceptedIssuers");
		return null;
	}

}
