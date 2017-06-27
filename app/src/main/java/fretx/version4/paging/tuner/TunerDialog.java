package fretx.version4.paging.tuner;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import fretx.version4.R;

/**
 * Created by pandor on 3/7/17.
 */

public class TunerDialog extends DialogFragment
{
    private Dialog dialog;

    interface TunerDialogListener {
        void onUpdate(boolean replay);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.paging_tuner_dialog);

        //set button listeners
        final Button button = (Button) dialog.findViewById(R.id.ok_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }
}