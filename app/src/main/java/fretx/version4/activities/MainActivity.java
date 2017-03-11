package fretx.version4.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.greysonparrelli.permiso.Permiso;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import fretx.version4.BluetoothClass;
import fretx.version4.Config;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.fretxapi.AppCache;
import fretx.version4.fretxapi.Network;
import fretx.version4.fretxapi.Songlist;
import fretx.version4.paging.chords.ChordFragment;
import fretx.version4.paging.learn.LearnFragment;
import fretx.version4.paging.play.PlayFragment;
import fretx.version4.paging.tuner.TunerFragment;
import rocks.fretx.audioprocessing.AudioProcessing;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.MusicUtils;



public class MainActivity extends ActionBarActivity {
	//VIEWS
	private ImageView bluetoothButton, connectButton;
	private BottomNavigationViewEx bottomNavigationView;
	//FLAGS
	private boolean AUDIO_PERMISSIONS_GRANTED = false;
	private String SHOWCASE_ID = "bluetoothConnect";
	private MainActivity mActivity = this;
	public HashMap<String,FingerPositions> chordFingerings;

    //AUDIO PARAMETERS
    public int fs = 16000;
    public double bufferSizeInSeconds = 0.1;
    public AudioProcessing audio;

	private GoogleApiClient client;  //ATTENTION: This was auto-generated to implement the App Indexing API. See https://g.co/AppIndexing/AndroidStudio for more information.
	static boolean mbSendingFlag = false;
	byte[] btNoLightsArray = {Byte.valueOf("0")};
	ImageView previewButton;
	public boolean previewEnabled = false;
//	ConnectThread btTurnOffLightsThread = new ConnectThread(btNoLightsArray);

	//LIFECYCLE
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_back);
		Permiso.getInstance().setActivity(this);

		chordFingerings = MusicUtils.parseChordDb();

		Context ctx = getApplicationContext();
		Network.initialize(ctx);
		AppCache.initialize(ctx);
		Songlist.initialize(this);

		getGuiReferences();
		setGuiEventListeners();

		setLocked(previewButton);

		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();   //ATTENTION: This was auto-generated to implement the App Indexing API. See https://g.co/AppIndexing/AndroidStudio for more information.
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_relative_layout, new PlayFragment())
				.commit();

		showTutorial();

	}


    @Override
    protected void onResume() {
        super.onResume();
        showConnectionState();
	    Permiso.getInstance().setActivity(this);
	    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
		    @Override
		    public void onPermissionResult(Permiso.ResultSet resultSet) {
			    if (resultSet.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
				    Log.d("Permissions","Audio permissions granted");
				    AUDIO_PERMISSIONS_GRANTED = true;
				    // Audio permission granted!
			    }
			    if (resultSet.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
				    // Phone permission granted!
				    Log.d("Permissions","Phone permissions granted");
			    }
			    if (resultSet.isPermissionGranted(Manifest.permission.BLUETOOTH_ADMIN)) {
				    // Bluetooth permission granted!
				    Log.d("Permissions","Bluetooth permissions granted");
			    }
			    if (resultSet.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
				    // Location permission granted!
				    Log.d("Permissions","Location permissions granted");
			    }
		    }
		    @Override
		    public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
			    Permiso.getInstance().showRationaleInDialog("Title", "Message", null, callback);
		    }
	    }, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION);

	    if(AUDIO_PERMISSIONS_GRANTED){
            if (audio == null) audio = new AudioProcessing();

            //Set target chords
            ArrayList<Chord> targetChords = new ArrayList<Chord>(0);
            String[] majorRoots = new String[]{"A", "C", "D", "E", "F", "G"};
            for (int i = 0; i < majorRoots.length; i++) {
                targetChords.add(new Chord(majorRoots[i], "maj"));
            }
            String[] minorRoots = new String[]{"A", "B", "D", "E"};
            for (int i = 0; i < minorRoots.length; i++) {
                targetChords.add(new Chord(minorRoots[i], "m"));
            }

            if (!audio.isInitialized()) audio.initialize(fs, bufferSizeInSeconds);
            if (!audio.isProcessing()) audio.start();
            Log.d("onResume", "starting audio processing");
	    }
    }

    @Override
    protected void onPause() {
        super.onPause();
	    if(audio != null){
		    if (audio.isInitialized() && audio.isProcessing() ) { audio.stop(); }
	    }
    }

    @Override
    protected void onStop() {
        super.onStop(); // ATTENTION: This was auto-generated to implement the App Indexing API. See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        if (audio != null) { audio.stop(); }
        audio = null;
        Log.d("onStop", "stopping audio processing");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

//    @Override
//    public void onBackPressed() {
//		//Intentionally do nothing so that back button is disabled
//    }

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		AppIndex.AppIndexApi.start(client, getIndexApiAction());
	}


	//INITIALIZATION
	public void getGuiReferences() {
		bluetoothButton = (ImageView) findViewById(R.id.bluetoothLogo);
		connectButton = (ImageView) findViewById(R.id.connectButton);
		bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation);
		previewButton = (ImageView) findViewById(R.id.previewButton);
	}

	public void setGuiEventListeners() {
		bottomNavigationView.enableAnimation(false);
		bottomNavigationView.enableShiftingMode(false);
		bottomNavigationView.enableItemShiftingMode(false);
		bottomNavigationView.setTextVisibility(true);
		bottomNavigationView.setIconVisibility(true);
		bottomNavigationView.setCurrentItem(0);
		bottomNavigationView.setOnNavigationItemSelectedListener(
				new BottomNavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(@NonNull MenuItem item) {
						switch (item.getItemId()) {
							case R.id.action_play:
								for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
									bottomNavigationView.getMenu().getItem(i).setChecked(false);
								}
								item.setChecked(true);
								BluetoothClass.sendToFretX(btNoLightsArray);
//								btTurnOffLightsThread.run();
								getSupportFragmentManager()
										.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
										.replace(R.id.main_relative_layout, new PlayFragment())
										.commit();
								mActivity.audio.disableNoteDetector();
								mActivity.audio.disablePitchDetector();
								mActivity.audio.disableChordDetector();
								previewButton.setVisibility(View.VISIBLE);
								break;
							case R.id.action_learn:
								for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
									bottomNavigationView.getMenu().getItem(i).setChecked(false);
								}
								item.setChecked(true);
//								btTurnOffLightsThread.run();
								BluetoothClass.sendToFretX(btNoLightsArray);
								getSupportFragmentManager()
										.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
										.replace(R.id.main_relative_layout, new LearnFragment())
										.commit();
								mActivity.audio.disablePitchDetector();
								mActivity.audio.disableNoteDetector();
								mActivity.audio.disableChordDetector();
								previewButton.setVisibility(View.INVISIBLE);
								break;
							case R.id.action_chords:
								for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
									bottomNavigationView.getMenu().getItem(i).setChecked(false);
								}
								item.setChecked(true);
								BluetoothClass.sendToFretX(btNoLightsArray);
//								btTurnOffLightsThread.run();
								getSupportFragmentManager()
										.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
										.replace(R.id.main_relative_layout, new ChordFragment())
										.commit();
								mActivity.audio.disableNoteDetector();
								mActivity.audio.disablePitchDetector();
								mActivity.audio.disableChordDetector();
								previewButton.setVisibility(View.INVISIBLE);
								break;
							case R.id.action_tuner:
								for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
									bottomNavigationView.getMenu().getItem(i).setChecked(false);
								}
								item.setChecked(true);
								BluetoothClass.sendToFretX(btNoLightsArray);
