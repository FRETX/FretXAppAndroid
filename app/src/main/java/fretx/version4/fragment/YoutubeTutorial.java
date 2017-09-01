package fretx.version4.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 10/07/17 17:37.
 */

public class YoutubeTutorial extends Fragment {
    private final static String TAG = "KJKP6_YOUTUBE_TUTORIAL";

    private YouTubePlayerSupportFragment youTubePlayerSupportFragment;
    private YouTubePlayer player;
    private static final String API_KEY = "AIzaSyAhxy0JS9M_oaDMW_bJMPyoi9R6oILFjNs";
    private boolean videoEnded;
    private String id;
    private YoutubeListener listener;

    public static YoutubeTutorial newInstance(@Nullable YoutubeListener listener, @NonNull String id) {
        Log.d(TAG, "YT new instance: " + listener);
        final YoutubeTutorial exercise = new YoutubeTutorial();
        exercise.listener = listener;
        exercise.id = id;
        return exercise;
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
            listener.onVideoEnded();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            Log.v(TAG, "error reason: " + errorReason.toString());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        youTubePlayerSupportFragment = new YouTubePlayerSupportFragment();
        init();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.hardware_setup, container, false);

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.youtube_player_container, youTubePlayerSupportFragment);
        fragmentTransaction.commit();

        return rootView;
    }

    private void init() {
        //check if music volume is up
        final AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        /*
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) < 5) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Audio volume")
                    .setMessage("The video has audio, make sure to turn speakers ON")
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            initializePlayer();
                        }
                    }).show();
        } else {
        */
        initializePlayer();
        //}
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (player != null)
                player.setShowFullscreenButton(false);
        } catch (IllegalStateException e) {
            Log.v(TAG, "exception catched");
            init();
        }
    }

    private void initializePlayer() {
        youTubePlayerSupportFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                if (!wasRestored) {
                    player = youTubePlayer;
                    player.setShowFullscreenButton(false);
                    youTubePlayer.setPlayerStateChangeListener(stateListener);
                    player.loadVideo(id);
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(getActivity(), "Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
