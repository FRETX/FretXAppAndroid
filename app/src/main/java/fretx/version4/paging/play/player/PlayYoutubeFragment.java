package fretx.version4.paging.play.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fretx.version4.view.FretboardView;
import fretx.version4.R;
import fretx.version4.fragment.ChordTimeline;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongPunch;
import fretx.version4.utils.Preference;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

public class PlayYoutubeFragment extends Fragment implements PlayerEndDialog.PlayedEndDialogListener {
    private final static String TAG = "KJKP6_PLAY_YOUTUBE";
    //Youtube
    private YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
    private YouTubePlayer youTubePlayer;
    private static final String API_KEY = "AIzaSyAhxy0JS9M_oaDMW_bJMPyoi9R6oILFjNs";
    private SongItem song;
    private ArrayList<SongPunch> punches;
    private int punchesIndex;
    private long youtubeDuration;
    private boolean playing = true;
    private boolean youtubePlayerLoaded;
    private PlayerEndDialog.PlayedEndDialogListener listener = this;

    //UI
    private SeekBar timeSeekBar;
    private Button loopStartButton, loopEndButton, loopButton;
    private Button preRollButton0, preRollButton025, preRollButton05, preRollButton1;
	private ArrayList<Button> preRollButtons;
	private Button playPauseButton;
	private TextView timeTotalText, timeElapsedText, songTitleText;
    private FretboardView fretboardCurrent;
    private ChordTimeline timelineFragment;

    //time sync
    private int preroll = 0;
    private long lastSysClockTime = 0;
    private long lastYoutubeElapsedTime = -1;
    private long currentTime = 0;
    private final Handler mCurTimeShowHandler = new Handler();
    private Chord currentChord;

    //Looper
    private boolean startButtonPressed;
    private boolean endButtonPressed;
    private boolean looping;
    private long startLoopTime = 0;
    private long endLoopTime = 0;

	private boolean seeking;
	private int seekToTarget = -1;


    ///////////////////////////////////// FRAGMENT CREATOR /////////////////////////////////////////
    static public PlayYoutubeFragment newInstance(SongItem song) {
        final PlayYoutubeFragment fragment = new PlayYoutubeFragment();
        fragment.setSong(song);
        return fragment;
    }

    public void setSong(SongItem song){
        this.song = song;
    }

