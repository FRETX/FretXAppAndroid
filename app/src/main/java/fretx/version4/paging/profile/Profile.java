package fretx.version4.paging.profile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import info.hoang8f.android.segmented.SegmentedGroup;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UnreadConversationCountListener;
import io.intercom.android.sdk.blocks.models.Image;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 19/05/17 10:35.
 */

public class Profile extends Fragment {
    private final static String TAG = "KJKP6_PROFILE";
    private TextView message;
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

        final ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        final TextView name = (TextView) rootView.findViewById(R.id.name);
        final SegmentedGroup hand = (SegmentedGroup) rootView.findViewById(R.id.hand);
        final RadioButton left = (RadioButton) rootView.findViewById(R.id.left);
        final RadioButton right = (RadioButton) rootView.findViewById(R.id.right);
        final SegmentedGroup guitar = (SegmentedGroup) rootView.findViewById(R.id.guitar);
        final RadioButton classical = (RadioButton) rootView.findViewById(R.id.classical);
        final RadioButton electric = (RadioButton) rootView.findViewById(R.id.electric);
        final RadioButton acoustic = (RadioButton) rootView.findViewById(R.id.acoustic);
        final SegmentedGroup level = (SegmentedGroup) rootView.findViewById(R.id.level);
        final RadioButton beginner = (RadioButton) rootView.findViewById(R.id.beginner);
        final RadioButton player = (RadioButton) rootView.findViewById(R.id.player);
        final SegmentedGroup preview = (SegmentedGroup) rootView.findViewById(R.id.preview);
        final RadioButton previewOn = (RadioButton) rootView.findViewById(R.id.previewOn);
        final RadioButton previewOff = (RadioButton) rootView.findViewById(R.id.previewOff);
        final TextView setup = (TextView) rootView.findViewById(R.id.setup);
        final TextView upgrade = (TextView) rootView.findViewById(R.id.upgrade);
        final TextView message = (TextView) rootView.findViewById(R.id.message);
        final TextView signout = (TextView) rootView.findViewById(R.id.signout);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "user connected");
            name.setText(user.getDisplayName());
            final Uri url = user.getPhotoUrl();
            if (url != null) {
                Picasso.with(getActivity()).load(url).placeholder(R.drawable.defaultthumb).into(photo);
            }
            signout.setVisibility(View.VISIBLE);

        }

        /* HAND */
        if (Preference.getInstance().isLeftHanded()) {
            left.setChecked(true);
            right.setChecked(false);
        } else {
            left.setChecked(false);
            right.setChecked(true);
        }
        hand.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                final Prefs.Builder builder = new Prefs.Builder();
                switch (checkedId) {
                    case R.id.left:
                        builder.setHand(Prefs.LEFT_HANDED);
                        break;
                    case R.id.right:
                        builder.setHand(Prefs.RIGHT_HANDED);
                        break;
                }
                Preference.getInstance().save(builder.build());
            }
        });

        /* GUITAR */
        if (Preference.getInstance().isClassicalGuitar()) {
            classical.setChecked(true);
            electric.setChecked(false);
            acoustic.setChecked(false);
        } else if (Preference.getInstance().isElectricGuitar()) {
            classical.setChecked(false);
            electric.setChecked(true);
            acoustic.setChecked(false);
        } else {
            classical.setChecked(false);
            electric.setChecked(false);
            acoustic.setChecked(true);
        }
        guitar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                final Prefs.Builder builder = new Prefs.Builder();
                switch (checkedId) {
                    case R.id.classical:
                        builder.setGuitar(Prefs.CLASSICAL_GUITAR);
                        break;
                    case R.id.electric:
                        builder.setGuitar(Prefs.ELECTRIC_GUITAR);
                        break;
                    case R.id.acoustic:
                        builder.setGuitar(Prefs.ACCOUSTIC_GUITAR);
                        break;
                }
                Preference.getInstance().save(builder.build());
            }
        });

        /* LEVEL */
        if (Preference.getInstance().isBeginner()) {
            beginner.setChecked(true);
            player.setChecked(false);
        } else {
            beginner.setChecked(false);
            player.setChecked(true);
        }
        level.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                final Prefs.Builder builder = new Prefs.Builder();
                switch (checkedId) {
                    case R.id.beginner:
                        builder.setLevel(Prefs.LEVEL_BEGINNER);
                        break;
                    case R.id.player:
                        builder.setLevel(Prefs.LEVEL_PLAYER);
                        break;
                }
                Preference.getInstance().save(builder.build());
            }
        });

        /* PREVIEW */
        if (Preference.getInstance().isSongPreview()) {
            previewOn.setChecked(true);
        } else {
            previewOff.setChecked(false);
        }
        preview.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                final Prefs.Builder builder = new Prefs.Builder();
                switch (checkedId) {
                    case R.id.previewOn:
                        builder.setSongPreview(Prefs.SONG_PREVIEW);
                        break;
                    case R.id.previewOff:
                        builder.setSongPreview(Prefs.NO_SONG_PREVIEW);
                        break;
                }
                Preference.getInstance().save(builder.build());
            }
        });

        /* SETUP */
        setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), OnboardingActivity.class);
                startActivity(intent);
            }
        });

        /* UPGRADE */
        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://store.fretx.rocks/"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        /* MESSAGE */
        final int nbUnread = Intercom.client().getUnreadConversationCount();
        updateUnreadButton(nbUnread);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intercom.client().displayMessenger();
            }
        });

            /* SIGNOUT */
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intercom.client().reset();
                final Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
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
        if (message == null)
            return;
        if (nbUnread > 0) {
            message.setText("Leave us a message (" + nbUnread + ")");
        } else {
            message.setText("Leave us a message");
        }
    }
}
