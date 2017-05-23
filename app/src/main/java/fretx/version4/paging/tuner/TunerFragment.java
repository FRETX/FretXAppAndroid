package fretx.version4.paging.tuner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.firebase.Analytics;


public class TunerFragment extends Fragment {
    private final static String TAG = "KJKP6_TUNER";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.getInstance().logSelectEvent("TAB", "Tuner");
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
        BluetoothLE.getInstance().clearMatrix();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "created");

        View rootView = inflater.inflate(R.layout.paging_tuner, container, false);
		TunerView tunerView = (TunerView) rootView.findViewById(R.id.tunerView);
		tunerView.setmActivity((MainActivity) getActivity());
		tunerView.setRootView((RelativeLayout) rootView);

        return rootView;
	}
}