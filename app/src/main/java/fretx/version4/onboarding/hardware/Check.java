package fretx.version4.onboarding.hardware;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.bluetooth.BluetoothListener;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 17:17.
 */

public class Check extends Fragment implements HardwareFragment{
    private final static String TAG = "KJKP6_CHECK";
    private LinearLayout errorLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hardware_check, container, false);
        errorLayout = (LinearLayout) rootView.findViewById(R.id.error_layout);
        final Button retry = (Button) rootView.findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorLayout.setVisibility(View.INVISIBLE);
                BluetoothLE.getInstance().scan();
            }
        });
        final Button skip = (Button) rootView.findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorLayout.setVisibility(View.INVISIBLE);
                onCheckSuccess();
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BluetoothLE.getInstance().setListener(new BluetoothListener() {
            @Override
            public void onConnect() {
                Log.d(TAG, "Success!");
                onCheckSuccess();
            }

            @Override
            public void onDisconnect() {
                Log.d(TAG, "Failure!");
                onCheckFailure();
            }

            @Override
            public void onScanFailure() {
                Log.d(TAG, "Failure!");
                onCheckFailure();
            }

            public void onFailure() {
                Log.d(TAG, "Failure!");
                onCheckFailure();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        errorLayout.setVisibility(View.INVISIBLE);
        BluetoothLE.getInstance().scan();
    }

    @Override
    public void onBackPressed() {
    }

    private void onCheckSuccess(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    private void onCheckFailure(){
        errorLayout.setVisibility(View.VISIBLE);
    }
}
