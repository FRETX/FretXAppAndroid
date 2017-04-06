package fretx.version4.paging.play;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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

import fretx.version4.BluetoothClass;
import fretx.version4.Config;
import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.fretxapi.SongItem;
import fretx.version4.fretxapi.SongPunch;
import rocks.fretx.audioprocessing.Chord;

public class PlayYoutubeFragment extends Fragment {

    private static final int RECOVERY_REQUEST = 1;
    public String            videoUri;
    public int               resourceId;

    private static final String API_KEY = Config.YOUTUBE_API_KEY;

    private SongItem song;
	private ArrayList<SongPunch> punches;

    private MainActivity mActivity;
    private View         rootView;
    private SeekBar      timeSeekBar;
    private Button loopStartButton, loopEndButton, loopButton;
    private Button       preRollButton0, preRollButton025, preRollButton05, preRollButton1;
	private ArrayList<Button> preRollButtons;
	private Button playPauseButton;
	private FretboardView fretboardCurrent, fretboardNext;
	private TextView timeTotalText, timeElapsedText;

    private int          preroll  = 0;
    private String       VIDEO_ID = "";
    private String       SONG_TXT;
    static  Hashtable    punch_list;
    static  int[]        arrayKeys;
    static  Boolean[]    arrayCallStatus;

    private boolean startButtonPressed = false;
    private boolean endButtonPressed = false;

    private YouTubePlayer               m_player = null;

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

    ///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        VIDEO_ID = getArguments().getString("URL");
//        SONG_TXT = getArguments().getString("RAW");
        VIDEO_ID = song.youtube_id;

