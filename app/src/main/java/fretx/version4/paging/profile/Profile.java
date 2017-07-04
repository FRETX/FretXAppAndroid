package fretx.version4.paging.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


import fretx.version4.R;
import fretx.version4.activities.ConnectivityActivity;
import fretx.version4.activities.LightActivity;
import fretx.version4.activities.LoginActivity;
import fretx.version4.activities.OnboardingActivity;
import fretx.version4.utils.Preference;
import fretx.version4.utils.Prefs;
import fretx.version4.utils.bluetooth.BluetoothAnimator;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UnreadConversationCountListener;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 19/05/17 10:35.
 */

public class Profile extends Fragment {
    private final static String TAG = "KJKP6_PROFILE";
    private TextView feedbackButton;
    private UnreadConversationCountListener unreadListener = new UnreadConversationCountListener() {
        @Override
        public void onCountUpdate(int nbUnread) {
            updateUnreadButton(nbUnread);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.paging_profile, container, false);

        final ImageView photo = (ImageView) rootView.findViewById(R.id.user_profile_photo);
        final TextView disconnectButton = (TextView) rootView.findViewById(R.id.disconnect_button);
        final TextView onboardingButton = (TextView) rootView.findViewById(R.id.onboarding_button);
        final TextView nameTextView = (TextView) rootView.findViewById(R.id.name_textview);
        final TextView emailTextView = (TextView) rootView.findViewById(R.id.email_textview);
        final Switch leftHandedSwitch = (Switch) rootView.findViewById(R.id.left_handed_switch);
        feedbackButton = (TextView) rootView.findViewById(R.id.feedback_button);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "user connected");
            nameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());
            final Uri url = user.getPhotoUrl();
            if (url != null) {
                Picasso.with(getActivity()).load(url).placeholder(R.drawable.defaultthumb).into(photo);
            }
            disconnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Intercom.client().reset();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            });
            onboardingButton.setVisibility(View.VISIBLE);
            onboardingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), OnboardingActivity.class);
                    startActivity(intent);
                }
            });
            final int nbUnread = Intercom.client().getUnreadConversationCount();
            updateUnreadButton(nbUnread);
            feedbackButton.setVisibility(View.VISIBLE);
            feedbackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intercom.client().displayMessenger();
                }
            });
        } else {
            Log.d(TAG, "anonymously connected");
            onboardingButton.setVisibility(View.GONE);
            feedbackButton.setVisibility(View.GONE);
            disconnectButton.setText("Login");
            disconnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (Preference.getInstance().isLeftHanded())
            leftHandedSwitch.setChecked(true);
        else
            leftHandedSwitch.setChecked(false);

        leftHandedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    final Prefs.Builder builder = new Prefs.Builder();
                    builder.setHand(Prefs.LEFT_HANDED);
                    Preference.getInstance().save(builder.build());
                } else {
                    final Prefs.Builder builder = new Prefs.Builder();
                    builder.setHand(Prefs.RIGHT_HANDED);
                    Preference.getInstance().save(builder.build());
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intercom.client().addUnreadConversationCountListener(unreadListener);
        BluetoothAnimator.getInstance().stringFall();
    }

    @Override
    public void onPause() {
        super.onPause();
        Intercom.client().removeUnreadConversationCountListener(unreadListener);
    }

    private void updateUnreadButton(int nbUnread) {
        if (nbUnread > 0) {
            feedbackButton.setText("Leave a feedback (" + nbUnread + ")");
        } else {
            feedbackButton.setText("Leave a message");
        }
    }
}
