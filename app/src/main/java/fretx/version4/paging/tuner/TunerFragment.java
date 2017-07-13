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

import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.view.HeadStockView;
import fretx.version4.R;
import fretx.version4.view.TunerBarView;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.firebase.Analytics;
import fretx.version4.utils.audio.Audio;
import rocks.fretx.audioprocessing.AudioAnalyzer;
import rocks.fretx.audioprocessing.MusicUtils;


public class TunerFragment extends Fragment {
    private static final String TAG = "KJKP6_TUNER";
    private static final int UPDATE_DELAY_MS = 10;
    private static final int HALF_PITCH_RANGE_CTS = 100;

	private final Handler handler = new Handler();

    private int currentPitchIndex;
    private final double centerPitchesHz[] = new double[6];
    private final double centerPitchesCts[] = new double[6];
    private final double pitchDifferenceHz[] = new double[6];
    private double leftMostPitchHz;
    private double rightMostPitchHz;

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
            centerPitchesHz[index] = MusicUtils.midiNoteToHz(tuningMidiNote[index]);
            centerPitchesCts[index] = MusicUtils.hzToCent(centerPitchesHz[index]);
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "created");

        final View rootView = inflater.inflate(R.layout.paging_tuner_fragment, container, false);
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
	}

	private final Runnable update = new Runnable() {
		@Override
		public void run() {
            final double currentPitchHz = Audio.getInstance().getPitch();

            if (currentPitchHz == -1) {
                //handle no note played for predefined time
                //if (!shown && System.currentTimeMillis() - lastNote > NO_NOTE_DELAY_MS) {
                    //Log.d(TAG, "no note");
                tunerLowText.setVisibility(View.INVISIBLE);
                tunerHighText.setVisibility(View.INVISIBLE);
                tunerBarView.setPitch(-1, -1);
                    //shown = true;
                    //dialog.show(getActivity().getSupportFragmentManager(), null);
                //}
            } else {
                final double currentPitchCts = MusicUtils.hzToCent(currentPitchHz);

                //dismiss dialog
                if (shown) {
                    shown = false;
                    dialog.dismiss();
                }

                //auto set the played note
                if (tunerSwitch.isChecked()) {
                    autoDetectNote(currentPitchHz);
                }

                //update text
                if (currentPitchHz < leftMostPitchHz) {
                    //Log.d(TAG, "too low");
                    tunerLowText.setVisibility(View.VISIBLE);
                    tunerHighText.setVisibility(View.INVISIBLE);
                } else if (currentPitchHz > rightMostPitchHz) {
                    //Log.d(TAG, "too high");
                    tunerLowText.setVisibility(View.INVISIBLE);
                    tunerHighText.setVisibility(View.VISIBLE);
                } else {
                    //Log.d(TAG, "in the range");
                    tunerLowText.setVisibility(View.INVISIBLE);
                    tunerHighText.setVisibility(View.INVISIBLE);
                }

                //update tuner bar
                tunerBarView.setPitch(currentPitchCts, currentPitchHz);
            }

            handler.postDelayed(update, UPDATE_DELAY_MS);
		}
	};

	//update the tuner bar for a specified note
	private void setNote(int index) {
        currentPitchIndex = index;
        final double centerPitchCts = centerPitchesCts[index];
        leftMostPitchHz = MusicUtils.centToHz(centerPitchCts - HALF_PITCH_RANGE_CTS);
        rightMostPitchHz = MusicUtils.centToHz(centerPitchCts + HALF_PITCH_RANGE_CTS);
        tunerBarView.setTargetPitch(centerPitchCts - HALF_PITCH_RANGE_CTS,
                centerPitchCts, centerPitchCts + HALF_PITCH_RANGE_CTS);
        headStockView.setSelectedEar(index);
        Bluetooth.getInstance().setString(index + 1);
    }

    //find the closest note to the one played (auto mode)
    private void autoDetectNote(double pitchHz) {
        for (int index = 0; index < pitchDifferenceHz.length; index++) {
            pitchDifferenceHz[index] = pitchHz - centerPitchesHz[index];
            pitchDifferenceHz[index] = Math.abs(pitchDifferenceHz[index]);
        }

        final int minIndex = AudioAnalyzer.findMinIndex(pitchDifferenceHz);
        if (currentPitchIndex != minIndex) {
            setNote(minIndex);
        }
    }
}