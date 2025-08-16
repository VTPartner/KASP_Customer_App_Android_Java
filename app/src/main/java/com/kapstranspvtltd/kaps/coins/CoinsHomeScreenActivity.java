package com.kapstranspvtltd.kaps.coins;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.coins.BottomSheets.HowToEarnCoinsBottomSheet;
import com.kapstranspvtltd.kaps.databinding.ActivityAllGoodsVehiclesBinding;
import com.kapstranspvtltd.kaps.databinding.ActivityCoinsHomeScreenBinding;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CoinsHomeScreenActivity extends AppCompatActivity {
    private ActivityCoinsHomeScreenBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoinsHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Utility.applyEdgeToEdgePadding(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        setupClickEvents();
        fetchCoinsSummary();
    }

    private void fetchCoinsSummary() {
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "get_coins_summary",
                    requestBody,
                    response -> {
                        binding.tvCoinsCount.setText(String.valueOf(response.optInt("available_coins", 0)));

                        int expiringCoins = response.optInt("expiring_coins", 0);
                        String expiringText = expiringCoins > 0
                                ? expiringCoins + " coins expiring this month"
                                : "No coins expiring this month";
                        binding.tvExpiringCoins.setText(expiringText);
                    },
                    error -> {
                        Toast.makeText(this, "Error fetching coins data", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupClickEvents() {
        binding.coinHistoryLyt.setOnClickListener(v ->
                startActivity(new Intent(this, CoinsHistoryActivity.class)));

        binding.howToEarnCoinLyt.setOnClickListener(v ->
                new HowToEarnCoinsBottomSheet().show(getSupportFragmentManager(), "HowToEarn"));

        binding.transferBtn.setOnClickListener(v -> {
            int availableCoins = Integer.parseInt(binding.tvCoinsCount.getText().toString());
            if (availableCoins < 25) {
                Toast.makeText(this, "Minimum 25 coins required to transfer", Toast.LENGTH_SHORT).show();
                return;
            }
            transferCoinsToWallet(availableCoins);
        });
    }

    private void transferCoinsToWallet(int coins) {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing coin transfer...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");
        String customerName = preferenceManager.getStringValue("customer_name");
        String mobileNo = preferenceManager.getStringValue("customer_mobile_no");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);
            requestBody.put("coins", coins);
            requestBody.put("customer_name", customerName);
            requestBody.put("mobile_no", mobileNo);
            requestBody.put("created_at", "2025-06-29 16:39:51");
            requestBody.put("created_by", "mohammed786-svg");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "transfer_coins_to_wallet",
                    requestBody,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            String status = response.getString("status");
                            if ("success".equals(status)) {
                                // Show success message
                                String message = response.optString("message", "Coins transferred successfully");
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                                // Update remaining coins display
                                int remainingCoins = response.optInt("remaining_coins", 0);
                                binding.tvCoinsCount.setText(String.valueOf(remainingCoins));

                                // Close bottom sheet if open
                                if (getSupportFragmentManager().findFragmentByTag("transferCoins") != null) {
                                    ((DialogFragment) getSupportFragmentManager()
                                            .findFragmentByTag("transferCoins"))
                                            .dismiss();
                                }

                                // Refresh coins summary
                                fetchCoinsSummary();
                            } else {
                                String errorMessage = response.optString("message", "Failed to transfer coins");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to process response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        String errorMessage;
                        if (error instanceof NetworkError) {
                            errorMessage = "No internet connection";
                        } else if (error instanceof TimeoutError) {
                            errorMessage = "Request timed out";
                        } else if (error instanceof ServerError) {
                            errorMessage = "Server error";
                        } else {
                            errorMessage = "Failed to transfer coins";
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // Set timeout for request
            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000, // 30 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Error processing request", Toast.LENGTH_SHORT).show();
        }
    }
}