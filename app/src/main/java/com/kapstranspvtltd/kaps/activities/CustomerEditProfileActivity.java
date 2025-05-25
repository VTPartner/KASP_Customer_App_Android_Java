package com.kapstranspvtltd.kaps.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityCustomerEditProfileBinding;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomerEditProfileActivity extends AppCompatActivity {
    private ActivityCustomerEditProfileBinding binding;
    private String customerId;
    private ProgressDialog progressDialog;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
//        setSupportActionBar(binding.toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Edit Profile");
//        }
        preferenceManager = new PreferenceManager(this);
        customerId = preferenceManager.getStringValue("customer_id");
        if (customerId == null || customerId.isEmpty()) {
            Toast.makeText(this, "Invalid customer ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        loadCustomerDetails();
        setupUpdateButton();
    }

    private void loadCustomerDetails() {
        progressDialog.show();

        String fcmToken = preferenceManager.getStringValue("fcm_token");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("customer_id", customerId);
            jsonBody.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "get_customer_details",
                jsonBody,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject customer = response.getJSONObject("customer");
                        populateFields(customer);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing customer details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error loading customer details", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void populateFields(JSONObject customer) throws JSONException {
        binding.editTextName.setText(customer.getString("customer_name"));
        binding.editTextEmail.setText(customer.getString("email"));
        binding.editTextAddress.setText(customer.getString("full_address"));
        binding.editTextGstNo.setText(customer.getString("gst_no"));
        binding.editTextGstAddress.setText(customer.getString("gst_address"));
        binding.editTextPurpose.setText(customer.getString("purpose"));
        binding.editTextPincode.setText(customer.getString("pincode"));
        binding.editTextBankName.setText(customer.getString("bank_name"));
        binding.editTextIfscCode.setText(customer.getString("ifsc_code"));
        binding.editTextAccountNumber.setText(customer.getString("account_number"));
        binding.editTextAccountName.setText(customer.getString("account_name"));

        // Mobile number is displayed but not editable
        binding.textViewMobile.setText(customer.getString("mobile_no"));
    }

    private void setupUpdateButton() {
        binding.buttonUpdate.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        progressDialog.show();

        String fcmToken = preferenceManager.getStringValue("fcm_token");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("customer_id", customerId);
            jsonBody.put("customer_name", binding.editTextName.getText().toString());
            jsonBody.put("email", binding.editTextEmail.getText().toString());
            jsonBody.put("full_address", binding.editTextAddress.getText().toString());
            jsonBody.put("gst_no", binding.editTextGstNo.getText().toString());
            jsonBody.put("gst_address", binding.editTextGstAddress.getText().toString());
            jsonBody.put("purpose", binding.editTextPurpose.getText().toString());
            jsonBody.put("pincode", binding.editTextPincode.getText().toString());
            jsonBody.put("bank_name", binding.editTextBankName.getText().toString());
            jsonBody.put("ifsc_code", binding.editTextIfscCode.getText().toString());
            jsonBody.put("account_number", binding.editTextAccountNumber.getText().toString());
            jsonBody.put("account_name", binding.editTextAccountName.getText().toString());
            jsonBody.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "update_customer_details",
                jsonBody,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}