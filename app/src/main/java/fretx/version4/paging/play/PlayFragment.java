package fretx.version4.paging.play;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.utils.firebase.Analytics;


public class PlayFragment extends Fragment {

    private MainActivity mActivity;

    private View rootView = null;

    public PlayFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.paging_play, container, false);

        Analytics.getInstance().logSelectEvent("TAB","Play");

        return rootView;
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d("PlayFragment","onStop");
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentByTag("PlayYoutubeFragment");
        if(fragment==null){
            Log.d("tagged fragment","isnull");
            return;
        }
        fragment.onStop();
    }
}