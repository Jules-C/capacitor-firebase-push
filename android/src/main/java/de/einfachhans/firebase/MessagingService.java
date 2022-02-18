package de.einfachhans.firebase;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

  private static final String TAG = "FirebasePushPlugin";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived");
      Log.d(TAG, "myFirebaseMessagingService - onMessageReceived - message: " + remoteMessage);

      //Intent dialogIntent = new Intent(this, NotificationActivity.class);
      //dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      //dialogIntent.putExtra("msg", remoteMessage);
      //startActivity(dialogIntent);
      startCallService();
        //FirebasePushPlugin.onNewRemoteMessage(remoteMessage);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startCallService() {
      Intent intent = new Intent(this, IncomingCallService.class);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent);
      }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
      Log.e("Refreshed token:",token);
        FirebasePushPlugin.onNewToken(token);
    }
}
