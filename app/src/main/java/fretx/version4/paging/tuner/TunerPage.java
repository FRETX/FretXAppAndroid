package fretx.version4.paging.tuner;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import fretx.version4.R;
import fretx.version4.fragment.YoutubeListener;
import fretx.version4.fragment.YoutubeTutorial;
import fretx.version4.utils.firebase.Analytics;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 12/07/17 15:50.
 */

public class TunerPage extends Fragment implements YoutubeListener {
    private final static String TAG = "KJKP6_TUNER_PAGE";
    private FrameLayout fragmentContainer;
    private Fragment fragment;
    private String youtubeId = "mLaL0exs0GA";
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        fragmentManager = getActivity().getSupportFragmentManager();
        Analytics.getInstance().logSelectEvent("TAB", "Learn");
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
        final View rootView = inflater.inflate(R.layout.paging_tuner, container, false);
        fragmentContainer = (FrameLayout) rootView.findViewById(R.id.container);
        setYoutube();
        return rootView;
    }

    private void setYoutube() {
        if (youtubeId.isEmpty()) {
            Log.d(TAG, "no video to display");
        } else {
            Log.d(TAG, "display the video");
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = YoutubeTutorial.newInstance(TunerPage.this, youtubeId);
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();
        }
    }

    private void setTuner() {
        Log.d(TAG, "display the tuner");
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new TunerFragment();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onVideoEnded() {
    }
}
