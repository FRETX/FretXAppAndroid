package fretx.version4.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fretx.version4.Config;
import fretx.version4.R;
import fretx.version4.login.Facebook;
import fretx.version4.login.LoginFragnent;
import fretx.version4.login.User;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UserAttributes;
import io.intercom.android.sdk.identity.Registration;

public class LoginActivity extends BaseActivity {
    private final static String TAG = "KJKP6_LOGIN_ACT";
    private Fragment fragment;
    private Button skip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new Facebook();
        fragmentTransaction.add(R.id.login_fragment_container, fragment);
        fragmentTransaction.commit();

        skip = (Button) findViewById(R.id.skip_login);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        if (!isInternetAvailable()) {
            skip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack();

            fragment = getSupportFragmentManager().findFragmentById(R.id.login_fragment_container);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public AlertDialog noInternetAccessDialod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Access");
        builder.setMessage("Intenet access is required to login");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        skip.setVisibility(View.VISIBLE);
        return builder.create();
    }

    //// TODO: 18/05/17 move this to Network class
    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onServiceLoginSuccess(AuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "firebase login success");
                            onFirebaseLoginSuccess();
                        } else {
                            Log.w(TAG, "firebase login failed", task.getException());
                            Toast.makeText(getActivity(), "firebase login failed", Toast.LENGTH_SHORT).show();
                            ((LoginFragnent) fragment).onLoginFailure();
                        }
                    }
                });
    }

    public void onServiceLoginFailed(String serviceName) {
        Toast.makeText(this, serviceName + " login failed", Toast.LENGTH_SHORT).show();
    }

    public void onFirebaseLoginSuccess() {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

                //intercom
                Registration registration = Registration.create().withUserId(fUser.getUid());
                Intercom.client().registerIdentifiedUser(registration);
                UserAttributes userAttributes = new UserAttributes.Builder()
                        .withName(fUser.getDisplayName())
                        .withEmail(fUser.getEmail())
                        .build();
                Intercom.client().updateUser(userAttributes);

                //preferences
                if (Config.onboarding && dataSnapshot.child("users").child(fUser.getUid()).getValue(User.class) == null) {
                    Intent intent = new Intent(getActivity(), OnboardingActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                   startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onUserCreation(final String name, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User profile updated");
                                                } else {
                                                    Log.d(TAG, "User profile update failed");
                                                }
                                            }
                                        });
                                onFirebaseLoginSuccess();
                            } else {
                                Log.d(TAG, "user creation failed");
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "User creation failed.", Toast.LENGTH_SHORT).show();
                            ((LoginFragnent) fragment).onLoginFailure();
                        }
                    }
                });
    }
}