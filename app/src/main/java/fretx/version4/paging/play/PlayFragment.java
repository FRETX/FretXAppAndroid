package fretx.version4.paging.play;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;


public class PlayFragment extends Fragment {

    private MainActivity mActivity;

    private View rootView = null;

    public PlayFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.play_fragment, container, false);

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.play_container, new PlayFragmentSearchList());
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        return rootView;
    }
}