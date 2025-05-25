package com.kapstranspvtltd.kaps.activities;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kapstranspvtltd.kaps.model.CountryCodeItem;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.SessionManager;
import com.kapstranspvtltd.kaps.utility.Utility;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityLoginBinding;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private List<CountryCodeItem> cCodes = new ArrayList<>();
    private String codeSelect;
    private SessionManager sessionManager;
    private CustPrograssbar custPrograssbar;
    private static final int DEFAULT_INDIA_POSITION = 0; // Will be updated when we get codes

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setupClickListeners();
        setupLocationServices();
        getCountryCodes();
        setupMobileValidation();
    }

    private void setupLocationServices() {
        requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && Utility.hasGPSDevice(this)) {
            Toast.makeText(this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            Utility.enableLoc(this);
        }
    }

    private void setupMobileValidation() {
        binding.edMobile.addTextChangedListener(new TextWatcher() {
            private boolean isProcessing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Skip if we're already processing to avoid recursion
                if (isProcessing) return;

                // Handle pasted text
                if (count > 1) {
                    isProcessing = true;
                    String input = s.toString();

                    // Remove common country code prefixes
                    input = input.replaceAll("^\\+?91|^0091|^91", "");

                    // Remove any non-digit characters
                    input = input.replaceAll("[^0-9]", "");

                    // Take last 10 digits if longer
                    if (input.length() > 10) {
                        input = input.substring(input.length() - 10);
                    }

                    // Update text field
                    binding.edMobile.setText(input);
                    binding.edMobile.setSelection(input.length());

                    isProcessing = false;
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isProcessing) return;

                isProcessing = true;

                // Handle single character input
                String filtered = s.toString().replaceAll("[^0-9]", "");

                if (filtered.length() > 10) {
                    filtered = filtered.substring(0, 10);
                }

                if (!filtered.equals(s.toString())) {
                    binding.edMobile.setText(filtered);
                    binding.edMobile.setSelection(filtered.length());
                }

                isProcessing = false;
            }
        });
    }

    private void getCountryCodes() {
        cCodes.clear();


        cCodes.add(new CountryCodeItem("IN", "+91", "India", R.drawable.ic_flag_india));

        // Create adapter for spinner
        CountryCodeAdapter adapter = new CountryCodeAdapter(this, cCodes);
        binding.spinner.setAdapter(adapter);

        // Set India as default
        binding.spinner.setSelection(DEFAULT_INDIA_POSITION);
    }

    private void setupClickListeners() {
        binding.imgBack.setOnClickListener(v -> finish());

        binding.txtContinue.setOnClickListener(v -> {
            if (isValidMobileNumber(binding.edMobile.getText().toString())) {
                String mobileNumber = binding.edMobile.getText().toString();
                String countryCode = codeSelect; // Get selected country code

                // Create intent for OTP screen
                Intent intent = new Intent(LoginActivity.this, SendOTPActivity.class);
                intent.putExtra("mobile", mobileNumber);
                intent.putExtra("countryCode", countryCode);
                startActivity(intent);
            }
        });
    }

    private boolean isValidMobileNumber(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            binding.edMobile.setError("Please enter mobile number");
            return false;
        }

        if (mobile.length() != 10) {
            binding.edMobile.setError("Mobile number must be 10 digits");
            return false;
        }

        if (!mobile.matches("^[0-9]*$")) {
            binding.edMobile.setError("Invalid mobile number");
            return false;
        }

        return true;
    }

    // Create CountryCodeAdapter class
    private class CountryCodeAdapter extends ArrayAdapter<CountryCodeItem> {
        public CountryCodeAdapter(Context context, List<CountryCodeItem> codes) {
            super(context, R.layout.item_country_code, codes);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_country_code, parent, false);
            }

            CountryCodeItem item = getItem(position);
            if (item != null) {
                ImageView flagImage = view.findViewById(R.id.flag_image);
                TextView codeText = view.findViewById(R.id.code_text);

                flagImage.setImageResource(item.getFlagResource());
                codeText.setText(item.getCode());
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

}