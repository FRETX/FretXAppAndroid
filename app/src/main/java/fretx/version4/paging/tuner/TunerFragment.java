package fretx.version4.paging.tuner;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.TunerView;


public class TunerFragment extends Fragment {
	private MainActivity mActivity;
	RelativeLayout rootView = null;
	TunerView tunerView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity) getActivity();
		if(mActivity == null || mActivity.audio == null) return;
			mActivity.audio.enablePitchDetector();
			mActivity.audio.disableNoteDetector();
			mActivity.audio.disableChordDetector();
		initSystemServices();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mActivity == null || mActivity.audio == null) return;
		mActivity.audio.disablePitchDetector();
		mActivity.audio.disableNoteDetector();
		mActivity.audio.disableChordDetector();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		Log.d("Tuner Fragment", "created");
		rootView = (RelativeLayout) inflater.inflate(R.layout.tuner_fragment, container, false);
		tunerView = (TunerView) rootView.findViewById(R.id.tunerView);
		tunerView.setmActivity(mActivity);
		tunerView.setRootView(rootView);
		return rootView;
	}

	private void initSystemServices() {
		//TODO: needs testing with calls
		TelephonyManager telephonyManager =
				(TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (mActivity.audio == null) return;
				if (state == TelephonyManager.CALL_STATE_IDLE) {
					if (!mActivity.audio.isInitialized()) mActivity.audio.initialize(mActivity.fs, mActivity.bufferSizeInSeconds);
					if (!mActivity.audio.isProcessing()) mActivity.audio.start();
				} else {
					mActivity.audio.stop();
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}
}