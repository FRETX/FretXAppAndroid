package fretx.version4.view;

import android.util.Log;

import fretx.version4.R;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 27/06/17 12:19.
 */

public class HeadstockViewDescriptor {

    public enum Headstock {CLASSIC, ELECTRIC}
    public int ressourceId;
    public final Ear ears[] = new Ear[6];
    public float stringWidthRatio;
    public float earRadiusRatio;
    public float stringWidth;
    public float earRadius;
    public float stringBottom;

    public HeadstockViewDescriptor(Headstock headstock) {
        switch (headstock) {
            case CLASSIC:
                ressourceId = R.drawable.classical_headstock;
                ears[0] = new Ear(0.09f, 0.51f, 0.32f, 0.77f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[0]).substring(0,1));
                ears[1] = new Ear(0.09f, 0.36f, 0.38f, 0.77f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[1]).substring(0,1));
                ears[2] = new Ear(0.09f, 0.215f, 0.45f, 0.77f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[2]).substring(0,1));
                ears[3] = new Ear(0.88f, 0.215f, 0.52f, 0.77f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[3]).substring(0,1));
                ears[4] = new Ear(0.88f, 0.36f, 0.58f, 0.77f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[4]).substring(0,1));
                ears[5] = new Ear(0.88f, 0.51f, 0.65f, 0.77f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[5]).substring(0,1));
                stringWidthRatio = 0.02f;
                earRadiusRatio = 0.05f;
                break;
            case ELECTRIC:
                ressourceId = R.drawable.electric_headstock;
                ears[0] = new Ear(0.54f, 0.05f, 0.37f, 0.63f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[0]).substring(0,1));
                ears[1] = new Ear(0.61f, 0.14f, 0.42f, 0.63f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[1]).substring(0,1));
                ears[2] = new Ear(0.68f, 0.23f, 0.46f, 0.63f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[2]).substring(0,1));
                ears[3] = new Ear(0.73f, 0.31f, 0.50f, 0.63f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[3]).substring(0,1));
                ears[4] = new Ear(0.79f, 0.41f, 0.55f, 0.63f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[4]).substring(0,1));
                ears[5] = new Ear(0.84f, 0.50f, 0.59f, 0.63f, MusicUtils.midiNoteToName(MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD)[5]).substring(0,1));
                stringWidthRatio = 0.02f;
                earRadiusRatio = 0.05f;
                break;
        }
    }

    public void update(int headStockImageWidth, int headStockImageHeight, int headStockImagePosX, int headStockImagePosY) {
        for (Ear ear: ears) {
            ear.update(headStockImageWidth, headStockImageHeight, headStockImagePosX, headStockImagePosY);
        }
        earRadius = earRadiusRatio * headStockImageHeight;
        stringWidth = stringWidthRatio * headStockImageWidth;
        stringBottom = headStockImagePosY + headStockImageHeight;
    }

    public class Ear {
        public float erx = 0;
        public float ery = 0;
        public float srx = 0;
        public float sry = 0;
        public float ex = 0;
        public float ey = 0;
        public float sx = 0;
        public float sy = 0;
        public float exm = 0;
        public float sxm = 0;
        public String name;

        Ear(float erx, float ery, float srx, float sry, String name) {
            this.erx = erx;
            this.ery = ery;
            this.srx = srx;
            this.sry = sry;
            this.name = name;
        }

        void update(int x, int y, int px, int py) {
            Log.d("KJKP6_DESCRIPTOR", "x = " + x);
            Log.d("KJKP6_DESCRIPTOR", "px = " + px);
            ex = px + erx * x;
            ey = py + ery * y;
            sx = px + srx * x;
            sy = py + sry * y;
            exm = px + erx * x;
            Log.d("KJKP6_DESCRIPTOR", "exm = " + exm);
            sxm = px + srx * x;
            Log.d("KJKP6_DESCRIPTOR", "sxm = " + sxm);
        }
    }
}