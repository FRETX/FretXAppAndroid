package fretx.version4.paging.play.preview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fragment.preview.PreviewFragment;
import fretx.version4.fragment.preview.PreviewListener;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.paging.play.player.PlayYoutubeFragment;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by Kickdrum on 05-Jan-17.
 */

public class PlayPreview extends Fragment implements PreviewListener, PlayPreviewDialog.PlayPreviewDialogListener {
    private static final String TAG = "KJKP6_PLAY_PREVIEW";

    private PreviewFragment previewFragment;

    private FragmentManager fragmentManager;
    private SongItem song;
    private MainActivity mActivity;

    //exercises
    private ArrayList<Chord> exerciseChords;

    static public PlayPreview newInstance(@NonNull SongItem song) {
        final PlayPreview fragment = new PlayPreview();
        fragment.song = song;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.getInstance().logSelectEvent("PREVIEW", song.song_title);
        Bluetooth.getInstance().clearMatrix();
        exerciseChords = song.getChords();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();

        final View rootView = inflater.inflate(R.layout.paging_play_preview, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        previewFragment = new PreviewFragment();
        previewFragment.setListener(this);
        previewFragment.setTargetChords(exerciseChords);
        previewFragment.setChords(exerciseChords);
        fragmentTransaction.replace(R.id.preview_fragment_container, previewFragment);
        fragmentTransaction.commit();

        final TextView title = (TextView) rootView.findViewById(R.id.songTitleText);
        title.setText(song.artist + " - " + song.song_title);

        return rootView;
    }

    //when the exercise fragment reports the end of current exercise
    @Override
    public void onFinish(int min, int sec) {
        Log.d(TAG, "Exercise finished");
        PlayPreviewDialog dialog = PlayPreviewDialog.newInstance(this, min, sec);
        dialog.show(fragmentManager, "dialog");
    }

    //when the exercise fragment reports the end of current exercise
    @Override
    public void onPlaySong() {
        Log.d(TAG, "Play song");
        onUpdate(false);
    }

    @Override
    public void onUpdate(boolean replay) {
        if (replay) {
            previewFragment.reset();
        } else {
            PlayYoutubeFragment youtubeFragment = new PlayYoutubeFragment();
            youtubeFragment.setSong(song);
            mActivity.fragNavController.pushFragment(youtubeFragment);
        }
    }
}
