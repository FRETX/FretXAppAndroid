package fretx.version4.activities;

import android.content.Context;

import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

import co.mobiwise.materialintro.prefs.PreferencesManager;
import fretx.version4.R;
import fretx.version4.fretxapi.AppCache;
import fretx.version4.fretxapi.Network;
import fretx.version4.fretxapi.song.SongList;
import fretx.version4.login.User;
import fretx.version4.paging.chords.ChordFragment;
import fretx.version4.paging.learn.LearnFragment;
import fretx.version4.paging.play.list.PlayFragmentSearchList;
import fretx.version4.paging.profile.Profile;
import fretx.version4.paging.tuner.TunerFragment;
import fretx.version4.utils.Preference;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.bluetooth.BluetoothListener;

public class MainActivity extends BaseActivity {
	private static final String TAG = "KJKP6_MAINACTIVITY";

	//VIEWS
	private ImageView bluetoothButton;
	private BottomBar bottomBar;

	//FLAGS
	private MainActivity mActivity = this;

	private ImageView previewButton;
	private ImageButton connectButton;
	public boolean previewEnabled = true;

	private List<Fragment> fragments = new ArrayList<>(4);
	public FragNavController fragNavController;

	private static int INDEX_PLAY = FragNavController.TAB1;
	private static int INDEX_LEARN = FragNavController.TAB2;
	private static int INDEX_CHORDS = FragNavController.TAB3;
	private static int INDEX_TUNER = FragNavController.TAB4;
	private static int INDEX_PROFILE = FragNavController.TAB5;

	private final Runnable setConnected = new Runnable() {
		@Override
		public void run() {
            setNonGreyed(connectButton);
			invalidateOptionsMenu();
		}
	};

	private Runnable setDisconnected = new Runnable() {
		@Override
		public void run() {
            setGreyed(connectButton);
            invalidateOptionsMenu();
		}
	};

	private final BluetoothListener bluetoothListener = new BluetoothListener() {
		@Override
		public void onConnect() {
			Log.d(TAG, "Connected!");
			runOnUiThread(setConnected);
		}

		@Override
		public void onScanFailure() {
			Log.d(TAG, "Failed!");
			runOnUiThread(setDisconnected);
		}

		@Override
		public void onDisconnect() {
			Log.d(TAG, "Failed!");
			runOnUiThread(setDisconnected);
		}

		@Override
		public void onFailure(){
			Log.d(TAG, "Failed!");
			runOnUiThread(setDisconnected);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed token: " + refreshedToken);

        //// TODO: 24/04/17 move this to splashscreen
        Context ctx = getApplicationContext();
		Network.initialize(ctx);
		AppCache.initialize(ctx);
		SongList.initialize();

		fragments.add(new PlayFragmentSearchList());
		fragments.add(new LearnFragment());
		fragments.add(new ChordFragment());
		fragments.add(new TunerFragment());
		fragments.add(new Profile());
		fragNavController= new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.main_relative_layout, fragments, INDEX_PLAY);

        bluetoothButton = (ImageView) findViewById(R.id.bluetoothLogo);
        connectButton = (ImageButton) findViewById(R.id.connectButton);
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        previewButton = (ImageView) findViewById(R.id.previewButton);

		setGuiEventListeners();

		bottomBar.selectTabAtPosition(INDEX_PLAY);

        BluetoothLE.getInstance().setListener(bluetoothListener);

		final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
		final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
		if (fUser != null) {
			mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					User user = dataSnapshot.child("users").child(fUser.getUid()).getValue(User.class);
					Preference.getInstance().init(user.guitar, user.hand, user.level);
				}
				@Override
				public void onCancelled(DatabaseError databaseError) {
					//failure, use local save instead...
                    Preference.getInstance().init("classic", "right", "beginner");
				}
			});
		} else {
			//user not connected, use local save instead...
            Preference.getInstance().init("classic", "right", "beginner");
		}
	}

    @Override
    protected void onResume() {
        super.onResume();

        if (previewEnabled) {
			setNonGreyed(previewButton);
        } else {
			setGreyed(previewButton);
        }

        if (BluetoothLE.getInstance().isConnected()) {
            setNonGreyed(connectButton);
        } else {
            setGreyed(connectButton);
        }
    }

    public void setGuiEventListeners() {
		bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
			@Override
			public void onTabSelected(@IdRes int tabId) {
				BluetoothLE.getInstance().clearMatrix();
				previewButton.setVisibility(View.INVISIBLE);

				switch(tabId){
					case R.id.bottomtab_play:
						fragNavController.switchTab(INDEX_PLAY);
						previewButton.setVisibility(View.VISIBLE);
						break;
					case R.id.bottomtab_learn:
						fragNavController.switchTab(INDEX_LEARN);
						break;
					case R.id.bottomtab_chords:
						fragNavController.switchTab(INDEX_CHORDS);
						break;
					case R.id.bottomtab_tuner:
						fragNavController.switchTab(INDEX_TUNER);
						break;
					case R.id.bottomtab_profile:
						fragNavController.switchTab(INDEX_PROFILE);
						break;
				}
			}
		});

		bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
			@Override
			public void onTabReSelected(@IdRes int tabId) {
				fragNavController.clearStack();
			}
		});

		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                setGreyed(connectButton);
				if (BluetoothLE.getInstance().isEnabled()) {
					BluetoothLE.getInstance().disconnect();
					BluetoothLE.getInstance().scan();
				}
			}
		});

		//Hidden setting for us to use during testing
		bluetoothButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				PreferencesManager tutorialPrefs = new PreferencesManager(getApplicationContext());
				tutorialPrefs.resetAll();
				SongList.getIndexFromServer();
				Toast.makeText(mActivity,"All tutorials reset, cache refreshed",Toast.LENGTH_SHORT).show( );
				return true;
			}
		});

		previewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(previewEnabled){
					setGreyed((ImageView) view);
					previewEnabled = false;
				} else {
					setNonGreyed( (ImageView) view );
					previewEnabled = true;
				}
			}
		});

	}

	@Override
	public void onBackPressed(){
		if (!fragNavController.isRootFragment()) {
			fragNavController.popFragment();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (fragNavController != null) {
			fragNavController.onSaveInstanceState(outState);
		}
	}

	public static void setGreyed(ImageView v) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(0);  //0 means grayscale
		ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
		v.setColorFilter(cf);
		v.setAlpha(128);   // 128 = 0.5
	}

	public static void setNonGreyed(ImageView v) {
		v.setColorFilter(null);
		v.setAlpha(255);
	}
}
