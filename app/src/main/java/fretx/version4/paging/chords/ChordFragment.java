package fretx.version4.paging.chords;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by Misho on 2/4/2016.
 */

public class ChordFragment extends Fragment
{
	Chord currentChord;
    MainActivity mActivity;
    View rootView;
	FretboardView fretboardView;

	HashMap<String,FingerPositions> chordFingerings;

	public ChordFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.chord_fragment, container, false);

//        vvMain = (ObservableVideoView)rootView.findViewById(R.id.vvMain);

//        imgBack = (ImageView)rootView.findViewById(R.id.imgBackground);

//	    imgBack.setImageResource(imageBackgroundIDs[0]);

        // input data file
//        videoUri[0] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.cmajor);
//        videoUri[1] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.dmajor);
//        videoUri[2] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.emajor);
//        videoUri[3] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.fmajor);
//        videoUri[4] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.gmajor);
//        videoUri[5] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.amajor);
//        videoUri[6] = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.bmajor);


        // input text file

//        byte[] array1 = data2array(R.raw.cmajortxt);
//        byte[] array2 = data2array(R.raw.dmajortxt);
//        byte[] array3 = data2array(R.raw.emajortxt);
//        byte[] array4 = data2array(R.raw.fmajortxt);
//        byte[] array5 = data2array(R.raw.gmajortxt);
//        byte[] array6 = data2array(R.raw.amajortxt);
//        byte[] array7 = data2array(R.raw.bmajortxt);

//        musicArray.add(array1);
//        musicArray.add(array2);
//        musicArray.add(array3);
//        musicArray.add(array4);
//        musicArray.add(array5);
//        musicArray.add(array6);
//        musicArray.add(array7);

        // init gallery
//        Gallery gallery = (Gallery) rootView.findViewById(R.id.gallery1);
//        gallery.setAdapter(new ImageAdapter(mActivity));
//        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(mActivity.getBaseContext(), "pic" + (position + 1) + " selected", Toast.LENGTH_SHORT).show();
//                playChord(position);
//            }
//        });
        return  rootView;
    }
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		fretboardView = (FretboardView) mActivity.findViewById(R.id.fretboardView);
		chordFingerings = MusicUtils.parseChordDb();

		BluetoothClass.sendToFretX(Util.str2array("{0}"));
//		ConnectThread connectThread = new ConnectThread(Util.str2array("{0}"));
//		connectThread.run();

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
		showTutorial();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mActivity == null || mActivity.audio == null) return;
//		mActivity.audio.disablePitchDetector();
//		mActivity.audio.disableNoteDetector();
//		mActivity.audio.disableChordDetector();
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
	}

}


