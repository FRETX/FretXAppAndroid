package fretx.version4.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import fretx.version4.view.ChordTimelineView;
import fretx.version4.R;
import fretx.version4.fretxapi.song.SongPunch;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 13/06/17 12:24.
 */

public class ChordTimeline extends Fragment {
    private static final String TAG = "KJKP6_CHORD_TIMELINE";
    private static final int DEFAULT_LEFT_SPAN_MS = 0;
    private static final int DEFAULT_RIGHT_SPAN_MS = 3000;

    private ArrayList<SongPunch> punches;
    private ArrayList<SongPunch> playingPunches = new ArrayList<>();
    private int punchesIndex;
    private int leftSpanMs;
    private int rightSpanMs;

    private ChordTimelineView chordTimelineView;

    public static ChordTimeline newInstance(ArrayList<SongPunch> punches) {
        final ChordTimeline timeline = new ChordTimeline();
        timeline.setPunches(punches);
        timeline.setSpanMs(DEFAULT_LEFT_SPAN_MS, DEFAULT_RIGHT_SPAN_MS);
        return timeline;
    }

    public void setPunches(ArrayList<SongPunch> punches) {
        if (punches == null)
            this.punches = new ArrayList<>();
        else
            this.punches = punches;
    }

    public void setSpanMs(int leftSpanMs, int rightSpanMs) {
        this.leftSpanMs = leftSpanMs;
        this.rightSpanMs = rightSpanMs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_chord_timeline, container, false);
        chordTimelineView = (ChordTimelineView) rootView.findViewById(R.id.chordTimelineView);
        chordTimelineView.setSpan(leftSpanMs, rightSpanMs);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void init(long startTimeMs) {
        playingPunches.clear();
        boolean started = false;
        for (punchesIndex = 0; punchesIndex < punches.size(); punchesIndex++) {
            final SongPunch songPunch = punches.get(punchesIndex);
            //skip already played song punch
            if (songPunch.timeMs < startTimeMs - leftSpanMs) {
                continue;
            }
            //add the not visible terminating song punch
            if (songPunch.timeMs >= startTimeMs + rightSpanMs) {
                playingPunches.add(songPunch);
                break;
            }
            //add started and not yet finished punch
            if (!started) {
                started = true;
                if (punchesIndex > 0)
                    playingPunches.add(punches.get(punchesIndex - 1));
            }
            playingPunches.add(songPunch);
        }
        chordTimelineView.setPunches(playingPunches);
        chordTimelineView.update(startTimeMs);
    }

    public void update(long currentTimeMs) {
        boolean playingPunchesChanged = false;
        //remove finished chords
        for (int index = 0; index < playingPunches.size() - 1; index++) {
            if (playingPunches.get(index + 1).timeMs > currentTimeMs - leftSpanMs)
                break;
            playingPunches.remove(index);
            playingPunchesChanged = true;
        }
        //add started chords
        for (; punchesIndex < punches.size() - 1; punchesIndex++) {
            if (punches.get(punchesIndex).timeMs > currentTimeMs + rightSpanMs)
                break;
            playingPunches.add(punches.get(punchesIndex + 1));
            playingPunchesChanged = true;
        }
        if (playingPunchesChanged)
            chordTimelineView.setPunches(playingPunches);
        chordTimelineView.update(currentTimeMs);
    }
}
