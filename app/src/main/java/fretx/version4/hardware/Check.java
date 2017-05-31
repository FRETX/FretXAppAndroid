package fretx.version4.hardware;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 17:17.
 */

public class Check extends Fragment implements HardwareFragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hardware_check, container, false);
        return rootView;
    }

    @Override
    public void onBackPressed() {
    }
}
