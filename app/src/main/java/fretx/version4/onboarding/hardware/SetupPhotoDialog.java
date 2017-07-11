package fretx.version4.onboarding.hardware;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import fretx.version4.R;

import static android.app.Activity.RESULT_OK;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 31/05/17 19:08.
 */

public class SetupPhotoDialog extends DialogFragment{
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Dialog dialog;
    private ImageView photo;

    public static SetupPhotoDialog newInstance(SetupListener listener) {
        final SetupPhotoDialog dialog = new SetupPhotoDialog();
        dialog.setTargetFragment((Fragment) listener, 4321);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.hardware_setup_photo_dialog);

        //set button listeners
        final TextView replay = (TextView) dialog.findViewById(R.id.replay);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((SetupListener) getTargetFragment()).onReplay();
            }
        });

        final TextView assistance = (TextView) dialog.findViewById(R.id.assistance);
        assistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SetupListener) getTargetFragment()).onAssist();
            }
        });

        final Button ready = (Button) dialog.findViewById(R.id.ready);
        ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((SetupListener) getTargetFragment()).onNext();
            }
        });

        photo = (ImageView) dialog.findViewById(R.id.photo);
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            photo.setVisibility(View.VISIBLE);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            });
        } else {
            photo.setVisibility(View.GONE);
        }

        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            photo.setImageBitmap(imageBitmap);

            final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            final FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference imageRef = storage.getReference().child(fUser.getUid() + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataBytes = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(dataBytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }
    }
}
