package com.kapstranspvtltd.kaps.activities;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.LocationHelper;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityCustomerRegistrationBinding;
import com.kapstranspvtltd.kaps.utility.Utility;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomerRegistrationActivity extends BaseActivity {
    private ActivityCustomerRegistrationBinding binding;
    private PreferenceManager preferenceManager;
    private LocationHelper locationHelper;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        binding = ActivityCustomerRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Utility.applyEdgeToEdgePadding(binding.getRoot());
        locationHelper = new LocationHelper(this);
        preferenceManager = new PreferenceManager(this);
        requestQueue = Volley.newRequestQueue(this);

        setupViews();
    }

    private void setupViews() {
        binding.scrollView.setFocusable(true);
        binding.scrollView.setFocusableInTouchMode(true);
        binding.scrollView.requestFocus();

        binding.backButton.setOnClickListener(v -> finish());

        // Setup account type spinner
        String[] accountTypes = {"Personal", "Business"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                accountTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.accountTypeSpinner.setAdapter(adapter);

        // Show phone number
        String phoneNumber = preferenceManager.getStringValue("customer_mobile_no");
        binding.phoneNumberText.setText(phoneNumber);

        binding.continueButton.setOnClickListener(v -> {
            if (validateInputs()) {
                registerCustomer();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String fullName = binding.fullNameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();

        // Validate full name
        if (fullName.isEmpty()) {
            binding.fullNameInput.setError("Full name required");
            isValid = false;
        }

        // Validate email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError("Valid email required");
            isValid = false;
        }

        return isValid;
    }

    private void registerCustomer() {
        showLoading(true);

        String fullName = binding.fullNameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String accountType = binding.accountTypeSpinner.getSelectedItem().toString();

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallbackListener() {
            @Override
            public void onSuccess(LocationHelper.LocationDetails locationDetails) {
                String customerId = preferenceManager.getStringValue("customer_id");
                String fcmToken = preferenceManager.getStringValue("fcm_token");

                Map<String, String> params = new HashMap<>();
                params.put("customer_id", customerId);
                params.put("full_address", locationDetails.getAddress());
                params.put("customer_name", fullName);
                params.put("email", email);
                params.put("purpose", accountType);
                params.put("pincode", locationDetails.getPostalCode());
                params.put("r_lat", String.valueOf(locationDetails.getLatitude()));
                params.put("r_lng", String.valueOf(locationDetails.getLongitude()));
                params.put("auth", fcmToken);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        APIClient.baseUrl + "customer_registration",
                        new JSONObject(params),
                        response -> {
                            showLoading(false);
                            saveUserDetails(fullName, email, locationDetails.getAddress());
                            navigateToHome();
                        },
                        error -> {
                            error.printStackTrace();
                            System.out.println("customer registration error::"+error.getMessage());
                            showLoading(false);
                            showError("Registration failed. Please try again.");
                        }
                );

                requestQueue.add(request);
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                showError("Failed to get location: " + e.getMessage());
            }
        });
    }

    private void saveUserDetails(String name, String email, String address) {
        preferenceManager.saveStringValue("customer_name", name);
        preferenceManager.saveStringValue("email", email);
        preferenceManager.saveStringValue("full_address", address);
    }

    private void showLoading(boolean show) {
        binding.progressBarContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.continueButton.setEnabled(!show);
        binding.backButton.setEnabled(!show);
        binding.fullNameInput.setEnabled(!show);
        binding.emailInput.setEnabled(!show);
        binding.accountTypeSpinner.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
    }
}