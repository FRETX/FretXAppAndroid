package fretx.version4.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import fretx.version4.R;
import fretx.version4.hardware.Check;
import fretx.version4.hardware.HardwareFragment;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 10:19.
 */

public class HardwareActivity extends BaseActivity{
    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new Check();
        fragmentTransaction.add(R.id.hardware_container, fragment);
        fragmentTransaction.commit();
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onBackPressed() {
        ((HardwareFragment) fragment).onBackPressed();
    }
}
