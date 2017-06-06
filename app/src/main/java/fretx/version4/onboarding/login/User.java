package fretx.version4.onboarding.login;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 19/05/17 16:18.
 */

public class User {
    private final static String TAG = "KJKP6_USER";

    public String guitar;
    public String hand;
    public String level;

    public User() {
    }

    public User(String guitar, String hand, String level) {
        this.guitar = guitar;
        this.hand = hand;
        this.level = level;
    }
}
