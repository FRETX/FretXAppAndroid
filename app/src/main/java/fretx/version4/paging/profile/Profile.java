package fretx.version4.paging.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


import fretx.version4.R;
import fretx.version4.activities.LoginActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 19/05/17 10:35.
 */

public class Profile extends Fragment {
    private final static String TAG = "KJKP6_PROFILE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_profile, container, false);

        final ImageView photo = (ImageView) rootView.findViewById(R.id.user_profile_photo);
        final TextView disconnectButton = (TextView) rootView.findViewById(R.id.disconnect_button);
        final TextView nameTextView = (TextView) rootView.findViewById(R.id.name_textview);
        final TextView emailTextView = (TextView) rootView.findViewById(R.id.email_textview);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "user connected");
            nameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());
        } else {
            Log.d(TAG, "anonymously connected");
            disconnectButton.setVisibility(View.GONE);
        }

        final Uri url = user.getPhotoUrl();
        if (url != null)
            Picasso.with(getActivity()).load(url).placeholder(R.drawable.defaultthumb).into(photo);

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
