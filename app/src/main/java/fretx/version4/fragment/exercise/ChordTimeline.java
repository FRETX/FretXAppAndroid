package fretx.version4.fragment.exercise;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import fretx.version4.ChordTimelineView;
import fretx.version4.R;
import fretx.version4.fretxapi.song.SongPunch;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 13/06/17 12:24.
 */

public class ChordTimeline extends Fragment {
    private static final String TAG = "KJKP6_CHORD_TIMELINE";
    private static final int DEFAULT_HALF_SPAN_MS = 500;
    private ArrayList<SongPunch> punches;
    private int punchesIndex;
    private final ArrayList<SongPunch> playingPunches = new ArrayList<>();
    private int spanMs;
    private ChordTimelineView chordTimelineView;

    public static ChordTimeline newInstance(ArrayList<SongPunch> punches) {
        final ChordTimeline timeline = new ChordTimeline();
        timeline.setPunches(punches);
        timeline.setSpanMs(DEFAULT_HALF_SPAN_MS);
        return timeline;
    }

    public void setPunches(ArrayList<SongPunch> punches) {
        this.punches = punches;
    }

    public void setSpanMs(int spanMs) {
        this.spanMs = spanMs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.chord_timeline_fragment, container, false);
        chordTimelineView = (ChordTimelineView) rootView.findViewById(R.id.chordTimelineView);
        chordTimelineView.setSpan(spanMs);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void init(long startTimeMs) {
        Log.v(TAG, "-- update: " + startTimeMs + " --");
        playingPunches.clear();
        boolean started = false;
        for (punchesIndex = 0; punchesIndex < punches.size(); punchesIndex++) {
            final SongPunch songPunch = punches.get(punchesIndex);
            if (songPunch.timeMs < startTimeMs - spanMs)
                continue;
            if (songPunch.timeMs >= startTimeMs + spanMs)
                break;
            if (!started) {
                started = true;
                if (punchesIndex > 0)
                    playingPunches.add(punches.get(punchesIndex - 1));
            }
            playingPunches.add(songPunch);
        }
        Log.v(TAG, "playing punches: " + playingPunches.toString());
        chordTimelineView.setPunches(playingPunches);
        chordTimelineView.update(startTimeMs);
    }

    public void update(long currentTimeMs) {
        Log.v(TAG, "-- update: " + currentTimeMs + " --");
        boolean playingPunchesChanged = false;
        //remove finished chords
        for (int index = 0; index < playingPunches.size() - 1; index++) {
            if (playingPunches.get(index + 1).timeMs > currentTimeMs - spanMs)
                break;
            playingPunches.remove(index);
            playingPunchesChanged = true;
        }
        //add started chords
        for (; punchesIndex < punches.size(); punchesIndex++) {
            final SongPunch songPunch = punches.get(punchesIndex);
            if (songPunch.timeMs > currentTimeMs + spanMs)
                break;
            playingPunches.add(songPunch);
            playingPunchesChanged = true;
        }
        Log.v(TAG, "playing punches: " + playingPunches.toString());
        if (playingPunchesChanged)
            chordTimelineView.setPunches(playingPunches);
        chordTimelineView.update(currentTimeMs);
    }
}
