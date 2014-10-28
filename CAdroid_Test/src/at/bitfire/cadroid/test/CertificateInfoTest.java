package at.bitfire.cadroid.test;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;
import at.bitfire.cadroid.CertificateInfo;

public class CertificateInfoTest extends InstrumentationTestCase {
	//private static final String TAG = "cadroid.CertificateInfoTest";
	
	private AssetManager assetManager;
	private CertificateFactory certificateFactory;
	
	CertificateInfo
		infoDebianTestCA,
		infoDebianTestNoCA,
		infoGTECyberTrust,
		infoMehlMX;

	protected void setUp() throws Exception {
		assetManager = getInstrumentation().getContext().getAssets();
		certificateFactory = CertificateFactory.getInstance("X.509");
		
		infoDebianTestCA = loadCertificateInfo("DebianTestCA.pem");
		infoDebianTestNoCA = loadCertificateInfo("DebianTestNoCA.pem");
		infoGTECyberTrust = loadCertificateInfo("GTECyberTrustGlobalRoot.pem");
		
		// user-submitted test cases
		infoMehlMX = loadCertificateInfo("mehl.mx.pem");
	}
	
	protected CertificateInfo loadCertificateInfo(String assetFileName) throws IOException, CertificateException {
		InputStream is = null;
		try {
			is = assetManager.open(assetFileName);
			X509Certificate rootGTECyberTrust = (X509Certificate)certificateFactory.generateCertificate(is);
			return new CertificateInfo(rootGTECyberTrust);
		} finally {
			is.close();
		}
	}
	
	
	public void testIsCA() {
		assertTrue(infoDebianTestCA.isCA());
		assertFalse(infoDebianTestNoCA.isCA());
		assertNull(infoGTECyberTrust.isCA());
		
		assertFalse(infoMehlMX.isCA());
	}
	
	public void testIsTrusted() throws Exception {
		assertFalse(infoDebianTestCA.isTrusted());
		assertFalse(infoDebianTestNoCA.isTrusted());
		assertTrue(infoGTECyberTrust.isTrusted());
		
		assertFalse(infoMehlMX.isTrusted());
	}

}
