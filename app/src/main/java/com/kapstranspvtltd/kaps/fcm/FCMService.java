package com.kapstranspvtltd.kaps.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.OngoingGoodsDetailActivity;
import com.kapstranspvtltd.kaps.common_activities.ScheduledBookingsActivity;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.bookings.DriverOngoingBookingDetailsActivity;
import com.kapstranspvtltd.kaps.handyman_customer_app.activities.bookings.HandymanOngoingBookingDetailsActivity;
import com.kapstranspvtltd.kaps.jcb_crane_customer_app.activities.bookings.JcbCraneBookingDetailsActivity;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FCMService extends FirebaseMessagingService {
    private PreferenceManager preferenceManager;
    private static final String CHANNEL_ID = "VT_Partner_Channel";
    private static final String CHANNEL_NAME = "VT Partner Notifications";
    private NotificationManager notificationManager;
    private static final String BOOKING_CHANNEL_ID = "booking_notifications";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceManager = new PreferenceManager(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createBookingChannel();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        preferenceManager.saveStringValue("fcm_token",token);
        updateAuthToken(token);
    }

    private void updateAuthToken(String deviceToken) {
        Log.d("FCMNewTokenFound", "updating goods driver authToken");

        String customerId = preferenceManager.getStringValue("customer_id");
        if (customerId.isEmpty() || deviceToken == null || deviceToken.isEmpty()) {
            return;
        }

        executorService.execute(() -> {
            try {
                String serverToken = AccessToken.getAccessToken();
                String url = APIClient.baseUrl + "update_firebase_customer_token";

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("customer_id", customerId);
                jsonBody.put("authToken", deviceToken);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        response -> {
                            String message = response.optString("message");
                            Log.d("Auth", "Token update response: " + message);
                        },
                        error -> error.printStackTrace()
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Bearer " + serverToken);
                        return headers;
                    }
                };

                VolleySingleton.getInstance(FCMService.this).addToRequestQueue(request);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void createBookingChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    BOOKING_CHANNEL_ID,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new bookings");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 1000});
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setBypassDnd(true);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("FCM Notification Received " + remoteMessage.getMessageId());
        
        Map<String, String> data = remoteMessage.getData();
        System.out.println("FCM data::"+data);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        
        String intent = data.get("intent");
        System.out.println("FCM intent");
        if (intent != null) {
            String title = data.get("title");
            String body = data.get("body");
            showRegularNotification(title != null ? title : "", body != null ? body : "", data);
            switch (intent) {
                case "booking_expired":
                    Intent s = new Intent(this, ScheduledBookingsActivity.class);
                    s.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(s);
                    break;
                case "customer_home":
                    System.out.println("customer_home:: send to home page here");
                    preferenceManager.saveBooleanValue("live_ride",false);
                    preferenceManager.saveStringValue("current_booking_id","");
                    Intent i = new Intent(this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    break;
                case "live_tracking":
                    handleLiveTracking(data);
                    break;
                case "cab_live_tracking":
                    handleCabLiveTracking(data);
                    break;
                case "driver_live_tracking":
                    handleDriverLiveTracking(data);
                    break;
                case "jcb_crane_live_tracking":
                    handleJcbCraneLiveTracking(data);
                    break;
                case "handyman_live_tracking":
                    handleHandyManLiveTracking(data);
                    break;

                case "goods_booking_live_track":
                    handleGoodsBookingLiveTrack(data);
                    break;
                case "cab_booking_live_track":
                    handleCabBookingLiveTrack(data);
                    break;
                case "end_live_tracking":
                    preferenceManager.saveBooleanValue("live_ride",false);
                    preferenceManager.saveStringValue("current_booking_id","");
                    handleEndLiveTracking(data);
                    break;

                case "end_cab_live_tracking":
                    preferenceManager.saveBooleanValue("live_ride",false);
                    preferenceManager.saveStringValue("cab_current_booking_id","");
                    handleEndCabLiveTracking();
                    break;
                case "end_driver_live_tracking":
                    preferenceManager.saveBooleanValue("live_ride",false);
                    preferenceManager.saveStringValue("cab_current_booking_id","");
                    handleEndDriverLiveTracking();
                    break;
                case "end_jcb_crane_driver_live_tracking":
                    preferenceManager.saveBooleanValue("live_ride",false);
                    preferenceManager.saveStringValue("cab_current_booking_id","");
                    handleEndJcbCraneLiveTracking();
                    break;
                case "end_handyman_live_tracking":
                    preferenceManager.saveBooleanValue("live_ride",false);
                    preferenceManager.saveStringValue("cab_current_booking_id","");
                    handleEndHandyManLiveTracking();
                    break;
                default:

                    String message = notification != null ? notification.getBody() : data.get("message");
                    if(title.equalsIgnoreCase("Package Deliveried")){
                        preferenceManager.saveBooleanValue("live_ride",false);
                        preferenceManager.saveStringValue("current_booking_id","");
                    }
                    showRegularNotification(title != null ? title : "", message != null ? message : "", data);
                    break;
            }
        }else {
            showRegularNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(),data);
        }
    }

    private void handleEndHandyManLiveTracking() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(
                    getApplicationContext(),
                    "Handyman has completed the service successfully",
                    Toast.LENGTH_LONG
            ).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                sendBroadcast(new Intent("FINISH_EXISTING_ACTIVITIES"));
            }, 1000);
        });
    }

    private void handleEndJcbCraneLiveTracking() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(
                    getApplicationContext(),
                    "Jcb/Crane Driver has completed the service successfully",
                    Toast.LENGTH_LONG
            ).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                sendBroadcast(new Intent("FINISH_EXISTING_ACTIVITIES"));
            }, 1000);
        });
    }

    private void handleEndDriverLiveTracking() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(
                    getApplicationContext(),
                    "Driver has completed the service successfully",
                    Toast.LENGTH_LONG
            ).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                sendBroadcast(new Intent("FINISH_EXISTING_ACTIVITIES"));
            }, 1000);
        });
    }

    private void handleHandyManLiveTracking(Map<String, String> data) {
        String bookingId = data.get("booking_id");
        preferenceManager.saveStringValue("current_booking_id", bookingId != null ? bookingId : "");

        Intent intent = new Intent(this, HandymanOngoingBookingDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("route", "ongoing_ride");
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("isFromFCM", true);
        intent.putExtra("cab",true);
        startActivity(intent);
    }

    private void handleJcbCraneLiveTracking(Map<String, String> data) {
        String bookingId = data.get("booking_id");
        preferenceManager.saveStringValue("current_booking_id", bookingId != null ? bookingId : "");

        Intent intent = new Intent(this, JcbCraneBookingDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("route", "ongoing_ride");
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("isFromFCM", true);
        intent.putExtra("cab",true);
        startActivity(intent);
    }

    private void handleDriverLiveTracking(Map<String, String> data) {
        String bookingId = data.get("booking_id");
        preferenceManager.saveStringValue("current_booking_id", bookingId != null ? bookingId : "");

        Intent intent = new Intent(this, DriverOngoingBookingDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("route", "ongoing_ride");
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("isFromFCM", true);
        intent.putExtra("cab",true);
        startActivity(intent);
    }

    private void handleEndCabLiveTracking() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(
                    getApplicationContext(),
                    "You have reached your destination successfully",
                    Toast.LENGTH_LONG
            ).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                sendBroadcast(new Intent("FINISH_EXISTING_ACTIVITIES"));
            }, 1000);
        });
    }

    private void handleCabBookingLiveTrack(Map<String, String> data) {
        String bookingId = data.get("booking_id");
        preferenceManager.saveStringValue("current_booking_id", bookingId != null ? bookingId : "");

        Intent intent = new Intent(this, OngoingGoodsDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("route", "ongoing_ride");
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("isFromFCM", true);
        intent.putExtra("cab",true);
        startActivity(intent);
    }

    private void handleCabLiveTracking(Map<String, String> data) {
        String bookingId = data.get("booking_id");
        preferenceManager.saveStringValue("current_booking_id", bookingId != null ? bookingId : "");

        Intent intent = new Intent(this, OngoingGoodsDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("route", "ongoing_ride");
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("isFromFCM", true);
        intent.putExtra("cab",true);
        startActivity(intent);
    }

    private void handleLiveTracking(Map<String, String> data) {

        String bookingId = data.get("booking_id");
        preferenceManager.saveStringValue("current_booking_id", bookingId != null ? bookingId : "");
        preferenceManager.saveBooleanValue("live_ride",true);
        Intent intent = new Intent(this, OngoingGoodsDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                       Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("route", "ongoing_ride");
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("isFromFCM", true);
        startActivity(intent);
    }

    private void handleGoodsBookingLiveTrack(Map<String, String> data) {
        String bookingId = data.get("booking_id");
        System.out.println("Live Location track refresh");
        
        Intent intent = new Intent(this, OngoingGoodsDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("booking_id", bookingId);
        startActivity(intent);
    }

    private void handleEndLiveTracking(Map<String, String> data) {
        String orderID = data.get("order_id");
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(
                    getApplicationContext(),
                    "Your Package is successfully delivered",
                    Toast.LENGTH_LONG
            ).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                              Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                              Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("order_id", orderID);
                startActivity(intent);

                sendBroadcast(new Intent("FINISH_EXISTING_ACTIVITIES"));
            }, 1000);
        });
    }

    private void showRegularNotification(String title, String message, Map<String, String> data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    BOOKING_CHANNEL_ID,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for booking updates");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, BOOKING_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    public static void cancelAllNotifications(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
            Log.d("FCMService", "All notifications cancelled");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}