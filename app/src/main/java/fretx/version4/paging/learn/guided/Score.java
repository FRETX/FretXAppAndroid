package fretx.version4.paging.learn.guided;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 06/06/17 12:11.
 */

public class Score {
    public String score;

    public Score() {};

    public Score(int scoreValue) {
        this.score = Integer.toString(scoreValue);
    }

    public void add(int scoreValue) {
        this.score += " " + Integer.toString(scoreValue);
    }
}