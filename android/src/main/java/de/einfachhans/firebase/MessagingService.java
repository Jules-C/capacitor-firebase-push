package de.einfachhans.firebase;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

  private static final String TAG = "PushPluginMsgService";

  // VoIP
  private static final String CHANNEL_VOIP = "Voip";
  private static final String CHANNEL_NAME = "TCVoip";
  private BroadcastReceiver voipNotificationActionBR;
  public static final int VOIP_NOTIFICATION_ID = 168697;
  public static final int oneTimeID = (int) SystemClock.uptimeMillis();

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Log.d(TAG, "onMessageReceived");
    Log.d(TAG, "myFirebaseMessagingService - onMessageReceived - message: " + remoteMessage);
    try {
      Log.d(TAG, "From: " + remoteMessage.getFrom());

      // Check if message contains a data payload.
      if (remoteMessage.getData().size() > 0) {
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        showVOIPNotification(remoteMessage.getData());
        //startActivity(intentForLaunchActivity());
      }
      // TODO: Add "type" to payload
      // String notifDataType = remoteMessage.getData().get("type");
      // String startCallType = "incomingcall";
      // String disconnectCallType = "calldisconnected";
      // if (startCallType.equals(notifDataType) ||
      // disconnectCallType.equals(notifDataType)) {
      // showIncomingCallScreen(remoteMessage, !isAppRunning());
      // return;
      // }
    } catch (Exception e) {

    }
    // Intent dialogIntent = new Intent(this, NotificationActivity.class);
    // dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // dialogIntent.putExtra("msg", remoteMessage);
    // startActivity(dialogIntent);

    // startCallService();

    FirebasePushPlugin.onNewRemoteMessage(remoteMessage);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public void startCallService() {
    Intent intent = new Intent(this, IncomingCallService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(intent);
    }
  }

  private Spannable getActionText(String title, @ColorRes int colorRes) {
    Spannable spannable = new SpannableString(title);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
      spannable.setSpan(
          new ForegroundColorSpan(this.getColor(colorRes)), 0, spannable.length(), 0);
    }
    return spannable;
  }

  /**
   * Create and show a custom notification containing the received FCM message.
   *
   */
  @RequiresApi(api = Build.VERSION_CODES.M)
  // private void sendNotification(Map<String, String> messageData) {

  //   String channelId = "fcm_call_channel";
  //   String channelName = "Incoming Call";
  //  // Uri uri = Uri.parse("viauapp://");

  //   Log.d(TAG, "sendNotification()");
  //   // Prepare data from messageData
  //   String caller = "Unknown caller";
  //   if (messageData.containsKey("Username")) {
  //     caller = messageData.get("Username");
  //   }
  //   String callId = messageData.get("ConnectionId");
  //   String callbackUrl = messageData.get("ConnectionId");

  //   Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  //   // String notification_title= remoteMessage.getData().get("title");
  //   String notification_title = "Incoming Call";

  //   // Intent intent = new Intent(this, IncomingCallActivity.class);
  //   // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  //   // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
  //   // PendingIntent.FLAG_ONE_SHOT);

  //   // // notification action buttons start
  //   // PendingIntent acptIntent =
  //   // IncomingCallActivity.getActionIntent(oneTimeID,uri,this);
  //   // PendingIntent rjctIntent =
  //   // IncomingCallActivity.getActionIntent(oneTimeID,uri, this);

  //   // NotificationCompat.Action rejectCall=new
  //   // NotificationCompat.Action.Builder(R.drawable.rjt_btn,getActionText("Decline",android.R.color.holo_red_light),rjctIntent).build();
  //   // NotificationCompat.Action acceptCall=new
  //   // NotificationCompat.Action.Builder(R.drawable.acpt_btn,getActionText("Answer",android.R.color.holo_green_light),acptIntent).build();
  //   // //end

  //   // Intent for LockScreen or tapping on notification
  //   Intent fullScreenIntent = new Intent(this, IncomingCallActivity.class);
  //   fullScreenIntent.putExtra("caller", caller);
  //   PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
  //       fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

  //   // Intent for tapping on Answer
  //   Intent acceptIntent = new Intent(IncomingCallActivity.VOIP_ACCEPT);
  //   PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, 10, acceptIntent, 0);

  //   // Intent for tapping on Reject
  //   Intent declineIntent = new Intent(IncomingCallActivity.VOIP_DECLINE);
  //   PendingIntent declinePendingIntent = PendingIntent.getBroadcast(this, 20, declineIntent, 0);

  //   // when device locked show fullscreen notification start
  //   // Intent i = new Intent(getApplicationContext(), IncomingCallActivity.class);
  //   // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
  //   // Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
  //   // i.putExtra("APP_STATE", isAppRunning());
  //   // i.putExtra("caller", caller);
  //   // i.putExtra("FALL_BACK", true);
  //   // i.putExtra("NOTIFICATION_ID", oneTimeID);
  //   // PendingIntent fullScreenIntent = PendingIntent.getActivity(this, 0 /* Request
  //   // code */, i,
  //   // PendingIntent.FLAG_ONE_SHOT);
  //   // end

  //   NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
  //       .setSmallIcon(getResources().getIdentifier("pushicon", "drawable", getPackageName()))
  //       .setContentTitle(notification_title)
  //       .setContentText(caller)
  //       .setPriority(NotificationCompat.PRIORITY_MAX)
  //       .setCategory(NotificationCompat.CATEGORY_CALL)
  //       // Show main activity on lock screen or when tapping on notification
  //       .setFullScreenIntent(fullScreenPendingIntent, true)
  //       // Show Accept button
  //       .addAction(new NotificationCompat.Action(0, "Accept",
  //           acceptPendingIntent))
  //       // Show decline action
  //       .addAction(new NotificationCompat.Action(0, "Decline",
  //           declinePendingIntent))
  //       // Make notification dismiss on user input action
  //       .setAutoCancel(true)
  //       // Cannot be swiped by user
  //       .setOngoing(true)
  //       // Set ringtone to notification (< Android O)
  //       .setSound(notification_sound)

  //       // .setContentIntent(pendingIntent)
  //       .setDefaults(Notification.DEFAULT_VIBRATE);

  //   // .setSmallIcon(R.mipmap.ic_launcher);

  //   Notification incomingCallNotification = notificationBuilder.build();

  //   NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
  //   int importance = NotificationManager.IMPORTANCE_HIGH;

  //   // channel creation start
  //   if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
  //     Log.d(TAG, "createChannel start");
  //     NotificationChannel mChannel = new NotificationChannel(
  //         channelId, channelName, importance);
  //     AudioAttributes attributes = new AudioAttributes.Builder()
  //         .setUsage(AudioAttributes.USAGE_NOTIFICATION)
  //         .build();
  //     mChannel.setSound(notification_sound, attributes);
  //     mChannel.setDescription(channelName);
  //     mChannel.enableLights(true);
  //     mChannel.enableVibration(true);
  //     NotificationManager notificationManager2 = getSystemService(NotificationManager.class);
  //     notificationManager2.createNotificationChannel(mChannel);
  //   }
  //   // end

  //   notificationManager.notify(oneTimeID, incomingCallNotification);

  //   // Add broadcast receiver for notification button actions
  //   if (voipNotificationActionBR == null) {
  //     IntentFilter filter = new IntentFilter();
  //     filter.addAction(IncomingCallActivity.VOIP_ACCEPT);
  //     filter.addAction(IncomingCallActivity.VOIP_DECLINE);

  //     Context appContext = this.getApplicationContext();
  //     voipNotificationActionBR = new BroadcastReceiver() {
  //       @Override
  //       public void onReceive(Context context, Intent intent) {
  //         // Remove BR after responding to notification action
  //         appContext.unregisterReceiver(voipNotificationActionBR);
  //         voipNotificationActionBR = null;

  //         // Handle action
  //         dismissVOIPNotification();
  //         String voipStatus = intent.getAction();
  //         // Update Webhook status to CONNECTED
  //         // updateWebhookVOIPStatus(callbackUrl, callId, voipStatus);

  //         // Start cordova activity on answer
  //         if (voipStatus.equals(IncomingCallActivity.VOIP_ACCEPT)) {
  //           startActivity(intentForLaunchActivity());
  //         }
  //       }
  //     };

  //     appContext.registerReceiver(voipNotificationActionBR, filter);
  //   }
  // }

  //   // VoIP implementation
    private Intent intentForLaunchActivity() {
    Log.d(TAG, "intentForLaunchActivity()");
      PackageManager pm = getPackageManager();
      return pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
  }

  private Uri defaultRingtoneUri() {
      return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  }

  private void createNotificationChannel() {
      // Create the NotificationChannel, but only on API 26+ because
      // the NotificationChannel class is new and not in the support library
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          int importance = NotificationManager.IMPORTANCE_HIGH;
          NotificationChannel channel = new NotificationChannel(CHANNEL_VOIP, CHANNEL_NAME, importance);
          channel.setDescription("Channel For VOIP Calls");

          // Set ringtone to notification (>= Android O)
          AudioAttributes audioAttributes = new AudioAttributes.Builder()
                  .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                  .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                  .build();
          channel.setSound(defaultRingtoneUri(), audioAttributes);

          // Register the channel with the system; you can't change the importance
          // or other notification behaviors after this
          NotificationManager notificationManager = getSystemService(NotificationManager.class);
          notificationManager.createNotificationChannel(channel);
      }
  }

  private void showVOIPNotification(Map<String, String> messageData) {
      createNotificationChannel();
    Log.d(TAG, "sendVOIPNotification()");
      // Prepare data from messageData
      String caller = "Unknown caller";
      if (messageData.containsKey("Username")) {
          caller = messageData.get("Username");
      }
      String callId = messageData.get("callId");
      String callbackUrl = messageData.get("callbackUrl");

      // Update Webhook status to CONNECTED
     // updateWebhookVOIPStatus(callbackUrl, callId, IncomingCallActivity.VOIP_CONNECTED);

      // Intent for LockScreen or tapping on notification
      Intent fullScreenIntent = new Intent(this, IncomingCallActivity.class);
      fullScreenIntent.putExtra("caller", caller);
      PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
              fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      // Intent for tapping on Answer
      Intent acceptIntent = new Intent(IncomingCallActivity.VOIP_ACCEPT);
      PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, 10, acceptIntent, 0);

      // Intent for tapping on Reject
      Intent declineIntent = new Intent(IncomingCallActivity.VOIP_DECLINE);
      PendingIntent declinePendingIntent = PendingIntent.getBroadcast(this, 20, declineIntent, 0);

      NotificationCompat.Builder notificationBuilder =
              new NotificationCompat.Builder(this, CHANNEL_VOIP)
                      .setSmallIcon(R.drawable.pushicon)
                      .setContentTitle("Incoming call")
                      .setContentText(caller)
                      .setPriority(NotificationCompat.PRIORITY_HIGH)
                      .setCategory(NotificationCompat.CATEGORY_CALL)
                      // Show main activity on lock screen or when tapping on notification
                      .setFullScreenIntent(fullScreenPendingIntent, true)
                      // Show Accept button
                      .addAction(R.drawable.common_google_signin_btn_icon_dark_focused, "Accept",
                              acceptPendingIntent)
                      // Show decline action
                      .addAction(R.drawable.common_google_signin_btn_icon_dark_focused, "Decline",
                              declinePendingIntent)
                      // Make notification dismiss on user input action
                      .setAutoCancel(true)
                      // Cannot be swiped by user
                      .setOngoing(true)
                      // Set ringtone to notification (< Android O)
                      .setSound(defaultRingtoneUri());

      Notification incomingCallNotification = notificationBuilder.build();

      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
      // Display notification
      notificationManager.notify(VOIP_NOTIFICATION_ID, incomingCallNotification);

      // Add broadcast receiver for notification button actions
      if (voipNotificationActionBR == null) {
          IntentFilter filter = new IntentFilter();
          filter.addAction(IncomingCallActivity.VOIP_ACCEPT);
          filter.addAction(IncomingCallActivity.VOIP_DECLINE);

          Context appContext = this.getApplicationContext();

          voipNotificationActionBR = new BroadcastReceiver() {


              @Override
              public void onReceive(Context context, Intent intent) {
                  // Remove BR after responding to notification action
                  appContext.unregisterReceiver(voipNotificationActionBR);
                  voipNotificationActionBR = null;

                  // Handle action
                  dismissVOIPNotification();
                  String voipStatus = intent.getAction();
                  // Update Webhook status to CONNECTED
                  //updateWebhookVOIPStatus(callbackUrl, callId, voipStatus);

                  // Start cordova activity on answer
                if (voipStatus.equals(IncomingCallActivity.VOIP_ACCEPT)) {
                    Log.d(TAG, "voipStatus, start Cordova?");
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    startActivity(intentForLaunchActivity());
                  }
                }
              }
          };

          appContext.registerReceiver(voipNotificationActionBR, filter);
      }
  }

  private void dismissVOIPNotification() {
    NotificationManagerCompat.from(this).cancel(oneTimeID);
    if (IncomingCallActivity.instance != null) {
      IncomingCallActivity.instance.finish();
    }
  }

  private void showIncomingCallScreen(RemoteMessage remoteMessage, boolean isAppRunning) {
    String notifDataType = remoteMessage.getData().get("type");
    String startCallType = "incomingcall";
    String disconnectCallType = "calldisconnected";
    Log.d(TAG, "showIncomingCallScreen");
    // if (startCallType.equals(notifDataType)) {
    // Intent i = new Intent(getApplicationContext(),
    // IncomingCallScreenActivity.class);
    // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
    // Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    // i.putExtra("CALLER_NAME", remoteMessage.getData().get("Username"));

    // TODO: Uncomment after adding type
    // i.putExtra("CALL_TYPE", remoteMessage.getData().get("type"));
    // i.putExtra("APP_STATE", isAppRunning);
    // startActivity(i);
    // } else if (disconnectCallType.equals((notifDataType))) {
    // LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
    // .getInstance(FirebaseMessagingService.this);
    // localBroadcastManager.sendBroadcast(new Intent(
    // "com.incomingcallscreenactivity.action.close"));
    // }
  }

  private boolean isAppRunning() {
    ActivityManager m = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTaskInfoList = m.getRunningTasks(10);
    Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
    int n = 0;
    while (itr.hasNext()) {
      n++;
      itr.next();
    }
    if (n == 1) { // App is killed
      return false;
    }
    return true; // App is in background or foreground
  }

  @Override
  public void onNewToken(String token) {
    super.onNewToken(token);
    Log.e("Refreshed token:", token);
    FirebasePushPlugin.onNewToken(token);
  }
}
