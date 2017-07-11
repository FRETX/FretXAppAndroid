package fretx.version4.activities;


import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.apache.commons.math3.geometry.euclidean.threed.Line;

import fretx.version4.R;
import fretx.version4.fragment.exercise.ExerciseFragment;
import fretx.version4.fragment.exercise.ExerciseListener;
import fretx.version4.fragment.exercise.YoutubeExercise;
import fretx.version4.fragment.exercise.YoutubeListener;
import fretx.version4.paging.learn.guided.GuidedExerciseList;

public class ExerciseActivity extends BaseActivity implements YoutubeListener, ExerciseListener{
    private static final String TAG = "KJKP6_EXERCISE_ACTIVITY";
    private GuidedExerciseList exerciseList;
    private FrameLayout container;
    private String exerciseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        exerciseList = (GuidedExerciseList) getIntent().getSerializableExtra("exerciseList");
        exerciseId = getIntent().getStringExtra("exerciseId");

        Log.d(TAG, "exercise title: " + exerciseList.getExercise(exerciseId).getName());
        container = (FrameLayout) findViewById(R.id.fragment_container);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = YoutubeExercise.newInstance(this);
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onVideoEnded() {
        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = ExerciseFragment.newInstance(this, exerciseList.getExercise(exerciseId).getChords());
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        container.setVisibility(View.INVISIBLE);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onFinish(int min, int sec) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        container.setVisibility(View.VISIBLE);
        super.onConfigurationChanged(newConfig);
    }
}
