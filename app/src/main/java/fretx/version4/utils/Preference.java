package fretx.version4.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import fretx.version4.activities.BaseActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/05/17 18:56.
 */

public class Preference {
    private final static String TAG = "KJKP6_PREFERENCE";
    private final static String FILENAME = "preferences.json";
    private DatabaseReference mDatabasePrefs;
    private Prefs prefs;
    private FirebaseUser user;

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

    public void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mDatabasePrefs = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("prefs");
        }
        load();
    }

    private boolean localSave(Prefs prefs) {
        Log.d(TAG,"preferences local save");
        final FileOutputStream outputStream;
        try {
            outputStream = BaseActivity.getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(prefs.toJson().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void remoteSave(Prefs prefs) {
        Log.d(TAG,"preferences remote save");
        mDatabasePrefs.setValue(prefs);
    }

    public void save(Prefs prefs) {
        this.prefs = prefs;
        if (user != null) {
            remoteSave(prefs);
        }
        localSave(prefs);
    }

    private Prefs localLoad() {
        final String jsonSave;
        try {
            final FileInputStream fis = BaseActivity.getActivity().openFileInput(FILENAME);
            final InputStreamReader isr = new InputStreamReader(fis);
            final BufferedReader bufferedReader = new BufferedReader(isr);
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            isr.close();
            fis.close();
            jsonSave = sb.toString();
        } catch (IOException e) {
            Log.v(TAG, "local save retrieval failed");
            e.printStackTrace();
            return null;
        }
        Log.v(TAG, "local save retrieval succeeded: >" + jsonSave + "<");
        return Prefs.fromJson(jsonSave);
    }

    private void remoteLoad() {
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Prefs remotePrefs = dataSnapshot.getValue(Prefs.class);
                if (remotePrefs != null) {
                    prefs = remotePrefs;
                    Log.w(TAG, "remote prefs retrieval succeeded");
                    Log.w(TAG, "hand: " + prefs.hand);
                    Log.w(TAG, "guitar: " + prefs.guitar);
                    Log.w(TAG, "level: " + prefs.level);
                    Log.w(TAG, "tunerTuto: " + prefs.tunerTutorial);
                    Log.w(TAG, "previewTuto: " + prefs.previewTutorial);
                    Log.w(TAG, "playTuto: " + prefs.playTutorial);
                    Log.w(TAG, "songPreview: " + prefs.songPreview);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "remote prefs retrieval failed", databaseError.toException());
            }
        };
        mDatabasePrefs.addListenerForSingleValueEvent(listener);
    }

    public void load() {
        prefs = localLoad();
        if (prefs == null) {
            Log.v(TAG, "using default prefs");
            prefs = new Prefs();
        }
        if (user != null) {
            remoteLoad();
        }
    }

    public Prefs getPrefsCopy() {
        if (prefs == null)
            return new Prefs();
        return new Prefs(prefs);
    }

    public boolean isLeftHanded(){return prefs.hand.equals(Prefs.LEFT_HANDED);}
    public boolean isClassicalGuitar(){return prefs.guitar.equals(Prefs.CLASSICAL_GUITAR);}
    public boolean isElectricGuitar(){return prefs.guitar.equals(Prefs.ELECTRIC_GUITAR);}
    public boolean isAcousticGuitar(){return prefs.guitar.equals(Prefs.ACCOUSTIC_GUITAR);}
    public boolean isSongPreview() {return prefs.songPreview.equals("true");}
    public boolean isBeginner() {return prefs.level.equals(Prefs.LEVEL_BEGINNER);}
    public boolean isPlayer() {return prefs.level.equals(Prefs.LEVEL_PLAYER);}

    public boolean needTunerTutorial() {return prefs.tunerTutorial.equals("true");}
    public boolean needPreviewTutorial() {return prefs.previewTutorial.equals("true");}
    public boolean needPlayTutorial() {return prefs.playTutorial.equals("true");}
}
