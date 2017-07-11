package fretx.version4.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import fretx.version4.R;
import fretx.version4.onboarding.hardware.HardwareFragment;
import fretx.version4.onboarding.hardware.Setup;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 10:19.
 */

public class HardwareActivity extends BaseActivity {
    private HardwareFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware);

        final Bundle b = getIntent().getExtras();
        if(b != null) {
            int start = b.getInt("start");
            launchSetup(start);
        } else {
            launchSetup(0);
        }
    }

    public void setFragment(HardwareFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onBackPressed() {
        fragment.onBackPressed();
    }

    private void launchSetup(int start) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new Setup();
        fragment.setStart(start);
        fragmentTransaction.add(R.id.hardware_container, (Fragment) fragment);
        fragmentTransaction.commit();
    }
}
