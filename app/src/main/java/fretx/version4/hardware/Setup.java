package fretx.version4.hardware;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 10:18.
 */

public class Setup extends Fragment implements EasyVideoCallback, SetupListener, HardwareFragment {
    private final static String TAG = "KJKP6_SETUP";

    private EasyVideoPlayer player;
    private static final String TEST_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private int state = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hardware_setup, container, false);

        // Grabs a reference to the player view
        player = (EasyVideoPlayer) rootView.findViewById(R.id.player);
        // Sets the callback to this Activity, since it inherits EasyVideoCallback
        player.setCallback(this);
        // Sets the source to the HTTP URL held in the TEST_URL variable.
        // To play files, you can use Uri.fromFile(new File("..."))
        updateState();
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Make sure the player stops playing if the user presses the home button.
        player.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        player.release();
    }

    // Methods for the implemented EasyVideoCallback
    @Override
    public void onPreparing(EasyVideoPlayer player) {
        // TODO handle if needed
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        // TODO handle
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
        switch (state) {
            case 0:
            case 1:
            case 2:
                final SetupDialog dialog = SetupDialog.newInstance(this);
                dialog.show(getActivity().getSupportFragmentManager(), null);
                break;
            case 3:
                final SetupPhotoDialog photoDialog = SetupPhotoDialog.newInstance(this);
                photoDialog.show(getActivity().getSupportFragmentManager(), null);
                break;
            default:
                Log.v(TAG, "state is out of bounds");
                break;
        }
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
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        // TODO handle if needed
    }

    //Setup dialog implementation
    public void onReplay(){
        player.seekTo(0);
        player.start();
    }

    public void onNext(){
        ++state;
        updateState();
    }

    private void updateState() {
        player.reset();
        String url = null;
        switch (state) {
            case 0:
                Log.v(TAG, "update to state: " + state);
                url = TEST_URL;
                break;
            case 1:
                Log.v(TAG, "update to state: " + state);
                url = TEST_URL;
                break;
            case 2:
                Log.v(TAG, "update to state: " + state);
                url = TEST_URL;
                break;
            case 3:
                Log.v(TAG, "update to state: " + state);
                url = TEST_URL;
                break;
            case 4:
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Fragment fragment = new Check();
                fragmentTransaction.replace(R.id.hardware_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            default:
                Log.v(TAG, "state is out of bounds");
                break;
        }
        if (url != null) {
            player.setSource(Uri.parse(url));
            player.setAutoPlay(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (state > 0) {
            --state;
            updateState();
        }
    }
}
