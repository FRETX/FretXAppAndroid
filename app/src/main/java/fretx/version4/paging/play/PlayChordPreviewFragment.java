package fretx.version4.paging.play;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import fretx.version4.Config;
import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.SongItem;
import fretx.version4.utils.Audio;
import fretx.version4.utils.Bluetooth;
import fretx.version4.utils.Midi;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by Kickdrum on 05-Jan-17.
 */

public class PlayChordPreviewFragment extends Fragment implements Audio.AudioListener
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
	private SongItem songItem;
	
	private int chordIndex;
	private final ArrayList<Chord> exerciseChords = new ArrayList<>();
	private final ArrayList<Chord> targetChords = new ArrayList<>();
	private final ArrayList<Chord> majorChords = new ArrayList<>();
	
	public PlayChordPreviewFragment(){
	
		majorChords.add(new Chord("A", "maj"));
		majorChords.add(new Chord("B", "maj"));
		majorChords.add(new Chord("C", "maj"));
		majorChords.add(new Chord("D", "maj"));
		majorChords.add(new Chord("E", "maj"));
		majorChords.add(new Chord("F", "maj"));
		majorChords.add(new Chord("G", "maj"));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "EXERCISE");
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Song Preview");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
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
	
	
	@Override public void onPause(){
		super.onPause();
		Audio.getInstance().stopListening();
		
	}
	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		Audio.getInstance().setAudioDetectorListener(this);
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
				playButton.setClickable(false);
				Audio.getInstance().stopListening();
				//check if music volume is up
				AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
				if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) < 5) {
					Toast.makeText(getActivity(), "Volume is low", Toast.LENGTH_SHORT).show();
				}
				Midi.getInstance().playChord(exerciseChords.get(chordIndex));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						playButton.setClickable(true);
						Audio.getInstance().startListening();
					}
				}, 1500);
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
	    if (exerciseChords.size() > 0 && chordIndex < exerciseChords.size()) {
		    Audio.getInstance().setTargetChords(targetChords);
		    setChord();
		    Audio.getInstance().startListening();
	    }
        int[] config = Midi.getInstance().config();
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
    }

	@SuppressWarnings("unchecked")
	public void setChords(ArrayList<Chord> chords) {
		exerciseChords.clear();
		exerciseChords.addAll(chords);
		setTargetChords(exerciseChords);
		
	}

	private void setChord() {
		if (chordIndex >= exerciseChords.size()) chordIndex = 0;
		Chord actualChord = exerciseChords.get(chordIndex);
		
		Audio.getInstance().setTargetChord(actualChord);
		Audio.getInstance().startListening();
		
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
	
	@Override
	public void onProgress() {
		double progress = Audio.getInstance().getProgress();
		//chord totally played
		if (progress >= 100) {
			++chordIndex;
			setChord();
		}
	}
	
	@Override
	public void onLowVolume() {
		
	}
	
	@Override
	public void onHighVolume() {
		
	}
	
	@Override
	public void onTimeout() {
		
	}

	
	public void setTargetChords(ArrayList<Chord> chords) {
		targetChords.clear();
		HashSet<Chord> hs = new HashSet<>(chords);
		Log.d("KJK hashset chords",hs.toString());
		targetChords.addAll(new HashSet<>(chords));
		Log.d("KJKtargetChords",targetChords.toString());
		for (Chord majorChord : majorChords) {
			final String chordRoot = majorChord.getRoot();
			boolean rootExist = false;
			for (Chord e : chords) {
				if (e.getRoot().equals(chordRoot) ||
						((e.getRoot().equals("A")) && chordRoot.equals("F")) || //temporary heuristic
						((e.getRoot().equals("F")) && chordRoot.equals("A"))
						) {
					rootExist = true;
					break;
				}
			}
			if (!rootExist)
				targetChords.add(majorChord);
		}
		
	}
}
