package fretx.version4.paging.learn;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;

public class LearnFragmentTwo extends Fragment {

	MainActivity mActivity;

	LinearLayout rootView = null;
	LearnScaleExerciseView scaleView;
	LinearLayout scalePicker;
	String[] scaleNames = {"MinorPentatonic","Blues"};
//	FretboardView fretboardView;

	public LearnFragmentTwo(){

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity)getActivity();
		rootView = (LinearLayout) inflater.inflate(R.layout.learn_scale_exercise_layout, container, false);

		scaleView = (LearnScaleExerciseView) rootView.findViewById(R.id.scaleView);
		scaleView.setmActivity(mActivity);

		scalePicker = (LinearLayout) rootView.findViewById(R.id.scalePickerView);
		//shitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcode
		updateScale("MinorPentatonic");

		TextView tmpTextView;
		for (String str :scaleNames) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(30);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			scalePicker.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.scalePickerView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondary_text));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primary_text));
					updateScale(((TextView) view).getText().toString());
				}
			});
		}

//		ArrayList<Chord> exerciseChords = new ArrayList<Chord>(0);
//		String[] majorRoots = new String[]{"G","D"};
//		for (int i = 0; i < majorRoots.length; i++) {
//			exerciseChords.add(new Chord(majorRoots[i], "maj"));
//		}
//		String[] minorRoots = new String[]{"A"};
//		for (int i = 0; i < minorRoots.length; i++) {
//			exerciseChords.add(new Chord(minorRoots[i], "m"));
//		}
//		exerciseChords.add(new Chord("C","maj"));
//
//		chordExerciseView.setChords(exerciseChords);

		return rootView;
	}
	//shitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcodeshitcode :(((
	private void updateScale(String scaleName){
		int [] notes = new int[0];
		switch(scaleName){
			case "MinorPentatonic" :
				notes = new int[] {41,44,46,48,51,53,56,58,60,63,65,68};
				break;
			case "Blues" :
				notes = new int[] {41,44,46,47,48,51,53,56,58,59,60,63,65,68};
				break;
			default:
				break;
		}
		scaleView.setNotes(notes);
	}
}
