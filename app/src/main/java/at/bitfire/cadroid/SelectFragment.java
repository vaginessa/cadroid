package at.bitfire.cadroid;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ListAdapter;

import java.security.cert.X509Certificate;

public class SelectFragment extends ListFragment {
	public static final String
			TAG = "cadroid.Select";

	boolean mayContinue = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity main = (MainActivity)getActivity();
		main.onShowFragment(TAG);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView v = (TextView)super.getView(position, convertView, parent);
				if (Build.VERSION.SDK_INT >= 17)
					v.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_certificate, 0, 0, 0);
				else
					v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_certificate, 0, 0, 0);
				v.setPadding(0, 10, 0, 10);
				v.setCompoundDrawablePadding(10);
				return v;
			}
		};
		for (X509Certificate cert : ((MainActivity)getActivity()).getConnectionInfo().getCertificates())
			adapter.add(new CertificateInfo(cert).getSubjectName());
		setListAdapter(adapter);

		setHasOptionsMenu(true);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ConnectionInfo connectionInfo = ((MainActivity)getActivity()).getConnectionInfo();

		mayContinue = true;

		// host name matching?
		if (!connectionInfo.isHostNameMatching()) {
			mayContinue = false;
			getListView().addFooterView(getActivity().getLayoutInflater().inflate(R.layout.select_invalid_hostname, null), null, false);
		} else
			try {       // already trusted?
				if (connectionInfo.isTrusted()) {
					mayContinue = false;
					getListView().addFooterView(getActivity().getLayoutInflater().inflate(R.layout.select_already_trusted, null), null, false);
				}
			} catch (Exception e) {
				Log.e(TAG, "Couldn't determine trust status of certificate", e);
				mayContinue = false;
			}

		if (mayContinue) {
			TextView tv = new TextView(view.getContext());
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			tv.setText(R.string.select_text);
			tv.setPadding(0, 0, 0, 10);
			addHeaderToListView(getListView(), tv, null, false);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity)activity).onShowFragment(TAG);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mayContinue) {
			MainActivity main = (MainActivity) getActivity();
			main.setIdxSelectedCertificate(position - 1);
			main.showFragment(VerifyFragment.TAG, true);
		}
	}

	private void addHeaderToListView(ListView listView, View headerView, Object data, boolean isSelectable)
	{
		ListAdapter adapter = listView.getAdapter();

		// Note: When first introduced, this method could only be called before setting the adapter with setAdapter(ListAdapter).
		// Starting with KITKAT (Android 4.4), this method may be called at any time.
		// see https://developer.android.com/reference/android/widget/ListView.html#addHeaderView%28android.view.View%29
		if (adapter != null) {
			listView.setAdapter(null);
		}

		listView.addHeaderView(headerView, data, isSelectable);

		// re-apply list adapter
		if (adapter != null) {
			listView.setAdapter(adapter);
		}
	}
}
