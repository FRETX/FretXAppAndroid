package fretx.version4.paging.learn;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fretx.version4.activities.MainActivity;
import fretx.version4.R;


public class LearnFragment extends Fragment {

    private MainActivity mActivity;

    private View rootView = null;

    public LearnFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.learn_fragment, container, false);

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.learn_container, new LearnFragmentButton());
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
        return rootView;
    }
}