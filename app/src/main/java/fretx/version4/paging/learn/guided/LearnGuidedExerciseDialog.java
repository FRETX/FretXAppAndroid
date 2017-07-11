package fretx.version4.paging.learn.guided;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import fretx.version4.R;

/**
 * Created by pandor on 3/7/17.
 */

public class LearnGuidedExerciseDialog extends DialogFragment
{
    private static final String ELAPSED_TIME_MIN = "elapsed_time_min";
    private static final String ELAPSED_TIME_SEC = "elapsed_time_sec";
    private static final String LAST_EXERCISE = "last_exercise";
    private LearnGuidedChordExerciseListener listener;

    public static LearnGuidedExerciseDialog newInstance(@NonNull LearnGuidedChordExerciseListener listener, int min, int sec, boolean last) {
        LearnGuidedExerciseDialog dialog = new LearnGuidedExerciseDialog();
        dialog.listener = listener;
        final Bundle args = new Bundle();
        args.putInt(ELAPSED_TIME_MIN, min);
        args.putInt(ELAPSED_TIME_SEC, sec);
        args.putBoolean(LAST_EXERCISE, last);
        dialog.setArguments(args);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.paging_learn_guided_exercise_dialog);

        //retrieve time from arguments
        int min = getArguments().getInt(ELAPSED_TIME_MIN);
        int sec = getArguments().getInt(ELAPSED_TIME_SEC);
        boolean last = getArguments().getBoolean(LAST_EXERCISE);
        getArguments().remove(ELAPSED_TIME_MIN);
        getArguments().remove(ELAPSED_TIME_SEC);
        getArguments().remove(LAST_EXERCISE);

        //display elapsed time
        TextView timeText = (TextView) dialog.findViewById(R.id.time);
        timeText.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", min, sec));

        //set button listeners
        final TextView replay = (TextView) dialog.findViewById(R.id.replay);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onReplay();
            }
        });
        final Button next = (Button) dialog.findViewById(R.id.nextButton);
        if (last) {
            next.setVisibility(View.GONE);
        } else {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    listener.onNext();
                }
            });
        }
        final TextView menu = (TextView) dialog.findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onGoBack();
            }
        });

        return dialog;
    }
}