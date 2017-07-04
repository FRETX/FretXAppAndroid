package fretx.version4.onboarding.userInfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 18/05/17 09:41.
 */

public class Hand extends Fragment implements HandDialog.HandDialogListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.onboarding_hand, container, false);

        final HandDialog.HandDialogListener listener = this;

        final RadioButton electricRadio = (RadioButton) rootView.findViewById(R.id.leftRadio);
        electricRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (((RadioButton) v).isChecked())
                //    Toast.makeText(getActivity(), "Left", Toast.LENGTH_SHORT).show();
            }
        });
        final RadioButton acousticRadio = (RadioButton) rootView.findViewById(R.id.rightRadio);
        acousticRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (((RadioButton) v).isChecked())
                //    Toast.makeText(getActivity(), "Right", Toast.LENGTH_SHORT).show();
            }
        });
        final TextView notSure = (TextView) rootView.findViewById(R.id.not_sure);
        notSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandDialog.newInstance(listener).show(getFragmentManager(), null);
            }
        });
        return rootView;
    }

    @Override
    public void onUpdate() {
    }
}
