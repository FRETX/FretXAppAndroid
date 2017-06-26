package fretx.version4.paging.tuner;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import fretx.version4.HeadStockView;
import fretx.version4.R;
import fretx.version4.TunerBarView;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.firebase.Analytics;
import fretx.version4.utils.audio.Audio;
import rocks.fretx.audioprocessing.AudioAnalyzer;
import rocks.fretx.audioprocessing.MusicUtils;


public class TunerFragment extends Fragment {
    private static final String TAG = "KJKP6_TUNER";
    private static final int UPDATE_DELAY_MS = 20;
    private static final int HALF_PITCH_RANGE_CTS = 100;
    private static final int NO_NOTE_DELAY_MS = 10000;

	private final Handler handler = new Handler();
    private long lastNote = 0;

    private int currentPitchIndex;
    private final double centerPitchesCts[] = new double[6];
    private final double pitchDifference[] = new double[6];
    private double leftMostPitchCts;
    private double rightMostPitchCts;

    private HeadStockView headStockView;
    private TunerBarView tunerBarView;
    private TextView tunerLowText;
    private TextView tunerHighText;
    private Switch tunerSwitch;

    private final TunerDialog dialog = new TunerDialog();
    private boolean shown;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.getInstance().logSelectEvent("TAB", "Tuner");

        final int tuningMidiNote[] = MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD);
        for (int index = 0; index < tuningMidiNote.length; ++index) {
            final double hz = MusicUtils.midiNoteToHz(tuningMidiNote[index]);
            centerPitchesCts[index] = MusicUtils.hzToCent(hz);
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "created");

        View rootView = inflater.inflate(R.layout.paging_tuner, container, false);
		headStockView = (HeadStockView) rootView.findViewById(R.id.headStockView);
        tunerBarView = (TunerBarView) rootView.findViewById(R.id.tuner_bar);
        tunerLowText = (TextView) rootView.findViewById(R.id.tuner_low_text);
        tunerHighText = (TextView) rootView.findViewById(R.id.tuner_high_text);
        tunerSwitch = (Switch) rootView.findViewById(R.id.tuner_mode_switch);

        setNote(0);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        headStockView.setOnEarSelectedListener(new HeadStockView.OnEarSelectedListener() {
            @Override
            public void onEarSelected(int selectedIndex) {
                setNote(selectedIndex);
            }
        });

        tunerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Switch s = (Switch) v;
                if (s.isChecked()) {
                    headStockView.setClickable(false);
                } else {
                    headStockView.setClickable(true);
                }
            }
        });

        handler.post(update);
	}

	@Override
	public void onResume() {
		super.onResume();
		BluetoothAnimator.getInstance().stringFall();

        lastNote = System.currentTimeMillis();
	}

	private final Runnable update = new Runnable() {
		@Override
		public void run() {
            final double currentPitch = Audio.getInstance().getPitch();

            if (currentPitch == -1) {
                //handle no note played for predefined time
                if (!shown && System.currentTimeMillis() - lastNote > NO_NOTE_DELAY_MS) {
                    Log.d(TAG, "no note");
                    tunerLowText.setVisibility(View.INVISIBLE);
                    tunerHighText.setVisibility(View.INVISIBLE);
                    tunerBarView.setPitch(-1);
                    shown = true;
                    dialog.show(getActivity().getSupportFragmentManager(), null);
                }
            } else {
                Log.d(TAG, "current pitch: " + currentPitch);
                lastNote = System.currentTimeMillis();

                //dismiss dialog
                if (shown) {
                    shown = false;
                    dialog.dismiss();
                }

                //auto set the played note
                if (tunerSwitch.isChecked()) {
                    autoDetectNote(currentPitch);
                }

                //update text and tuner bar
                if (currentPitch < leftMostPitchCts) {
                    Log.d(TAG, "too low");
                    tunerLowText.setVisibility(View.VISIBLE);
                    tunerHighText.setVisibility(View.INVISIBLE);
                    tunerBarView.setPitch(leftMostPitchCts);
                } else if (currentPitch > rightMostPitchCts) {
                    Log.d(TAG, "too high");
                    tunerLowText.setVisibility(View.INVISIBLE);
                    tunerHighText.setVisibility(View.VISIBLE);
                    tunerBarView.setPitch(rightMostPitchCts);
                } else {
                    Log.d(TAG, "in the range");
                    tunerLowText.setVisibility(View.INVISIBLE);
                    tunerHighText.setVisibility(View.INVISIBLE);
                    tunerBarView.setPitch(currentPitch);
                }
            }

            handler.postDelayed(update, UPDATE_DELAY_MS);
		}
	};

	//update the tuner bar for a specified note
	private void setNote(int index) {
        currentPitchIndex = index;
        final double centerPitchCts = centerPitchesCts[index];
        leftMostPitchCts = centerPitchCts - HALF_PITCH_RANGE_CTS;
        rightMostPitchCts = centerPitchCts + HALF_PITCH_RANGE_CTS;

        Log.d(TAG, "==== SET PITCHES ====");
        Log.d(TAG, "left: " + leftMostPitchCts);
        Log.d(TAG, "center: " + centerPitchCts);
        Log.d(TAG, "right: " + rightMostPitchCts);

        tunerBarView.setTargetPitch(leftMostPitchCts, centerPitchCts, rightMostPitchCts);
        headStockView.setSelectedEar(index);
    }

    //find the closest note to the one played (auto mode)
    private void autoDetectNote(double pitch) {
        for (int index = 0; index < pitchDifference.length; index++) {
            pitchDifference[index] = pitch - centerPitchesCts[index];
            pitchDifference[index] = Math.abs(pitchDifference[index]);
        }

        final int minIndex = AudioAnalyzer.findMinIndex(pitchDifference);
        if (currentPitchIndex != minIndex) {
            setNote(minIndex);
        }
    }
}