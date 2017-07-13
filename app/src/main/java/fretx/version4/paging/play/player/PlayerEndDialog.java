package fretx.version4.paging.play.player;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.Locale;
import java.util.Random;

import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongList;

/**
 * Created by pandor on 3/7/17.
 */

public class PlayerEndDialog extends DialogFragment
{
    private static final String SONG_NAME = "songName";
    private SongItem randomItem;
    private Dialog dialog;

    public interface PlayedEndDialogListener {
        void onCancel();
        void onReplay();
        void onRandom(SongItem item);
    }

    public static PlayerEndDialog newInstance(PlayedEndDialogListener listener, String songName) {
        PlayerEndDialog dialog = new PlayerEndDialog();
        dialog.setTargetFragment((Fragment) listener, 4321);
        final Bundle args = new Bundle();
        args.putString(SONG_NAME, songName);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.paging_play_youtube_end_dialog);

        //retrieve time from arguments
        final String songName = getArguments().getString(SONG_NAME);
        getArguments().remove(SONG_NAME);

        //display elapsed time
        final TextView timeText = (TextView) dialog.findViewById(R.id.song_name_textview);
        timeText.setText("You successfully played: \n" + songName);

        //set button listeners
        final Button replay = (Button) dialog.findViewById(R.id.replay_button);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                final Fragment parentFragment = getTargetFragment();
                ((PlayedEndDialogListener) parentFragment).onReplay();
            }
        });
        final Button back = (Button) dialog.findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                final Fragment parentFragment = getTargetFragment();
                ((PlayedEndDialogListener) parentFragment).onCancel();
            }
        });
        final ImageView randomImage = (ImageView) dialog.findViewById(R.id.random_imageview);
        final int length = SongList.length();
        if (length == 0) {
            randomImage.setVisibility(View.GONE);
        } else {
            while (randomItem == null || randomItem.imageURL() == null) {
                randomItem  = SongList.getRandomSongItem();
            }
            Picasso.with(BaseActivity.getActivity()).load(randomItem.imageURL()).placeholder(R.drawable.defaultthumb).into(randomImage);
            randomImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    final Fragment parentFragment = getTargetFragment();
                    ((PlayedEndDialogListener) parentFragment).onRandom(randomItem);
                }
            });
        }

        return dialog;
    }
}