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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.bluetooth.BluetoothListener;
import io.intercom.android.sdk.Intercom;
import io.intercom.com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 17:17.
 */

public class Check extends Fragment implements HardwareFragment{
    private final static String TAG = "KJKP6_CHECK";
    private LinearLayout errorLayout;
    private FrameLayout progressLayout;
    private ImageView gifView;
    private BluetoothListener bluetoothListener = new BluetoothListener() {
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

        @Override
        public void onFailure() {
            Log.d(TAG, "Failure!");
            onCheckFailure();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hardware_check, container, false);

        errorLayout = (LinearLayout) rootView.findViewById(R.id.error_layout);
        progressLayout = (FrameLayout) rootView.findViewById(R.id.progress_layout);
        gifView = (ImageView) rootView.findViewById(R.id.gif);

        Glide.with(getActivity()).load(R.raw.on_light).into(gifView);

        final Button retry = (Button) rootView.findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
                Bluetooth.getInstance().connect();
            }
        });
        final Button skip = (Button) rootView.findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckSuccess();
            }
        });
        final Button assist = (Button) rootView.findViewById(R.id.assistance);
        assist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intercom.client().displayMessageComposer("[Connectivity check]: need help!");
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        Bluetooth.getInstance().registerBluetoothListener(bluetoothListener);
        Bluetooth.getInstance().connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Bluetooth.getInstance().unregisterBluetoothListener(bluetoothListener);
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
        Glide.with(getActivity()).load(R.raw.on_light).into(gifView);
        progressLayout.setVisibility(View.GONE);
    }
}
