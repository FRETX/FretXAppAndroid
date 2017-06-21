package fretx.version4.paging.play.preview;

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

public class PlayPreviewDialog extends DialogFragment
{
    private static final String ELAPSED_TIME_MIN = "elapsed_time_min";
    private static final String ELAPSED_TIME_SEC = "elapsed_time_sec";
    private boolean replay;
    private Dialog dialog;

    public interface PlayPreviewDialogListener {
        void onUpdate(boolean replay);
    }

    public static PlayPreviewDialog newInstance(PlayPreviewDialogListener listener, int min, int sec) {
        PlayPreviewDialog dialog = new PlayPreviewDialog();
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
        dialog.setContentView(R.layout.paging_play_preview_dialog);

        //retrieve time from arguments
        int min = getArguments().getInt(ELAPSED_TIME_MIN);
        int sec = getArguments().getInt(ELAPSED_TIME_SEC);
        getArguments().remove(ELAPSED_TIME_MIN);
        getArguments().remove(ELAPSED_TIME_SEC);

        //display elapsed time
        TextView timeText = (TextView) dialog.findViewById(R.id.finishedElapsedTimeText);
        timeText.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", min, sec));

        //set button listeners
        Button button = (Button) dialog.findViewById(R.id.replayButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replay = true;
                dialog.dismiss();
            }
        });
        button = (Button) dialog.findViewById(R.id.backButton);
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
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);

        Fragment parentFragment = getTargetFragment();
        ((PlayPreviewDialogListener) parentFragment).onUpdate(replay);
    }
}