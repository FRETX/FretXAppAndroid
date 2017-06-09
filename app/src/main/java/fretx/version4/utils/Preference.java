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
    private final static String TAG = "KJKP6_ANALYTICS";
    private final static String FILENAME = "preferences.json";
    private DatabaseReference mDatabasePrefs;
    private Prefs prefs;

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
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabasePrefs = FirebaseDatabase.getInstance().getReference().child("users").child(fUser.getUid()).child("prefs");

        //retrieve local save
        prefs = load();
        if (prefs == null)
            prefs = new Prefs();
        //retrieve remote save
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                prefs = dataSnapshot.getValue(Prefs.class);
                save(prefs);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "prefs retrieval failed", databaseError.toException());
            }
        };
        mDatabasePrefs.addListenerForSingleValueEvent(listener);
    }

    public void save(Prefs prefs) {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null)
            return;

        //remote save
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(fUser.getUid()).child("prefs").setValue(prefs);
        //local save
        final FileOutputStream outputStream;
        try {
            outputStream = BaseActivity.getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(prefs.toJson().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Prefs load() {
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
            e.printStackTrace();
            return null;
        }

        return Prefs.fromJson(jsonSave);
    }

    public Prefs getPrefsCopy() {
        return new Prefs(prefs);
    }

    public boolean isLeftHanded(){
        return prefs.hand.equals(Prefs.LEFT_HANDED);
    }
}
