package fretx.version4.paging.play;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import fretx.version4.Config;
import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.SongItem;
import fretx.version4.utils.Bluetooth;
import fretx.version4.utils.Midi;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by Kickdrum on 05-Jan-17.
 */

public class PlayChordPreviewFragment extends Fragment
{
	private MainActivity mActivity;

	//view
	private FretboardView fretboardView;
	private TextView chordText;
    private TextView position;
	private Button nextButton;
	private Button playButton;
	private ImageButton playChordButton;

	//chords
	private HashMap<String,FingerPositions> chordDb;
	private int chordIndex;
	private ArrayList<Chord> exerciseChords;
	private SongItem songItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();

        //retrieve chords database
        chordDb = MusicUtils.parseChordDb();

        //setup view
		FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.paging_play_preview, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordText = (TextView) rootView.findViewById(R.id.textChord);
        position = (TextView) rootView.findViewById(R.id.position);
		nextButton = (Button) rootView.findViewById(R.id.previewNextChordButton);
		playButton = (Button) rootView.findViewById(R.id.previewStartSongButton);
		playChordButton = (ImageButton) rootView.findViewById(R.id.playChordButton);

		return rootView;
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
        //setup the first chord
		chordIndex = 0;
		if (exerciseChords.size() > 0)
			setChord();

		//setup onClickListeners
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                ++chordIndex;
                if (chordIndex == exerciseChords.size()) {
                    chordIndex = 0;
                }
                setChord();
			}
		});

		playChordButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				//check if music volume is up
				AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
				if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) < 5) {
					Toast.makeText(getActivity(), "Volume is low", Toast.LENGTH_SHORT).show();
				}
				Midi.getInstance().playChord(exerciseChords.get(chordIndex));
			}
		});

		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean loadOfflinePlayer = false;
				if (Config.useOfflinePlayer) {
					String fileName = "fretx" + songItem.youtube_id.toLowerCase().replace("-", "_");
					int resourceIdentifier = getContext().getResources().getIdentifier(fileName, "raw", getContext().getPackageName());
					if (resourceIdentifier != 0) {
						loadOfflinePlayer = true;
					}
				}
				if (loadOfflinePlayer) {
					PlayOfflinePlayerFragment fragmentYoutubeFragment = new PlayOfflinePlayerFragment();
					fragmentYoutubeFragment.setSong(songItem);
					mActivity.fragNavController.pushFragment(fragmentYoutubeFragment);
				} else {
					PlayYoutubeFragment fragmentYoutubeFragment = new PlayYoutubeFragment();
					fragmentYoutubeFragment.setSong(songItem);
					mActivity.fragNavController.pushFragment(fragmentYoutubeFragment);
				}
			}
		});
	}

	public void setSongData(SongItem item){
		songItem = item;
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

	@SuppressWarnings("unchecked")
	public void setChords(ArrayList<Chord> chords) {
		this.exerciseChords = (ArrayList<Chord>) chords.clone();
	}

	private void setChord() {
		Chord actualChord = exerciseChords.get(chordIndex);

		//update chord title
		chordText.setText(actualChord.toString());
		//update finger position
		fretboardView.setFretboardPositions(actualChord.getFingerPositions());
        //update position
        position.setText(chordIndex + "/" + exerciseChords.size());
		//update led
		Bluetooth.getInstance().setMatrix(actualChord);
        //chord preview
        //todo add midiPlayer
	}
}
