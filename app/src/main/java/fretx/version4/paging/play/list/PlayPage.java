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

public class PlayPage extends Fragment {
    private final static String TAG = "KJKP6_PLAY_PAGE";
    private RelativeLayout fragmentContainer;
    private Fragment fragment;
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        fragmentManager = getActivity().getSupportFragmentManager();
        Analytics.getInstance().logSelectEvent("TAB", "Play");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.paging_play, container, false);
        fragmentContainer = (RelativeLayout) rootView.findViewById(R.id.play_container);

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new PlayFragmentSearchList();
        fragmentTransaction.replace(R.id.play_container, fragment);
        fragmentTransaction.commit();

        return rootView;
    }
}
