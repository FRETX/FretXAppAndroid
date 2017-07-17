package fretx.version4.paging.play.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import fretx.version4.R;
import fretx.version4.fragment.YoutubeListener;
import fretx.version4.fragment.YoutubeTutorial;
import fretx.version4.utils.Preference;
import fretx.version4.utils.Prefs;
import fretx.version4.utils.firebase.Analytics;
import fretx.version4.utils.firebase.FirebaseConfig;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 12/07/17 15:50.
 */

public class PlayPage extends Fragment implements YoutubeListener {
    private final static String TAG = "KJKP6_PLAY_PAGE";
    private RelativeLayout fragmentContainer;
    private Fragment fragment;
    private String youtubeId = "";
//    private String youtubeId = "";
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        fragmentManager = getActivity().getSupportFragmentManager();
        Analytics.getInstance().logSelectEvent("TAB", "Play");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstance");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.paging_play, container, false);
        fragmentContainer = (RelativeLayout) rootView.findViewById(R.id.play_container);

        if (Preference.getInstance().needPlayTutorial()) {
            Log.d(TAG, "need to display video");
            youtubeId = FirebaseConfig.getInstance().getPlayUrl();
            Log.d(TAG, "video id: " + youtubeId);
        }
        setYoutube();
        return rootView;
    }

    private void setYoutube() {
        if (youtubeId.isEmpty()) {
            setPlay();
        } else {
            Log.d(TAG, "display the video");
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = YoutubeTutorial.newInstance(PlayPage.this, youtubeId);
            fragmentTransaction.replace(R.id.play_container, fragment);
            fragmentTransaction.commit();
        }
    }

    private void setPlay() {
        Log.d(TAG, "display the play tab");
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new PlayFragmentSearchList();
        fragmentTransaction.replace(R.id.play_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onVideoEnded() {
        final Prefs prefs = new Prefs.Builder().setPlayTutorial("false").build();
        Preference.getInstance().save(prefs);
        setPlay();
    }
}
