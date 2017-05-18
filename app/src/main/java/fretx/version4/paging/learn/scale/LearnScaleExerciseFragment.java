package fretx.version4.paging.learn.scale;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.Scale;

public class LearnScaleExerciseFragment extends Fragment {

	MainActivity mActivity;

	LinearLayout rootView = null;
	LinearLayout scaleRootPicker, scaleTypePicker;
	int[] notes;
	ArrayList<FretboardPosition> fretboardPositions;
	FretboardView fretboardView;
	Scale currentScale;

	public LearnScaleExerciseFragment(){}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity)getActivity();
		Analytics.getInstance().logSelectEvent("EXERCISE", "Scale");
		BluetoothLE.getInstance().clearMatrix();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.paging_learn_scale, container, false);

		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		scaleRootPicker = (LinearLayout) rootView.findViewById(R.id.scaleRootPickerView);
		scaleTypePicker = (LinearLayout) rootView.findViewById(R.id.scaleTypePickerView);

		TextView tmpTextView;
		for (String str : Scale.ALL_ROOT_NOTES) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(26);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			scaleRootPicker.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setBackgroundColor(getResources().getColor(R.color.primary));
			tmpTextView.setTextColor(getResources().getColor(R.color.tertiaryText));
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.scaleRootPickerView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
					}
					view.setBackgroundResource(R.drawable.picker_text_background);
					updateScale(((TextView) view).getText().toString(), currentScale.getType());
					scaleRootPicker.getChildAt(0).setSelected(true);
				}
			});
		}

		for (String str :Scale.ALL_SCALE_TYPES) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(26);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			scaleTypePicker.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setBackgroundColor(getResources().getColor(R.color.primary));
			tmpTextView.setTextColor(getResources().getColor(R.color.tertiaryText));
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.scaleTypePickerView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
					}
					view.setBackgroundResource(R.drawable.picker_text_background);
					updateScale(currentScale.getRoot() , ((TextView) view).getText().toString());
					scaleTypePicker.getChildAt(0).setSelected(true);
				}
			});
		}

		TextView initialRoot = (TextView) scaleRootPicker.getChildAt(0);
		TextView initialType = (TextView) scaleTypePicker.getChildAt(0);
		initialRoot.setBackgroundResource(R.drawable.picker_text_background);
		initialType.setBackgroundResource(R.drawable.picker_text_background);
		updateScale(initialRoot.getText().toString(), initialType.getText().toString());

		return rootView;
	}

	@Override
	public void onViewCreated(View v , Bundle savedInstanceState){
		updateScale( ((TextView)scaleRootPicker.getChildAt(0)).getText().toString() ,((TextView)scaleTypePicker.getChildAt(0)).getText().toString());
	}

	private void updateScale(String scaleRootNote, String scaleType){
		Log.d("update root",scaleRootNote);
		Log.d("update type",scaleType);

		currentScale = new Scale(scaleRootNote,scaleType);
		notes = currentScale.getNotes();
		fretboardPositions = currentScale.getFretboardPositions();

		//Show on FretboardView
		fretboardView.setFretboardPositions(fretboardPositions);

		//Send to FretX
		byte[] bluetoothArray = new byte[fretboardPositions.size()+1];
		for (int i = 0; i < fretboardPositions.size(); i++) {
			bluetoothArray[i] = fretboardPositions.get(i).getByteCode();
		}
		BluetoothLE.getInstance().setMatrix(bluetoothArray);
	}


	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}