//								btTurnOffLightsThread.run();
								getSupportFragmentManager()
										.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
										.replace(R.id.main_relative_layout, new TunerFragment())
										.commit();
								mActivity.audio.enablePitchDetector();
								mActivity.audio.disableNoteDetector();
								mActivity.audio.disableChordDetector();
								previewButton.setVisibility(View.INVISIBLE);
								break;
						}
						return false;
					}
				});

		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Config.bBlueToothActive == false) {
					Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
					startActivity(intent);
				} else {
					try {
						Util.stopViaData();
					} catch (Exception e) {
						e.printStackTrace();
					}
					new Handler().postDelayed(new Runnable() {
						public void run() {
							Config.bBlueToothActive = false;
							showConnectionState();
							BluetoothActivity.mBluetoothGatt.disconnect();
						}
					}, 200);
				}
			}
		});

		//Hidden setting for us to use during testing
		bluetoothButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				PreferencesManager tutorialPrefs = new PreferencesManager(getApplicationContext());
				tutorialPrefs.resetAll();
				Songlist.forceDownloadIndexFromServer();
				Toast.makeText(mActivity,"All tutorials reset, cache refreshed",Toast.LENGTH_SHORT).show( );
				return true;
			}
		});

		previewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(previewEnabled){
					setLocked((ImageView) view);
					previewEnabled = false;
				} else
				{
					setUnlocked( (ImageView) view );
					previewEnabled = true;
				}
			}
		});

	}


	//PERMISSIONS
    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
	    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	    Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }


	//UTILITY
	public void showConnectionState() {
		if (Config.bBlueToothActive == true) {
			setUnlocked(connectButton);
//			bluetoothButton.setImageResource(R.drawable.ic_fretx);
		} else {
			setLocked(connectButton);
//			bluetoothButton.setImageResource(R.drawable.ic_fretx);
		}
	}

	private void showTutorial(){

		new MaterialIntroView.Builder(this)
				.enableDotAnimation(false)
				.enableIcon(false)
				.setFocusGravity(FocusGravity.CENTER)
				.setFocusType(Focus.NORMAL)
				.setDelayMillis(300)
				.enableFadeAnimation(true)
				.performClick(true)
				.setInfoText("Turn on your FretX device and tap this button to connect to it")
				.setTarget((ImageView) findViewById(R.id.connectButton))
				.setUsageId("tutorialConnectBluetoothWithLogo") //THIS SHOULD BE UNIQUE ID
				.show();
	}


	//COMPAT STUFF
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }


	public static void setLocked(ImageView v) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(0);  //0 means grayscale
		ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
		v.setColorFilter(cf);
		v.setAlpha(128);   // 128 = 0.5
	}

	public static void setUnlocked(ImageView v) {
		v.setColorFilter(null);
		v.setAlpha(255);
	}


}
