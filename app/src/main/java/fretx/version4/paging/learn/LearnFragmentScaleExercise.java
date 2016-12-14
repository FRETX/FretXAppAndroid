package fretx.version4.paging.learn;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.math3.geometry.euclidean.threed.Line;

import java.io.IOException;
import java.util.ArrayList;

import fretx.version4.BluetoothClass;
import fretx.version4.FretboardView;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.MusicUtils;
import rocks.fretx.audioprocessing.Scale;

public class LearnFragmentScaleExercise extends Fragment {

	MainActivity mActivity;

	LinearLayout rootView = null;
	LinearLayout scaleRootPicker, scaleTypePicker;
	int[] notes;
	ArrayList<FretboardPosition> fretboardPositions;
	FretboardView fretboardView;
	Scale currentScale;

	public LearnFragmentScaleExercise(){

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
			tmpTextView.setTextSize(30);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			scaleRootPicker.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.scaleRootPickerView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					updateScale(((TextView) view).getText().toString(), currentScale.getType());
					scaleRootPicker.getChildAt(0).setSelected(true);
				}
			});
		}

		for (String str :Scale.ALL_SCALE_TYPES) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(30);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			scaleTypePicker.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.scaleTypePickerView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					updateScale(currentScale.getRoot() , ((TextView) view).getText().toString());
					scaleTypePicker.getChildAt(0).setSelected(true);
				}
			});
		}
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
