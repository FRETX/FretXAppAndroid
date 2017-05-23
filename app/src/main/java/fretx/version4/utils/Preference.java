package fretx.version4.utils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/05/17 18:56.
 */

public class Preference {
    private final static String TAG = "KJKP6_ANALYTICS";

    public final static String LEFT_HANDED = "left";
    public final static String RIGHT_HANDED = "right";

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

    public boolean isLeftHanded() {
        return hand.equals(LEFT_HANDED);
    }
}
