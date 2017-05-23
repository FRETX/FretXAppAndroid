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
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.activities.LoginActivity;
import io.fabric.sdk.android.Fabric;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 17/05/17 14:50.
 */

public class Other extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "KJKP6_OTHER";

    private Button loginButton;
    private Button recoverButton;
    private Button forgotButton;
    private SignInButton googleButton;
    private TwitterLoginButton twitterButton;
    private Button twitterOverlay;

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

        TwitterAuthConfig authConfig = new TwitterAuthConfig("OUWR4SvbsSYZAVdlJaLWCQ9Jw",
                "wz7H5TUVwPHIgFNxHcpbRqDqYl9WYVIf0ByNaqtqGZOfUy268B");
        Fabric.with(getActivity(), new Twitter(authConfig));

        if (mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                        }
                    });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_other, container, false);

        final EditText emailEditText = (EditText) rootView.findViewById(R.id.email_edittext);
        final EditText passwordEditText = (EditText) rootView.findViewById(R.id.password_edittext);

        loginButton = (Button) rootView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                } else if (!((LoginActivity)getActivity()).isInternetAvailable()) {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "email login success");
                                        ((LoginActivity)getActivity()).onLoginSuccess();
                                    } else {
                                        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                                        Log.w(TAG, "email login failure", task.getException());
                                    }
                                }
                            });
                }
            }
        });

        recoverButton = (Button) rootView.findViewById(R.id.recover_button);
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Recover fragment = new Recover();
                ((LoginActivity) getActivity()).setFragment(fragment);
                fragmentTransaction.replace(R.id.login_fragment_container, fragment, LoginActivity.RECOVER_TAG);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        forgotButton = (Button) rootView.findViewById(R.id.register_button);
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Register fragment = new Register();
                ((LoginActivity) getActivity()).setFragment(fragment);
                fragmentTransaction.replace(R.id.login_fragment_container, fragment, LoginActivity.REGISTER_TAG);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // Set the dimensions of the sign-in button.
        googleButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LoginActivity)getActivity()).isInternetAvailable()) {
                    buttonsClickable(false);
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                }
            }
        });

        twitterButton = (TwitterLoginButton) rootView.findViewById(R.id.twitter_button);
        twitterButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.v(TAG, "Twitter success");

                final TwitterSession session = result.data;

                AuthCredential credential = TwitterAuthProvider.getCredential(
                        session.getAuthToken().token,
                        session.getAuthToken().secret);

                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "firebase login success");
                                    ((LoginActivity)getActivity()).onLoginSuccess();
                                } else {
                                    Log.d(TAG, "firebase login failed");
                                    buttonsClickable(true);
                                }
                            }
                        });

            }

            @Override
            public void failure(TwitterException exception) {
                Log.v(TAG, "Twitter failed");
                Log.d(TAG, "Twitter login failed");
                buttonsClickable(true);
            }
        });

        twitterOverlay = (Button) rootView.findViewById(R.id.twitter_overlay);
        twitterOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LoginActivity)getActivity()).isInternetAvailable()) {
                    buttonsClickable(false);
                    twitterButton.performClick();
                } else {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                }
            }
        });

        return rootView;
    }

    private void buttonsClickable(boolean clickable) {
        loginButton.setClickable(clickable);
        recoverButton.setClickable(clickable);
        forgotButton.setClickable(clickable);
        googleButton.setClickable(clickable);
        twitterOverlay.setClickable(clickable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
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
                Log.d(TAG, "google login succeed.");

                //google credential
                AuthCredential credential = GoogleAuthProvider.getCredential(result.getSignInAccount().getIdToken(), null);

                mAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "firebase login success");
                            ((LoginActivity)getActivity()).onLoginSuccess();
                        } else {
                            if (mGoogleApiClient.isConnected()) {
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(@NonNull Status status) {
                                            }
                                        });
                            }
                            buttonsClickable(true);
                            Toast.makeText(getActivity(), "firebase login failed", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "firebase login failed", task.getException());
                        }
                    }
                });
            } else {
                buttonsClickable(true);
                Log.d(TAG, "google login failed (code: " + result.getStatus().getStatusCode() + ")");
                Toast.makeText(BaseActivity.getActivity(), "google login failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.v(TAG, "Twitter on activity result");
            twitterButton.onActivityResult(requestCode, resultCode, data);
        }
    }
}
