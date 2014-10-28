package at.bitfire.cadroid.test;

import java.net.URL;

import junit.framework.TestCase;
import at.bitfire.cadroid.ConnectionChecker;
import at.bitfire.cadroid.ConnectionInfo;
import at.bitfire.cadroid.ConnectionInfo.RootCertificateType;

public class ConnectionCheckerTest extends TestCase {

	public void testPreinstalledCertificate() throws Exception {
		ConnectionInfo result = ConnectionChecker.check(new URL("https://www.bing.com/nonexisting"));
		assertEquals(RootCertificateType.HIERARCHY, result.getRootCertificateType());
		assertEquals("www.bing.com", result.getHostName());
		assertTrue(result.isHostNameMatching());
	}

	public void testSelfSignedStandaloneCertificate() throws Exception {
		ConnectionInfo result = ConnectionChecker.check(new URL("https://www.pcwebshop.co.uk/"));
		assertEquals(RootCertificateType.STANDALONE, result.getRootCertificateType());
		assertEquals("www.pcwebshop.co.uk", result.getHostName());
		assertFalse(result.isHostNameMatching());
	}

}
