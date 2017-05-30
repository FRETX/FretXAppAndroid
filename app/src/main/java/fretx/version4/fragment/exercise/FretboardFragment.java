package fretx.version4.fragment.exercise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
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
    private ImageView green_bar;
    private RelativeLayout strummer_container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fretboard_fragment, container, false);

        fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
        strummer = (ImageView) rootView.findViewById(R.id.strummer);
        green_bar = (ImageView) rootView.findViewById(R.id.green_bar);
        strummer_container = (RelativeLayout) rootView.findViewById(R.id.strummer_container);

        if (Preference.getInstance().isLeftHanded()) {
            fretboardView.setScaleX(-1.0f);
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)strummer_container.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            strummer_container.setLayoutParams(params);
        } else {
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)strummer_container.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            strummer_container.setLayoutParams(params);
        }

        return rootView;
    }

        /*
    public void strum() {

        final Animation fadeInBar = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.strum_fade_in);
        final Animation fadeInStrummer = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.strum_fade_in);
        final Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.strum_fade_out);
        final Animation strum;
        if (Preference.getInstance().isLeftHanded()) {
            strum = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.strum_left_handed);
        } else {
            strum = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.strum_right_handed);
        }

        fadeInStrummer.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                strummer.startAnimation(strum);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        strum.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                strummer.startAnimation(fadeOut);
                green_bar.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        green_bar.startAnimation(fadeInBar);
        strummer.startAnimation(fadeInStrummer);

    }
      */

    public void strum() {
        float pos;
        if (Preference.getInstance().isLeftHanded()) {
            //bottom to top
            strummer.setY(strummer_container.getHeight() - strummer.getHeight());
            pos = 0;
        } else {
            //top to bottom
            strummer.setY(0);
            pos = strummer_container.getHeight() - strummer.getHeight();
        }

        strumFadeIn(pos);
    }

    private void strumFadeIn(final float pos) {
        strummer_container.setAlpha(0f);
        strummer_container.animate().alpha(1f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                strummer_container.setAlpha(1f);
                strumMove(pos);
            }
        });
    }

    private void strumMove(float pos) {
        strummer.animate().translationY(pos).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                strumFadeOut();
            }
        });
    }

    private void strumFadeOut() {
        strummer_container.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                strummer_container.setAlpha(0f);
            }
        });
    }

    public void setChord(@Nullable Chord chord) {
        if (chord != null)
            fretboardView.setFretboardPositions(chord.getFingerPositions());
    }
}