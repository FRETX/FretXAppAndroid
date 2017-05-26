package fretx.version4.fragment.exercise;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.utils.Preference;
import rocks.fretx.audioprocessing.Chord;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 26/05/17 18:10.
 */

public class FretboardFragment extends Fragment {
    private FretboardView fretboardView;
    private ImageView strummer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fretboard_fragment, container, false);

        fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
        strummer = (ImageView) rootView.findViewById(R.id.strummer);

        if (Preference.getInstance().isLeftHanded()) {
            fretboardView.setScaleX(-1.0f);

            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)strummer.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            strummer.setLayoutParams(params);
        } else {
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)strummer.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            strummer.setLayoutParams(params);
        }

        return rootView;
    }

    public void strum() {
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.strum_move);
        strummer.startAnimation(animation1);
    }

    public void setChord(@Nullable Chord chord) {
        if (chord != null)
            fretboardView.setFretboardPositions(chord.getFingerPositions());
    }
}