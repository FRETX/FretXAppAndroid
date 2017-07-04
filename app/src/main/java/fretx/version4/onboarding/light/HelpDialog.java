package fretx.version4.onboarding.light;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import fretx.version4.R;
import fretx.version4.activities.HardwareActivity;
import fretx.version4.onboarding.hardware.SetupListener;
import io.intercom.android.sdk.Intercom;

/**
 * Created by pandor on 3/7/17.
 */

public class HelpDialog extends DialogFragment
{
    private Dialog dialog;

    public static HelpDialog newInstance() {
        final HelpDialog dialog = new HelpDialog();
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.light_dialog);
        dialog.setCancelable(true);

        //set button listeners
        final Button help1 = (Button) dialog.findViewById(R.id.help1);
        help1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), HardwareActivity.class);
                final Bundle b = new Bundle();
                b.putInt("start", 3);
                intent.putExtras(b);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        final Button help2 = (Button) dialog.findViewById(R.id.help2);
        help2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intercom.client().displayMessageComposer("[Troubleshooting]: my device doesn't turn on!");
                dialog.dismiss();
            }
        });

        final Button help3 = (Button) dialog.findViewById(R.id.help3);
        help3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intercom.client().displayMessageComposer("[Troubleshooting]: my bluetooth doesn't connect!");
                dialog.dismiss();
            }
        });

        final Button help4 = (Button) dialog.findViewById(R.id.help4);
        help4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), HardwareActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        return dialog;
    }
}