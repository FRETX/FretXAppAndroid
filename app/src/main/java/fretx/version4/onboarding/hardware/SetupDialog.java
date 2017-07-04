package fretx.version4.onboarding.hardware;

import android.app.Dialog;
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

public class SetupDialog extends DialogFragment
{
    private Dialog dialog;

    public static SetupDialog newInstance(SetupListener listener) {
        final SetupDialog dialog = new SetupDialog();
        dialog.setTargetFragment((Fragment) listener, 4321);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.hardware_setup_dialog);

        //set button listeners
        final TextView replay = (TextView) dialog.findViewById(R.id.replay);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((SetupListener) getTargetFragment()).onReplay();
            }
        });

        final Button next = (Button) dialog.findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SetupListener) getTargetFragment()).onNext();
                dialog.dismiss();
            }
        });

        final TextView assistance = (TextView) dialog.findViewById(R.id.assistance);
        assistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SetupListener) getTargetFragment()).onAssist();
            }
        });

        return dialog;
    }
}