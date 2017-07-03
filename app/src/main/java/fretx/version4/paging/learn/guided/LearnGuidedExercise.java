package fretx.version4.paging.learn.guided;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fragment.exercise.ExerciseFragment;
import fretx.version4.fragment.exercise.ExerciseListener;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.firebase.Analytics;

public class LearnGuidedExercise extends Fragment implements ExerciseListener,
        LearnGuidedExerciseDialog.LearnGuidedChordExerciseListener {
    private static final String TAG = "KJKP6_GUIDED_EXERCISE";
    private FragmentManager fragmentManager;
    private ExerciseFragment exerciseFragment;

    //exercises
    private List<GuidedExercise> exerciseList;
    private int listIndex;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.getInstance().logSelectEvent("EXERCISE", "Guided Chord");
        Bluetooth.getInstance().clearMatrix();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_learn_guided_exercise_layout, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        exerciseFragment = new ExerciseFragment();
        exerciseFragment.setListener(this);
        final GuidedExercise exercise = exerciseList.get(listIndex);
        exerciseFragment.setTargetChords(exercise.getChords());
        exerciseFragment.setChords(exercise.getChords(), exercise.getRepetition());
        fragmentTransaction.replace(R.id.preview_fragment_container, exerciseFragment);
        fragmentTransaction.commit();

<<<<<<< Updated upstream
=======
        playerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean wasRestored) {
                LearnGuidedExercise.this.youtubePlayer = youTubePlayer;
                youtubePlayer.setShowFullscreenButton(false);
                youtubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {

                    }

                    @Override
                    public void onLoaded(String s) {
                        youTubePlayer.play();
                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {

                    }

                    @Override
                    public void onVideoEnded() {
                        startExercise();
                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {

                    }
                });
                if (!wasRestored) {
                    youTubePlayer.loadVideo("avP5d16wEp0");
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });

>>>>>>> Stashed changes
        return rootView;
    }

    //when the exercise fragment report the end of current exercise
    @Override
    public void onFinish(final int min, final int sec) {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null) {
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String exerciseName = exerciseList.get(listIndex).getName();
                    final Score prevScore = dataSnapshot.child("users").child(fUser.getUid()).child("score").child(exerciseName).getValue(Score.class);
                    if (prevScore == null) {
                        mDatabase.child("users").child(fUser.getUid()).child("score").child(exerciseName).setValue(new Score(min * 60 + sec));
                    } else {
                        prevScore.add(min * 60 + sec);
                        mDatabase.child("users").child(fUser.getUid()).child("score").child(exerciseName).setValue(prevScore);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            LearnGuidedExerciseDialog dialog = LearnGuidedExerciseDialog.newInstance(this, min, sec,
                    exerciseList == null || listIndex == exerciseList.size() - 1);
            dialog.show(fragmentManager, "dialog");
        }
    }

    //retrieve result of the finished exercise dialog
    @Override
    public void onUpdate(boolean replay) {
        //replay the actual exercise
        if (replay) {
            exerciseFragment.reset();
        } else {
            LearnGuidedExercise guidedChordExerciseFragment = new LearnGuidedExercise();
            guidedChordExerciseFragment.setExercise(exerciseList, listIndex + 1);
            ((MainActivity)getActivity()).fragNavController.replaceFragment(guidedChordExerciseFragment);
        }
    }

    //setup exercise flow & current exercise
<<<<<<< Updated upstream
    public void setExercise(List<GuidedExercise> exerciseList, int listIndex) {
        this.exerciseList = exerciseList;
        this.listIndex = listIndex;
=======
    public void setExercise(HashMap<String, GuidedExercise> exercises, String exerciseId) {
        this.exercises = exercises;
        this.exerciseId = exerciseId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
>>>>>>> Stashed changes
    }
}