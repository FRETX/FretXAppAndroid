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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fretx.version4.R;
import fretx.version4.fragment.exercise.ExerciseFragment;
import fretx.version4.fragment.exercise.ExerciseListener;
import fretx.version4.fragment.exercise.YoutubeExercise;
import fretx.version4.fragment.YoutubeListener;
import fretx.version4.paging.learn.guided.GuidedExercise;
import fretx.version4.paging.learn.guided.GuidedExerciseList;
import fretx.version4.paging.learn.guided.LearnGuidedChordExerciseListener;
import fretx.version4.paging.learn.guided.LearnGuidedExerciseDialog;
import fretx.version4.paging.learn.guided.Score;

public class ExerciseActivity extends BaseActivity implements YoutubeListener, ExerciseListener, LearnGuidedChordExerciseListener {
    private static final String TAG = "KJKP6_EXERCISE_ACTIVITY";
    private GuidedExerciseList exerciseList;
    private FrameLayout container;
    private String exerciseId;
    private GuidedExercise exercise;
    private FragmentManager fragmentManager;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        container = (FrameLayout) findViewById(R.id.fragment_container);

        fragmentManager = getSupportFragmentManager();
        exerciseList = (GuidedExerciseList) getIntent().getSerializableExtra("exerciseList");
        exerciseId = getIntent().getStringExtra("exerciseId");
        exercise = exerciseList.getExercise(exerciseId);
        Log.d(TAG, "exercise title: " + exercise.getName());

        setYoutube();
    }

    private void setYoutube() {
        if (exercise.getYoutubeId().isEmpty()) {
            setExercise();
        } else {
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = YoutubeExercise.newInstance(this, exercise.getYoutubeId());
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            if (getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                container.setVisibility(View.INVISIBLE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    private void setExercise() {
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = ExerciseFragment.newInstance(this, exercise.getChords());
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        if (getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            container.setVisibility(View.INVISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onVideoEnded() {
        setExercise();
    }

    @Override
    public void onFinish(final int min, final int sec) {
        for (String childId: exercise.getChildren()) {
            exerciseList.setUnlocked(childId);
        }
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null) {
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(fUser.getUid()).child("score").child(exerciseId);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Score prevScore = dataSnapshot.getValue(Score.class);
                    if (prevScore == null) {
                        mDatabase.setValue(new Score(min * 60 + sec));
                    } else {
                        prevScore.add(min * 60 + sec);
                        mDatabase.setValue(prevScore);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            LearnGuidedExerciseDialog dialog = LearnGuidedExerciseDialog.newInstance(this, min, sec, exercise.getChildren().size() == 0);
            dialog.show(fragmentManager, "dialog");
        }
    }

    //retrieve result of the finished exercise dialog
    @Override
    public void onReplay() {
        ((ExerciseFragment) fragment).reset();
    }

    @Override
    public void onGoBack() {
        finish();
    }

    @Override
    public void onNext() {
        exerciseId = exercise.getChildren().get(0);
        exercise = exerciseList.getExercise(exerciseId);
        setYoutube();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        container.setVisibility(View.VISIBLE);
        super.onConfigurationChanged(newConfig);
    }
}
