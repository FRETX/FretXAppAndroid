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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.bluetooth.BluetoothListener;
import io.intercom.android.sdk.Intercom;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 17:17.
 */

public class Check extends Fragment implements HardwareFragment{
    private final static String TAG = "KJKP6_CHECK";
    private final static String CONNECTION_PROGRESS = "We are connecting...";
    private final static String CONNECTION_FAILED = "Couldn't connect your FretX";
    private LinearLayout errorLayout;
    private FrameLayout progressLayout;
    private ImageView gifView;
    private TextView actionText;
    private BluetoothListener bluetoothListener = new BluetoothListener() {
        @Override
        public void onConnect() {
            Log.d(TAG, "Success!");
            onCheckSuccess();
        }

        @Override
        public void onDisconnect() {
            Log.d(TAG, "Failure!");
            onCheckFailure("disconnected");
        }

        @Override
        public void onScanFailure(String errorMessage) {
            Log.d(TAG, "Failure!");
            onCheckFailure(errorMessage);
        }

        @Override
        public void onFailure(String errorMessage) {
            Log.d(TAG, "Failure!");
            onCheckFailure(errorMessage);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hardware_check, container, false);

        errorLayout = (LinearLayout) rootView.findViewById(R.id.error_layout);
        progressLayout = (FrameLayout) rootView.findViewById(R.id.progress_layout);
        gifView = (ImageView) rootView.findViewById(R.id.gif);
        actionText = (TextView) rootView.findViewById(R.id.action_text);

        setProgressLayout();

        final Button retry = (Button) rootView.findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressLayout();
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

    private void setProgressLayout() {
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        actionText.setText(CONNECTION_PROGRESS);
    }

    private void setErrorLayout() {
        errorLayout.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
        actionText.setText(CONNECTION_FAILED);
        Glide.with(getActivity()).load(R.raw.on_light).into(gifView);

    }

    @Override
    public void onResume() {
        super.onResume();
        setProgressLayout();
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

    private void onCheckFailure(String errorMessage){
        setErrorLayout();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }
}
