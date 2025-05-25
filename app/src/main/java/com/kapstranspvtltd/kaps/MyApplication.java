package com.kapstranspvtltd.kaps;

import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kapstranspvtltd.kaps.fcm.AccessToken;
import com.kapstranspvtltd.kaps.fcm.FCMService;
import com.kapstranspvtltd.kaps.language_change_utils.LocaleHelper;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    public static Context mContext;
    public LocationViewModel locationViewModel;
    private static boolean activityVisible;
    private PreferenceManager preferenceManager;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();



        mContext = this;
        preferenceManager = new PreferenceManager(this);
        locationViewModel = new LocationViewModel(this);
        executorService = Executors.newSingleThreadExecutor();
        if(dropList != null){
            dropList.clear();
        }

        FirebaseApp.initializeApp(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Get and upload FCM token
        getFCMToken();
        // Cancel all existing notifications when app starts
        FCMService.cancelAllNotifications(this);

    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Save token locally
                    preferenceManager.saveStringValue("fcm_token",token);

                    // Upload token to server
                    updateAuthToken(token);
                });
    }

    private void updateAuthToken(String deviceToken) {
        Log.d("FCMToken", "Updating FCM token");

        String customerId = preferenceManager.getStringValue("customer_id");
        if (customerId.isEmpty() || deviceToken == null || deviceToken.isEmpty()) {
            return;
        }

        executorService.execute(() -> {
            try {
                String serverToken = AccessToken.getAccessToken();
                System.out.println("serverToken::"+serverToken);
                String url = APIClient.baseUrl + "update_firebase_customer_token";
                System.out.println("deviceToken::"+deviceToken);
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
                        error -> {
                            Log.e("Auth", "Error updating token: " + error.getMessage());
                            error.printStackTrace();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
//                        headers.put("Authorization", "Bearer " + serverToken);
                        return headers;
                    }
                };

                VolleySingleton.getInstance(this).addToRequestQueue(request);

            } catch (Exception e) {
                Log.e("Auth", "Error in token update process: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base, LocaleHelper.getLanguage(base)));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
    }
}