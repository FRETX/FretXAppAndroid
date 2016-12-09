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

import java.io.IOException;

import fretx.version4.BluetoothClass;
import fretx.version4.FretboardView;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.MusicUtils;

public class LearnFragmentTwo extends Fragment {

	MainActivity mActivity;

	LinearLayout rootView = null;
	LearnScaleExerciseView scaleView;
	LinearLayout scalePicker;
	String[] scaleNames = {"FMinorPentatonic","Blues"};
	int[] notes;
	FretboardView fretboardView;

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
		updateScale("FMinorPentatonic");

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
							((TextView) v).setTextColor(mActivity.getResources().getColor(R.color.secondaryText));
						}
					}
					((TextView) view).setTextColor(mActivity.getResources().getColor(R.color.primaryText));
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
		switch(scaleName){
			case "FMinorPentatonic" :
				notes = new int[] {41,46,51,56,60,65,48,53,58,44,63,68};
//				notes = new int[] {41,44,46,48,51,53,56,58,60,63,65,68};
				break;
			case "Blues" :
				notes = new int[] {41,44,46,47,48,51,53,56,58,59,60,63,65,68};
				break;
			default:
				notes = new int[0];
				break;
		}

		byte[] bluetoothArray = new byte[notes.length+1];
		for (int i = 0; i < notes.length; i++) {
			FretboardPosition tmpFp =  MusicUtils.midiNoteToFretboardPosition(notes[i]);
			bluetoothArray[i] = Byte.valueOf(Integer.toString(tmpFp.getFret() * 10 + tmpFp.getString()));
		}
		bluetoothArray[notes.length] = 0;
		ConnectThread connectThread = new ConnectThread(bluetoothArray);
		connectThread.run();
		scaleView.setNotes(notes);

//		rootView.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				byte[] tempByte = {Byte.valueOf("0")};
//				ConnectThread connectThread = new ConnectThread(tempByte);
//				connectThread.run();
//				scaleView.setNotes(notes);
//			}
//		}, 2000); // 5000ms delay
	}





	/////////////////////////////////BlueToothConnection/////////////////////////
	static private class ConnectThread extends Thread {
		byte[] array;

		public ConnectThread(byte[] tmp) {
			array = tmp;
		}

		public void run() {
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				Util.startViaData(array);
			} catch (Exception connectException) {
				Log.i(BluetoothClass.tag, "connect failed");
				// Unable to connect; close the socket and get out
				try {
					BluetoothClass.mmSocket.close();
				} catch (IOException closeException) {
					Log.e(BluetoothClass.tag, "mmSocket.close");
				}
				return;
			}
			// Do work to manage the connection (in a separate thread)
			if (BluetoothClass.mHandler == null)
				Log.v("debug", "mHandler is null @ obtain message");
			else
				Log.v("debug", "mHandler is not null @ obtain message");
		}
	}


}
