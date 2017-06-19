package fretx.version4.activities;

import android.content.Context;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

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
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

import fretx.version4.R;
import fretx.version4.fretxapi.AppCache;
import fretx.version4.fretxapi.Network;
import fretx.version4.fretxapi.song.SongList;
import fretx.version4.onboarding.login.User;
import fretx.version4.paging.learn.LearnFragment;
import fretx.version4.paging.play.list.PlayFragmentSearchList;
import fretx.version4.paging.profile.Profile;
import fretx.version4.paging.tuner.TunerFragment;
import fretx.version4.utils.Preference;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.bluetooth.BluetoothListener;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UnreadConversationCountListener;

public class MainActivity extends BaseActivity {
	private static final String TAG = "KJKP6_MAINACTIVITY";

	//VIEWS
	private BottomBar bottomBar;
	private ImageView previewButton;
	private ImageButton connectButton;
	public boolean previewEnabled = true;

	public FragNavController fragNavController;

	private static int INDEX_PLAY = FragNavController.TAB1;
	private static int INDEX_LEARN = FragNavController.TAB2;
	private static int INDEX_TUNER = FragNavController.TAB3;
	private static int INDEX_PROFILE = FragNavController.TAB4;

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

	private UnreadConversationCountListener unreadListener = new UnreadConversationCountListener() {
		@Override
		public void onCountUpdate(int nbUnread) {
			updateSettingTab(nbUnread);
		}
	};

	private final BluetoothListener bluetoothListener = new BluetoothListener() {
		@Override
		public void onConnect() {
            Log.d(TAG, "Bluetooth device connected!");
			BluetoothAnimator.getInstance().stringFall();
			runOnUiThread(setConnected);
		}

		@Override
		public void onScanFailure() {
			Log.d(TAG, "Bluetooth scan Failed!");
			runOnUiThread(setDisconnected);
		}

		@Override
		public void onDisconnect() {
			Log.d(TAG, "Bluetooth device disconnected!");
			runOnUiThread(setDisconnected);
		}

		@Override
		public void onFailure(){
			Log.d(TAG, "Bluetooth connection failed!");
			runOnUiThread(setDisconnected);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		connectButton = (ImageButton) findViewById(R.id.connectButton);
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        previewButton = (ImageView) findViewById(R.id.previewButton);

		final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed token: " + refreshedToken);

        //// TODO: 24/04/17 move this to splashscreen
        Context ctx = getApplicationContext();
		Network.initialize(ctx);
		AppCache.initialize(ctx);
		SongList.initialize();

        final List<Fragment> fragments = new ArrayList<>();
		fragments.add(new PlayFragmentSearchList());
		fragments.add(new LearnFragment());
		fragments.add(new TunerFragment());
		fragments.add(new Profile());
		fragNavController= new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.main_relative_layout, fragments, INDEX_PLAY);
        bottomBar.selectTabAtPosition(INDEX_PLAY);

		setGuiEventListeners();

		Preference.getInstance().init();
	}

    @Override
    protected void onResume() {
        super.onResume();

        if (previewEnabled) {
			setNonGreyed(previewButton);
        } else {
			setGreyed(previewButton);
        }

        if (Bluetooth.getInstance().isConnected()) {
            setNonGreyed(connectButton);
        } else {
            setGreyed(connectButton);
        }

		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			Intercom.client().addUnreadConversationCountListener(unreadListener);
		}

        Bluetooth.getInstance().registerBluetoothListener(bluetoothListener);
    }

	@Override
	protected void onPause() {
		super.onPause();
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			Intercom.client().removeUnreadConversationCountListener(unreadListener);
		}

        Bluetooth.getInstance().unregisterBluetoothListener(bluetoothListener);
	}

	public void setGuiEventListeners() {
		bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
			@Override
			public void onTabSelected(@IdRes int tabId) {
				previewButton.setVisibility(View.INVISIBLE);

				switch(tabId){
					case R.id.bottomtab_play:
						fragNavController.switchTab(INDEX_PLAY);
						previewButton.setVisibility(View.VISIBLE);
						break;
					case R.id.bottomtab_learn:
						fragNavController.switchTab(INDEX_LEARN);
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
                if (Bluetooth.getInstance().isConnected()) {
                    Bluetooth.getInstance().disconnect();
                    Log.d(TAG, "Disconnected!");
                } else {
                    Bluetooth.getInstance().connect();
                }
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

	private void updateSettingTab(int nbUnread) {
		final BottomBarTab tab = bottomBar.getTabAtPosition(INDEX_PROFILE);
		if (nbUnread > 0) {
			tab.setBadgeCount(nbUnread);
		} else {
			tab.removeBadge();
		}
	}
}
