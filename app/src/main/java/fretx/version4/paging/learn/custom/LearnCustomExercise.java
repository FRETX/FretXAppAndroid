package fretx.version4.paging.learn.custom;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fragment.exercise.ExerciseFragment;
import fretx.version4.fragment.exercise.ExerciseListener;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/05/17 12:22.
 */

public class LearnCustomExercise extends Fragment implements ExerciseListener, LearnCustomExerciseDialog.LearnCustomExerciseDialogListener {
    private static final String TAG = "KJKP6_CUSTOM_EXERCISE";

    private FragmentManager fragmentManager;
    private ExerciseFragment exerciseFragment;

    private ArrayList<Chord> chords;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.getInstance().logSelectEvent("EXERCISE", "Custom Chord");
        Bluetooth.getInstance().clearMatrix();
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_learn_custom_exercise_layout, container, false);

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        exerciseFragment = new ExerciseFragment();
        exerciseFragment.setListener(this);
        exerciseFragment.setChords(chords);
        exerciseFragment.setTargetChords(chords);
        fragmentTransaction.replace(R.id.preview_fragment_container, exerciseFragment);
        fragmentTransaction.commit();

        return rootView;
    }

    //when the exercise fragment report the end of current exercise
    @Override
    public void onFinish(int min, int sec) {
        Log.d(TAG, "Exercise finished");
        LearnCustomExerciseDialog dialog = LearnCustomExerciseDialog.newInstance(this, min, sec);
        dialog.show(fragmentManager, "dialog");
    }

    @Override
    public void onUpdate(boolean replay) {
        if (replay) {
            exerciseFragment.reset();
        } else {
            ((MainActivity)getActivity()).fragNavController.popFragment();
        }
    }

    //setup exercise flow & current exercise
    public void setChords(ArrayList<Chord> chords) {
        this.chords = chords;
    }
}