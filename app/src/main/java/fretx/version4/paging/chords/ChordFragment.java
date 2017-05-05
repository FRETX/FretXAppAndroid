package fretx.version4.paging.chords;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.audio.Midi;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

public class ChordFragment extends Fragment
{
	Chord currentChord;
    MainActivity mActivity;
    View rootView;
	FretboardView fretboardView;
	ImageButton playChordButton;
	HashMap<String,FingerPositions> chordFingerings;

	public ChordFragment (){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        rootView = inflater.inflate(R.layout.paging_chord, container, false);
        return  rootView;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Chords");
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "TAB");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

		fretboardView = (FretboardView) mActivity.findViewById(R.id.fretboardView);
		
		playChordButton = (ImageButton) mActivity.findViewById(R.id.playChordButton);
		
		chordFingerings = MusicUtils.parseChordDb();

		BluetoothLE.getInstance().clearMatrix();

		String[] rootNotes = {"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
		String [] chordTypes = {"maj","m","maj7","m7","sus2","sus4","dim","dim7","aug",};

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
							((TextView) v).setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));
						}
					}
//					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.tertiaryText));
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
//							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.deepBackground));
							((TextView) v).setBackgroundResource(0);
							v.setBackgroundColor(getContext().getResources().getColor(R.color.primary));

						}
					}
//					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.tertiaryText));
					((TextView) view).setBackgroundResource(R.drawable.picker_text_background);
					updateCurrentChord(currentChord.getRoot(),((TextView) view).getText().toString());
				}
			});
		}

		TextView initialRoot = (TextView) rootNoteView.getChildAt(0);
		TextView initialType = (TextView) chordTypeView.getChildAt(0);
//		initialRoot.setTextColor(mActivity.getResources().getColor(R.color.tertiaryText));
//		initialType.setTextColor(mActivity.getResources().getColor(R.color.tertiaryText));
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

	@Override
	public void onResume(){
		super.onResume();

		int[] config = Midi.getInstance().config();
		Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
		Log.d(this.getClass().getName(), "numChannels: " + config[1]);
		Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
		Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
	}

	private void updateCurrentChord(String root , String type){
		currentChord = new Chord(root,type);
		Log.d("Chord Selector",currentChord.toString());
//		FingerPositions fp = chordFingerings.get(currentChord.toString());

		fretboardView.setFretboardPositions(currentChord.getFingerPositions());
		TextView textChord = (TextView) rootView.findViewById(R.id.textChord);
		textChord.setText(root + " " + type);

//		chordView.setFingerPositions(chordFingerings.get(currentChord.toString()));
//		int[] chordNotes = currentChord.getNotes();
//		byte[] bluetoothArray = new byte[chordNotes.length+1];
//		//TODO: gotta take care of the exceptions here, or somewhere this will probably create wrong fingerings for some chords
		BluetoothLE.getInstance().setMatrix(currentChord);
	}
}