        inflateView(inflater, container);
        initVars();
        setEventListeners();
        initTxt(SONG_TXT);
//        initLoop();
        buildYoutubePlayer();
        return rootView;
    }

    @Override public void onStop(){
        super.onStop();
        Log.d("PlayFragmentYT","onStop");
            if(m_player != null){
                try {
                    if (m_player.isPlaying()) {
                        m_player.pause();
                    }
                } catch (Exception e){
                    Log.e("YoutubeFragment",e.toString());
                }
            }
    }

    ///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////

    ////////////////////////////////////////// SETUP ///////////////////////////////////////////////////////////////////////

    public void setSong(SongItem song){
        this.song = song;
    }

    private void initVars() {
        mActivity = (MainActivity) getActivity();
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
	            if(startButtonPressed == false && endButtonPressed == true) {
		            toggleEndButton(v);
//		            loopOff(v);
	            }
	            if(startButtonPressed == true && endButtonPressed == true){
//	                loopOn(v);
                }
            }
        });

        loopEndButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
	            toggleEndButton(v);
	            if(endButtonPressed == false && startButtonPressed == true){
		            toggleStartButton(v);
//		            loopOff(v);
	            }
	            if(endButtonPressed == true && startButtonPressed == false){
//		            loopOn(v);
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

//        loopTglOn.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View view) { loopOn(view); }
//        });
//
//        loopTglOff.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View view) { loopOff(view); }
//        });

//        prerollSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                preroll = progress * 10;
//                prerollValue.setText(String.format("%d ms",preroll));
//            }
//            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
//            @Override public void onStopTrackingTouch(SeekBar seekBar)  { }
//        });
    }

	private void toggleStartButton(View v){
		if(startButtonPressed == false){
			if(m_currentTime >= endPos) return;
			startPos = m_currentTime;
//			loopStartButton.setText(String.format("%02d : %02d",
//					TimeUnit.MILLISECONDS.toMinutes(m_currentTime),
//					TimeUnit.MILLISECONDS.toSeconds(m_currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(m_currentTime))
//			));

            loopStartButton.setBackgroundColor(getResources().getColor(R.color.activeButton));
//            loopStartButton.setBackground(getContext().getDrawable(R.drawable.buttona));

			startButtonPressed = true;
		} else {
			startPos = 0;
			//TODO: do these with proper strings.xml values
//			loopStartButton.setText(getString(R.string.loopA));
			loopStartButton.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
			startButtonPressed = false;
		}
	}

	private void toggleEndButton(View v){
		if(endButtonPressed == false){
			if(m_currentTime <= startPos) return;
			endPos = m_currentTime;
			//showMessage("Button Start");
//			loopEndButton.setText(String.format("%02d : %02d",
//					TimeUnit.MILLISECONDS.toMinutes(m_currentTime),
//					TimeUnit.MILLISECONDS.toSeconds(m_currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(m_currentTime))
//			));
			loopEndButton.setBackgroundColor(getResources().getColor(R.color.activeButton));
//            loopEndButton.setBackground(getContext().getDrawable(R.drawable.buttonb));
			endButtonPressed = true;
		} else {
			endPos = m_player.getDurationMillis();
//			loopEndButton.setText(getString(R.string.loopB));
			loopEndButton.setBackgroundColor(getResources().getColor(R.color.inactiveButton));
			endButtonPressed = false;
		}
	}

    private View inflateView(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.paging_play_youtube, container, false);
        return rootView;
    }

    ////////////////////////////////////////// SETUP ///////////////////////////////////////////////////////////////////////

    //////////////////////////////////////// YOUTUBE ///////////////////////////////////////////////////////////////////////

    private void buildYoutubePlayer() {
        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_view, youTubePlayerFragment).commit();

        youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (wasRestored) return;
                m_player = player;
                setYoutubePlayerProps();
                setYoutubePlayerListeners();
                m_player.loadVideo(VIDEO_ID);
                m_player.play();
            }
            @Override public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                String errorMessage = error.toString();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                Log.d("errorMessage:", errorMessage);
            }
        });
    }

    private void setYoutubePlayerProps() {
        m_player.setFullscreen(false);
        m_player.setShowFullscreenButton(false);
        m_player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        m_player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
    }

    private void setYoutubePlayerListeners() {
        m_player.setPlaybackEventListener( new MyPlaybackEventListener() );
        m_player.setPlayerStateChangeListener( new MyPlayerStateChangeListener() );
    }

    //////////////////////////////////////// YOUTUBE ///////////////////////////////////////////////////////////////////////

    /////////////////////////////////// YOUTUBE CALLBACKS //////////////////////////////////////////////////////////////////

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        @Override public void onPlaying() {
            showMessage("Playing");
//            clearLoopFlags();
            mbPlaying = true;
//            getStartEndTime();
            startTimingLoop();
        }

        @Override public void onPaused() {
            showMessage("Paused");
//            clearLoopFlags();
            mbPlaying = false;
            //Util.stopViaData();
        }

        @Override public void onStopped()               { mbPlaying = false; }
        @Override public void onSeekTo(int currentTime) { /*checkLoopFlags(currentTime);*/ }
        @Override public void onBuffering(boolean b)    {}
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
        @Override public void onLoading()                            { showMessage("YOUTUBE Loading!");      }
        @Override public void onLoaded(String s)                     {
            showMessage("YOUTUBE loaded!");
            endPos = m_player.getDurationMillis();
	        timeTotalText.setText(String.format("%02d : %02d",
			        TimeUnit.MILLISECONDS.toMinutes(endPos),
			        TimeUnit.MILLISECONDS.toSeconds(endPos) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endPos))
	        ));
	        youtubePlayerLoaded = true;

        }
        @Override public void onAdStarted()                          { showMessage("YOUTUBE Ad Started");    }
        @Override public void onVideoStarted()                       { showMessage("YOUTUBE VideoStarted!"); }
        @Override public void onVideoEnded()                         { showMessage("YOUTUBE VideoEnded!");   }
        @Override public void onError(YouTubePlayer.ErrorReason err) { showMessage("YOUTUBE Error");         }
    }

    /////////////////////////////////// YOUTUBE CALLBACKS //////////////////////////////////////////////////////////////////

    // TUTORIALS
    private void showTutorial(){
//        new MaterialIntroView.Builder(this)
//                .enableDotAnimation(false)
//                .enableIcon(false)
//                .setFocusGravity(FocusGravity.CENTER)
//                .setFocusType(Focus.NORMAL)
//                .setDelayMillis(300)
//                .enableFadeAnimation(true)
//                .performClick(true)
//                .setInfoText("Turn on your FretX device and tap the FretX logo to connect to it")
//                .setTarget((ImageView) mActivity.findViewById(R.id.bluetoothLogo))
//                .setUsageId("tutorialConnectBluetoothWithLogo") //THIS SHOULD BE UNIQUE ID
//                .show();
    }


    ////////////////////////////////////// TIMING LOOP /////////////////////////////////////////////////////////////////////

    private void startTimingLoop() { mCurTimeShowHandler.post(playerTimingLoop); }

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

        showMessage( "Current Time : " + m_currentTime );
    }

    Runnable playerTimingLoop = new Runnable() {
        @Override public void run() {
            try {
                if ( m_player == null      ) return;
                if ( !m_player.isPlaying() ) return;
                setCurrentTime();
                changeText( (int) m_currentTime );  ///Set the current title of current time.
//                if(mbLoopable){ checkLoop(); }
	            if( (startButtonPressed && endButtonPressed) && (m_currentTime < startPos || m_currentTime > endPos) && looping){
		            m_player.seekToMillis((int) startPos);
	            }
                mCurTimeShowHandler.postDelayed(this, 100);
            }
            catch (IllegalStateException e) { mCurTimeShowHandler.removeCallbacks(this); }
        }
    };

    ////////////////////////////////////// TIMING LOOP /////////////////////////////////////////////////////////////////////

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
//                BluetoothClass.sendToFretX(Util.str2array((String) punch_list.get(arrayKeys[nIndex])));
                BluetoothClass.sendToFretX(punch_list.get(arrayKeys[nIndex]));
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
//            BluetoothClass.sendToFretX(Util.str2array((String) punch_list.get(arrayKeys[arrayKeys.length - 1])));
            BluetoothClass.sendToFretX(punch_list.get(arrayKeys[arrayKeys.length - 1]));
            Util.setDefaultValues(arrayCallStatus);
            arrayCallStatus[arrayKeys.length -1] = true;
	        SongPunch sp = punches.get(arrayKeys.length - 1);
	        Chord c = new Chord(sp.root, sp.type);
	        fretboardCurrent.setFretboardPositions(c.getFingerPositions());
	        c = new Chord("A","X");
	        fretboardNext.setFretboardPositions(c.getFingerPositions());
        }
    }

