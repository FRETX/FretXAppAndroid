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
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fretx.version4.R;
import fretx.version4.activities.LoginActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 17/05/17 14:50.
 */

public class Facebook extends Fragment {
    private final static String TAG = "KJKP6_FACEBOOK";

    private CallbackManager callbackManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_facebook, container, false);

        final Button otherButton = (Button) rootView.findViewById(R.id.other_button);
        otherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Other fragment = new Other();
                fragmentTransaction.replace(R.id.login_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        final LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.facebook_button);
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook login Success");

                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    ((LoginActivity)getActivity()).onLoginSuccess();
                                    Log.d(TAG, "firebase login success");
                                } else {
                                    Log.w(TAG, "firebase login failed", task.getException());
                                    LoginManager.getInstance().logOut();
                                    Toast.makeText(getActivity(), "login failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook login cancelled");
                Toast.makeText(getActivity(), "login cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook login failed: " + error.toString());
                Toast.makeText(getActivity(), "login failed", Toast.LENGTH_SHORT).show();
            }
        });

        final Button facebookOverlay = (Button) rootView.findViewById(R.id.facebook_button_overlay);
        facebookOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LoginActivity)getActivity()).isInternetAvailable()) {
                    loginButton.performClick();
                } else {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
