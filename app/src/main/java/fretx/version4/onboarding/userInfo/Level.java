package fretx.version4.onboarding.userInfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 18/05/17 09:41.
 */

public class Level extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.onboarding_level, container, false);

        final RadioButton electricRadio = (RadioButton) rootView.findViewById(R.id.beginnerRadio);
        electricRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (((RadioButton) v).isChecked())
                //    Toast.makeText(getActivity(), "Beginner", Toast.LENGTH_SHORT).show();
            }
        });
        final RadioButton acousticRadio = (RadioButton) rootView.findViewById(R.id.playerRadio);
        acousticRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (((RadioButton) v).isChecked())
                //    Toast.makeText(getActivity(), "Player", Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }
}
