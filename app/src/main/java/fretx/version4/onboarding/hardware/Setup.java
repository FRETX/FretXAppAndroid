package fretx.version4.onboarding.hardware;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.activities.HardwareActivity;
import fretx.version4.utils.firebase.FirebaseConfig;
import io.intercom.android.sdk.Intercom;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 10:18.
 */

public class Setup extends Fragment implements HardwareFragment, SetupListener {
    private final static String TAG = "KJKP6_SETUP";

    private YouTubePlayerSupportFragment youTubePlayerSupportFragment;
    private ArrayList<String> urls;
    private YouTubePlayer player;
    private SetupListener setupListener = this;
    private static final String API_KEY = "AIzaSyAhxy0JS9M_oaDMW_bJMPyoi9R6oILFjNs";
    private int state = 0;
    private boolean videoEnded;

    @Override
    //Setup dialog implementation
    public void onReplay(){
        videoEnded = false;
        player.seekToMillis(0);
        player.play();
    }

    @Override
    public void onNext(){
        ++state;
        updateState();
    }

    @Override
    public void onAssist() {
        Intercom.client().displayMessageComposer("[Step " + (state + 1) + "]: need help!");
    }

    private final YouTubePlayer.PlayerStateChangeListener stateListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            if (videoEnded)
                return;
            Log.d(TAG, "onVideoEnded");
            videoEnded = true;
            if (state == urls.size() - 1) {
                final SetupPhotoDialog photoDialog = SetupPhotoDialog.newInstance(setupListener);
                photoDialog.show(getActivity().getSupportFragmentManager(), null);
            } else {
                final SetupDialog dialog = SetupDialog.newInstance(setupListener);
                dialog.show(getActivity().getSupportFragmentManager(), null);
            }
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            Log.v(TAG, "error reason: " + errorReason.toString());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urls = FirebaseConfig.getInstance().getSetupUrls();
        Log.d(TAG, "urls(" + urls.size() + "):" + urls.toString());
        youTubePlayerSupportFragment = new YouTubePlayerSupportFragment();
        initializePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (player != null)
                player.setShowFullscreenButton(false);
        } catch (IllegalStateException e) {
            Log.v(TAG, "exception catched");
            initializePlayer();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hardware_setup, container, false);

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.youtube_player_container, youTubePlayerSupportFragment);
        fragmentTransaction.commit();

        return rootView;
    }

    private void initializePlayer() {
        youTubePlayerSupportFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                if (!wasRestored) {
                    player = youTubePlayer;
                    player.setShowFullscreenButton(false);
                    player.setFullscreen(false);
                    youTubePlayer.setPlayerStateChangeListener(stateListener);
                    updateState();
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(getActivity(), "Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    private void updateState() {
        Log.d(TAG, "state: " + state);
        if (state != urls.size()) {
            player.loadVideo(urls.get(state));
            videoEnded = false;
        } else {
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            final Fragment fragment = new Check();
            ((HardwareActivity) getActivity()).setFragment(fragment);
            fragmentTransaction.replace(R.id.hardware_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
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
