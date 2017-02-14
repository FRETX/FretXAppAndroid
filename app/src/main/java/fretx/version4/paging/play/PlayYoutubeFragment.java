package fretx.version4.paging.play;

import android.app.ProgressDialog;
import android.os.AsyncTask;
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

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
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
import rocks.fretx.audioprocessing.FretboardPosition;

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
				    long youtubeDuration = m_player.getDurationMillis();
				    float progressPercentage = (float) progress / 100f;
				    seekToTarget = Math.round((float) youtubeDuration * progressPercentage);
				    timeElapsedText.setText(String.format("%02d : %02d",
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
        rootView = inflater.inflate(R.layout.play_fragment_youtube_fragment, container, false);
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
		    timeElapsedText.setText(String.format("%02d : %02d",
				    TimeUnit.MILLISECONDS.toMinutes(youtubeElapsedTime),
				    TimeUnit.MILLISECONDS.toSeconds(youtubeElapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(youtubeElapsedTime))
		    ));
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


    //////////////////////////////////////// LOOPING ///////////////////////////////////////////////////////////////////////


    ///Set startPos and endPos from TextView of tvStartTime and tvEndTime
    ///Convert String data of TextView to Integer data.
//    void getStartEndTime() {
//        if(loopStartTime.getText().toString().length() != 0)
//            startPos = Integer.parseInt(loopStartTime.getText().toString());
//        else
//            startPos = 0;
//        if(loopEndTime.getText().toString().length() != 0)
//            endPos = Integer.parseInt(loopEndTime.getText().toString());
//        else
//            endPos = 0;
//    }

//    public void initLoop(){
//        loopStartTime.setText("0");
//        loopEndTime.setText("0");
//        setLoopFb(false);
//    }

//    private void setLoopFb(Boolean state) {
//        loopTglOff.setVisibility ( state ? View.INVISIBLE : View.VISIBLE );
//        loopTglOn.setVisibility  ( state ? View.VISIBLE : View.INVISIBLE );
//    }

//    private void checkLoopFlags(int currentTime) {
//        if (!mbLoopable) return;
//        if (currentTime < startPos) { bStartCheckFlag = false; return; }
//        if (currentTime > endPos)   { bEndCheckFlag   = false; return; }
//        bStartCheckFlag = true;   // current time is in the duration of loop.
//        bEndCheckFlag = false;
//    }
//
//    private void clearLoopFlags() {
//        bStartCheckFlag = false;        //Flag that current time is passed start time.
//        bEndCheckFlag   = false;        //Flag that current time is passed end time.
//    }

//    private void checkLoop() {
//        if(startPos >= endPos) { mbLoopable = false; setLoopFb(false); return; }
//
//        if((m_currentTime + 500 < startPos) && (!bStartCheckFlag)){     //if currentTime is smaller than startPos then set bStartCheckFlag true
//            m_player.seekToMillis( (int) startPos );                    //and set endChekFlag to false. Set current pos to startPos. and loop video
//            bStartCheckFlag = true;
//            bEndCheckFlag = false;
//        }
//
//        if((m_currentTime > endPos) && (!bEndCheckFlag)){           //if currentTime is bigger than startPos then set bEndCheckFlag true
//            bEndCheckFlag = false;                                  //and set startChekFlag to false. Set current pos to startPos. and loop video.
//            bStartCheckFlag = true;
//            showMessage("Big Case why : begin");
//            m_player.seekToMillis( (int) startPos );
//            showMessage("Start Pos = " + startPos);
//            showMessage("Current Time = " + m_currentTime);
//            showMessage("Big Case why : end");
//        }
//    }

//    private void loopOff(View view) {
//        setLoopFb(true);
//
//        if (mbLoopable) {
////            loopStartTime.setTextColor(Color.parseColor("#000000"));
////            loopEndTime.setTextColor(Color.parseColor("#000000"));
//            mbLoopable = false;
//        }
//
//        else {
//            ///check current time is in duration of startPosition and endPosition.
////            String strStartPos = loopStartTime.getText().toString();
////            String strEndPos = loopEndTime.getText().toString();
//            int nTmpStartPos, nTmpEndPos;
//
////            if (strStartPos.length() != 0) { nTmpStartPos = Integer.parseInt(strStartPos); }
////            else                           { nTmpStartPos = 0; loopStartTime.setText("0");   }
////
////            if (strEndPos.length() != 0)   { nTmpEndPos = Integer.parseInt(strEndPos);     }
////            else                           { nTmpEndPos = 0; loopEndTime.setText("0");       }
//
//            ///if start position is bigger than end position then can not loop.
//
////            if (nTmpStartPos >= nTmpEndPos) {
//	        if (startPos >= endPos) {
////                loopStartTime.setTextColor(Color.parseColor("#000000"));
////                loopEndTime.setTextColor(Color.parseColor("#000000"));
//                mbLoopable = false;
//                Toast.makeText(mActivity, "Start time is bigger than End time.", Toast.LENGTH_LONG).show();
//                //tgSwitch.setChecked(false);
////                setLoopFb(false);
//            }
//
//            else {
////                loopStartTime.setTextColor(Color.parseColor("#FF0000"));
////                loopEndTime.setTextColor(Color.parseColor("#0000FF"));
//                getStartEndTime();
//                if ((m_currentTime < startPos) || (m_currentTime > endPos)) {
//                    m_currentTime = startPos;
//                    m_player.seekToMillis((int)startPos);
//                }
//                bStartCheckFlag = false;
//                bEndCheckFlag = false;
//                mbLoopable = true;
//            }
//        }
//    }

//    private void loopOn(View view) {
//        setLoopFb(false);
//
//        if (mbLoopable) {
////            loopStartTime.setTextColor(Color.parseColor("#000000"));
////            loopEndTime.setTextColor(Color.parseColor("#000000"));
//            mbLoopable = false;
//        } else {
//            ///check current time is in duration of startPosition and endPosition.
//            String strStartPos = loopStartTime.getText().toString();
//            String strEndPos = loopEndTime.getText().toString();
//            int nTmpStartPos, nTmpEndPos;
//            if (strStartPos.length() != 0)
//                nTmpStartPos = Integer.parseInt(strStartPos);
//            else {
//                nTmpStartPos = 0;
//                loopStartTime.setText("0");
//            }
//            if (strEndPos.length() != 0)
//                nTmpEndPos = Integer.parseInt(strEndPos);
//            else {
//                nTmpEndPos = 0;
//                loopEndTime.setText("0");
//            }
//            ///if start position is bigger than end position then can not loop.
//            if (nTmpStartPos >= nTmpEndPos) {
//                loopStartTime.setTextColor(Color.parseColor("#000000"));
//                loopEndTime.setTextColor(Color.parseColor("#000000"));
//                mbLoopable = false;
//                Toast.makeText(mActivity, "Start time is bigger than End time.", Toast.LENGTH_LONG).show();
//                //tgSwitch.setChecked(false);
//                setLoopFb(false);
//            } else {
//                loopStartTime.setTextColor(Color.parseColor("#FF0000"));
//                loopEndTime.setTextColor(Color.parseColor("#0000FF"));
//                getStartEndTime();
//                if ((m_currentTime < startPos) || (m_currentTime > endPos)) {
//                    m_currentTime = startPos;
//                    m_player.seekToMillis((int)startPos);
//                }
//                bStartCheckFlag = false;
//                bEndCheckFlag = false;
//                mbLoopable = true;
//            }
//        }
//    }

            /*tgSwitch = (ToggleButton)rootView.findViewById(R.id.tgSwitch);   ///ToggleButton that sets loop.
        tgSwitch.setChecked(false);
        tgSwitch.setOnClickListener(new View.OnClickListener() {   /////Set loopable flag.
            @Override
            public void onClick(View v) {
                if (mbLoopable) {
                    tvStartTime.setTextColor(Color.parseColor("#000000"));
                    tvEndTime.setTextColor(Color.parseColor("#000000"));
                    mbLoopable = false;
                } else {
                    ///check current time is in duration of startPosition and endPosition.
                    String strStartPos = tvStartTime.getText().toString();
                    String strEndPos = tvEndTime.getText().toString();
                    int nTmpStartPos, nTmpEndPos;
                    if (strStartPos.length() != 0)
                        nTmpStartPos = Integer.parseInt(strStartPos);
                    else {
                        nTmpStartPos = 0;
                        tvStartTime.setText("0");
                    }
                    if (strEndPos.length() != 0)
                        nTmpEndPos = Integer.parseInt(strEndPos);
                    else {
                        nTmpEndPos = 0;
                        tvEndTime.setText("0");
                    }
                    ///if start position is bigger than end position then can not loop.
                    if (nTmpStartPos >= nTmpEndPos) {
                        tvStartTime.setTextColor(Color.parseColor("#000000"));
                        tvEndTime.setTextColor(Color.parseColor("#000000"));
                        mbLoopable = false;
                        Toast.makeText(mActivity, "Start time is bigger than End time.", Toast.LENGTH_LONG).show();
                        tgSwitch.setChecked(false);
                    } else {
                        tvStartTime.setTextColor(Color.parseColor("#FF0000"));
                        tvEndTime.setTextColor(Color.parseColor("#0000FF"));
                        getStartEndTime();
                        if ((m_currentTime < startPos) || (m_currentTime > endPos)) {
                            m_currentTime = startPos;
                            m_player.seekToMillis(startPos);
                        }
                        bStartCheckFlag = false;
                        bEndCheckFlag = false;
                        mbLoopable = true;
                    }
                }
            }
        });*/

    //////////////////////////////////////// LOOPING ///////////////////////////////////////////////////////////////////////


    private static void showMessage(String message) {
        Log.d("+++", message);
    }

    private class GetSongChordTxtFile extends AsyncTask<String, Void, Void> {
        // A dialog to let the user know we are retrieving the files
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mActivity,
                    mActivity.getString(R.string.refreshing),
                    mActivity.getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... inputs) {
            String accessFolder = Util.checkS3Access(mActivity);

            // Queries files in the bucket from S3.
            File chordFile = new File(mActivity.getFilesDir().toString()+ "/" + inputs[0]);
            if(!chordFile.isFile()) {
                TransferObserver observer = Util.downloadFile(mActivity, accessFolder, inputs[0]);
                observer.setTransferListener(new DownloadListener());
                while (true) {
                    if (TransferState.COMPLETED.equals(observer.getState())
                            || TransferState.FAILED.equals(observer.getState())) {
                        break;
                    }
                }
                initTxt(SONG_TXT);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    /*
     * A TransferListener class that can listen to a download task and be
     * notified when the status changes.
     */
    public class DownloadListener implements TransferListener {
        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("MainActivity", "onError: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("MainActivity", String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d("MainActivity", "onStateChanged: " + id + ", " + state);

        }
    }
}