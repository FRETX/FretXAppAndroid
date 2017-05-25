package fretx.version4.utils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/05/17 18:56.
 */

public class Preference {
    private final static String TAG = "KJKP6_ANALYTICS";

    public final static String LEFT_HANDED = "left";
    public final static String RIGHT_HANDED = "right";
    public final static String ACCOUSTIC_GUITAR = "acoustic";
    public final static String ELECTRIC_GUITAR = "electric";
    public final static String CLASSICAL_GUITAR = "classical";
    public final static String LEVEL_BEGINNER = "beginner";
    public final static String LEVEL_PLAYER = "player";

    private String guitar;
    private String hand;
    private String level;

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Preference instance = new Preference();
    }

    private Preference() {
    }

    public static Preference getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = FIELDS = = = = = = = = = = = = = = = = = = = */

    public void init(String guitar, String hand, String level) {
        this.guitar = guitar;
        this.hand = hand;
        this.level = level;
    }

    public void setHand(String hand) {
        this.hand = hand;
    }

    public boolean isLeftHanded() {
        return hand.equals(LEFT_HANDED);
    }
}
