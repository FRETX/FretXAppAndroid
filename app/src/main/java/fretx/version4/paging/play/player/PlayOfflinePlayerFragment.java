package fretx.version4.paging.play.player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import fretx.version4.R;
import fretx.version4.utils.Util;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongPunch;
import fretx.version4.utils.bluetooth.Bluetooth;

public class PlayOfflinePlayerFragment extends Fragment {
	private SongItem song;
	private MainActivity context;
	private View rootView;
	private SeekBar prerollSlider;
	private TextView prerollValue;
	private Button loopStartBtn;
	private Button loopEndBtn;
	private int preroll = 0;
	private String VIDEO_ID = "";
	private String SONG_TXT;
	static Hashtable punch_list;
	static int[] arrayKeys;
	static Boolean[] arrayCallStatus;
	private boolean startButtonPressed = false;
	private boolean endButtonPressed = false;
	private EasyVideoPlayer m_player = null;
	static long m_currentTime = 0;
	private long startPos = 0;
	private long endPos = 0;
	static private Handler mCurTimeShowHandler = new Handler();
    static boolean mbPlaying = true;

	///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		VIDEO_ID = song.youtube_id;

		rootView = inflater.inflate(R.layout.paging_play_offline_player, container, false);
		initVars();
		setEventListeners();
		initTxt(SONG_TXT);
		m_player = (EasyVideoPlayer) rootView.findViewById(R.id.videoPlayer);
		m_player.setCallback(new MyPlaybackEventListener());

		final String fileName = "fretx" + VIDEO_ID.toLowerCase().replace("-", "_");
		m_player.setAutoPlay(true);
		m_player.setSource(Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + fileName));

		prerollValue.setText("0 ms");
		final TextView tv10 = (TextView) rootView.findViewById(R.id.textView10);
		tv10.setText("Early Lights");

		return rootView;
	}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
	public void onStop() {
		super.onStop();
		Log.d("PlayFragmentYT", "onStop");
		if (m_player != null) {
			try {
				if (m_player.isPlaying()) {
					m_player.pause();
				}
			} catch (Exception e) {
				Log.e("YoutubeFragment", e.toString());
			}
		}
	}

	///////////////////////////////////// LIFECYCLE EVENTS /////////////////////////////////////////////////////////////////

	////////////////////////////////////////// SETUP ///////////////////////////////////////////////////////////////////////

	public void setSong(SongItem song) {
		this.song = song;
	}

	private void initVars() {
		context = (MainActivity) getActivity();
		prerollSlider = (SeekBar) rootView.findViewById(R.id.prerollSlider);
		prerollValue = (TextView) rootView.findViewById(R.id.prerollValView);
		loopStartBtn = (Button) rootView.findViewById(R.id.btnStartLoop);
		loopEndBtn = (Button) rootView.findViewById(R.id.btnEndLoop);
	}

	private void setEventListeners() {

		loopStartBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleStartButton(v);
				if (startButtonPressed == false && endButtonPressed == true) {
					toggleEndButton(v);
				}
				if (startButtonPressed == true && endButtonPressed == true) {
				}
			}
		});

		loopEndBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleEndButton(v);
				if (endButtonPressed == false && startButtonPressed == true) {
					toggleStartButton(v);
				}
				if (endButtonPressed == true && startButtonPressed == false) {
				}
			}
		});

		prerollSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				preroll = progress * 10;
				prerollValue.setText(String.format("%d ms", preroll));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private void toggleStartButton(View v) {
		if (startButtonPressed == false) {
			if (m_currentTime >= endPos) return;
			startPos = m_currentTime;
			loopStartBtn.setText(String.format("%02d : %02d",
					TimeUnit.MILLISECONDS.toMinutes(m_currentTime),
					TimeUnit.MILLISECONDS.toSeconds(m_currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(m_currentTime))
			));
			loopStartBtn.setBackgroundColor(getResources().getColor(R.color.primaryDark));
			startButtonPressed = true;
		} else {
			startPos = 0;
			//TODO: do these with proper strings.xml values
			loopStartBtn.setText(getString(R.string.loopA));
			loopStartBtn.setBackgroundColor(getResources().getColor(R.color.secondaryText));
			startButtonPressed = false;
		}
	}

	private void toggleEndButton(View v) {
		if (endButtonPressed == false) {
			if (m_currentTime <= startPos) return;
			endPos = m_currentTime;
			//showMessage("Button Start");
			loopEndBtn.setText(String.format("%02d : %02d",
					TimeUnit.MILLISECONDS.toMinutes(m_currentTime),
					TimeUnit.MILLISECONDS.toSeconds(m_currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(m_currentTime))
			));
			loopEndBtn.setBackgroundColor(getResources().getColor(R.color.primaryDark));
			endButtonPressed = true;
		} else {
			endPos = m_player.getDuration();
			loopEndBtn.setText(getString(R.string.loopB));
			loopEndBtn.setBackgroundColor(getResources().getColor(R.color.secondaryText));
			endButtonPressed = false;
		}
	}

	////////////////////////////////////////// SETUP ///////////////////////////////////////////////////////////////////////

