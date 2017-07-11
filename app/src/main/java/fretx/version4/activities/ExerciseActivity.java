package fretx.version4.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import fretx.version4.R;
import fretx.version4.fragment.exercise.YoutubeExercise;
import fretx.version4.fragment.exercise.YoutubeListener;
import fretx.version4.paging.learn.guided.GuidedExerciseWrapper;

public class ExerciseActivity extends BaseActivity implements YoutubeListener{
    private GuidedExerciseWrapper wrapper;
    private String exerciseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        wrapper = (GuidedExerciseWrapper) getIntent().getSerializableExtra("wrapper");
        exerciseId = getIntent().getStringExtra("exerciseId");

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = YoutubeExercise.newInstance(this);
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onVideoEnded() {

    }
}
