package fretx.version4.onboarding.login;

import android.os.Bundle;
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
 * Created by pandor on 17/05/17 14:53.
 */

public class Recover extends Fragment implements LoginFragnent {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_recover, container, false);

        final EditText emailEditText = (EditText) rootView.findViewById(R.id.email_signin_edittext);

        final Button recoverButton = (Button) rootView.findViewById(R.id.recover_button);
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = emailEditText.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                } else if (!((LoginActivity)getActivity()).isInternetAvailable()) {
                    ((LoginActivity)getActivity()).noInternetAccessDialod().show();
                } else {
                    Toast.makeText(getActivity(), "Recover password!", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onLoginFailure() {
    }
}
