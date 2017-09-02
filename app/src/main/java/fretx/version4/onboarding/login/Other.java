package fretx.version4.onboarding.login;

import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

import fretx.version4.R;
import fretx.version4.activities.LoginActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 17/05/17 14:50.
 */

public class Other extends Fragment implements GoogleApiClient.OnConnectionFailedListener, LoginFragnent {
    private static final String TAG = "KJKP6_OTHER";


    private LoginActivity activity;
    private Button loginButton;
    private TextView recoverButton;
    private TextView forgotButton;
    private SignInButton googleButton;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 666;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (LoginActivity) getActivity();
        //google setup
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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

        final EditText emailEditText = (EditText) rootView.findViewById(R.id.email_signin_edittext);
        final EditText passwordEditText = (EditText) rootView.findViewById(R.id.password_signin_edittext);

        loginButton = (Button) rootView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                } else if (!((LoginActivity)getActivity()).isInternetAvailable()) {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    activity.onServiceLoginSuccess(credential);
                }
            }
        });

        recoverButton = (TextView) rootView.findViewById(R.id.recover);
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Recover fragment = new Recover();
                ((LoginActivity) getActivity()).setFragment(fragment);
                fragmentTransaction.replace(R.id.login_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        forgotButton = (TextView) rootView.findViewById(R.id.register);
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Register fragment = new Register();
                ((LoginActivity) getActivity()).setFragment(fragment);
                fragmentTransaction.replace(R.id.login_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // Set the dimensions of the sign-in button.
        googleButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (((LoginActivity)getActivity()).isInternetAvailable()) {
                    onLoginStart();
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
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google connection failed");
        activity.onServiceLoginFailed("Google");
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
                activity.onServiceLoginSuccess(credential);
            } else {
                onLoginFailure();
                Log.d(TAG, "google login failed (code: " + result.getStatus().getStatusCode() + ")");
                activity.onServiceLoginFailed("Google");
            }
        }
    }

    private void onLoginStart() {
        buttonsClickable(false);
    }

    public void onLoginFailure(){
        buttonsClickable(true);
    }

    private void buttonsClickable(boolean clickable) {
        loginButton.setClickable(clickable);
        recoverButton.setClickable(clickable);
        forgotButton.setClickable(clickable);
        googleButton.setClickable(clickable);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
