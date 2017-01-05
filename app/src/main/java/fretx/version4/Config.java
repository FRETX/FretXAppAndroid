package fretx.version4;

import fretx.version4.activities.MainActivity;

/**
 * Created by echessa on 7/17/15.
 */
public final class Config {

    private Config() {
    }

    public static final String YOUTUBE_API_KEY = "AIzaSyAhxy0JS9M_oaDMW_bJMPyoi9R6oILFjNs";
    public static boolean bBlueToothActive = false;
    public static MainActivity mActivity = null;
    public static final String apiBase = "http://staging.fretx.rocks/";
    public static final boolean useOfflinePlayer = true;
}