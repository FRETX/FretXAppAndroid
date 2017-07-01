package fretx.version4.paging.learn.custom;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fretx.version4.utils.Preference;
import fretx.version4.view.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by onurb_000 on 15/12/16.
 */

public class LearnCustomBuilderFragment extends Fragment
implements LearnCustomBuilderDialog.LearnCustomBuilderDialogListener {

	//view
	FrameLayout rootView;
	FretboardView fretboardView;
	TextView chordText;
	Button addButton;
	Button addedButton;
	Button startButton;

	//chords
	Chord currentChord;
	ArrayList<Sequence> sequences;
	int currentSequenceIndex;

	public LearnCustomBuilderFragment(){}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Analytics.getInstance().logSelectEvent("EXERCISE", "Custom Chord");

		sequences = LearnCustomBuilderJson.load(getContext());
		sequences.add(0, new Sequence(null, new ArrayList<Chord>()));
		currentSequenceIndex = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//setup view
		rootView = (FrameLayout) inflater.inflate(R.layout.paging_learn_custom_builder, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordText = (TextView) rootView.findViewById(R.id.textChord);
		addButton = (Button) rootView.findViewById(R.id.addChordButton);
		addedButton = (Button) rootView.findViewById(R.id.addedChordButton);
		startButton = (Button) rootView.findViewById(R.id.startExerciseButton);

		if (Preference.getInstance().isLeftHanded()) {
			fretboardView.setScaleX(-1.0f);
		}

		return  rootView;
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		populateChordPicker();
		setOnClickListeners();
	}

	@Override
	public void onResume() {
		super.onResume();
		BluetoothAnimator.getInstance().stringFall();
	}

	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		fretboardView.setFretboardPositions(currentChord.getFingerPositions());
		chordText.setText(currentChord.toString());
	}

	private TextView populateChordPickerLine(String[] contents, @IdRes int idRes,
										 View.OnClickListener onClickListener) {
		LinearLayout linearLayout = (LinearLayout) BaseActivity.getActivity().findViewById(idRes);
		TextView tmpTextView;
		for (String str : contents) {
			tmpTextView = new TextView(BaseActivity.getActivity());
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
		initial.setTextColor(BaseActivity.getActivity().getResources().getColor(R.color.tertiaryText));
		return initial;
	}

	private void populateChordPicker(){
		String[] rootNotes = {"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
		String [] chordTypes = {"maj","m","5","maj7","m7","sus2","sus4","dim","dim7","aug",};

		TextView initialRoot = populateChordPickerLine(rootNotes, R.id.chordPickerRootNoteView,
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						LinearLayout layout = (LinearLayout) BaseActivity.getActivity().findViewById(R.id.chordPickerRootNoteView);
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
						LinearLayout layout = (LinearLayout) BaseActivity.getActivity().findViewById(R.id.chordPickerTypeView);
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
		final LearnCustomBuilderDialog.LearnCustomBuilderDialogListener listener = this;

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
                LearnCustomBuilderDialog dialog =
						LearnCustomBuilderDialog.newInstance(listener, sequences,
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
		if(chords.size() < 1)
			return;
		LearnCustomExercise fragmentChordExercise = new LearnCustomExercise();
		fragmentChordExercise.setChords(chords);

		((MainActivity)getActivity()).fragNavController.pushFragment(fragmentChordExercise);
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
