package fretx.version4.paging.play.preview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fragment.exercise.ExerciseFragment;
import fretx.version4.fragment.exercise.ExerciseListener;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.paging.play.player.PlayYoutubeFragment;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.firebase.Analytics;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by Kickdrum on 05-Jan-17.
 */

public class PlayPreview extends Fragment implements ExerciseListener, PlayPreviewDialog.PlayPreviewDialogListener {
    private static final String TAG = "KJKP6_GUIDED_EXERCISE";

    private ExerciseFragment exerciseFragment;

    private FragmentManager fragmentManager;
    private SongItem song;
    private MainActivity mActivity;

    //exercises
    private ArrayList<Chord> exerciseChords;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.getInstance().logSelectEvent("PREVIEW", song.song_title);
        Bluetooth.getInstance().clearMatrix();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();

        View rootView = inflater.inflate(R.layout.paging_play_preview, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        exerciseFragment = new ExerciseFragment();
        exerciseFragment.setListener(this);
        exerciseFragment.setTargetChords(exerciseChords);
        exerciseFragment.setChords(exerciseChords);
        fragmentTransaction.replace(R.id.exercise_fragment_container, exerciseFragment);
        fragmentTransaction.commit();

        final Button play = (Button) rootView.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayYoutubeFragment youtubeFragment = new PlayYoutubeFragment();
                youtubeFragment.setSong(song);
                mActivity.fragNavController.pushFragment(youtubeFragment);
            }
        });

        final Button nextChord = (Button) rootView.findViewById(R.id.nextChord);
        nextChord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exerciseFragment.nextChord();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //when the exercise fragment report the end of current exercise
    @Override
    public void onFinish(int min, int sec) {
        Log.d(TAG, "Exercise finished");
        PlayPreviewDialog dialog = PlayPreviewDialog.newInstance(this, min, sec);
        dialog.show(fragmentManager, "dialog");
    }

    @Override
    public void onUpdate(boolean replay) {
        if (replay) {
            exerciseFragment.reset();
        } else {
            PlayYoutubeFragment youtubeFragment = new PlayYoutubeFragment();
            youtubeFragment.setSong(song);
            mActivity.fragNavController.pushFragment(youtubeFragment);
        }
    }

    @SuppressWarnings("unchecked")
    public void setChords(ArrayList<Chord> chords) {
        this.exerciseChords = (ArrayList<Chord>) chords.clone();
    }

    public void setSong(SongItem song) {
        this.song = song;
    }
}
