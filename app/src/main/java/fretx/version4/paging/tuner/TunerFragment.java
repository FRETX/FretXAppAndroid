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

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;


public class TunerFragment extends Fragment {
	private MainActivity mActivity;
	RelativeLayout rootView = null;
	TunerView tunerView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity) getActivity();
		if(mActivity == null || mActivity.audio == null) return;
		initSystemServices();
	}

	private void showTutorial(){
		new MaterialIntroView.Builder(mActivity)
				.enableDotAnimation(false)
				.enableIcon(false)
				.setFocusGravity(FocusGravity.CENTER)
				.setFocusType(Focus.NORMAL)
				.setDelayMillis(300)
				.enableFadeAnimation(true)
				.performClick(true)
				.setInfoText("Play strings one by one. Adjust until needle is green for each string. It's that simple to tune your guitar!")
				.setTarget((TunerView) mActivity.findViewById(R.id.tunerView))
				.setUsageId("tutorialTuner") //THIS SHOULD BE UNIQUE ID
				.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("Tuner Fragment", "created");
		rootView = (RelativeLayout) inflater.inflate(R.layout.tuner_fragment, container, false);
		tunerView = (TunerView) rootView.findViewById(R.id.tunerView);
		tunerView.setmActivity(mActivity);
		tunerView.setRootView(rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState){
		showTutorial();
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