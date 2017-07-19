package fretx.version4.activities;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import android.content.DialogInterface;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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
import fretx.version4.paging.learn.LearnPage;
import fretx.version4.paging.play.list.PlayFragmentSearchList;
import fretx.version4.paging.profile.Profile;
import fretx.version4.paging.tuner.TunerPage;
import fretx.version4.utils.Preference;
import fretx.version4.utils.audio.Audio;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import fretx.version4.utils.bluetooth.BluetoothListener;
import fretx.version4.utils.bluetooth.ScanResultDialog;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UnreadConversationCountListener;

public class MainActivity extends BaseActivity {
	private static final String TAG = "KJKP6_MAINACTIVITY";

    private String errorMessage = "no data";

	//VIEWS
    private BottomBar bottomBar;
	private MenuItem bluetoothItem;
	public FragNavController fragNavController;
	private static int INDEX_PLAY = FragNavController.TAB1;
	private static int INDEX_LEARN = FragNavController.TAB2;
	private static int INDEX_TUNER = FragNavController.TAB3;
	private static int INDEX_PROFILE = FragNavController.TAB4;
	private final Runnable setConnected = new Runnable() {
		@Override
		public void run() {
            Toast.makeText(getActivity(), "FretX connected", Toast.LENGTH_SHORT).show();
            setNonGreyed(bluetoothItem);
			invalidateOptionsMenu();
		}
	};
	private Runnable setDisconnected = new Runnable() {
		@Override
		public void run() {
            Toast.makeText(getActivity(), "FretX disconnected", Toast.LENGTH_SHORT).show();
            setGreyed(bluetoothItem);
            invalidateOptionsMenu();
            Bluetooth.getInstance().disconnect();
		}
	};
    private Runnable setFailed = new Runnable() {
        @Override
        public void run() {
            if (errorMessage != null)
                Toast.makeText(getActivity(), "FretX connection failed - " + errorMessage, Toast.LENGTH_SHORT).show();
            setGreyed(bluetoothItem);
            invalidateOptionsMenu();
            Bluetooth.getInstance().disconnect();
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
        public void onScanFailure(String errorMessage) {
            MainActivity.this.errorMessage = errorMessage;
            Log.d(TAG, "Bluetooth scan Failed - " + errorMessage);
            runOnUiThread(setFailed);
        }

        @Override
        public void onDisconnect() {
            Log.d(TAG, "Bluetooth device disconnected!");
            runOnUiThread(setDisconnected);
        }

        @Override
        public void onFailure(String errorMessage){
            MainActivity.this.errorMessage = errorMessage;
            Log.d(TAG, "Bluetooth connection failed - " + errorMessage);
            runOnUiThread(setFailed);
        }

        @Override
        public void onMultipleScanResult(SparseArray<BluetoothDevice> results) {
            MainActivity.this.errorMessage = null;
            runOnUiThread(setFailed);

            ScanResultDialog.newInstance(results).show(getSupportFragmentManager(), "dialog");
        }
    };

	private UnreadConversationCountListener unreadListener = new UnreadConversationCountListener() {
		@Override
		public void onCountUpdate(int nbUnread) {
			updateSettingTab(nbUnread);
		}
	};

    /*----------------------------------- LIFECYCLE ----------------------------------------------*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Preference.getInstance().init();

		final Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_fretx_withtext));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);

		final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed token: " + refreshedToken);

        //// TODO: 24/04/17 move this to splashscreen
        Context ctx = getApplicationContext();
        Network.initialize(ctx);
        AppCache.initialize(ctx);
        SongList.initialize();

        final List<Fragment> fragments = new ArrayList<>();
        fragments.add(new PlayFragmentSearchList());
        fragments.add(new LearnPage());
        fragments.add(new TunerPage());
        fragments.add(new Profile());
        fragNavController= new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.main_relative_layout, fragments, INDEX_PLAY);
        bottomBar.selectTabAtPosition(INDEX_PLAY);

        Bluetooth.getInstance().registerBluetoothListener(bluetoothListener);
        setGuiEventListeners();

	}

    @Override
    protected void onResume() {
        super.onResume();

        updateMenu();

		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			Intercom.client().addUnreadConversationCountListener(unreadListener);
		}
    }

	@Override
	protected void onPause() {
		super.onPause();
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			Intercom.client().removeUnreadConversationCountListener(unreadListener);
		}
	}

    @Override
    protected void onDestroy() {
        Bluetooth.getInstance().unregisterBluetoothListener(bluetoothListener);
        super.onDestroy();
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

	/*----------------------------------- OPTION MENU --------------------------------------------*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        bluetoothItem = menu.findItem(R.id.action_bluetooth);
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth:
                setGreyed(item);
                if (Bluetooth.getInstance().isConnected()) {
                    new AlertDialog.Builder(this)
                            .setMessage("You're about to TURN the FRETX Device OFF\n\n"
                                    + "By disconnecting your Bluetooth you automatically turn your FRETX Device OFF.\n\n"
                                    + "If you want to Connect it again, make sure you TURN ON your Hardware Device.")
                            .setPositiveButton("I'll disconnect", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Bluetooth.getInstance().disconnect();
                                    Toast.makeText(getActivity(), "FretX disconnected", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "disconnected!");
                                }
                            })
                            .setNegativeButton("Remain connected", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            }).show();
                } else {
                    item.setActionView(new ProgressBar(this));
                    Toast.makeText(getActivity(), "Connecting to FretX...", Toast.LENGTH_SHORT).show();
                    Bluetooth.getInstance().connectFretX();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	/*--------------------------------------- UTILS ----------------------------------------------*/

    public void setGuiEventListeners() {
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch(tabId){
                    case R.id.bottomtab_play:
                        fragNavController.switchTab(INDEX_PLAY);
                        Audio.getInstance().setMode(Audio.modeOptimization.CHORD);
                        break;
                    case R.id.bottomtab_learn:
                        fragNavController.switchTab(INDEX_LEARN);
                        Audio.getInstance().setMode(Audio.modeOptimization.CHORD);
                        break;
                    case R.id.bottomtab_tuner:
                        fragNavController.switchTab(INDEX_TUNER);
                        Audio.getInstance().setMode(Audio.modeOptimization.TUNER);
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
    }

    public static void setGreyed(MenuItem item) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        final Drawable icon = item.getIcon();
        icon.setColorFilter(cf);
        icon.setAlpha(128);
        item.setIcon(icon);
    }

	public static void setNonGreyed(MenuItem item) {
        final Drawable icon = item.getIcon();
        icon.setColorFilter(null);
        icon.setAlpha(255);
        item.setIcon(icon);
	}

    public void updateMenu() {
        if (bluetoothItem != null) {
            if (Bluetooth.getInstance().isConnected()) {
                setNonGreyed(bluetoothItem);
            } else {
                setGreyed(bluetoothItem);
            }
        }
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
