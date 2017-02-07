package fretx.version4.paging.learn;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.BluetoothClass;
import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
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

	public LearnScaleExerciseFragment(){

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity)getActivity();
		rootView = (LinearLayout) inflater.inflate(R.layout.learn_scale_exercise_layout, container, false);

		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);

		scaleRootPicker = (LinearLayout) rootView.findViewById(R.id.scaleRootPickerView);
		scaleTypePicker = (LinearLayout) rootView.findViewById(R.id.scaleTypePickerView);

//		currentScale = new Scale(Scale.ALL_ROOT_NOTES[0],Scale.ALL_SCALE_TYPES[0]);
//		currentScale = new Scale("A","Major");

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
//							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
							((TextView) v).setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
					}
//					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					((TextView) view).setBackgroundResource(R.drawable.picker_text_background);
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
//							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
							((TextView) v).setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
					}
//					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					((TextView) view).setBackgroundResource(R.drawable.picker_text_background);
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
		bluetoothArray[fretboardPositions.size()] = Byte.valueOf("0");
		BluetoothClass.sendToFretX(bluetoothArray);



	}






}
