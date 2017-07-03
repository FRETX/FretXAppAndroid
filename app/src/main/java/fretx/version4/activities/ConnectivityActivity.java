package fretx.version4.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import fretx.version4.R;
import fretx.version4.onboarding.hardware.Check;

import static fretx.version4.activities.BaseActivity.getActivity;

public class ConnectivityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final Fragment fragment = new Check();
        ((HardwareActivity) getActivity()).setFragment(fragment);
        fragmentTransaction.replace(R.id.connectivity_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
    }
}
