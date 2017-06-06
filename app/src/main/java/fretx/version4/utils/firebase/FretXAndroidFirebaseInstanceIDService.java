package fretx.version4.utils.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Kickdrum on 13-Mar-17.
 */

public class FretXAndroidFirebaseInstanceIDService extends FirebaseInstanceIdService {
	public static final String TAG = "FirebaseIIDService";

	@Override
	public void onTokenRefresh(){
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed token: " + refreshedToken);

		// If you want to send messages to this application instance or
		// manage this apps subscriptions on the server side, send the
		// Instance ID token to your app server.
//		sendRegistrationToServer(refreshedToken);

	}

	private void sendRegistrationToServer(String token) {
		//You can implement this method to store the token on your server
		//Not required for current project
	}

}
