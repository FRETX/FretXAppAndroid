package fretx.version4.utils.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;

/**
 * Created by Kickdrum on 13-Mar-17.
 */

public class FretXAndroidFirebaseMessagingService extends FirebaseMessagingService{
	private final static String TAG = "FirebaseMsgService";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		//Displaying data in log
		//It is optional
		Log.d(TAG, "From: " + remoteMessage.getFrom());
		Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
		Log.d(TAG, "Message type: " + remoteMessage.getMessageType());
		Map<String,String> messageData = remoteMessage.getData();
		String action = messageData.get("action");

		//Calling method to generate notification
		sendNotification(remoteMessage.getNotification().getBody(),action);
	}

	//This method is only generating push notification
	//It is same as we did in earlier posts
	private void sendNotification(String messageBody, String action) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_ONE_SHOT);
//		if(action != null){
		if(true){
			if(action.toLowerCase().equals("update")){
				Log.d(TAG,"This is an update notif!");
				String appPackageName = getPackageName();
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
			}
		}


		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.fretx_icon)
				.setContentTitle("FretX")
				.setContentText(messageBody)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0, notificationBuilder.build());
	}
}
