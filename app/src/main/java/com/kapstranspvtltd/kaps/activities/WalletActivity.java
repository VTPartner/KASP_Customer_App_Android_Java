package com.kapstranspvtltd.kaps.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.models.WalletTransaction;
import com.kapstranspvtltd.kaps.adapters.WalletHistoryAdapter;
import com.kapstranspvtltd.kaps.databinding.ActivityWalletBinding;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletActivity extends AppCompatActivity implements PaymentResultListener {
    private ActivityWalletBinding binding;
    private PreferenceManager preferenceManager;
    private CustPrograssbar custPrograssbar;
    private WalletHistoryAdapter adapter;

    private double selectedAmount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Checkout.preload(getApplicationContext());

        initViews();

        fetchWalletDetails();
    }

    private void showAddMoneyDialogOld() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_money, null);
        builder.setView(dialogView);

        final EditText amountInput = dialogView.findViewById(R.id.amount_input);

        builder.setTitle("Add Money to Wallet")
                .setPositiveButton("Add", null) // Set to null, we'll override this below
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to prevent dialog from closing on invalid input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (amountStr.isEmpty()) {
                amountInput.setError("Please enter amount");
                return;
            }

            try {
                selectedAmount = Double.parseDouble(amountStr);
                if (selectedAmount < 1) {
                    amountInput.setError("Minimum amount should be ₹1");
                    return;
                }
                dialog.dismiss();
                startRazorpayPayment(selectedAmount);
            } catch (NumberFormatException e) {
                amountInput.setError("Invalid amount");
            }
        });
    }

    private void showAddMoneyDialog() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_add_money, null);
        mBottomSheetDialog.setContentView(sheetView);

        final EditText amountInput = sheetView.findViewById(R.id.amount_input);
        TextView addButton = sheetView.findViewById(R.id.add_button);


        addButton.setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (amountStr.isEmpty()) {
                amountInput.setError("Please enter amount");
                return;
            }

            try {
                selectedAmount = Double.parseDouble(amountStr);
                if (selectedAmount < 1) {
                    amountInput.setError("Minimum amount should be ₹1");
                    return;
                }
                mBottomSheetDialog.dismiss();
                startRazorpayPayment(selectedAmount);
            } catch (NumberFormatException e) {
                amountInput.setError("Invalid amount");
            }
        });


        mBottomSheetDialog.show();
    }


    private void startRazorpayPayment(double amount) {
        showLoading(true);
        Checkout checkout = new Checkout();
        checkout.setKeyID(APIClient.RAZORPAY_KEY); // Replace with your key
        checkout.setImage(R.drawable.logo); // Your app logo

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Kaps Trans Private Limited");
            options.put("description", "Wallet Recharge");
            options.put("currency", "INR");
            options.put("amount", (int)(amount * 100)); // Amount in paise
            options.put("send_sms_hash", true);

            JSONObject prefill = new JSONObject();
            prefill.put("email", preferenceManager.getStringValue("email"));
            prefill.put("contact", preferenceManager.getStringValue("mobile"));
            options.put("prefill", prefill);

            checkout.open(this, options);
        } catch (Exception e) {
            e.printStackTrace();
            showLoading(false);
            showError("Error in payment: " + e.getMessage());
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        updateWalletBalance(razorpayPaymentId, selectedAmount);
    }

    @Override
    public void onPaymentError(int code, String description) {
        runOnUiThread(() -> {
            try {
                showError("Payment failed: " + description);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateWalletBalance(String razorpayPaymentId, double amount) {
        showLoading(true);
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        JSONObject params = new JSONObject();
        try {
            params.put("customer_id", customerId);
            params.put("razorpay_payment_id", razorpayPaymentId);
            params.put("amount", amount);
            params.put("payment_mode", "Razorpay");
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "update_wallet_balance",
                params,
                response -> {
                    showLoading(false);
                    try {
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        fetchWalletDetails(); // Refresh wallet details
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Error updating wallet");
                    }
                },
                error -> {
                    showLoading(false);
                    showError("Failed to update wallet");
                    error.printStackTrace();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void initViews() {
        custPrograssbar = new CustPrograssbar();
        preferenceManager = new PreferenceManager(this);

        // Setup RecyclerView
        binding.recycleviewHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WalletHistoryAdapter(this);
        binding.recycleviewHistory.setAdapter(adapter);

        // Add Money Button Click
        binding.addpayment.setOnClickListener(v -> {
            showAddMoneyDialog();
        });
    }

    private void fetchWalletDetails() {
        showLoading(true);
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");
        JSONObject params = new JSONObject();
        try {
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String url = APIClient.baseUrl + "customer_wallet_details";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                params,
                response -> {
                    showLoading(false);
                    try {
                        if (response.has("message")) {
                            // Handle no data found case
                            binding.lvlNotfound.setVisibility(View.VISIBLE);
                            binding.recycleviewHistory.setVisibility(View.GONE);
                            binding.txtWallet.setText("Balance ₹0.00");
                            return;
                        }

                        if (response.has("results")) {
                            JSONObject results = response.getJSONObject("results");
                            updateWalletUI(results);
                        } else {
                            binding.lvlNotfound.setVisibility(View.VISIBLE);
                            binding.recycleviewHistory.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Failed to parse response");
                        binding.lvlNotfound.setVisibility(View.VISIBLE);
                        binding.recycleviewHistory.setVisibility(View.GONE);
                    }
                },
                error -> {
                    error.printStackTrace();
                    showLoading(false);

                    // Handle different types of errors
                    String errorMessage;
                    if (error instanceof NetworkError) {
                        errorMessage = "No internet connection";
                    } else if (error instanceof TimeoutError) {
                        errorMessage = "Request timed out";
                    } else if (error instanceof ServerError) {
                        errorMessage = "Server error";
                    } else if (error instanceof ParseError) {
                        errorMessage = "Data parsing error";
                    } else {
                        errorMessage = "Network request failed";
                    }

//                    showError(errorMessage);
                    binding.lvlNotfound.setVisibility(View.VISIBLE);
                    binding.recycleviewHistory.setVisibility(View.GONE);

                    // Log the error details
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {
                        Log.e("WalletActivity", "Error Status Code: " + networkResponse.statusCode);
                        Log.e("WalletActivity", "Error URL: " + url);
                        Log.e("WalletActivity", "Error Params: " + params.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add any other required headers
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateWalletUI(JSONObject results) throws JSONException {
        // Update wallet balance
        JSONObject walletDetails = results.getJSONObject("wallet_details");
        double balance = walletDetails.getDouble("current_balance");
        binding.txtWallet.setText(String.format("Balance ₹%.2f", balance));

        // Show add payment button if needed
        binding.addpayment.setVisibility(View.VISIBLE);

        // Update transaction history
        JSONArray transactions = results.getJSONArray("transaction_history");
        List<WalletTransaction> transactionList = new ArrayList<>();

        for (int i = 0; i < transactions.length(); i++) {
            JSONObject transaction = transactions.getJSONObject(i);
            transactionList.add(new WalletTransaction(
                    transaction.getString("transaction_id"),
                    transaction.getString("transaction_type"),
                    transaction.getDouble("amount"),
                    transaction.getString("status"),
                    transaction.getString("transaction_date"),
                    transaction.getString("remarks"),
                    transaction.getString("payment_mode"),
                    transaction.getString("razorpay_payment_id")
            ));
        }

        if (transactionList.isEmpty()) {
            binding.lvlNotfound.setVisibility(View.VISIBLE);
            binding.recycleviewHistory.setVisibility(View.GONE);
        } else {
            binding.lvlNotfound.setVisibility(View.GONE);
            binding.recycleviewHistory.setVisibility(View.VISIBLE);
            adapter.setTransactions(transactionList);
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            custPrograssbar.prograssCreate(this);
        } else {
            custPrograssbar.closePrograssBar();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}