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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongPunch;
import fretx.version4.utils.Preference;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

public class PlayYoutubeFragment extends Fragment implements PlayerEndDialog.PlayedEndDialogListener {
    private final static String TAG = "KJKP6_YOUTUBE";
    //Youtube
    private static final String API_KEY = "AIzaSyAhxy0JS9M_oaDMW_bJMPyoi9R6oILFjNs";
    private SongItem song;
    private ArrayList<SongPunch> punches;
    private int          preroll  = 0;
    private String       VIDEO_ID = "";
    static  Hashtable    punch_list;
    static  int[]        arrayKeys;
    static  Boolean[]    arrayCallStatus;

    //UI
    private SeekBar timeSeekBar;
    private Button loopStartButton, loopEndButton, loopButton;
    private Button preRollButton0, preRollButton025, preRollButton05, preRollButton1;
	private ArrayList<Button> preRollButtons;
	private Button playPauseButton;
	private FretboardView fretboardCurrent, fretboardNext;
	private TextView timeTotalText, timeElapsedText;
    private YouTubePlayer m_player;

    private boolean startButtonPressed;
    private boolean endButtonPressed;

    static long    lastSysClockTime = 0;
    static long    lastYoutubeElapsedTime = 0;
    static long    m_currentTime = 0;          // Now playing time.

    private long    startPos = 0;               // start point of loop
    private long    endPos = 0;                 // end point of loop

    static boolean mbPlaying = true;           // Flag of now playing.
    private boolean youtubePlayerLoaded = false;
	private boolean looping = false;
	private boolean seeking = false;
	private int seekToTarget = -1;
    static private Handler mCurTimeShowHandler = new Handler();
    private PlayerEndDialog.PlayedEndDialogListener listener = this;

