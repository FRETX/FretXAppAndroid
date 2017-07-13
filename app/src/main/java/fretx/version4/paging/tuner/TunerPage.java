package fretx.version4.paging.tuner;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    private FrameLayout fragmentContainer;
    private Fragment fragment;
    private String youtubeId = "mLaL0exs0GA";
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getActivity().getSupportFragmentManager();
        Analytics.getInstance().logSelectEvent("TAB", "Learn");
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
            setTuner();
        } else {
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = YoutubeTutorial.newInstance(TunerPage.this, youtubeId);
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();
            /*
            if (getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                fragmentContainer.setVisibility(View.INVISIBLE);
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            */
        }
    }

    private void setTuner() {
        //final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragment = new TunerFragment();
        //fragmentTransaction.replace(R.id.container, fragment);
        //fragmentTransaction.commit();
        if (getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            fragmentContainer.setVisibility(View.INVISIBLE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onVideoEnded() {
        setTuner();
    }
}
