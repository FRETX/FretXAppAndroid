package fretx.version4.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fretx.version4.R;
import fretx.version4.login.User;
import fretx.version4.onboarding.Guitar;
import fretx.version4.onboarding.Hand;
import fretx.version4.onboarding.Level;

public class OnboardingActivity extends BaseActivity {
    private Fragment fragment;
    private int state;
    private SeekBar seekBar;

    private String guitar;
    private String hand;
    private String level;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new Guitar();
        fragmentTransaction.add(R.id.onboarding_fragment_container, fragment);
        fragmentTransaction.commit();

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        final Button next = (Button) findViewById(R.id.next_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager;
                final FragmentTransaction fragmentTransaction;
                final RadioGroup group;

                switch (state) {
                    case 0:
                        group = (RadioGroup) findViewById(R.id.radioGroup);
                        switch (group.getCheckedRadioButtonId()) {
                            case R.id.electricRadio:
                                guitar = "electric";
                                break;
                            case R.id.acousticRadio:
                                guitar = "acoustic";
                                break;
                            case R.id.classicalRadio:
                                guitar = "classical";
                                break;
                            default:
                                return;
                        }

                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragment = new Hand();
                        fragmentTransaction.replace(R.id.onboarding_fragment_container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    case 1:
                        group = (RadioGroup) findViewById(R.id.radioGroup);
                        switch (group.getCheckedRadioButtonId()) {
                            case R.id.leftRadio:
                                hand = "left";
                                break;
                            case R.id.rightRadio:
                                hand = "right";
                                break;
                            default:
                                return;
                        }

                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragment = new Level();
                        fragmentTransaction.replace(R.id.onboarding_fragment_container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    case 2:
                        group = (RadioGroup) findViewById(R.id.radioGroup);
                        switch (group.getCheckedRadioButtonId()) {
                            case R.id.beginnerRadio:
                                level = "beginner";
                                break;
                            case R.id.playerRadio:
                                level = "player";
                                break;
                            default:
                                return;
                        }

                        saveData();

                        final Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        break;
                }
                ++state;
                seekBar.setProgress(state);
            }
        });
    }

    private void saveData() {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("users").child(fUser.getUid()).setValue(new User(guitar, hand, level));
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            --state;
            seekBar.setProgress(state);
        }
    }
}
