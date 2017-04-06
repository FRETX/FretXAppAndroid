package fretx.version4.paging.learn.guided;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageButton;

import fretx.version4.R;

/**
 * Created by pandor on 3/7/17.
 */

public class LearnGuidedChordExerciseDialog extends DialogFragment
{
    private Dialog dialog;

    public static LearnGuidedChordExerciseDialog newInstance() {
        LearnGuidedChordExerciseDialog dialog = new LearnGuidedChordExerciseDialog();
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.paging_learn_custom_builder_dialog);

        //setup listeners
        ImageButton ib = (ImageButton) dialog.findViewById(R.id.delete_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedItem() != null) {
//                    Toast.makeText(getActivity(), "Delete", Toast.LENGTH_SHORT).show();
                    deleteConfirmationAlertDialogBuilder().show();
                }
            }
        });


        return dialog;
    }
}
