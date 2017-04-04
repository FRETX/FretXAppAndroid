package fretx.version4.paging.chords;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.MidiConstants;
import org.billthefarmer.mididriver.MidiDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import fretx.version4.BluetoothClass;
import fretx.version4.FretboardView;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.MusicUtils;

public class ChordFragment extends Fragment implements MidiDriver.OnMidiStartListener
{
	Chord currentChord;
    MainActivity mActivity;
    View rootView;
	FretboardView fretboardView;

	Button playChordButton;

	MidiDriver midiDriver;
	private byte[] event;
	private int[] config;
	private int notesIndex;
	Handler handler = new Handler();

	HashMap<String,FingerPositions> chordFingerings;

	@Override
	public void onMidiStart() {
		Log.d(this.getClass().getName(), "onMidiStart()");
		event = new byte[2];
		event[0] = (byte) 0xC0; //"Program Change" event for channel 1
		event[1] = GeneralMidiConstants.ACOUSTIC_GUITAR_NYLON; //set instrument
		midiDriver.write(event);
	}

	public ChordFragment (){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();
        rootView = inflater.inflate(R.layout.chord_fragment, container, false);
        return  rootView;
    }
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Chords Tab activated");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

		fretboardView = (FretboardView) mActivity.findViewById(R.id.fretboardView);
		
		playChordButton = (Button) mActivity.findViewById(R.id.playChordButton);
		
		chordFingerings = MusicUtils.parseChordDb();

		// Instantiate the driver.
		midiDriver = new MidiDriver();
		// Set the listener.
		midiDriver.setOnMidiStartListener(this);


		BluetoothClass.sendToFretX(Util.str2array("{0}"));

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
				final int[] notes = currentChord.getMidiNotes();
				notesIndex = 0;
				final int noteDelay = 30;
				final int sustainDelay = 500;

				Log.d("notes", Arrays.toString(notes));

				final Runnable turnOffAllNotes = new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < notes.length; i++) {
							stopNote(notes[i]);
						}		
					}
				};

				Runnable playNoteSequence = new Runnable() {
					@Override
					public void run() {
						if(notesIndex < notes.length){
							playNote(notes[notesIndex]);
							notesIndex++;
							handler.postDelayed(this,noteDelay);
						} else {
							handler.postDelayed(turnOffAllNotes,sustainDelay);
						}

					}
				};

				handler.post(playNoteSequence);


				
			}
		});

		showTutorial();

	}

	private void playNote(int note){
		event = new byte[3];
		event[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
		event[1] =  Byte.parseByte(Integer.toString(note));
		event[2] = (byte) 0x7F;  // 0x7F = the maximum velocity (127)
		midiDriver.write(event);

		Log.d("playing note",Integer.toString(note));
	}

	private void stopNote(int note) {
		event = new byte[3];
		event[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
		event[1] = Byte.parseByte(Integer.toString(note));
		event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)
		midiDriver.write(event);
		Log.d("stopping note", Integer.toString(note));
	}

	@Override
	public void onResume(){
		super.onResume();
		midiDriver.start();
		config = midiDriver.config();
		Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
		Log.d(this.getClass().getName(), "numChannels: " + config[1]);
		Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
		Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
	}

	@Override
	public void onPause(){
		super.onPause();
		midiDriver.stop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mActivity == null || mActivity.audio == null) return;
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

		byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(currentChord.toString(),chordFingerings);
		Log.d("Chord picker BT","sending :" + bluetoothArray.toString());

		BluetoothClass.sendToFretX(bluetoothArray);
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