//	//////////////////////////////////////// YOUTUBE ///////////////////////////////////////////////////////////////////////
//
//	private void buildYoutubePlayer() {
//		YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
//		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//		transaction.add(R.id.youtube_view, youTubePlayerFragment).commit();
//
//		youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
//			@Override
//			public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
//				if (wasRestored) return;
//				m_player = player;
//				setYoutubePlayerProps();
//				setYoutubePlayerListeners();
//				m_player.loadVideo(VIDEO_ID);
//				m_player.play();
//			}
//
//			@Override
//			public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
//				String errorMessage = error.toString();
//				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
//				Log.d("errorMessage:", errorMessage);
//			}
//		});
//	}
//
//	private void setYoutubePlayerProps() {
//		m_player.setFullscreen(false);
//		m_player.setShowFullscreenButton(false);
//		m_player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
//		m_player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
//	}
//
//	private void setYoutubePlayerListeners() {
//		m_player.setPlaybackEventListener(new MyPlaybackEventListener());
//		m_player.setPlayerStateChangeListener(new MyPlayerStateChangeListener());
//	}

	//////////////////////////////////////// YOUTUBE ///////////////////////////////////////////////////////////////////////

	/////////////////////////////////// YOUTUBE CALLBACKS //////////////////////////////////////////////////////////////////

	private final class MyPlaybackEventListener implements EasyVideoCallback {

		@Override
		public void onPreparing(EasyVideoPlayer player) {
			// TODO handle if needed
		}

		@Override
		public void onPrepared(EasyVideoPlayer player) {
			// TODO handle
			showMessage("YOUTUBE loaded!");
			endPos = m_player.getDuration();
			Log.d("duration", Long.toString(endPos));
		}

		@Override
		public void onBuffering(int percent) {
			// TODO handle if needed
		}

		@Override
		public void onError(EasyVideoPlayer player, Exception e) {
			// TODO handle
		}

		@Override
		public void onCompletion(EasyVideoPlayer player) {
			// TODO handle if needed
		}

		@Override
		public void onRetry(EasyVideoPlayer player, Uri source) {
			// TODO handle if used
		}

		@Override
		public void onSubmit(EasyVideoPlayer player, Uri source) {
			// TODO handle if used
		}

		@Override
		public void onStarted(EasyVideoPlayer player) {
			// TODO handle if needed
			showMessage("Playing");
//            clearLoopFlags();
			mbPlaying = true;
//            getStartEndTime();
			startTimingLoop();
		}

		@Override
		public void onPaused(EasyVideoPlayer player) {
			// TODO handle if needed
			showMessage("Paused");
//            clearLoopFlags();
			mbPlaying = false;
			//Util.stopViaData();
		}
	}

	/////////////////////////////////// YOUTUBE CALLBACKS //////////////////////////////////////////////////////////////////

	// TUTORIALS
	private void showTutorial() {
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

	private void startTimingLoop() {
		mCurTimeShowHandler.post(playerTimingLoop);
	}

	private void setCurrentTime() {
//		long youtubeDuration = m_player.getDuration();
//		long youtubeElapsedTime = m_player.getCurrentPosition();
//
//		if (preroll > 0) {
//			youtubeElapsedTime += preroll;
//			youtubeElapsedTime = youtubeElapsedTime > youtubeDuration ? youtubeDuration : youtubeElapsedTime;
//		}
//
//		long sysClockTime = SystemClock.uptimeMillis();
//		boolean repeatedTime = youtubeElapsedTime == lastYoutubeElapsedTime;
//		long sysClockDelta = lastSysClockTime == 0 ? 0 : sysClockTime - lastSysClockTime;
//		lastYoutubeElapsedTime = youtubeElapsedTime;
//
//		if (repeatedTime) {
//			m_currentTime = youtubeElapsedTime + sysClockDelta;
//		} else {
//			lastSysClockTime = sysClockTime;
//			m_currentTime = youtubeElapsedTime;
//		}

		m_currentTime = m_player.getCurrentPosition();

		showMessage("Current Time : " + m_currentTime);
	}

	Runnable playerTimingLoop = new Runnable() {
		@Override
		public void run() {
			try {
				if (m_player == null) return;
				if (!m_player.isPrepared()) return;
//				if (!m_player.isPlaying()) return;
				setCurrentTime();
				changeText((int) m_currentTime);  ///Set the current title of current time.
//                if(mbLoopable){ checkLoop(); }
				if ((startButtonPressed && endButtonPressed) && (m_currentTime < startPos || m_currentTime > endPos)) {
					Log.d("startPos", Long.toString(startPos));
					Log.d("trying to seek to", Long.toString((int) (startPos + 2000)));
					m_player.seekTo((int) (startPos + 2000));
				}
				mCurTimeShowHandler.postDelayed(this, 100);
			} catch (IllegalStateException e) {
				mCurTimeShowHandler.removeCallbacks(this);
			}
		}
	};

	////////////////////////////////////// TIMING LOOP /////////////////////////////////////////////////////////////////////

	///////////////////////////////// TEXT FILE PROCESSING /////////////////////////////////////////////////////////////////

	//From the first to number of hashtable keys, Search index that its value is bigger than
	// current time. Then sets the text that was finded in hashtable keys.

	public void changeText(int currentTime) {
		for (int nIndex = 0; nIndex < arrayKeys.length - 1; nIndex++) {
			if (arrayKeys[nIndex] <= currentTime && arrayKeys[nIndex + 1] > currentTime) {
				if (arrayCallStatus[nIndex])
					return;

				arrayCallStatus[nIndex] = true;
//                BluetoothClass.sendToFretX(Util.str2array((String) punch_list.get(arrayKeys[nIndex])));
				Bluetooth.getInstance().setMatrix((byte[]) punch_list.get(arrayKeys[nIndex]));
				Util.setDefaultValues(arrayCallStatus);
				arrayCallStatus[nIndex] = true;

			}
		}

		if (arrayKeys[arrayKeys.length - 1] <= currentTime) {
			if (arrayCallStatus[arrayKeys.length - 1])
				return;

			arrayCallStatus[arrayKeys.length - 1] = true;
//            BluetoothClass.sendToFretX(Util.str2array((String) punch_list.get(arrayKeys[arrayKeys.length - 1])));
			Bluetooth.getInstance().setMatrix((byte[]) punch_list.get(arrayKeys[arrayKeys.length - 1]));
			Util.setDefaultValues(arrayCallStatus);
			arrayCallStatus[arrayKeys.length - 1] = true;
		}
	}

	//    public Hashtable songtxtToHashtable(String data) {
	public Hashtable songtxtToHashtable() {
		punch_list = new Hashtable();
		ArrayList<SongPunch> punches = song.punches();

		for (SongPunch sp : punches) {
			//Skipping the conversion of this part for now, in favor of using byte[] arrays directly
			//We can revert back to String if need be
//            if( punch_list.containsKey( punch_time ) ) {               // not sure why we need to handle two chords on the same time ???
//                byte[] bluetoothArrayTmp = (byte[]) punch_list.get(punch_time);
//                punch_list.put(punch_time, strTemp + ":" + strText);
//                continue;
//            }
			punch_list.put(sp.timeMs, sp.fingering);
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
		for (Enumeration e = punch_list.keys(); e.hasMoreElements(); ) {
			arrayKeys[i] = (int) e.nextElement();
			arrayCallStatus[i] = false;
			i++;
		}
		Arrays.sort(arrayKeys);
		Log.d("initTxt", Long.toString((System.currentTimeMillis() - timeStart)));
	}

	///////////////////////////////// TEXT FILE PROCESSING /////////////////////////////////////////////////////////////////


	private static void showMessage(String message) {
		Log.d("+++", message);
	}

	/*
	 * A TransferListener class that can startListening to a download task and be
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