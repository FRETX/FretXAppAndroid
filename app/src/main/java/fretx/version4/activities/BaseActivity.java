package fretx.version4.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;

import fretx.version4.utils.audio.Audio;
import fretx.version4.utils.audio.Midi;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * FretXapp for FretX
 * Created by pandor on 14/04/17 15:00.
 */

public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "KJKP6_BASEACTIVITY";
    private static WeakReference<AppCompatActivity> activity = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "============================== ON CREATE ==============================");
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Set weak reference");
        activity = new WeakReference<AppCompatActivity>(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        Log.d(TAG, "========================= ON CREATE PERSISTABLE ========================");
        super.onCreate(savedInstanceState, persistentState);

        Log.d(TAG, "Set weak reference");
        activity = new WeakReference<AppCompatActivity>(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "============================== ON RESUME ==============================");
        super.onResume();

        Log.d(TAG, "Set weak reference");
        activity = new WeakReference<AppCompatActivity>(this);

        Midi.getInstance().start();
        Audio.getInstance().start();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "============================== ON PAUSE ===============================");
        super.onPause();

        Midi.getInstance().stop();
        Audio.getInstance().stop();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Log.d(TAG, "========================= ATTACH BASE CONTEXT =========================");
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public static AppCompatActivity getActivity() {
        return activity == null ? null : activity.get();
    }
}
