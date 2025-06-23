package com.kapstranspvtltd.kaps.common_activities;

import static com.kapstranspvtltd.kaps.common_activities.Glb.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.models.GuidelineModel;
import com.kapstranspvtltd.kaps.activities.models.UpgradePrice;
import com.kapstranspvtltd.kaps.activities.goods_service_booking_activities.CouponCodeActivity;
import com.kapstranspvtltd.kaps.adapters.GuidelinesAdapter;
import com.kapstranspvtltd.kaps.databinding.ActivityServiceBookingReviewBinding;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.DriverAgentSearchingActivity;
import com.kapstranspvtltd.kaps.fcm.AccessToken;
import com.kapstranspvtltd.kaps.handyman_customer_app.activities.HandymanSearchingActivity;
import com.kapstranspvtltd.kaps.jcb_crane_customer_app.activities.JcbCraneSearchingActivity;
import com.kapstranspvtltd.kaps.model.Coupon;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ServiceBookingReviewActivity extends AppCompatActivity {
    private static final int COUPON_REQUEST_CODE = 100;
    private ActivityServiceBookingReviewBinding binding;

    private double finalAmount = 0.0;
    private double totalPrice = 0.0;
    private double discountAmount = 0.0;
    private String appliedCouponCode = "";

    int couponId = -1;
    private GuidelinesAdapter guidelinesAdapter;
    private List<GuidelineModel> guidelines = new ArrayList<>();

    PreferenceManager preferenceManager;

    CustPrograssbar custPrograssbar;

    private boolean isScheduledBooking = false;
    private String scheduledTime = "";

    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceBookingReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        custPrograssbar = new CustPrograssbar();

        initializeViews();
        setupListeners();
        loadData();
        setupScheduledBookings();
    }

    private void setupScheduledBookings() {
        binding.bookingTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isScheduledBooking = checkedId == R.id.rbSchedule;
            binding.scheduleTimeLayout.setVisibility(isScheduledBooking ? View.VISIBLE : View.GONE);

            if (isScheduledBooking) {
                binding.selectedTimeText.setText("Select Time");
                scheduledTime = "";
            }
        });

        binding.selectedTimeText.setOnClickListener(v -> showTimePickerDialog());
    }

    private void showTimePickerDialog() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, selectedMinute) -> {
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, selectedMinute);

                    // Check if selected time is valid (after current time and within same day)
                    if (selectedTime.before(currentTime)) {
                        showError("Please select a future time");
                        return;
                    }

                    Calendar endOfDay = Calendar.getInstance();
                    endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                    endOfDay.set(Calendar.MINUTE, 59);

                    if (selectedTime.after(endOfDay)) {
                        showError("Please select a time within today");
                        return;
                    }

                    // Save selected time
                    selectedDateTime = selectedTime;
                    isScheduledBooking = true;

                    // Format time for display
                    String formattedTime = String.format("%02d:%02d", hourOfDay, selectedMinute);
                    binding.selectedTimeText.setText("Selected Time: " + formattedTime);

                    // Save for API
                    scheduledTime = String.format("%02d:%02d:00", hourOfDay, selectedMinute);
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    private void initializeViews() {
        // Setup RecyclerView
//        guidelinesAdapter = new GuidelinesAdapter(guidelines);
        binding.guidelinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.guidelinesRecyclerView.setAdapter(guidelinesAdapter);

        // Set initial service details
//        String serviceName = getIntent().getStringExtra("service_name");
//        String subCategoryName = getIntent().getStringExtra("sub_category_name");
//        categoryName = Glb.categoryName;
//        categoryId = Glb.categoryId;
//        subCatName = Glb.sub_cat_name;
//        subCatID = Integer.parseInt(Glb.sub_cat_id);
//        serviceName = Glb.serviceName;
//        serviceID = Glb.serviceID;
//        serviceBasePrice = Double.parseDouble(Glb.sub_cat_base_price);

        binding.serviceName.setText(Glb.serviceName.isEmpty() == false ? Glb.sub_cat_name + " / " + Glb.serviceName : Glb.sub_cat_name);
        System.out.println("Glb.categoryId::" + Glb.categoryId + " Glb.exactDistance:" + Glb.exactDistance);
        if (Glb.categoryId == 4) { // For driver category
//            String distance = getIntent().getStringExtra("distance");
//            String duration = getIntent().getStringExtra("duration");
            String distance = Glb.exactDistance;
            String duration = Glb.exactTime;
            binding.serviceDetails.setVisibility(View.VISIBLE);
            binding.serviceDetails.setText("Distance: " + distance + " | Duration: " + duration);
        } else {
            binding.serviceDetails.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.checkServiceDetails.setOnClickListener(v -> {
            finish();
            finish();
        });

        binding.applyCouponLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, CouponCodeActivity.class);
            intent.putExtra("category_id", Glb.categoryId);
            intent.putExtra("total_amount", totalPrice);
            startActivityForResult(intent, COUPON_REQUEST_CODE);
        });

        binding.removeCoupon.setOnClickListener(v -> removeCoupon());

