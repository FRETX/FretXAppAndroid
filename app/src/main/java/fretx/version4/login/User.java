package fretx.version4.login;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 19/05/17 16:18.
 */

public class User {
    public String guitar;
    public String hand;
    public String level;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String guitar, String hand, String level) {
        this.guitar = guitar;
        this.hand = hand;
        this.level = level;
    }
}
