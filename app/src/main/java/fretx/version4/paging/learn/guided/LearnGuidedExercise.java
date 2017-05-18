package fretx.version4.paging.learn.guided;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fragment.exercise.ExerciseFragment;
import fretx.version4.fragment.exercise.ExerciseListener;
import fretx.version4.utils.bluetooth.BluetoothLE;
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
        BluetoothLE.getInstance().clearMatrix();
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
        fragmentTransaction.replace(R.id.exercise_fragment_container, exerciseFragment);
        fragmentTransaction.commit();

        return rootView;
    }

    //when the exercise fragment report the end of current exercise
    @Override
    public void onFinish(int min, int sec) {
        LearnGuidedExerciseDialog dialog = LearnGuidedExerciseDialog.newInstance(this, min, sec,
                exerciseList == null || listIndex == exerciseList.size() - 1);
        dialog.show(fragmentManager, "dialog");
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
    public void setExercise(List<GuidedExercise> exerciseList, int listIndex) {
        this.exerciseList = exerciseList;
        this.listIndex = listIndex;
    }
}