package fretx.version4.paging.learn;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.w3c.dom.Text;

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

public class LearnCustomChordExerciseFragment extends Fragment
implements LearnCustomChordExerciseDialog.LearnCustomChordExerciseListener {
	MainActivity mActivity;

	//view
	FrameLayout rootView;
	FretboardView fretboardView;
	TextView chordText;
	Button addButton;
	Button addedButton;
	Button startButton;

	//chords
	Chord currentChord;
	HashMap<String,FingerPositions> chordDb;
	ArrayList<Sequence> sequences;
	int currentSequenceIndex;

	public LearnCustomChordExerciseFragment(){}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//retrieve saved sequences
		sequences = LearnCustomChordExerciseJson.load(getContext());

		//add a new empty sequence
		sequences.add(0, new Sequence(null, new ArrayList<Chord>()));
		currentSequenceIndex = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();

		//retrieve chords database
		chordDb = MusicUtils.parseChordDb();

		//firebase log
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Custom Chord Exercise activated");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

		//setup view
		rootView = (FrameLayout) inflater.inflate(R.layout.chord_custom_sequence_layout, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordText = (TextView) rootView.findViewById(R.id.textChord);
		addButton = (Button) rootView.findViewById(R.id.addChordButton);
		addedButton = (Button) rootView.findViewById(R.id.addedChordButton);
		startButton = (Button) rootView.findViewById(R.id.startExerciseButton);

		return  rootView;
	}

    @Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		//clear fretx matrix
		BluetoothClass.sendToFretX(Util.str2array("{0}"));
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		populateChordPicker();
		setOnClickListeners();
		showTutorial();
	}

	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		fretboardView.setFretboardPositions(currentChord.getFingerPositions());
		chordText.setText(currentChord.toString());
	}

	private TextView populateChordPickerLine(String[] contents, @IdRes int idRes,
										 View.OnClickListener onClickListener) {
		LinearLayout linearLayout = (LinearLayout) mActivity.findViewById(idRes);
		TextView tmpTextView;
		for (String str : contents) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(26);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			linearLayout.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setBackgroundColor(getResources().getColor(R.color.primary));
			tmpTextView.setTextColor(getResources().getColor(R.color.tertiaryText));
			tmpTextView.setOnClickListener(onClickListener);
		}
		TextView initial = (TextView) linearLayout.getChildAt(0);
		initial.setBackgroundResource(R.drawable.picker_text_background);
		initial.setTextColor(mActivity.getResources().getColor(R.color.tertiaryText));
		return initial;
	}

	private void populateChordPicker(){
		String[] rootNotes = {"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
		String [] chordTypes = {"maj","m","5","maj7","m7","sus2","sus4","dim","dim7","aug",};

		TextView initialRoot = populateChordPickerLine(rootNotes, R.id.chordPickerRootNoteView,
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerRootNoteView);
						for (int i = 0; i < layout.getChildCount(); i++) {
							View v = layout.getChildAt(i);
							v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
						view.setBackgroundResource(R.drawable.picker_text_background);
						updateCurrentChord(((TextView) view).getText().toString(),currentChord.getType());
					}
				});

		TextView initialType = populateChordPickerLine(chordTypes, R.id.chordPickerTypeView,
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerTypeView);
						for (int i = 0; i < layout.getChildCount(); i++) {
							View v = layout.getChildAt(i);
							v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
						view.setBackgroundResource(R.drawable.picker_text_background);
						updateCurrentChord(currentChord.getRoot(), ((TextView) view).getText().toString());
					}
				});

		updateCurrentChord(initialRoot.getText().toString(),initialType.getText().toString());
	}

	private void setOnClickListeners(){
		final LearnCustomChordExerciseDialog.LearnCustomChordExerciseListener listener = this;

		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(currentChord == null)
					return;
				sequences.get(currentSequenceIndex).addChord(currentChord);
				Toast.makeText(getContext(), "Chord added to list", Toast.LENGTH_SHORT).show();
			}
		});

		addedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                LearnCustomChordExerciseDialog dialog =
						LearnCustomChordExerciseDialog.newInstance(listener, sequences,
								currentSequenceIndex);
                dialog.show(getFragmentManager(), "dialog");
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
		ArrayList<Chord> chords = sequences.get(currentSequenceIndex).getChords();
		if(chords.size()<1) return;
		LearnChordExerciseFragment fragmentChordExercise = new LearnChordExerciseFragment();
		fragmentChordExercise.setChords(chords);
		mActivity.fragNavController.pushFragment(fragmentChordExercise);
	}

	private void showTutorial(){
        /*
        new MaterialIntroView.Builder(mActivity)
        .enableDotAnimation(false)
        .enableIcon(false)
        .setFocusGravity(FocusGravity.CENTER)
        .setFocusType(Focus.ALL)
        .setDelayMillis(300)
        .enableFadeAnimation(true)
        .performClick(true)
        .setInfoText("This is the Chord Library. You can review or learn any chord you choose here. \nJust pick any combination of chord and watch it show up on your guitar!")
        .setTarget((LinearLayout) mActivity.findViewById(R.id.chordPickerContainer))
        .setUsageId("tutorialChordLibrary") //THIS SHOULD BE UNIQUE ID
        .show();
         */
	}

    public void onUpdate(ArrayList<Sequence> sequences, int currentSequenceIndex){
        this.sequences = sequences;
        if (this.sequences.size() == 0)
        {
            sequences.add(new Sequence(null, new ArrayList<Chord>()));
            this.currentSequenceIndex = 0;
        } else {
            this.currentSequenceIndex = currentSequenceIndex;
        }
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
