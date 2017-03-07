package fretx.version4.paging.learn;

import android.os.Bundle;
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

public class LearnCustomChordExerciseFragment extends Fragment {
	Chord currentChord;
	MainActivity mActivity;
	FrameLayout rootView;
	LearnCustomChordExerciseView chordExerciseView;
	HashMap<String,FingerPositions> chordFingerings;
	Button addButton, addedButton, startButton;
	ArrayList<Chord> chordSequence = new ArrayList<>();

	public LearnCustomChordExerciseFragment(){}

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

	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		chordExerciseView.setChord(currentChord);
	}

	private void initGui(){
		chordExerciseView = (LearnCustomChordExerciseView) rootView.findViewById(R.id.customChordExerciseView);
		chordExerciseView.setRootView(rootView);
		chordExerciseView.setFretBoardView((FretboardView) rootView.findViewById(R.id.fretboardView));
		chordExerciseView.setChordDb(chordFingerings);
		addButton = (Button) rootView.findViewById(R.id.addChordButton);
		addedButton = (Button) rootView.findViewById(R.id.addedChordButton);
		startButton = (Button) rootView.findViewById(R.id.startExerciseButton);
		populateChordPicker();
		setOnClickListeners();
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
			tmpTextView.setTextSize(26);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			rootNoteView.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setBackgroundColor(getResources().getColor(R.color.primary));
			tmpTextView.setTextColor(getResources().getColor(R.color.tertiaryText));
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerRootNoteView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
//							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
						}
						((TextView) v).setBackgroundResource(0);
						v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
					}
//					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					((TextView) view).setBackgroundResource(R.drawable.picker_text_background);
					updateCurrentChord(((TextView) view).getText().toString(),currentChord.getType());
				}
			});
		}
		tmpTextView = new TextView(chordTypeView.getContext());
		for (String str : chordTypes) {
			tmpTextView = new TextView(mActivity);
			tmpTextView.setText(str);
			tmpTextView.setTextSize(26);
			tmpTextView.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			chordTypeView.addView(tmpTextView);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tmpTextView.getLayoutParams();
			params.setMargins(30, 0, 30, 0);
			tmpTextView.setLayoutParams(params);
			tmpTextView.setBackgroundColor(getResources().getColor(R.color.primary));
			tmpTextView.setTextColor(getResources().getColor(R.color.tertiaryText));
			tmpTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.chordPickerTypeView);
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
//							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
						}
						((TextView) v).setBackgroundResource(0);
						v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
					}
//					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
					((TextView) view).setBackgroundResource(R.drawable.picker_text_background);
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
				Toast.makeText(getContext(), "Chord added", Toast.LENGTH_SHORT).show();
			}
		});

		addedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                LearnCustomChordExerciseDialog dialog = LearnCustomChordExerciseDialog.newInstance(chordSequence);
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
		//TODO: launch fragment
		if(chordSequence.size()<1) return;
		LearnChordExerciseFragment fragmentChordExercise = new LearnChordExerciseFragment();
		FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.learn_container, fragmentChordExercise, "fragmentCustomChordExercise");
		fragmentChordExercise.setChords(chordSequence);
		fragmentTransaction.addToBackStack("customChordMakerToCustomChordExercise");
		fragmentTransaction.commit();
		fragmentManager.executePendingTransactions();
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

    /*
    private HashMap<String, Sequence> getSequencesFromJson(String filename) {
        File file = new File(getContext().getFilesDir(), filename);
        if(file.exists()) {
            StringBuilder sb;
            //retrieve file as string
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
                sb = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
            String jsonString = sb.toString();

            //retrieve a list of sequence
            try {
                JSONObject jsonObj = new JSONObject(jsonString);
                JSONArray jsonSequences = jsonObj.getJSONArray("sequences");
                HashMap<String, Sequence> sequences = new HashMap<>();
                for (int i = 0; i < jsonSequences.length(); i++) {
                    try {
                        JSONObject jsonSequence = jsonSequences.getJSONObject(i);
                        String name = jsonSequence.getString("name");
                        Sequence sequence = new Sequence(name, new ArrayList<Chord>());
                        JSONArray jsonChords = jsonSequence.getJSONArray("chords");
                        for (int j = 0; j < jsonChords.length(); j++) {
                            JSONObject jsonChord = jsonChords.getJSONObject(j);
                            String root = jsonChord.getString("root");
                            String type = jsonChord.getString("type");
                            Chord chord = new Chord(root, type);
                            sequence.addChord(chord);
                        }
                        sequences.put(sequence.getName(), sequence);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return sequences;
            } catch (JSONException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        } else {
            return new HashMap<>();
        }
    }

    private void setJsonFromSequences(ArrayList<Sequence> sequences) {
    }
    */
}
