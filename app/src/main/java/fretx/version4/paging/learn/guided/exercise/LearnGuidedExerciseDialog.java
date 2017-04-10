package fretx.version4.paging.learn.guided.exercise;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fretx.version4.R;

/**
 * Created by pandor on 3/7/17.
 */

public class LearnGuidedExerciseDialog extends DialogFragment
{
    private static final String ELAPSED_TIME_MIN = "elapsed_time_min";
    private static final String ELAPSED_TIME_SEC = "elapsed_time_sec";
    private Dialog dialog;
    private int min;
    private int sec;
    private boolean replay = true;

    interface LearnGuidedChordExerciseListener {
        void onUpdate(boolean replay);
    }

    public static LearnGuidedExerciseDialog newInstance(LearnGuidedChordExerciseListener listener, int min, int sec) {
        LearnGuidedExerciseDialog dialog = new LearnGuidedExerciseDialog();
        dialog.setTargetFragment((Fragment) listener, 4321);
        Bundle args = new Bundle();
        args.putInt(ELAPSED_TIME_MIN, min);
        args.putInt(ELAPSED_TIME_SEC, sec);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.paging_learn_guided_exercise_dialog);

        //retrieve time from arguments
        min = getArguments().getInt(ELAPSED_TIME_MIN);
        sec = getArguments().getInt(ELAPSED_TIME_MIN);
        getArguments().remove(ELAPSED_TIME_MIN);
        getArguments().remove(ELAPSED_TIME_SEC);

        //display elapsed time
        TextView timeText = (TextView) dialog.findViewById(R.id.finishedElapsedTimeText);
        timeText.setText(String.format("%1$02d:%2$02d", min, sec));

        //set button listeners
        Button button = (Button) dialog.findViewById(R.id.finishedBackButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        button = (Button) dialog.findViewById(R.id.finishedNextExerciseButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replay = false;
                dialog.dismiss();
            }
        });

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        Fragment parentFragment = getTargetFragment();
        ((LearnGuidedChordExerciseListener) parentFragment).onUpdate(replay);
    }
}

/*
    public void finishExercise(long elapsedTime){


        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.guided_exercise_finished_layout);



        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        TextView finishedElapsedTimeText = (TextView) dialog.findViewById(R.id.finishedElapsedTimeText);
        finishedElapsedTimeText.setText("You finished this exercise in: " + String.format("%d:%02d", minutes, seconds));

        //Set onclicklisteners
        Button backToListButton = (Button) dialog.findViewById(R.id.finishedBackButton);
        Button nextExerciseButton = (Button) dialog.findViewById(R.id.finishedNextExerciseButton);

        final boolean lastExerciseInList;
        if (listPosition == listData.size() - 1) {
            nextExerciseButton.setText("FINISH");
            lastExerciseInList = true;
        } else lastExerciseInList = false;

        backToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.fragNavController.popFragment();
                dialog.dismiss();
            }
        });

        nextExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastExerciseInList) {
                    mActivity.fragNavController.popFragment();

                    dialog.dismiss();
                } else {
                    LearnGuidedExerciseFragment guidedChordExerciseFragment = new LearnGuidedExerciseFragment();
                    guidedChordExerciseFragment.setExercise(listData.get(listPosition + 1));
                    guidedChordExerciseFragment.setListData(listData, listPosition + 1);
                    mActivity.fragNavController.replaceFragment(guidedChordExerciseFragment);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


    }
*/