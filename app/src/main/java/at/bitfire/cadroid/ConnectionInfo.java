package at.bitfire.cadroid;

import java.security.cert.X509Certificate;

import lombok.Getter;
import lombok.Setter;
import android.os.Parcel;
import android.os.Parcelable;

public class ConnectionInfo implements Parcelable {
	
	ConnectionInfo() { }

	// host name in URL
	@Getter @Setter private String hostName;
	
	// was there an exception while fetching the certificate?
	@Getter Exception exception;
	ConnectionInfo(Exception exception) { this.exception = exception; }
	
	// certificate details
	public enum RootCertificateType {
		HIERARCHY,		// part of a hierarchy (chain length > 1)
		STANDALONE		// self-signed (chain length = 1)
	}
	@Getter @Setter private RootCertificateType rootCertificateType;
	
	@Getter @Setter private X509Certificate rootCertificate;
	
	// verification results
	@Getter @Setter private boolean hostNameMatching;

	
	// Parcelable
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(hostName);
		dest.writeSerializable(exception);
		dest.writeSerializable(rootCertificateType);
		dest.writeSerializable(rootCertificate);
		dest.writeByte(hostNameMatching ? (byte)1 : (byte)0);
	}

	public static final Parcelable.Creator<ConnectionInfo> CREATOR = new Parcelable.Creator<ConnectionInfo>() {
		@Override
		public ConnectionInfo createFromParcel(Parcel in) {
			ConnectionInfo info = new ConnectionInfo();
			info.hostName = in.readString();
			info.exception = (Exception)in.readSerializable();
			info.rootCertificateType = (RootCertificateType)in.readSerializable();
			info.rootCertificate = (X509Certificate)in.readSerializable();
			info.hostNameMatching = in.readByte() == 1;
			return info;
		}

		@Override
		public ConnectionInfo[] newArray(int size) {
			return new ConnectionInfo[size];
		}
	};
}