//    public Hashtable songtxtToHashtable(String data) {
      public Hashtable songtxtToHashtable() {
          punch_list = new Hashtable();
	      punches = song.punches();

        for (SongPunch sp : punches){
            //Skipping the conversion of this part for now, in favor of using byte[] arrays directly
            //We can revert back to String if need be
//            if( punch_list.containsKey( punch_time ) ) {               // not sure why we need to handle two chords on the same time ???
//                byte[] bluetoothArrayTmp = (byte[]) punch_list.get(punch_time);
//                punch_list.put(punch_time, strTemp + ":" + strText);
//                continue;
//            }
            punch_list.put(sp.timeMs,sp.fingering);
        }
        return punch_list;
    }

    public void initTxt(String data) {
        long timeStart = System.currentTimeMillis();
        punch_list = songtxtToHashtable();
        //save the key array of hashtable to int array.
        arrayKeys = new int[punch_list.size()];
        arrayCallStatus = new Boolean[punch_list.size()];

        int i = 0;
        for ( Enumeration e = punch_list.keys(); e.hasMoreElements(); ) {
            arrayKeys[i] = (int) e.nextElement();
            arrayCallStatus[i] = false;
            i++;
        }
        Arrays.sort(arrayKeys);
        Log.d( "initTxt" , Long.toString((System.currentTimeMillis()-timeStart)));
    }

    ///////////////////////////////// TEXT FILE PROCESSING /////////////////////////////////////////////////////////////////


	//UTILITY

	private void resetPrerollButtons(){
		if(preRollButtons.size() > 0){
			for (Button b:preRollButtons
			     ) {
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

    private static void showMessage(String message) {
        Log.d("+++", message);
    }
}