//        binding.proceedButton.setOnClickListener(v -> proceedWithBooking());
//        binding.proceedButton.setOnClickListener(v -> showPriceAdjustmentSheet());
        boolean showHikePrice = preferenceManager.getStringValue("hike_price_show", "No")
                .equalsIgnoreCase("Yes");

        binding.proceedButton.setOnClickListener(v -> {
            if (showHikePrice) {
                showPriceAdjustmentSheet();
            } else {
                saveBookingDetails();
            }
        });
    }

    private void loadData() {
        calculateInitialPrice();
        fetchGuidelines();
    }

    private void calculateInitialPrice() {
        double servicePrice = getIntent().getDoubleExtra("service_price_per_hour", 0);
        int serviceHours = getIntent().getIntExtra("service_hours", 0);
        double basePrice = getIntent().getDoubleExtra("service_base_price", 0);

//        totalPrice = Math.max(basePrice * serviceHours, basePrice);
        finalAmount = totalPrice = basePrice * serviceHours;
        updatePriceDetails();
    }

    private void updatePriceDetails() {
        // Calculate final price after discount
        double finalPrice = totalPrice - discountAmount;

        // Update UI elements
        binding.tripFareText.setText(String.format("₹%.2f", totalPrice));
        binding.serviceHoursText.setText(String.format("%d Hrs",
                getIntent().getIntExtra("service_hours", 0)));

        if (discountAmount > 0) {
            binding.discountLayout.setVisibility(View.VISIBLE);
            binding.discountText.setText(String.format("-₹%.2f", discountAmount));
        } else {
            binding.discountLayout.setVisibility(View.GONE);
        }

        // Set finalAmount after applying discount and any adjustments
        finalAmount = adjustedPrice > 0 ? adjustedPrice : Math.round(finalPrice); // Changed from totalPrice to finalPrice

        // Update UI with final amount
        binding.finalAmountText.setText(String.format("₹%.2f", finalAmount));
        binding.bottomSheetAmount.setText(String.format("₹%.2f", finalAmount));
    }

    private void fetchGuidelines() {
        // Show loading
//        progressBar.setVisibility(View.VISIBLE);

        // Generate random reward points
        Random random = new Random();
        int randomNumber = random.nextInt(10) + 1;

        // Save to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.putInt("reward_goods_points", randomNumber);
        editor.apply();

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        // Create request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("category_id", Glb.categoryId); // Replace with your category ID
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        String url = APIClient.baseUrl + "get_all_guide_lines";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
//                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<GuidelineModel> guidelines = new ArrayList<>();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject guidelineJson = results.getJSONObject(i);
                            GuidelineModel guideline = GuidelineModel.fromJson(guidelineJson);
                            guidelines.add(guideline);
                        }

                        // Update RecyclerView

                        GuidelinesAdapter guidelinesAdapter = new GuidelinesAdapter();
                        binding.guidelinesRecyclerView.setAdapter(guidelinesAdapter);
                        binding.guidelinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                        guidelinesAdapter.submitList(guidelines);
                        System.out.println("Guidelines loaded: " + guidelines.size());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("Error parsing guidelines");
                    }
                },
                error -> {
//                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        System.out.println("No Guidelines Found");
                    } else {
                        System.out.println("Error loading guidelines");
                    }
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

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void proceedWithBooking() {

        saveBookingDetails();
    }

    private void saveBookingDetails() {
        if (isScheduledBooking && scheduledTime.isEmpty()) {
            showError("Please select a schedule time");
            return;
        }
        showLoading(true);

        try {
            String cityId = preferenceManager.getStringValue("city_id");
            String customerId = preferenceManager.getStringValue("customer_id");
            String fcmToken = preferenceManager.getStringValue("fcm_token");

            String url = "";

            if (Glb.categoryId == 3) { // Jcb Crane Booking
                url = APIClient.baseUrl + "generate_new_jcb_crane_booking_id_get_nearby_agents_with_fcm_token";
            } else if (Glb.categoryId == 4) { //Driver Booking
                url = APIClient.baseUrl + "generate_new_other_driver_booking_id_get_nearby_agents_with_fcm_token";
            } else { // HandyMan
                url = APIClient.baseUrl + "generate_new_handyman_booking_id_get_nearby_agents_with_fcm_token";
            }


            String serverAccessToken = AccessToken.getAccessToken();

            //discountAmount,couponId,totalPrice
            double beforeCouponAmount = totalPrice;

            double serviceFare = totalPrice - discountAmount;

            int serviceHours = getIntent().getIntExtra("service_hours", 0);

            System.out.println("service totalPrice before hike price::"+serviceFare);
            if(adjustedPrice>0){
                adjustedPrice-=serviceFare;
            }
            serviceFare+=adjustedPrice;
            System.out.println("cab adjustedPrice::"+adjustedPrice);

            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            try {
                if (categoryId == 4) {
                    jsonBody.put("destination_lat", drop.getLat());
                    jsonBody.put("destination_lng", drop.getLog());
                    jsonBody.put("drop_address", drop.getAddress());
                }

                jsonBody.put("sub_cat_id", Integer.parseInt(sub_cat_id));
                jsonBody.put("service_id", serviceID);
                jsonBody.put("radius_km", 5);
                jsonBody.put("customer_id", customerId);
                jsonBody.put("pickup_lat", pickup.getLat());
                jsonBody.put("pickup_lng", pickup.getLog());

                jsonBody.put("distance", totalDistanceValue);
                jsonBody.put("time", exactTime);
                jsonBody.put("total_price", serviceFare);
                jsonBody.put("base_price", service_base_price);
                jsonBody.put("gst_amount", 0);
                jsonBody.put("igst_amount", 0);

                jsonBody.put("payment_method", "NA");
                jsonBody.put("city_id", cityId);

                jsonBody.put("pickup_address", pickup.getAddress());
                jsonBody.put("service_hour", serviceHours);
                jsonBody.put("server_access_token", serverAccessToken);
                jsonBody.put("coupon_applied", discountAmount > 0 ? "Yes" : "No");
                jsonBody.put("coupon_id", couponId);
                jsonBody.put("coupon_amount", discountAmount);
                jsonBody.put("before_coupon_amount", beforeCouponAmount);

                jsonBody.put("hike_price", adjustedPrice);
                jsonBody.put("auth", fcmToken);

                jsonBody.put("is_scheduled", isScheduledBooking);
                if (isScheduledBooking) {
                    if (scheduledTime.isEmpty()) {
                        showError("Please select a schedule time");
                        return;
                    }
                    jsonBody.put("scheduled_time", scheduledTime);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                showLoading(false);
                showError("Error creating request body: " + e.getMessage());
                return;
            }

            Log.d("SaveBooking", "Request Body: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            showLoading(false);
                            Log.d("SaveServiceBooking", "Response: " + response.toString());

                            JSONArray results = response.getJSONArray("result");
                            if (results.length() > 0) {
                                String bookingId = "";

                                bookingId = results.getJSONObject(0).getString("booking_id");

                                if(adjustedPrice>0){
                                    finalAmount-=adjustedPrice;
                                    binding.finalAmountText.setText(String.format("₹%d", (int)finalAmount));
                                    binding.bottomSheetAmount.setText(String.format("₹%d", (int)finalAmount));
                                    adjustedPrice=0;
                                }

                                if (isScheduledBooking) {
                                    Toast.makeText(this, "Your booking has been scheduled. A agent will be assigned at the scheduled time.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                } else {
                                    Intent intent;

                                    switch (categoryId) {


                                        case 3: // JCB/Crane
                                            intent = new Intent(this, JcbCraneSearchingActivity.class);
                                            intent.putExtra("booking_id", bookingId);
                                            intent.putExtra("pickup", pickup);
                                            startActivity(intent);
                                            break;

                                        case 4: // Driver Agent
                                            intent = new Intent(this, DriverAgentSearchingActivity.class);
                                            intent.putExtra("booking_id", bookingId);
                                            intent.putExtra("pickup", pickup);
                                            intent.putExtra("drop", drop);
                                            startActivity(intent);
                                            break;

                                        case 5: // Handyman
                                            intent = new Intent(this, HandymanSearchingActivity.class);
                                            intent.putExtra("booking_id", bookingId);
                                            intent.putExtra("pickup", pickup);
                                            startActivity(intent);
                                            break;

                                        default:
                                            Toast.makeText(this, "Invalid category.", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }



                            } else {
                                showError("No booking ID received");
                            }
                        } catch (Exception e) {
                            showError("Error processing response: " + e.getMessage());
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        showLoading(false);
                        if (error instanceof NoConnectionError) {
                            showError("No internet connection");
                        } else if (error instanceof TimeoutError) {
                            showError("Request timed out");
                        } else if (error instanceof ServerError) {
                            if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                                showError("No drivers available");
                            } else {
                                showError("Server error");
                            }
                        } else {
                            showError("Error creating booking: " + error.getMessage());
                        }
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

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            showLoading(false);
            showError("Error preparing request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        binding.proceedButton.setEnabled(!show);
        // Show/hide loading indicator
        if (show)
            custPrograssbar.prograssCreate(this);
        else custPrograssbar.closePrograssBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COUPON_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Coupon selectedCoupon = (Coupon) data.getSerializableExtra("selected_coupon");
            discountAmount = selectedCoupon.getDiscountValue();
            couponId = selectedCoupon.getCouponId();
            appliedCouponCode = data.getStringExtra("coupon_code");
//            discountAmount = data.getDoubleExtra("discount_amount", 0);

            binding.applyCouponLayout.setVisibility(View.GONE);
            binding.appliedCouponLayout.setVisibility(View.VISIBLE);
//            binding.appliedCouponCode.setText("Coupon Applied: " + appliedCouponCode);
            binding.couponDiscount.setText("You saved ₹" + discountAmount);

            updatePriceDetails();
        }
    }

    private void removeCoupon() {
        appliedCouponCode = "";
        discountAmount = 0;
        binding.applyCouponLayout.setVisibility(View.VISIBLE);
        binding.appliedCouponLayout.setVisibility(View.GONE);
        updatePriceDetails();
    }

    private double originalPrice = 0.0;
    private double adjustedPrice = 0.0;
    private BottomSheetDialog priceAdjustmentDialog;

    private List<UpgradePrice> upgradePrices = new ArrayList<>();

    private void showPriceAdjustmentSheet() {
        priceAdjustmentDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.price_adjustment_bottom_sheet, null);
        priceAdjustmentDialog.setContentView(sheetView);

        // Initialize views
        ImageView ivClose = sheetView.findViewById(R.id.ivClose);
        TextView tvAdjustedPrice = sheetView.findViewById(R.id.tvAdjustedPrice);
        TextView titleText = sheetView.findViewById(R.id.ptitleTxt);
        TextView priceText = sheetView.findViewById(R.id.ppriceText);
        Slider priceSlider = sheetView.findViewById(R.id.priceSlider);
        Button btnConfirmPrice = sheetView.findViewById(R.id.btnConfirmPrice);
        LinearLayout priceRangeContainer = sheetView.findViewById(R.id.priceRangeContainer);

        titleText.setText("Set Your Price\nfor Quick Service at Your Doorstep");
        priceText.setText("Offering a fair price helps you get\nmatched with a service provider faster");


        // Set initial values
        originalPrice = finalAmount;
        adjustedPrice = originalPrice;
        tvAdjustedPrice.setText("₹" + (int)adjustedPrice);
        btnConfirmPrice.setText("Book " + (!Glb.serviceName.isEmpty() ? Glb.sub_cat_name + " / " + Glb.serviceName : Glb.sub_cat_name) + " for ₹" + (int) adjustedPrice);


        // Fetch upgrade prices
        fetchUpgradePrices(serviceID, priceRangeContainer, priceSlider,tvAdjustedPrice,btnConfirmPrice);

        // Handle close
        ivClose.setOnClickListener(v -> {
            adjustedPrice = originalPrice;
            updatePriceDetails();
            priceAdjustmentDialog.dismiss();
        });

        // Handle confirm
        btnConfirmPrice.setOnClickListener(v -> {
            finalAmount = adjustedPrice;
            updatePriceDetails();
            priceAdjustmentDialog.dismiss();
            saveBookingDetails();
        });

        priceAdjustmentDialog.show();
    }

    private void fetchUpgradePrices(int serviceID, LinearLayout container, Slider slider, TextView tvAdjustedPrice, Button btnConfirmPrice) {
        try {

            String customerId = preferenceManager.getStringValue("customer_id");
            String fcmToken = preferenceManager.getStringValue("fcm_token");


            JSONObject requestBody = new JSONObject();
            requestBody.put("sub_cat_id", sub_cat_id);
            requestBody.put("service_id", serviceID);
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "get_service_upgrade_prices",
                    requestBody,
                    response -> {
                        try {
                            // Show only base price option
                            container.removeAllViews();
                            container.addView(createPriceRangeView("Base", 0));

                            // Hide slider by default
                            slider.setVisibility(View.GONE);

                            // Check if upgrade prices exist
                            if (response.has("upgrade_prices")) {
                                JSONArray upgradeArray = response.getJSONArray("upgrade_prices");
                                if (upgradeArray.length() > 0) {
                                    // Show slider if we have upgrade prices
                                    slider.setVisibility(View.VISIBLE);
                                    setupUpgradePrices(upgradeArray, container, slider, tvAdjustedPrice, btnConfirmPrice);
                                }
                            }

                            // Initial price display
                            adjustedPrice = originalPrice;
                            updatePriceDisplay(tvAdjustedPrice, btnConfirmPrice, 0);
                            highlightSelectedPriceRange(container, 0);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            handleUpgradePricesError(container, slider, tvAdjustedPrice, btnConfirmPrice);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            // Handle 404 - show only base price
                            handleUpgradePricesError(container, slider, tvAdjustedPrice, btnConfirmPrice);
                        } else {
                            showError("Error loading price ranges");
                        }
                    }
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            e.printStackTrace();
            handleUpgradePricesError(container, slider, tvAdjustedPrice, btnConfirmPrice);
        }
    }

    private void handleUpgradePricesError(LinearLayout container, Slider slider, TextView tvAdjustedPrice, Button btnConfirmPrice) {
        // Clear container and show only base price
        container.removeAllViews();
        container.addView(createPriceRangeView("Base", 0));

        // Hide slider
        slider.setVisibility(View.GONE);

        // Set initial price display
        adjustedPrice = originalPrice;
        updatePriceDisplay(tvAdjustedPrice, btnConfirmPrice, 0);
        highlightSelectedPriceRange(container, 0);
    }

    private void setupUpgradePrices(JSONArray upgradeArray, LinearLayout container, Slider slider, TextView tvAdjustedPrice, Button btnConfirmPrice) throws JSONException {
        upgradePrices.clear();
        List<Float> validSteps = new ArrayList<>();
        validSteps.add(0f);

        for (int i = 0; i < upgradeArray.length(); i++) {
            JSONObject upgrade = upgradeArray.getJSONObject(i);
            float price = (float) upgrade.getDouble("price");
            String name = upgrade.getString("upgrade_name");

            upgradePrices.add(new UpgradePrice(price, name));
            container.addView(createPriceRangeView(name, price));
            validSteps.add(price);
        }

        // Configure slider
        float maxUpgrade = validSteps.get(validSteps.size() - 1);
        slider.setValueFrom(0);
        slider.setValueTo(maxUpgrade);

        // Calculate step size based on number of valid steps
        float stepSize = maxUpgrade / (validSteps.size() - 1);
        slider.setStepSize(stepSize);

        // Update slider listener
        slider.addOnChangeListener((s, value, fromUser) -> {
            float closestStep = findClosestValidStep(value, validSteps);
            adjustedPrice = originalPrice + closestStep;
            updatePriceDisplay(tvAdjustedPrice, btnConfirmPrice, closestStep);
            highlightSelectedPriceRange(container, closestStep);
        });
    }
    private float findClosestValidStep(float value, List<Float> validSteps) {
        float closest = validSteps.get(0);
        float minDiff = Math.abs(value - closest);

        for (float step : validSteps) {
            float diff = Math.abs(value - step);
            if (diff < minDiff) {
                minDiff = diff;
                closest = step;
            }
        }
        return closest;
    }

    private void updatePriceDisplay(TextView tvAdjustedPrice, Button btnConfirmPrice, float upgrade) {
        tvAdjustedPrice.setText(String.format("₹%d", (int)adjustedPrice));
        String buttonText = upgrade == 0 ?
                String.format("Book %s for ₹%d", (!Glb.serviceName.isEmpty() ? Glb.sub_cat_name + " / " + Glb.serviceName : Glb.sub_cat_name), (int)adjustedPrice) :
                String.format("Book %s for ₹%d (+₹%d)", (!Glb.serviceName.isEmpty() ? Glb.sub_cat_name + " / " + Glb.serviceName : Glb.sub_cat_name), (int)adjustedPrice, (int)upgrade);
        btnConfirmPrice.setText(buttonText);
    }

    private void highlightSelectedPriceRange(LinearLayout container, float selectedUpgrade) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                float upgradeValue = i == 0 ? 0 : upgradePrices.get(i-1).price;

                if (Math.abs(upgradeValue - selectedUpgrade) < 0.01) { // Use small epsilon for float comparison
                    tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    tv.setTextColor(getResources().getColor(R.color.grey));
                    tv.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }


    private View createPriceRangeView(String label, float price) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        tv.setText(price == 0 ? "Base" : "+" + (int)price);
        tv.setTextColor(getResources().getColor(R.color.grey));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16, 8, 16, 8);
        return tv;
    }

}