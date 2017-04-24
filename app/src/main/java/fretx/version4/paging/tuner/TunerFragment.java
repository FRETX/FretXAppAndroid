package fretx.version4.paging.tuner;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.utils.Audio;


public class TunerFragment extends Fragment {
	private MainActivity mActivity;
	RelativeLayout rootView = null;
	TunerView tunerView = null;
	private boolean backgroundMicWarningShown;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity) getActivity();
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Tuner");
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "TAB");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

		if (!Audio.getInstance().isEnabled())
			return;
	}

	private void showTutorial(){
//		new MaterialIntroView.Builder(mActivity)
//				.enableDotAnimation(false)
//				.enableIcon(false)
//				.setFocusGravity(FocusGravity.CENTER)
//				.setFocusType(Focus.NORMAL)
//				.setDelayMillis(300)
//				.enableFadeAnimation(true)
//				.performClick(true)
//				.setInfoText("Play strings one by one. Adjust until needle is green for each string. It's that simple to tune your guitar!")
//				.setTarget((TunerView) mActivity.findViewById(R.id.tunerView))
//				.setUsageId("tutorialTuner") //THIS SHOULD BE UNIQUE ID
//				.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("Tuner Fragment", "created");
		rootView = (RelativeLayout) inflater.inflate(R.layout.paging_tuner, container, false);
		tunerView = (TunerView) rootView.findViewById(R.id.tunerView);
		tunerView.setmActivity(mActivity);
		tunerView.setRootView(rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState){
		showTutorial();
	}
}