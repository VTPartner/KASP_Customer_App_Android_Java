package com.kapstranspvtltd.kaps.common_activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityServiceDurationBinding;

public class ServiceDurationActivity extends AppCompatActivity {
    private ActivityServiceDurationBinding binding;
    private int selectedHours = 1;
    private int selectedDays = 1;
    private boolean isValid = true;

    double serviceBasePrice = 0.0;

    private int categoryId, subCatID, serviceID;

    String categoryName, subCatName, serviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceDurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        categoryName = Glb.categoryName;
        categoryId = Glb.categoryId;
        subCatName = Glb.sub_cat_name;
        subCatID = Integer.parseInt(Glb.sub_cat_id);
        serviceName = Glb.serviceName;
        serviceID = Glb.serviceID;
        serviceBasePrice = Double.parseDouble(Glb.sub_cat_base_price);

//        categoryName = getIntent().getStringExtra("category_name");
//        categoryId = getIntent().getIntExtra("category_id", -1);
//
//        subCatName = getIntent().getStringExtra("subCatName");
//        subCatID = getIntent().getIntExtra("subCatID", -1);
//
//        serviceName = getIntent().getStringExtra("service_name");
//        serviceID = getIntent().getIntExtra("service_id", -1);
//
//        serviceBasePrice = getIntent().getDoubleExtra("sub_cat_base_price", 0.0);
//
//
//        double subCatBasePrice = getIntent().getDoubleExtra("service_base_price", 0.0);
//
//        if (subCatBasePrice != 0.0) {
//            serviceBasePrice = subCatBasePrice;
//        }

        if (serviceBasePrice == 0.0) {
            Toast.makeText(this, "Please service base price not selected", Toast.LENGTH_LONG).show();
            return;
        }
        setupToolbar();
        setupInputs();
        setupDaysDropdown();
        setupContinueButton();
        updateDurationText();
    }

    private void setupToolbar() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupInputs() {
        binding.hoursInput.setText("1");
        binding.hoursInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateHours(s.toString());
            }
        });
    }

    private void setupDaysDropdown() {
        String[] days = new String[30];
        for (int i = 0; i < 30; i++) {
            days[i] = (i + 1) + " day" + (i > 0 ? "s" : "");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_item,
                days
        );

        binding.daysDropdown.setAdapter(adapter);
        binding.daysDropdown.setText(adapter.getItem(0), false);

        binding.daysDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedDays = position + 1;
            // Only disable hours input if more than 1 day is selected
            if (selectedDays > 1) {
                binding.hoursInput.setText("24"); // Set to 24 hours for multiple days
                binding.hoursInput.setEnabled(false);
            } else {
                binding.hoursInput.setEnabled(true);
                // Keep the current hours or set to 1 if empty
                if (binding.hoursInput.getText().toString().isEmpty()) {
                    binding.hoursInput.setText("1");
                }
            }
            updateDurationText();
        });
    }
    private void validateHours(String input) {
        if (input.isEmpty()) {
            binding.hoursInputLayout.setError(null);
            isValid = false;
            return;
        }

        int hours = Integer.parseInt(input);
        if (hours < 1 || hours > 24) {
            binding.hoursInputLayout.setError("Hours must be between 1 and 24");
            isValid = false;
        } else {
            binding.hoursInputLayout.setError(null);
            selectedHours = hours;
            isValid = true;
        }
        updateDurationText();
        updateContinueButton();
    }

    private void updateDurationText() {
        int totalHours;
        if (selectedDays > 1) {
            totalHours = selectedDays * 24;
        } else {
            // For 1 day, use the entered hours
            String hoursText = binding.hoursInput.getText().toString();
            totalHours = hoursText.isEmpty() ? 1 : Integer.parseInt(hoursText);
        }

        binding.durationText.setText(String.format(
                "Selected Duration: %d hours",
                totalHours
        ));
    }

    private void updateContinueButton() {
        binding.continueButton.setEnabled(isValid);
    }

    private void setupContinueButton() {
        binding.continueButton.setOnClickListener(v -> {
            int totalHours;
            if (selectedDays > 1) {
                totalHours = selectedDays * 24;
            } else {
                // For 1 day, use the entered hours
                String hoursText = binding.hoursInput.getText().toString();
                totalHours = hoursText.isEmpty() ? 1 : Integer.parseInt(hoursText);
            }

            Intent intent = new Intent(this, ServiceBookingReviewActivity.class);
            intent.putExtra("category_name", categoryName);
            intent.putExtra("category_id", categoryId);
            intent.putExtra("service_hours", totalHours);
            intent.putExtra("service_base_price", serviceBasePrice);
            intent.putExtra("service_id", serviceID);
            intent.putExtra("service_name", serviceName);
            intent.putExtra("sub_cat_id", subCatID);
            intent.putExtra("sub_cat_name", subCatName);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}