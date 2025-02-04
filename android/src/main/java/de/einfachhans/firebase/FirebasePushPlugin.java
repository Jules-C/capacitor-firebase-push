package de.einfachhans.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import com.getcapacitor.Bridge;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginHandle;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.json.JSONException;

@CapacitorPlugin(name = "FirebasePush", permissions = @Permission(strings = {}, alias = "receive"))
public class FirebasePushPlugin extends Plugin {

    private static final String TAG = "FirebasePushPlugin";
    public static Bridge staticBridge = null;

    private static boolean registered = false;
    private static ArrayList<Bundle> notificationStack = null;

    public NotificationManager notificationManager;

    @Override
    public void load() {
        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        staticBridge = this.bridge;

        if (isIncomingCall())
            Log.d("incomingCall", "exists FirebasePush");
    }

    // TODO create method to delete all storage
    @PluginMethod
    public void deleteStorage(PluginCall call) {

        removeAllPreferences();
        call.resolve();

    }

    // TODO: create method to check incomingCall status
    @PluginMethod
    public void hasStorage(PluginCall call) {
        JSObject data = new JSObject();
        data.put("value", isIncomingCall());
        call.resolve(data);
    }
    @PluginMethod
    public void register(PluginCall call) {
        new Handler()
                .post(
                        () -> {
                            FirebaseApp.initializeApp(this.getContext());
                            registered = true;
                            this.sendStacked();
                            call.resolve();
                            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                            FirebaseInstanceId
                                    .getInstance()
                                    .getInstanceId()
                                    .addOnSuccessListener(
                                            getActivity(),
                                            new OnSuccessListener<InstanceIdResult>() {
                                                @Override
                                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                                    sendToken(instanceIdResult.getToken());
                                                }
                                            });
                        });
        FirebaseInstanceId
                .getInstance()
                .getInstanceId()
                .addOnFailureListener(
                        new OnFailureListener() {
                            public void onFailure(Exception e) {
                                Log.e(String.valueOf(e), "ah!");
                            }
                        });
        call.resolve();
    }

    @PluginMethod
    public void unregister(PluginCall call) {
        new Handler()
                .post(
                        () -> {
                            FirebaseInstallations.getInstance().delete();
                            call.resolve();
                        });
    }

    @PluginMethod
    public void getDeliveredNotifications(PluginCall call) {
        JSArray notifications = new JSArray();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

            for (StatusBarNotification notif : activeNotifications) {
                JSObject jsNotif = new JSObject();

                jsNotif.put("id", notif.getId());

                Notification notification = notif.getNotification();
                if (notification != null) {
                    jsNotif.put("title", notification.extras.getCharSequence(Notification.EXTRA_TITLE));
                    jsNotif.put("body", notification.extras.getCharSequence(Notification.EXTRA_TEXT));
                    jsNotif.put("group", notification.getGroup());
                    jsNotif.put("groupSummary", 0 != (notification.flags & Notification.FLAG_GROUP_SUMMARY));

                    JSObject extras = new JSObject();

                    for (String key : notification.extras.keySet()) {
                        extras.put(key, notification.extras.get(key));
                    }

                    jsNotif.put("data", extras);
                }

                notifications.put(jsNotif);
            }
        }

        JSObject result = new JSObject();
        result.put("notifications", notifications);
        call.resolve(result);
    }

    @PluginMethod
    public void removeDeliveredNotifications(PluginCall call) {
        JSArray notifications = call.getArray("ids");
        List<Integer> ids = new ArrayList<>();
        try {
            ids = notifications.toList();
        } catch (JSONException e) {
            call.reject(e.getMessage());
        }

        for (int id : ids) {
            notificationManager.cancel(id);
        }

        call.resolve();
    }

    // removed preferences from storage
    void removeAllPreferences() {

      // Storing incomingCall data into SharedPreferences

      SharedPreferences sharedPref = this.getContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "all shared preferences removed");
    }

    @PluginMethod
    public void removeAllDeliveredNotifications(PluginCall call) {
        notificationManager.cancelAll();
        call.resolve();
    }

    @PluginMethod
    public void getBadgeNumber(PluginCall call) {
        call.unimplemented("Not implemented on Android.");
    }

    @PluginMethod
    public void setBadgeNumber(PluginCall call) {
        call.unimplemented("Not implemented on Android.");
    }

    public void sendToken(String token) {
        JSObject data = new JSObject();
        data.put("token", token);
        notifyListeners("token", data, true);
    }

    public void sendRemoteMessage(RemoteMessage message) {
        String messageType = "data";
        String title = null;
        String body = null;
        String id = null;
        String sound = null;
        String vibrate = null;
        String color = null;
        String icon = null;
        String channelId = null;

        Map<String, String> data = message.getData();
        Log.d(TAG, String.valueOf(data));
        if (message.getNotification() != null) {
            messageType = "notification";
            id = message.getMessageId();
            RemoteMessage.Notification notification = message.getNotification();
            title = notification.getTitle();
            body = notification.getBody();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channelId = notification.getChannelId();
            }
            sound = notification.getSound();
            color = notification.getColor();
            icon = notification.getIcon();
        }

        if (TextUtils.isEmpty(id)) {
            Random rand = new Random();
            int n = rand.nextInt(50) + 1;
            id = Integer.toString(n);
        }

        Log.d(
                TAG,
                "sendMessage(): messageType=" +
                        messageType +
                        "; id=" +
                        id +
                        "; title=" +
                        title +
                        "; body=" +
                        body +
                        "; sound=" +
                        sound +
                        "; vibrate=" +
                        vibrate +
                        "; color=" +
                        color +
                        "; icon=" +
                        icon +
                        "; channel=" +
                        channelId +
                        "; data=" +
                        data.toString());
        Bundle bundle = new Bundle();
        for (String key : data.keySet()) {
            bundle.putString(key, data.get(key));
        }
        bundle.putString("messageType", messageType);
        this.putKVInBundle("google.message_id", id, bundle);
        this.putKVInBundle("title", title, bundle);
        this.putKVInBundle("body", body, bundle);
        this.putKVInBundle("sound", sound, bundle);
        this.putKVInBundle("vibrate", vibrate, bundle);
        this.putKVInBundle("color", color, bundle);
        this.putKVInBundle("icon", icon, bundle);
        this.putKVInBundle("channel_id", channelId, bundle);
        this.putKVInBundle("from", message.getFrom(), bundle);
        this.putKVInBundle("collapse_key", message.getCollapseKey(), bundle);
        this.putKVInBundle("google.sent_time", String.valueOf(message.getSentTime()), bundle);
        this.putKVInBundle("google.ttl", String.valueOf(message.getTtl()), bundle);

        if (!registered) {
            Log.d(TAG, "if!registered");
            if (FirebasePushPlugin.notificationStack == null) {
                FirebasePushPlugin.notificationStack = new ArrayList<>();
            }
            notificationStack.add(bundle);
            return;
        }

        this.sendRemoteBundle(bundle);
    }

    private void sendRemoteBundle(Bundle bundle) {
        JSObject json = new JSObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            json.put(key, bundle.get(key));
        }
        notifyListeners("message", json, true);
    }

    public static void onNewToken(String newToken) {
        FirebasePushPlugin pushPlugin = FirebasePushPlugin.getInstance();
        if (pushPlugin != null) {
            pushPlugin.sendToken(newToken);
        }
    }

    public static void onNewRemoteMessage(RemoteMessage message) {
        FirebasePushPlugin pushPlugin = FirebasePushPlugin.getInstance();
        Log.d(TAG, "onNewRemoteMessage");
        if (pushPlugin != null) {
            Log.d(TAG, "onNewRemoteMessage, pushPlugin!= null");
            pushPlugin.sendRemoteMessage(message);
        }
        Log.d(TAG, "pushPlugin=null");
    }

    @Override
    public void handleOnNewIntent(Intent intent) {
        final Bundle data = intent.getExtras();
        if (data != null && data.containsKey("google.message_id")) {
            data.putString("messageType", "notification");
            data.putString("tap", "background");
            Log.d(TAG, "Notification message on new intent: " + data.toString());
            this.sendRemoteBundle(data);
        }
    }

    private void sendStacked() {
        Log.d(TAG, "sendStacked()");
        if (FirebasePushPlugin.notificationStack != null) {
            for (Bundle bundle : FirebasePushPlugin.notificationStack) {
                this.sendRemoteBundle(bundle);
            }
            FirebasePushPlugin.notificationStack.clear();
        }
    }

    public static FirebasePushPlugin getInstance() {
        Log.d(TAG, "getInstance()");
        if (staticBridge != null && staticBridge.getWebView() != null) {
            Log.d(TAG, "getInstance()2");
            PluginHandle handle = staticBridge.getPlugin("FirebasePush");
            if (handle == null) {
                Log.d(TAG, "getInstance()3");
                return null;
            }
            Log.d(TAG, "getInstance()4");
            return (FirebasePushPlugin) handle.getInstance();
        }
        Log.d(TAG, "getInstance()5");
        return null;
    }

    private void putKVInBundle(String k, String v, Bundle o) {
        if (v != null && !o.containsKey(k)) {
            o.putString(k, v);
        }
    }

    boolean isIncomingCall() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return sharedPref.contains("incomingCall");
    }
}
