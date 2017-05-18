package fretx.version4.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import fretx.version4.R;
import fretx.version4.onboarding.Guitar;
import fretx.version4.onboarding.Hand;
import fretx.version4.onboarding.Level;

public class OnboardingActivity extends BaseActivity {

    private int state;
    private SeekBar seekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final Guitar fragment = new Guitar();
        fragmentTransaction.add(R.id.onboarding_fragment_container, fragment);
        fragmentTransaction.commit();

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        final Button next = (Button) findViewById(R.id.next_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager;
                final FragmentTransaction fragmentTransaction;
                final Fragment fragment;
                switch (state) {
                    case 0:
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragment = new Hand();
                        fragmentTransaction.replace(R.id.onboarding_fragment_container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    case 1:
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragment = new Level();
                        fragmentTransaction.replace(R.id.onboarding_fragment_container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    case 2:
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        break;
                }
                ++state;
                seekBar.setProgress(state);
            }
        });
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
