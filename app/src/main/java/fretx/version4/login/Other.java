package fretx.version4.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.activities.LoginActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 17/05/17 14:50.
 */

public class Other extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "KJKP6_OTHER";

    private FirebaseAuth mAuth;

    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 666;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_other, container, false);

        final EditText emailEditText = (EditText) rootView.findViewById(R.id.email_edittext);
        final EditText passwordEditText = (EditText) rootView.findViewById(R.id.password_edittext);

        final Button loginButton = (Button) rootView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "signInWithEmail:success");
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    }
                                }
                            });
                }

                if (((LoginActivity)getActivity()).isInternetAvailable()) {
                    Toast.makeText(getActivity(), "Logged in!", Toast.LENGTH_SHORT).show();
                } else {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                }
            }
        });

        final Button recoverButton = (Button) rootView.findViewById(R.id.recover_button);
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Recover fragment = new Recover();
                fragmentTransaction.replace(R.id.login_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        final Button forgotButton = (Button) rootView.findViewById(R.id.register_button);
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Register fragment = new Register();
                fragmentTransaction.replace(R.id.login_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // Set the dimensions of the sign-in button.
        final SignInButton signInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LoginActivity)getActivity()).isInternetAvailable()) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed");
        Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.d(TAG, "Authentication result: success.");
                Toast.makeText(BaseActivity.getActivity(), "Authentication result: success.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Authentication result: failed");
                Log.d(TAG, "Authentication result: " + result.getStatus().getStatusMessage());
                Log.d(TAG, "Authentication result: " + result.getStatus().getStatusCode());
                Toast.makeText(BaseActivity.getActivity(), "Authentication result: failure.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