    ///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.getInstance().logSelectEvent("SONG", song.song_title);
        Bluetooth.getInstance().clearMatrix();
        punches = song.punches();
        timelineFragment = ChordTimeline.newInstance(punches);
        punchesIndex = 0;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.paging_play_youtube, container, false);

        //get UI
        fretboardCurrent = (FretboardView) rootView.findViewById(R.id.fretboardCurrent);
        loopStartButton = (Button) rootView.findViewById(R.id.buttonA);
        loopEndButton = (Button) rootView.findViewById(R.id.buttonB);
        loopButton = (Button) rootView.findViewById(R.id.buttonLoop);
        preRollButton0 = (Button) rootView.findViewById(R.id.buttonEarly0);
        preRollButton025 = (Button) rootView.findViewById(R.id.buttonEarly025);
        preRollButton05 = (Button) rootView.findViewById(R.id.buttonEarly05);
        preRollButton1 = (Button) rootView.findViewById(R.id.buttonEarly1);
        preRollButtons = new ArrayList<>();
        preRollButtons.add(preRollButton0);
        preRollButtons.add(preRollButton025);
        preRollButtons.add(preRollButton05);
        preRollButtons.add(preRollButton1);
        playPauseButton = (Button) rootView.findViewById(R.id.playPauseButton);
        timeSeekBar = (SeekBar) rootView.findViewById(R.id.timeSeekbar);
        timeElapsedText = (TextView) rootView.findViewById(R.id.elapsedTimeText);
        timeTotalText = (TextView) rootView.findViewById(R.id.totalTimeText);
        songTitleText = (TextView) rootView.findViewById(R.id.songTitleText);


        //load chord timeline fragment
        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.timeline_container, timelineFragment).commit();

        //load youtube player fragment
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.youtube_view, youTubePlayerFragment).commit();

        //set Fretview hand
        if (Preference.getInstance().isLeftHanded()) {
            fretboardCurrent.setScaleX(-1.0f);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songTitleText.setText(song.artist + " - " + song.song_title);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!youtubePlayerLoaded)
                    return;
                final Button b = (Button) view;
                if(playing){
                    b.setBackground(getResources().getDrawable(R.drawable.ic_playbutton));
                    youTubePlayer.pause();
                    stopUpdateLoop();
                    playing = false;
                } else {
                    b.setBackground(getResources().getDrawable(R.drawable.ic_pausebutton));
                    youTubePlayer.play();
                    startUpdateLoop();
                    playing = true;
                }
            }
        });

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    seekToTarget = Math.round((float) progress / 100f * (float) youTubePlayer.getDurationMillis());
                    timeElapsedText.setText(String.format(Locale.ENGLISH,"%02d : %02d",
                            TimeUnit.MILLISECONDS.toMinutes(seekToTarget),
                            TimeUnit.MILLISECONDS.toSeconds(seekToTarget) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seekToTarget))
                    ));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seeking = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeking = false;
                if(seekToTarget > 0){
                    stopUpdateLoop();
                    punchesIndex = 0;
                    timelineFragment.init(seekToTarget);
                    youTubePlayer.seekToMillis(seekToTarget);
                    seekToTarget = -1;
                }
            }
        });

        //loop buttons
        loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button b = (Button) view;
                if(looping){
                    b.setBackground(getResources().getDrawable(R.drawable.ic_loop_inactive));
                    looping = false;
                } else {
                    b.setBackground(getResources().getDrawable(R.drawable.ic_loop_active));
                    looping = true;
                }
            }
        });

        loopStartButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleStartButton();
                if(!startButtonPressed && endButtonPressed) {
                    toggleEndButton();
                }
            }
        });

        loopEndButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleEndButton();
                if(!endButtonPressed && startButtonPressed){
                    toggleStartButton();
                }
            }
        });

        //pre roll buttons
        preRollButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPrerollButtons();
                activateButton((Button) view);
                preroll = 0;
            }
        });
        preRollButton025.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPrerollButtons();
                activateButton((Button) view);
                preroll = 250;
            }
        });
        preRollButton05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPrerollButtons();
                activateButton((Button) view);
                preroll = 500;
            }
        });
        preRollButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPrerollButtons();
                activateButton((Button) view);
                preroll = 1000;
            }
        });
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        punchesIndex = 0;

        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.youtube_view, youTubePlayerFragment).commit();

        timelineFragment.init(0);
        initYoutubePlayer();
    }

    @Override public void onPause(){
        super.onPause();
        Log.d(TAG,"onPause");
            if(youTubePlayer != null){
                try {
                    if (youTubePlayer.isPlaying()) {
                        stopUpdateLoop();
                        youTubePlayer.pause();
                    }
                } catch (Exception e){
                    Log.e(TAG,e.toString());
                }
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (youTubePlayer != null)
            youTubePlayer.release();
    }

    //////////////////////////////////////// YOUTUBE ///////////////////////////////////////////////
    private void initYoutubePlayer() {
        youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (wasRestored) return;
                youTubePlayer = player;
                //set youtube props
                youTubePlayer.setFullscreen(false);
                youTubePlayer.setShowFullscreenButton(false);
                youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                //set youtube listeners
                youTubePlayer.setPlaybackEventListener( new MyPlaybackEventListener() );
                youTubePlayer.setPlayerStateChangeListener( new MyPlayerStateChangeListener() );
                //play
                youTubePlayer.loadVideo(song.youtube_id);
                youTubePlayer.play();
            }
            @Override public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                final String errorMessage = error.toString();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, errorMessage);
            }
        });
    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {
        @Override public void onPlaying() {
            Log.v(TAG, "Playing");
            playing = true;
            startUpdateLoop();
        }
        @Override public void onPaused() {
            Log.d(TAG, "Paused");
            playing = false;
            stopUpdateLoop();
        }
        @Override public void onStopped() {
            playing = false;
            stopUpdateLoop();
        }
        @Override public void onSeekTo(int currentTime) {}
        @Override public void onBuffering(boolean b)    {}
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
        @Override public void onLoading() {Log.d(TAG, "YOUTUBE Loading!");}
        @Override public void onLoaded(String s) {
            Log.d(TAG, "YOUTUBE loaded!");
            youtubeDuration = youTubePlayer.getDurationMillis();
            endLoopTime = youtubeDuration;
            timeTotalText.setText(String.format(Locale.US, "%02d : %02d", TimeUnit.MILLISECONDS.toMinutes(endLoopTime),
                    TimeUnit.MILLISECONDS.toSeconds(endLoopTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endLoopTime))));
            youtubePlayerLoaded = true;
        }
        @Override public void onAdStarted() {Log.d(TAG, "YOUTUBE Ad Started");}
        @Override public void onVideoStarted() {Log.d(TAG, "YOUTUBE VideoStarted!");}
        @Override public void onVideoEnded() {
            Log.d(TAG, "YOUTUBE VideoEnded!");
            PlayerEndDialog dialog = PlayerEndDialog.newInstance(listener, song.song_title);
            dialog.show(getFragmentManager(), null);
        }
        @Override public void onError(YouTubePlayer.ErrorReason err) { Log.d(TAG, "YOUTUBE Error");}
    }

    ////////////////////////////////////// TIMING LOOP /////////////////////////////////////////////
    private void startUpdateLoop() {mCurTimeShowHandler.post(playerTimingLoop);}

    private void stopUpdateLoop() {mCurTimeShowHandler.removeCallbacksAndMessages(null);}

    private void setCurrentTime() {
        long youtubeElapsedTime = youTubePlayer.getCurrentTimeMillis();

        //update seeking bar
	    if(!seeking){
		    timeElapsedText.setText(String.format(Locale.ENGLISH,"%02d : %02d",
				    TimeUnit.MILLISECONDS.toMinutes(youtubeElapsedTime),
				    TimeUnit.MILLISECONDS.toSeconds(youtubeElapsedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(youtubeElapsedTime))));
		    timeSeekBar.setProgress(Math.round((float) youtubeElapsedTime / (float) youtubeDuration * 100));
	    }

	    //compute preroll time
        youtubeElapsedTime += preroll;
        youtubeElapsedTime = youtubeElapsedTime > youtubeDuration ? youtubeDuration : youtubeElapsedTime;

        //handle youtube elapsed time being the same
        final long sysClockTime = SystemClock.uptimeMillis();
        if (youtubeElapsedTime == lastYoutubeElapsedTime) {
            final long sysClockDelta = lastSysClockTime == 0 ? 0 : sysClockTime - lastSysClockTime;
            currentTime = youtubeElapsedTime + sysClockDelta;
        } else {
            lastYoutubeElapsedTime = youtubeElapsedTime;
            lastSysClockTime = sysClockTime;
            currentTime = youtubeElapsedTime;
        }

        //update current chord
        boolean changed = false;
        while (punchesIndex < punches.size() && punches.get(punchesIndex).timeMs < currentTime) {
            punchesIndex++;
            changed = true;
        }
        if (punchesIndex == 0) {
            currentChord = null;
        } else {
            final SongPunch punch = punches.get(punchesIndex - 1);
            currentChord = new Chord(punch.root, punch.type);
        }

        //update the chord timeline
        timelineFragment.update(currentTime);

        if (changed && currentChord != null && !currentChord.toString().isEmpty()) {
            Bluetooth.getInstance().setMatrix(currentChord);
            fretboardCurrent.setFretboardPositions(currentChord.getFingerPositions());
        }
    }

    private final Runnable playerTimingLoop = new Runnable() {
        @Override public void run() {
            try {
                if (youTubePlayer == null || !youTubePlayer.isPlaying())
                    return;

                setCurrentTime();

	            if(startButtonPressed && endButtonPressed && looping && (currentTime < startLoopTime || currentTime > endLoopTime) ){
		            youTubePlayer.seekToMillis((int) startLoopTime);
	            }
                mCurTimeShowHandler.postDelayed(this, 50);
            }
            catch (IllegalStateException e) {
                stopUpdateLoop();
            }
        }
    };

    ////////////////////////////////////////////// LOOP ////////////////////////////////////////////
    private void toggleStartButton(){
        if(!startButtonPressed){
            if(currentTime >= endLoopTime) return;
            startLoopTime = currentTime;
            activateButton(loopStartButton);
            startButtonPressed = true;
        } else {
            startLoopTime = 0;
            deactivateButton(loopStartButton);
            startButtonPressed = false;
        }
    }

    private void toggleEndButton(){
        if(!endButtonPressed){
            if(currentTime <= startLoopTime) return;
            endLoopTime = currentTime;
            activateButton(loopEndButton);
            endButtonPressed = true;
        } else {
            endLoopTime = youTubePlayer.getDurationMillis();
            deactivateButton(loopEndButton);
            endButtonPressed = false;
        }
    }

    //////////////////////////////////////////// PREROLL ///////////////////////////////////////////
	private void resetPrerollButtons(){
        for (Button b: preRollButtons) {
            deactivateButton(b);
		}
	}

	private void deactivateButton(Button b){
		b.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
	}

	private void activateButton(Button b) {
		b.setBackgroundColor(getResources().getColor(R.color.activeButton));
	}

    ///////////////////////////////////////// DIALOG LISTENER //////////////////////////////////////
    @Override
    public void onReplay() {
        punchesIndex = 0;
        initYoutubePlayer();
        timelineFragment.init(0);
    }

    @Override
    public void onCancel() {
        ((MainActivity)getActivity()).fragNavController.popFragment();
    }

    @Override
    public void onRandom(SongItem item) {
        final PlayYoutubeFragment youtubeFragment = new PlayYoutubeFragment();
        youtubeFragment.setSong(item);
        ((MainActivity)getActivity()).fragNavController.replaceFragment(youtubeFragment);
    }
}