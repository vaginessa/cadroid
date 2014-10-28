package at.bitfire.cadroid;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import at.bitfire.cadroid.ConnectionInfo.RootCertificateType;

@SuppressLint("ValidFragment")
public class VerifyFragment extends Fragment {
	public static final String TAG = "cadroid.VerifyFragment";
	
	ConnectionInfo connectionInfo;
	boolean mayContinue = false;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_verify, container, false);
		setHasOptionsMenu(true);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View v = getView();
		
		MainActivity main = (MainActivity)getActivity();
		main.onShowFragment(TAG);
		
		connectionInfo = main.getConnectionInfo();
		
		// show hint if chain length > 1
		TextView tv = (TextView)v.findViewById(R.id.warning_chain_length_not_zero);
		if (connectionInfo.getRootCertificateType() == RootCertificateType.STANDALONE)
			tv.setVisibility(View.GONE);
		else
			tv.setText(Html.fromHtml(getString(R.string.chain_length_not_zero)));
		
		// show certificate details
		CertificateInfo info = new CertificateInfo(connectionInfo.getRootCertificate());
		
		((TextView)v.findViewById(R.id.cert_cn)).setText(info.getSubjectName());
		
		String[] subjAltNames = info.getSubjectAltNames();
		((TextView)v.findViewById(R.id.cert_altsubjnames)).setText(
			(subjAltNames.length > 0) ? StringUtils.join(subjAltNames, ", ") : "—"
		);
		
		((TextView)v.findViewById(R.id.cert_serial)).setText(info.getSerialNumber());

		((TextView)v.findViewById(R.id.cert_sig_sha1)).setText(info.getSignature("SHA-1"));
		((TextView)v.findViewById(R.id.cert_sig_md5)).setText(info.getSignature("MD5"));
		
		((TextView)v.findViewById(R.id.cert_valid_from)).setText(info.getNotBefore().toString());
		((TextView)v.findViewById(R.id.cert_valid_until)).setText(info.getNotAfter().toString());

		String basicConstraintsInfo;
		if (info.isCA() == null)
			basicConstraintsInfo = getString(R.string.basic_constraints_not_present);
		else {
			if (info.isCA()) {
				basicConstraintsInfo = getString(R.string.is_a_ca,
					((info.getMaxPathLength() != null) ? info.getMaxPathLength() : "∞"));
			} else
				basicConstraintsInfo = getString(R.string.is_not_a_ca);
		}
		((TextView)v.findViewById(R.id.cert_basic_constraints)).setText(basicConstraintsInfo);
		
		
		// show/hide alerts (they're VISIBLE by default)
		
		mayContinue = true;
		
		// host name not matching
		TextView tvHostnameNotMatching = (TextView)v.findViewById(R.id.cert_hostname_not_matching);
		if (connectionInfo.isHostNameMatching())
			tvHostnameNotMatching.setVisibility(View.GONE);
		else {
			mayContinue = false;
			tvHostnameNotMatching.setText(getResources().getString(R.string.alert_hostname_not_matching, connectionInfo.getHostName()));
		}
		
		// expired / not yet valid
		if (info.isCurrentlyValid())
			v.findViewById(R.id.cert_currently_not_valid).setVisibility(View.GONE);
		else
			mayContinue = false;
		
		// CA flag not set
		TextView tvAlertCA = (TextView)v.findViewById(R.id.cert_is_not_a_ca);
		if (info.isCA() != null && info.isCA())
			tvAlertCA.setVisibility(View.GONE);
		else {
			mayContinue = false;
			tvAlertCA.setText(Html.fromHtml(getString(R.string.alert_is_not_a_ca)));
		}
		
		// already trusted?
		try {
			if (info.isTrusted())
				mayContinue = false;
			else
				v.findViewById(R.id.cert_already_trusted).setVisibility(View.GONE);
		} catch (Exception e) {
			Log.e(TAG, "Couldn't determine trust status of certificate", e);
			mayContinue = false;
		}
	}

	
	// options menu (action bar)
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.simple_next, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.next).setEnabled(mayContinue);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.next:
			((MainActivity)getActivity()).showFragment(ExportFragment.TAG, true);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
