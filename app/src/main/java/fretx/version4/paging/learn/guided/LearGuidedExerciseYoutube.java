package fretx.version4.paging.learn.guided;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 30/06/17 15:41.
 */

public class LearGuidedExerciseYoutube extends Fragment implements YouTubePlayer.OnInitializedListener{
    private static final String API_KEY = "AIzaSyAhxy0JS9M_oaDMW_bJMPyoi9R6oILFjNs";
    private YouTubePlayerView playerView;
    private YouTubePlayer player;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.paging_learn_guided_exercise_youtube, container, false);
        playerView = (YouTubePlayerView) rootView.findViewById(R.id.player);

        playerView.initialize(API_KEY, this);

        return rootView;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.player = player;
        if (!wasRestored) {
            player.cueVideo("avP5d16wEp0");
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}
