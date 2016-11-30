package fretx.version4.paging.learn;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import fretx.version4.BluetoothClass;
import fretx.version4.activities.MainActivity;
import fretx.version4.NoteItem;
import fretx.version4.R;
import fretx.version4.Util;

import static rocks.fretx.audioprocessing.MusicUtils.hzToMidiNote;


public class LearnFragmentNotesEx extends Fragment{
	private Thread guiThread;
	protected final double correctNoteThreshold = 0.5; //in semitones

	Timer timer;
	MyTimerTask myTimerTask;
	int nCounter = 0;

	private ArrayList<Button> buttons = new ArrayList<Button>();

	private TextView tvTimeLapse;
	//int oldString = 1;

	int completedCount = 0;
	int newPitch = 0;
	//int notes[] = new int[]{41, 47, 51, 57, 60, 66};
	ArrayList<NoteItem> notes = new ArrayList<NoteItem>();
	String labels[];

	private MainActivity mActivity;
	private View rootView;

	public LearnFragmentNotesEx(){

	}

	public static LearnFragmentNotesEx newInstance(int exId, String title, int nextExId, String[] labels) {
		LearnFragmentNotesEx myFragment = new LearnFragmentNotesEx();

		Bundle args = new Bundle();
		args.putInt("exId", exId);
		args.putString("title", title);
		args.putInt("nextExId", nextExId);
		args.putStringArray("labels", labels);
		myFragment.setArguments(args);

		return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		labels = getArguments().getStringArray("labels");
		notes = Util.getNoteItems(mActivity, getArguments().getStringArray("labels"));

		ConnectThread connectThread = new ConnectThread(Util.str2array(notes.get(newPitch).ledArray));
		connectThread.run();

		rootView = inflater.inflate(R.layout.learn_fragment_tuner, container, false);
		LinearLayout ll = (LinearLayout)rootView.findViewById(R.id.linearLayout);

		for(int i = 0; i < 6; i++) {
			Button myButton = new Button(mActivity);
			myButton.setText(labels[i]);
			myButton.setTag(i + labels[i]);
			myButton.setWidth(200);

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.addView(myButton, lp);
			buttons.add((Button) ll.findViewWithTag(i + labels[i]));

			if(i==0){
				buttons.get(i).setBackgroundColor(Color.GREEN);
			}else{
				buttons.get(i).setBackgroundColor(Color.BLUE);
			}
		}

		tvTimeLapse = (TextView)rootView.findViewById(R.id.tvTimeLapse);

		startTimer();
		return rootView;

    }

	public void startTimer(){
		if (timer != null){
			timer.cancel();
		}
		timer = new Timer();

		myTimerTask = new MyTimerTask();

		timer.schedule(myTimerTask, 1000, 1000);
	}

	private void initSystemServices() {
		//TODO: needs testing with calls
		TelephonyManager telephonyManager =
				(TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (mActivity.audio == null) return;
				if (state == TelephonyManager.CALL_STATE_IDLE) {
					if (!mActivity.audio.isInitialized())
						mActivity.audio.initialize(mActivity.fs, mActivity.bufferSizeInSeconds);
					if (!mActivity.audio.isProcessing()) mActivity.audio.start();
				} else {
					mActivity.audio.stop();
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			nCounter ++;
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					tvTimeLapse.setText("" + nCounter / 60 + " : " + nCounter % 60);
				}
			});
		}

	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity)getActivity();
		ConnectThread connectThread = new ConnectThread(Util.str2array("{0}"));
		connectThread.run();

		mActivity = (MainActivity) getActivity();
		mActivity.audio.enablePitchDetector();
		mActivity.audio.enableNoteDetector();
		mActivity.audio.disableChordDetector();

		initSystemServices();
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity = (MainActivity)getActivity();
		ConnectThread connectThread = new ConnectThread(Util.str2array("{0}"));
		connectThread.run();
		initSystemServices();
		if(!mActivity.audio.isInitialized()) mActivity.audio.initialize(mActivity.fs,mActivity.bufferSizeInSeconds);
		mActivity.audio.enablePitchDetector();
		mActivity.audio.enableNoteDetector();
		mActivity.audio.disableChordDetector();
		if(!mActivity.audio.isProcessing()) mActivity.audio.start();

		guiThread = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						//Even though YIN is producing a pitch estimate every 16ms, that's too fast for the UI on some devices
						//So we set it to 25ms, which is good enough
						Thread.sleep(25);
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								double pitch = -1;
								if(mActivity.audio != null){
									if (mActivity.audio.isInitialized() && mActivity.audio.isProcessing()) {
										pitch = mActivity.audio.getPitch();
									}
								}

								int currentNote = notes.get(newPitch).noteMidi;
								if (pitch > -1) {
									double pitchMidi = hzToMidiNote(pitch);

									if (Math.abs(currentNote - pitchMidi) < correctNoteThreshold) {
										buttons.get(newPitch % 6).setBackgroundColor(Color.BLUE);
										if (newPitch == notes.size() - 1) {
											completedCount++;
										}
										if (newPitch < notes.size() - 1) {
											newPitch++;
										} else {
											newPitch = 0;//rand.nextInt(notes.length);
										}
										buttons.get(newPitch % 6).setBackgroundColor(Color.GREEN);
										if (newPitch % 6 == 0) {
											for (int j = newPitch; j < newPitch + 6; j++) {
												if (j < labels.length) {
													buttons.get(j % 6).setText(labels[j]);
												} else {
													buttons.get(j % 6).setText("");
												}
											}
										}
										ConnectThread connectThread = new ConnectThread(Util.str2array(notes.get(newPitch).ledArray));
										connectThread.run();
									}
								}


							}
						});
					}
				} catch (InterruptedException e) {
				}
			}
		};
		guiThread.start();



	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ConnectThread connectThread = new ConnectThread(Util.str2array("{0}"));
		connectThread.run();
		mActivity.audio.disablePitchDetector();
		mActivity.audio.disableNoteDetector();
		mActivity.audio.disableChordDetector();
		guiThread.interrupt();
		try {
			guiThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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