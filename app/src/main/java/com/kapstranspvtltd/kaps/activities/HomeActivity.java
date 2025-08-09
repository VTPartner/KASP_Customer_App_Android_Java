package com.kapstranspvtltd.kaps.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kapstranspvtltd.kaps.fcm.AccessToken;
import com.kapstranspvtltd.kaps.fragments.AccountSettingsFragment;
import com.kapstranspvtltd.kaps.fragments.CabOrdersFragment;
import com.kapstranspvtltd.kaps.fragments.GoodsOrdersFragment;
import com.kapstranspvtltd.kaps.fragments.HomeSelectFragment;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.Utility;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
        private ActivityHomeBinding binding;

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    PreferenceManager preferenceManager;

    private void requestNotificationPermission() {
        // Check if Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if permission is not granted
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {

                // Show permission rationale if needed
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Notification Permission")
                            .setMessage("We need notification permission to keep you updated about your ride status.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Request permission
                                requestPermissions(
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                        NOTIFICATION_PERMISSION_CODE
                                );
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    // Request permission directly
                    requestPermissions(
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            NOTIFICATION_PERMISSION_CODE
                    );
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
//                Toast.makeText(this, "Notification permission granted",
//                        Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this,
                        "Notification permission denied. You may miss important updates.",
                        Toast.LENGTH_LONG).show();

                // Optionally open settings
                showSettingsDialog();
            }
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Notifications are important for ride updates. " +
                        "Please enable notifications in settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    dialog.dismiss();
                    // Open app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    String orderID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestNotificationPermission();
        preferenceManager = new PreferenceManager(this);
        orderID = getIntent().getStringExtra("order_id");
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && Utility.hasGPSDevice(this)) {
            Toast.makeText(this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            Utility.enableLoc(this);
        } else {
            openFragment(new HomeSelectFragment(), "Home");
        }
        executorService = Executors.newSingleThreadExecutor();
        getFCMToken();

        if(orderID !=null && !orderID.isEmpty()){
            showRatingDialog();
        }
    }

    private void showRatingDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_rate_driver, null);
        dialog.setContentView(sheetView);

        // Initialize views
        ImageView driverImage = sheetView.findViewById(R.id.driverImage);
        TextView driverName = sheetView.findViewById(R.id.driverName);
        TextView orderNumber = sheetView.findViewById(R.id.orderNumber);
        RatingBar ratingBar = sheetView.findViewById(R.id.ratingBar);
        TextView ratingDescription = sheetView.findViewById(R.id.ratingDescription);
//        EditText commentBox = sheetView.findViewById(R.id.commentBox);
        MaterialButton submitButton = sheetView.findViewById(R.id.submitButton);

        driverName.setVisibility(View.GONE);
        driverImage.setVisibility(View.GONE);
        orderNumber.setText("Order #" + orderID);

        // Set default rating
        ratingBar.setRating(3);
        updateRatingDescription(ratingDescription, 3);

        // Rating change listener
        ratingBar.setOnRatingBarChangeListener((rBar, rating, fromUser) -> {
            updateRatingDescription(ratingDescription, rating);
        });

        // Submit button click
        submitButton.setOnClickListener(v -> {
            submitRating(
                    dialog,
                    (int) ratingBar.getRating(),
                    ratingDescription.getText().toString().trim()
            );
        });

        dialog.show();
    }

    private void updateRatingDescription(TextView descriptionView, float rating) {
        String description;
        int color;

        if (rating <= 1) {
            description = "Worst Experience";
            color = getResources().getColor(R.color.colorerror);
        } else if (rating <= 2) {
            description = "Bad";
            color = getResources().getColor(R.color.orange);
        } else if (rating <= 3) {
            description = "Good";
            color = getResources().getColor(R.color.colorPrimaryDark);
        } else if (rating <= 4) {
            description = "Very Good";
            color = getResources().getColor(R.color.green);
        } else {
            description = "Excellent";
            color = getResources().getColor(R.color.green);
        }

        descriptionView.setText(description);
        descriptionView.setTextColor(color);
    }

    private void submitRating(BottomSheetDialog dialog, int rating, String comment) {
        if (orderID == null || orderID.isEmpty()) return;


        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        JSONObject params = new JSONObject();
        try {
            params.put("order_id", orderID);
            params.put("ratings", rating);
            params.put("ratings_description", comment);
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "save_order_ratings",
                params,
                response -> {

                    dialog.dismiss();
                    Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();

                },
                error -> {

                    Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private ExecutorService executorService;
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
//                    updateAuthToken(token);
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
                        error -> {
                            Log.e("Auth", "Error updating token: " + error.getMessage());
                            error.printStackTrace();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Bearer " + serverToken);
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

    public void openFragment(Fragment fragment, String tag) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, tag);
            // Don't add HomeSelectFragment to back stack to prevent blank screen issue
            if (!(fragment instanceof HomeSelectFragment)) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        } catch (Exception e) {
            Log.e("Error", "-->" + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Get the current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);

        if (currentFragment instanceof HomeSelectFragment) {
            // If we're on HomeSelectFragment, show exit dialog or finish activity
            showExitDialog();
        } else {
            // For other fragments, navigate back to home
            openFragment(new HomeSelectFragment(), "Home");
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            HomeSelectFragment home = (HomeSelectFragment) getSupportFragmentManager().findFragmentByTag("Home");
            if (home != null && home.isVisible()) {
                // DO STUFF
            } else {
                if (home == null) {
                    home = new HomeSelectFragment();
                }
                openFragment(home, "Home");
            }
        } else if (itemId == R.id.navigation_goods_orders) {
            openFragmentIfLoggedIn(new GoodsOrdersFragment(), "Goods");
        }
//        else if (itemId == R.id.navigation_cab_orders) {
//            openFragmentIfLoggedIn(new CabOrdersFragment(), "Cab");
//        }
        else if (itemId == R.id.navigation_user) {
            openFragmentIfLoggedIn(new AccountSettingsFragment(), "Account");
        } else {
            return false;
        }

        return true;
    }
    private void openFragmentIfLoggedIn(Fragment fragment, String tag) {
        String customerID = preferenceManager.getStringValue("customer_id");
        if (customerID != null && customerID.isEmpty() == false) {
            openFragment(fragment, tag);
        } else {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}