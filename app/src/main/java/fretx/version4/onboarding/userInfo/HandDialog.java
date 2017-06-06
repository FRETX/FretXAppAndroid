package fretx.version4.onboarding.userInfo;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import fretx.version4.R;

/**
 * Created by pandor on 3/7/17.
 */

public class HandDialog extends DialogFragment
{
    private Dialog dialog;

    public interface HandDialogListener {
        void onUpdate();
    }

    public static HandDialog newInstance(HandDialogListener listener) {
        HandDialog dialog = new HandDialog();
        dialog.setTargetFragment((Fragment) listener, 4321);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.onboarding_hand_dialog);

        //set button listeners
        Button button = (Button) dialog.findViewById(R.id.ok_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }
}