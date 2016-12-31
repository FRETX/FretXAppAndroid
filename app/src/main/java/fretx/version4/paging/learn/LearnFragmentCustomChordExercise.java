package fretx.version4.paging.learn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import fretx.version4.BluetoothClass;
import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by onurb_000 on 15/12/16.
 */

public class LearnFragmentCustomChordExercise extends Fragment {

	Chord currentChord;
	MainActivity mActivity;
	FrameLayout rootView;
	LearnCustomChordExerciseView chordExerciseView;
	HashMap<String,FingerPositions> chordFingerings;
	Button addButton, removeButton, startButton;
	ArrayList<Chord> chordSequence = new ArrayList<>();
	ListView chordSequenceListView;
	ArrayAdapter<Chord> chordSequenceListAdapter;

	public LearnFragmentCustomChordExercise(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		rootView = (FrameLayout) inflater.inflate(R.layout.chord_custom_sequence_layout, container, false);
		return  rootView;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		chordFingerings = MusicUtils.parseChordDb();
		BluetoothClass.sendToFretX(Util.str2array("{0}"));
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		initGui();
		showTutorial();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mActivity == null || mActivity.audio == null) return;
	}

	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		if(currentChord==null) return;
		chordExerciseView.setChord(currentChord);
	}

	private void initGui(){
		chordExerciseView = (LearnCustomChordExerciseView) rootView.findViewById(R.id.customChordExerciseView);
		chordExerciseView.setRootView(rootView);
		chordExerciseView.setFretBoardView((FretboardView) rootView.findViewById(R.id.fretboardView));
		chordExerciseView.setChordDb(chordFingerings);
		addButton = (Button) rootView.findViewById(R.id.addChordButton);
		removeButton = (Button) rootView.findViewById(R.id.removeChordButton);
		startButton = (Button) rootView.findViewById(R.id.startExerciseButton);
		chordSequenceListView = (ListView) rootView.findViewById(R.id.chordSequenceListView);
		populateChordPicker();
		setOnClickListeners();
		chordSequenceListAdapter = new ArrayAdapter<Chord>(mActivity,R.layout.chord_sequence_item,chordSequence);
		chordSequenceListView.setAdapter(chordSequenceListAdapter);
	}

	private void populateChordPicker(){
		String[] rootNotes = {"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
		String [] chordTypes = {"maj","m","5","maj7","m7","sus2","sus4","dim","dim7","aug",};

		LinearLayout rootNoteView = (LinearLayout) mActivity.findViewById(R.id.chordPickerRootNoteView);
		LinearLayout chordTypeView = (LinearLayout) mActivity.findViewById(R.id.chordPickerTypeView);

		//TODO: do proper, unrepeated code dammit
		TextView tmpTextView;
		for (String str :rootNotes) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(50);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			rootNoteView.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerRootNoteView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					updateCurrentChord(((TextView) view).getText().toString(),currentChord.getType());
				}
			});
		}
		tmpTextView = new TextView(chordTypeView.getContext());
		for (String str : chordTypes) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(50);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			chordTypeView.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerTypeView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					updateCurrentChord(currentChord.getRoot(),((TextView) view).getText().toString());
				}
			});
		}

		TextView initialRoot = (TextView) rootNoteView.getChildAt(0);
		TextView initialType = (TextView) chordTypeView.getChildAt(0);
		initialRoot.setTextColor(mActivity.getResources().getColor(R.color.primaryText));
		initialType.setTextColor(mActivity.getResources().getColor(R.color.primaryText));
		updateCurrentChord(initialRoot.getText().toString(),initialType.getText().toString());
	}

	private void setOnClickListeners(){
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(currentChord == null) return;
				chordSequence.add(currentChord);
				chordSequenceListAdapter.notifyDataSetChanged();
			}
		});

		removeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(chordSequence.size()>0){
					chordSequence.remove(chordSequence.size()-1);
					chordSequenceListAdapter.notifyDataSetChanged();
				}
			}
		});

		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startExercise();
			}
		});
	}

	private void startExercise(){
		//TODO: launch fragment
		if(chordSequence.size()<1) return;
		LearnFragmentChordExercise fragmentChordExercise = new LearnFragmentChordExercise();
		FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.learn_container, fragmentChordExercise, "fragmentCustomChordExercise");
		fragmentChordExercise.setChords(chordSequence);
		fragmentTransaction.addToBackStack("customChordMakerToCustomChordExercise");
		fragmentTransaction.commit();
		fragmentManager.executePendingTransactions();
	}
	private void showTutorial(){

//		new MaterialIntroView.Builder(mActivity)
//				.enableDotAnimation(false)
//				.enableIcon(false)
//				.setFocusGravity(FocusGravity.CENTER)
//				.setFocusType(Focus.ALL)
//				.setDelayMillis(300)
//				.enableFadeAnimation(true)
//				.performClick(true)
//				.setInfoText("This is the Chord Library. You can review or learn any chord you choose here. \nJust pick any combination of chord and watch it show up on your guitar!")
//				.setTarget((LinearLayout) mActivity.findViewById(R.id.chordPickerContainer))
//				.setUsageId("tutorialChordLibrary") //THIS SHOULD BE UNIQUE ID
//				.show();
	}
}