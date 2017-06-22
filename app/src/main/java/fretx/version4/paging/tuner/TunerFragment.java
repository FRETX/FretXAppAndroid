package fretx.version4.paging.tuner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import fretx.version4.HeadStockView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.firebase.Analytics;


public class TunerFragment extends Fragment {
    private final static String TAG = "KJKP6_TUNER";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.getInstance().logSelectEvent("TAB", "Tuner");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		BluetoothAnimator.getInstance().stringFall();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "created");

        View rootView = inflater.inflate(R.layout.paging_tuner, container, false);
		final HeadStockView headStockView = (HeadStockView) rootView.findViewById(R.id.headStockView);
        headStockView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(), "click on " + headStockView.getSelectedHammerIndex(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		BluetoothAnimator.getInstance().stringFall();
	}
}