    ///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.getInstance().logSelectEvent("SONG", song.song_title);
        VIDEO_ID = song.youtube_id;
        Bluetooth.getInstance().clearMatrix();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.paging_play_youtube, container, false);

        //get UI
        fretboardCurrent = (FretboardView) rootView.findViewById(R.id.fretboardCurrent);
        fretboardNext = (FretboardView) rootView.findViewById(R.id.fretboardNext);
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

        //set Fretview hand
        if (Preference.getInstance().isLeftHanded()) {
            fretboardCurrent.setScaleX(-1.0f);
            fretboardNext.setScaleX(-1.0f);
        }

        setEventListeners();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initTxt();
        initYoutubePlayer();
    }

    @Override public void onStop(){
        super.onStop();
        Log.d(TAG,"onStop");
            if(m_player != null){
                try {
                    if (m_player.isPlaying()) {
                        m_player.pause();
                    }
                } catch (Exception e){
                    Log.e(TAG,e.toString());
                }
            }
    }

    ///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////

    ////////////////////////////////////////// SETUP ///////////////////////////////////////////////////////////////////////

    public void setSong(SongItem song){
        this.song = song;
    }

    private void setEventListeners() {

	    playPauseButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    if(!youtubePlayerLoaded) return;
			    Button b = (Button) view;
				if(mbPlaying){
					b.setBackground(getResources().getDrawable(R.drawable.ic_playbutton));
					m_player.pause();
					mbPlaying = false;
				} else {
					b.setBackground(getResources().getDrawable(R.drawable.ic_pausebutton));
					m_player.play();
					mbPlaying = true;
				}
		    }
	    });

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
	            toggleStartButton(v);
	            if(!startButtonPressed && endButtonPressed) {
		            toggleEndButton(v);
	            }
            }
        });

        loopEndButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
	            toggleEndButton(v);
	            if(!endButtonPressed && startButtonPressed){
		            toggleStartButton(v);
	            }
            }
        });

	    timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		    @Override
		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			    if(fromUser){
				    seekToTarget = Math.round((float) progress / 100f * (float) m_player.getDurationMillis());
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
				    m_player.seekToMillis(seekToTarget);
				    seekToTarget = -1;
			    }
		    }
	    });


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

	private void toggleStartButton(View v){
		if(!startButtonPressed){
			if(m_currentTime >= endPos) return;
			startPos = m_currentTime;
            loopStartButton.setBackgroundColor(getResources().getColor(R.color.activeButton));
			startButtonPressed = true;
		} else {
			startPos = 0;
			loopStartButton.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
			startButtonPressed = false;
		}
	}

	private void toggleEndButton(View v){
		if(!endButtonPressed){
			if(m_currentTime <= startPos) return;
			endPos = m_currentTime;
			loopEndButton.setBackgroundColor(getResources().getColor(R.color.activeButton));
			endButtonPressed = true;
		} else {
			endPos = m_player.getDurationMillis();
			loopEndButton.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
			endButtonPressed = false;
		}
	}

    //////////////////////////////////////// YOUTUBE ///////////////////////////////////////////////
    private void initYoutubePlayer() {
        final YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.youtube_view, youTubePlayerFragment).commit();

        youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (wasRestored) return;
                m_player = player;

                //set youtube props
                m_player.setFullscreen(false);
                m_player.setShowFullscreenButton(false);
                m_player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                m_player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                //set youtube listeners
                m_player.setPlaybackEventListener( new MyPlaybackEventListener() );
                m_player.setPlayerStateChangeListener( new MyPlayerStateChangeListener() );
                //play
                m_player.loadVideo(VIDEO_ID);
                m_player.play();
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
            mbPlaying = true;
            startTimingLoop();
        }
        @Override public void onPaused() {
            Log.d(TAG, "Paused");
            mbPlaying = false;
        }
        @Override public void onStopped() {mbPlaying = false;}
        @Override public void onSeekTo(int currentTime) {}
        @Override public void onBuffering(boolean b)    {}
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
        @Override public void onLoading() {Log.d(TAG, "YOUTUBE Loading!");}
        @Override public void onLoaded(String s) {
            Log.d(TAG, "YOUTUBE loaded!");
            endPos = m_player.getDurationMillis();
            timeTotalText.setText(String.format("%02d : %02d", TimeUnit.MILLISECONDS.toMinutes(endPos),
                    TimeUnit.MILLISECONDS.toSeconds(endPos) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endPos))));
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

    ////////////////////////////////////// TIMING LOOP /////////////////////////////////////////////////////////////////////
    private void startTimingLoop() {mCurTimeShowHandler.post(playerTimingLoop);}

    private void setCurrentTime() {
        long youtubeDuration    = m_player.getDurationMillis();
        long youtubeElapsedTime = m_player.getCurrentTimeMillis();

	    if(!seeking){
		    timeElapsedText.setText(String.format(Locale.ENGLISH,"%02d : %02d",
				    TimeUnit.MILLISECONDS.toMinutes(youtubeElapsedTime),
				    TimeUnit.MILLISECONDS.toSeconds(youtubeElapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(youtubeElapsedTime))));

		    int progressPercentage = Math.round((float) youtubeElapsedTime / (float) youtubeDuration * 100);
		    timeSeekBar.setProgress(progressPercentage);
	    }

        if( preroll > 0 ) {
            youtubeElapsedTime += preroll;
            youtubeElapsedTime = youtubeElapsedTime > youtubeDuration ? youtubeDuration : youtubeElapsedTime;
        }

        long    sysClockTime       = SystemClock.uptimeMillis();
        boolean repeatedTime       = youtubeElapsedTime == lastYoutubeElapsedTime;
        long    sysClockDelta      = lastSysClockTime == 0 ? 0 : sysClockTime - lastSysClockTime;
        lastYoutubeElapsedTime     = youtubeElapsedTime;

        if ( repeatedTime ) { m_currentTime = youtubeElapsedTime + sysClockDelta;                  }
        else                { lastSysClockTime = sysClockTime; m_currentTime = youtubeElapsedTime; }
    }

    Runnable playerTimingLoop = new Runnable() {
        @Override public void run() {
            try {
                if ( m_player == null      ) return;
                if ( !m_player.isPlaying() ) return;
                setCurrentTime();
                changeText( (int) m_currentTime );
	            if( (startButtonPressed && endButtonPressed) && (m_currentTime < startPos || m_currentTime > endPos) && looping){
		            m_player.seekToMillis((int) startPos);
	            }
                mCurTimeShowHandler.postDelayed(this, 100);
            }
            catch (IllegalStateException e) { mCurTimeShowHandler.removeCallbacks(this); }
        }
    };

    ///////////////////////////////// TEXT FILE PROCESSING /////////////////////////////////////////////////////////////////
    //From the first to number of hashtable keys, Search index that its value is bigger than
    // current time. Then sets the text that was finded in hashtable keys.
    public  void changeText(int currentTime) {
	    if(arrayKeys.length < 1) return;
        for ( int nIndex = 0; nIndex < arrayKeys.length -1; nIndex++ )
        {
            if ( arrayKeys[nIndex] <= currentTime && arrayKeys[nIndex + 1] > currentTime )
            {
                if( arrayCallStatus[nIndex] )
                    return;
                arrayCallStatus[nIndex] = true;
                Bluetooth.getInstance().setMatrix((byte[]) punch_list.get(arrayKeys[nIndex]));
                Util.setDefaultValues(arrayCallStatus);
                arrayCallStatus[nIndex] = true;
	            SongPunch sp = punches.get(nIndex);
	            Chord c = new Chord(sp.root,sp.type);
	            fretboardCurrent.setFretboardPositions(c.getFingerPositions());
	            sp = punches.get(nIndex+1);
	            c = new Chord(sp.root, sp.type);
	            fretboardNext.setFretboardPositions(c.getFingerPositions());
            }
        }

        if ( arrayKeys[arrayKeys.length -1] <= currentTime )
        {
            if( arrayCallStatus[arrayKeys.length -1] )
                return;
            arrayCallStatus[arrayKeys.length -1] = true;
            Bluetooth.getInstance().setMatrix((byte[]) punch_list.get(arrayKeys[arrayKeys.length - 1]));
            Util.setDefaultValues(arrayCallStatus);
            arrayCallStatus[arrayKeys.length -1] = true;
	        SongPunch sp = punches.get(arrayKeys.length - 1);
	        Chord c = new Chord(sp.root, sp.type);
	        fretboardCurrent.setFretboardPositions(c.getFingerPositions());
	        c = new Chord("A","X");
	        fretboardNext.setFretboardPositions(c.getFingerPositions());
        }
    }

    public Hashtable songtxtToHashtable() {
        punch_list = new Hashtable();
        punches = song.punches();
        for (SongPunch sp : punches)
            punch_list.put(sp.timeMs,sp.fingering);
        return punch_list;
    }

    public void initTxt() {
        final long timeStart = System.currentTimeMillis();
        punch_list = songtxtToHashtable();
        arrayKeys = new int[punch_list.size()];
        arrayCallStatus = new Boolean[punch_list.size()];

        int i = 0;
        for ( Enumeration e = punch_list.keys(); e.hasMoreElements(); ) {
            arrayKeys[i] = (int) e.nextElement();
            arrayCallStatus[i] = false;
            i++;
        }
        Arrays.sort(arrayKeys);
        Log.v( TAG, "initTxt" + Long.toString((System.currentTimeMillis()-timeStart)));
    }

    /////////////////////////////////////////// UTILITIES //////////////////////////////////////////
	private void resetPrerollButtons(){
		if(preRollButtons.size() > 0){
			for (Button b:preRollButtons) {
				deactivateButton(b);
			}
		}
	}

	private void deactivateButton(Button b){
		b.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
	}

	private void activateButton(Button b) {
		b.setBackgroundColor(getResources().getColor(R.color.activeButton));
	}

    //////////////////////////////////////////// LISTENER //////////////////////////////////////////
    public void onReplay() {
        initTxt();
        initYoutubePlayer();
    }
    public void onCancel() {
        ((MainActivity)getActivity()).fragNavController.popFragment();
    }
    public void onRandom(SongItem item) {
        final PlayYoutubeFragment youtubeFragment = new PlayYoutubeFragment();
        youtubeFragment.setSong(item);
        ((MainActivity)getActivity()).fragNavController.replaceFragment(youtubeFragment);
    }

}