package fretx.version4.onboarding.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;

import fretx.version4.R;
import fretx.version4.activities.LoginActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 17/05/17 14:50.
 */

public class Facebook extends Fragment implements LoginFragnent {
    private final static String TAG = "KJKP6_FACEBOOK";

    private LoginActivity activity;
    private CallbackManager callbackManager;
    private Button facebookOverlay;
    private TextView other;

    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "facebook login Success");
            AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
            activity.onServiceLoginSuccess(credential);
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "facebook login cancelled");
            buttonsClickable(true);
        }

        @Override
        public void onError(FacebookException error) {
            Log.d(TAG, "facebook login failed: " + error.toString());
            activity.onServiceLoginFailed("Facebook");
            buttonsClickable(true);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginManager.getInstance().logOut();
        activity = (LoginActivity) getActivity();
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_facebook, container, false);

        //goes to other kind of login
        other = (TextView) rootView.findViewById(R.id.other);
        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                final Other fragment = new Other();
                ((LoginActivity) getActivity()).setFragment(fragment);
                fragmentTransaction.replace(R.id.login_fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        //invisible facebook button
        final LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.facebook_button);
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, facebookCallback);

        //visible facebook buton
        facebookOverlay = (Button) rootView.findViewById(R.id.facebook_button_overlay);
        facebookOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LoginActivity)getActivity()).isInternetAvailable()) {
                    buttonsClickable(false);
                    loginButton.performClick();
                } else {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                }
            }
        });

        return rootView;
    }

    private void buttonsClickable(boolean clickable) {
        facebookOverlay.setClickable(clickable);
        other.setClickable(clickable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onLoginFailure() {
        LoginManager.getInstance().logOut();
        buttonsClickable(true);
    }
}
