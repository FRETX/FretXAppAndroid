package fretx.version4.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import fretx.version4.Config;
import fretx.version4.R;
import fretx.version4.paging.SlidingTabLayout;
import fretx.version4.Util;
import fretx.version4.fretxapi.AppCache;
import fretx.version4.fretxapi.Network;
import fretx.version4.fretxapi.Songlist;
import fretx.version4.paging.ViewPagerAdapter;
import fretx.version4.paging.learn.LearnFragmentButton;
import fretx.version4.paging.play.PlayFragmentSearchList;
import rocks.fretx.audioprocessing.AudioProcessing;
import rocks.fretx.audioprocessing.Chord;


public class MainActivity extends ActionBarActivity {

    private ViewPager pager;
    //private LeftNavAdapter adapter;
    private SlidingTabLayout slidingTabLayout;
    private TextView  m_tvConnectionState;
    private ImageView on_button;
    private ImageView off_button;

    private int mCurrentPosition = 0;
    private int mPreviousPosition = 0;

    //AUDIO STUFF

    private final int PERMISSION_CODE_RECORD_AUDIO = 42;  //This is arbitrary, so why not The Answer to Life, Universe, and Everything.

    public int fs = 16000;
    public double bufferSizeInSeconds = 0.15;
    public AudioProcessing audio;
    private GoogleApiClient client;  //ATTENTION: This was auto-generated to implement the App Indexing API. See https://g.co/AppIndexing/AndroidStudio for more information.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_back);

        Context ctx = getApplicationContext();
        Network.initialize(ctx);
        AppCache.initialize(ctx);
        Songlist.initialize();

        getGuiReferences();
        setGuiEventListeners();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();   //ATTENTION: This was auto-generated to implement the App Indexing API. See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    public void getGuiReferences() {
        pager               = (ViewPager)        findViewById(R.id.viewpager);
        slidingTabLayout    = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        m_tvConnectionState = (TextView)         findViewById(R.id.tvConnectionState);
        on_button           = (ImageView)        findViewById(R.id.onb);
        off_button          = (ImageView)        findViewById(R.id.offb);

        pager.setAdapter( new ViewPagerAdapter(getSupportFragmentManager() ) );
        slidingTabLayout.setViewPager(pager);
        slidingTabLayout.setBackgroundColor(Color.argb(255, 240, 240, 240));
    }

    public void setGuiEventListeners() {
        // Was used in the past to stop Bluetooth data when leaving the Learn Tab
        /*
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                mPreviousPosition = mCurrentPosition;
                mCurrentPosition = position;
                if (mPreviousPosition == 1)
                    changeFragments(position);
                //Util.stopViaData();
                return Color.BLUE;

            }
        }); */
        m_tvConnectionState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        off_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });
    }

    public void showConnectionState() {
        if (Config.bBlueToothActive == true) {
            m_tvConnectionState.setText("FRETX is Connected");
            on_button.setVisibility(View.VISIBLE);
            off_button.setVisibility(View.INVISIBLE);
        } else {
            m_tvConnectionState.setText("FRETX not Connected");
            off_button.setVisibility(View.VISIBLE);
            on_button.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectionState();

        boolean permissionsGranted = askForPermissions();

        if (permissionsGranted) {
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
        if (audio.isInitialized() && audio.isProcessing() ) { audio.stop(); }
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

    @Override
    public void onBackPressed() {
        if (mCurrentPosition == 0) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.play_container, new PlayFragmentSearchList());
            fragmentTransaction.commit();
        } else if (mCurrentPosition == 1) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.learn_container, new LearnFragmentButton());
            fragmentTransaction.commit();
        }

    }
/*
    public void changeFragments(int position) {
        if (position == 2 || position == 0) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.learn_container, new LearnFragmentButton());
            fragmentTransaction.commit();
        }
    }
*/
    //Permissions
    private boolean askForPermissions() {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MainActivity.this,"You already have the permission",Toast.LENGTH_LONG).show();
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                //If the user has denied the permission previously your code will come to this block
                //Here you can explain why you need this permission
                //Explain here why you need this permission
            }
            //And finally ask for the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE_RECORD_AUDIO);
            return false;
        }
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Checking the request code of our request
        if (requestCode == PERMISSION_CODE_RECORD_AUDIO) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
//                Toast.makeText(this,"Permission granted now you can record audio",Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "FretX Tuner cannot work without this permission. Restart the app to ask for it again.", Toast.LENGTH_LONG).show();
            }
        }
    }

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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }
}
