package fretx.version4.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import fretx.version4.R;
import fretx.version4.onboarding.login.User;
import fretx.version4.onboarding.userInfo.Guitar;
import fretx.version4.onboarding.userInfo.Hand;
import fretx.version4.onboarding.userInfo.Level;
import fretx.version4.utils.Preference;
import fretx.version4.utils.Prefs;
import fretx.version4.utils.firebase.FirebaseConfig;

public class OnboardingActivity extends BaseActivity {
    private Fragment fragment;
    private int state;
    private SeekBar seekBar;
    private TextView title;

    private String guitar;
    private String hand;
    private String level;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        title = (TextView) findViewById(R.id.title);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        final String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (displayName != null) {
            final TextView name = (TextView) findViewById(R.id.name);
            name.setText("Hi " + displayName);
        }

        updateState();

        final Button next = (Button) findViewById(R.id.next_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        ++state;
                        updateState();
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
                        ++state;
                        updateState();
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

                        if (!FirebaseConfig.getInstance().isHardwareSetupSkipable()) {
                            final Intent intent = new Intent(getActivity(), HardwareIntroActivity.class);
                            startActivity(intent);
                        } else {
                            final Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        }
                        break;
                }
            }
        });
    }

    private void saveData() {
        Prefs.Builder builder = new Prefs.Builder();
        builder.setGuitar(guitar).setHand(hand).setLevel(level);
        Preference.getInstance().save(builder.build());
    }

    @Override
    public void onBackPressed() {
        if (state > 0 && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            --state;
            //updateState();
        }
    }

    private void updateState() {
        seekBar.setProgress(state);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (state) {
            case 0:
                title.setText("What kind of guitar do you have?");
                fragment = new Guitar();
                fragmentTransaction.replace(R.id.onboarding_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 1:
                title.setText("Are you a LEFT or RIGHT-HANDED?");
                fragment = new Hand();
                fragmentTransaction.replace(R.id.onboarding_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 2:
                title.setText("What is your level skill?");
                fragment = new Level();
                fragmentTransaction.replace(R.id.onboarding_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
        }
    }
}
