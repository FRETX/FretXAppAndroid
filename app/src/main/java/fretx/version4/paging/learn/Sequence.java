package fretx.version4.paging.learn;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;


/**
 * Created by pandor on 3/7/17.
 */

class Sequence {
    private String name;
    private ArrayList<Chord> chords;

    Sequence(String name, ArrayList<Chord> chords) {
        this.name = name;
        this.chords = chords;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Chord> getChords() {
        return chords;
    }

    public void setChords (ArrayList<Chord> chords) {
        this.chords = new ArrayList<>(chords);
    }

    public void setName(String name) {
        this.name = name;
    }
}
