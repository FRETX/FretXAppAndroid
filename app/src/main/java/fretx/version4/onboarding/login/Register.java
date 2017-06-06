package fretx.version4.onboarding.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import fretx.version4.R;
import fretx.version4.activities.LoginActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 17/05/17 14:51.
 */

public class Register extends Fragment implements LoginFragnent {
    private final static String TAG = "KJKP6_REGISTER";
    private LoginActivity activity;
    private Button registerButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (LoginActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_register, container, false);

        final EditText nameEditText = (EditText) rootView.findViewById(R.id.name_edittext);
        final EditText emailEditText = (EditText) rootView.findViewById(R.id.email_signin_edittext);
        final EditText passwordEditText = (EditText) rootView.findViewById(R.id.password_signin_edittext);

        registerButton = (Button) rootView.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameEditText.getText().toString();
                final String email = emailEditText.getText().toString();
                final String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                } else if (!((LoginActivity)getActivity()).isInternetAvailable()) {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                } else {
                    registerButton.setClickable(false);
                    activity.onUserCreation(name, email, password);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onLoginFailure() {
        registerButton.setClickable(true);
    }
}
