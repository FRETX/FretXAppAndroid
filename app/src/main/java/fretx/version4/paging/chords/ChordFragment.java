package fretx.version4.paging.chords;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import fretx.version4.FretboardView;
import fretx.version4.activities.BaseActivity;
import fretx.version4.R;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.audio.Midi;
import fretx.version4.utils.firebase.FirebaseAnalytics;
import rocks.fretx.audioprocessing.Chord;

public class ChordFragment extends Fragment
{
    private final static String TAG = "KJKP6_CHORD";
	private Chord currentChord;
    private FretboardView fretboardView;
	private ImageButton playChordButton;
    private LinearLayout rootNoteView;
    private LinearLayout chordTypeView;
    private TextView textChord;

	public ChordFragment (){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAnalytics.getInstance().logSelectEvent("TAB", "Chords");
        BluetoothLE.getInstance().clearMatrix();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_chord, container, false);

        fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
        playChordButton = (ImageButton) rootView.findViewById(R.id.playChordButton);
        rootNoteView = (LinearLayout) rootView.findViewById(R.id.chordPickerRootNoteView);
        chordTypeView = (LinearLayout) rootView.findViewById(R.id.chordPickerTypeView);
        textChord = (TextView) rootView.findViewById(R.id.textChord);

        return  rootView;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

        //// TODO: 15/05/17 use the fragment already coded on the other project
        String[] rootNotes = {"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
		String [] chordTypes = {"maj","m","maj7","m7","sus2","sus4","dim","dim7","aug",};

		for (String str :rootNotes) {
			final TextView tmpTextView = new TextView(BaseActivity.getActivity());
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
					LinearLayout layout = rootNoteView;
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
                            v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
					}
                    view.setBackgroundResource(R.drawable.picker_text_background);
					updateCurrentChord(((TextView) view).getText().toString(),currentChord.getType());
				}
			});
		}
		for (String str : chordTypes) {
			final TextView tmpTextView = new TextView(BaseActivity.getActivity());
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
					LinearLayout layout = chordTypeView;
					for (int i = 0; i < layout.getChildCount(); i++) {
						View v = layout.getChildAt(i);
						if (v instanceof TextView) {
							v.setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
					}
					view.setBackgroundResource(R.drawable.picker_text_background);
					updateCurrentChord(currentChord.getRoot(),((TextView) view).getText().toString());
				}
			});
		}

		TextView initialRoot = (TextView) rootNoteView.getChildAt(0);
		TextView initialType = (TextView) chordTypeView.getChildAt(0);
		initialRoot.setBackgroundResource(R.drawable.picker_text_background);
		initialType.setBackgroundResource(R.drawable.picker_text_background);
		updateCurrentChord(initialRoot.getText().toString(),initialType.getText().toString());

		playChordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Midi.getInstance().playChord(currentChord);
			}
		});
	}

	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		Log.d(TAG, currentChord.toString());
        fretboardView.setFretboardPositions(currentChord.getFingerPositions());
        textChord.setText(root + " " + type);
        BluetoothLE.getInstance().setMatrix(currentChord);
	}
}