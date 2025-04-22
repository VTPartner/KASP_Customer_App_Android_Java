package com.kapstranspvtltd.kaps.cab_customer_app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.models.AllGoodsTypesModel;
import com.kapstranspvtltd.kaps.activities.models.GuidelineModel;
import com.kapstranspvtltd.kaps.activities.models.VehicleModel;
import com.kapstranspvtltd.kaps.activities.pickup_activities.BookingReviewScreenActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.CouponCodeActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.SearchingGoodsDriverActivity;
import com.kapstranspvtltd.kaps.adapters.GoodsTypeAdapter;
import com.kapstranspvtltd.kaps.adapters.GuidelinesAdapter;
import com.kapstranspvtltd.kaps.adapters.VehicleAdapter;
import com.kapstranspvtltd.kaps.databinding.ActivityBookingReviewScreenBinding;
import com.kapstranspvtltd.kaps.databinding.ActivityCabBookingReviewBinding;
import com.kapstranspvtltd.kaps.fcm.AccessToken;
import com.kapstranspvtltd.kaps.model.Coupon;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CabBookingReviewActivity extends BaseActivity implements VehicleAdapter.RecyclerTouchListener {

    private ActivityCabBookingReviewBinding binding;

    boolean isCouponApplied = false;

    private double tripFare = 0.0;
    private double couponDiscount = 0.0;
    private double netFare = 0.0;
    private double finalAmount = 0.0;
    private int couponId = -1;


    //    double couponDiscountAmount = 0.0;
    private VehicleAdapter vehicleAdapter;

    GoodsTypeAdapter goodsTypeAdapter;
    RecyclerView recyclerView;
    //    private GoodsTypeAdapter goodsTypeAdapter;
//    private GuidelinesAdapter guidelinesAdapter;
    private ProgressDialog progressDialog;
    private String cityId;

    boolean cabService;
    private double totalDistance;
    private long totalDuration;

    private GoogleMap mMap;

    private CustPrograssbar custPrograssbar;
    private SessionManager sessionManager;
    private Pickup pickup;
    private Drop drop;
    private double tWallet = 0;
    private double itemPrice = 0;
    private double totalPrice = 0;

    private String exactTime = "", exactDistance = "";

    private VehicleModel selectedVehicle = null;

    PreferenceManager preferenceManager;

    String priceTypeId = "1";

    private TextView txtGoodType;
    private List<AllGoodsTypesModel> goodsTypesList = new ArrayList<>();
    private AllGoodsTypesModel selectedGoodsType;
    private BottomSheetDialog bottomSheetDialog;
    private int REQUEST_CODE_COUPON = 1000;

    int categoryId;

    String categoryName;

    private boolean isScheduledBooking = false;
    private String scheduledTime = "";
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCabBookingReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        categoryId = getIntent().getIntExtra("category_id", 1);
        categoryName = getIntent().getStringExtra("category_name");


        // Get intent extras
        if (getIntent() != null) {
            cabService = getIntent().getBooleanExtra("cab", false);
            // For Android 13 (API 33) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickup = getIntent().getParcelableExtra("pickup", Pickup.class);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    drop = getIntent().getParcelableExtra("drop", Drop.class);
                }
            } else {
                // For older Android versions
                pickup = getIntent().getParcelableExtra("pickup");
                drop = getIntent().getParcelableExtra("drop");
            }

            totalDistance = getIntent().getDoubleExtra("total_distance", 0.0);
            totalDuration = getIntent().getLongExtra("total_time", 0);
            exactTime = getIntent().getStringExtra("exact_time");
            exactDistance = getIntent().getStringExtra("exact_distance");

            // Log the received values
            Log.d("BookingReview", "Distance: " + totalDistance + " km");
            Log.d("BookingReview", "Duration: " + formatDuration(totalDuration));
            Log.d("BookingReview", "Pickup: " + (pickup != null ? pickup.getAddress() : "null"));
            Log.d("BookingReview", "Drop: " + (drop != null ? drop.getAddress() : "null"));
        }

        // Validate data
        if (pickup == null || drop == null) {
            Toast.makeText(this, "Error: Missing location data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        initViews();
        getIntentData();
        setupRecyclerViews();
        //fetchGoodsTypes();
        // Load all required data
        loadVehicles();
        fetchGuidelines();


        binding.txtApplycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CabBookingReviewActivity.this, CouponCodeActivity.class)
                        .putExtra("category_id", categoryId);

                startActivityForResult(intent, 1000);
            }
        });

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


    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    private void updateFareBreakdown() {
        // Store current guidelines visibility
        int guidelinesVisibility = binding.recyclerGuidelines.getVisibility();
        // Format currency
        DecimalFormat df = new DecimalFormat("0.00");
        if(selectedVehicle != null) tripFare = selectedVehicle.getPricePerKm();
        double baseFare = selectedVehicle.getBaseFare();
        if(tripFare < baseFare){
            binding.applyCouponLayout.setVisibility(View.GONE);
            tripFare = baseFare;
        }else{
            binding.applyCouponLayout.setVisibility(View.VISIBLE);
        }
        // Update trip fare
        binding.tripFareAmount.setText(String.format("₹%s", df.format(tripFare)));

        // Show/hide coupon discount
        if (isCouponApplied && couponDiscount > 0) {
            binding.couponLayout.setVisibility(View.VISIBLE);
            binding.couponDiscountAmount.setText(String.format("-₹%s", df.format(couponDiscount)));
        } else {
            binding.couponLayout.setVisibility(View.GONE);
        }

        // Calculate net fare
        netFare = tripFare - couponDiscount;
        binding.netFareAmount.setText(String.format("₹%s", df.format(netFare)));

        // Calculate final amount (rounded)
        finalAmount = Math.round(netFare);
        binding.finalAmount.setText(String.format("₹%d", (int) finalAmount));

        // Update bottom sheet amount
        binding.bottomTotalAmount.setText(String.format("₹%d", (int) finalAmount));

        // Update base fare note if needed
        if (selectedVehicle != null) {
            binding.baseFareNote.setText(String.format(
                    "If amount is less than base fare then you have to pay base fare. ₹%.2f",
                    selectedVehicle.getBaseFare()
            ));
        }

        // Restore guidelines visibility
        binding.recyclerGuidelines.post(() -> {
            binding.recyclerGuidelines.setVisibility(guidelinesVisibility);
        });
    }

    private void setupRecyclerViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.recyclerVehicles.setLayoutManager(layoutManager);
        binding.recyclerVehicles.setItemAnimator(new DefaultItemAnimator());

        // Initialize adapter with empty list
        vehicleAdapter = new VehicleAdapter(
                this,
                new ArrayList<>(),
                this,
                totalDistance
        );
        binding.recyclerVehicles.setAdapter(vehicleAdapter);
    }

    // Implement RecyclerTouchListener methods
    @Override
    public void onClickVehicleItem(VehicleModel item, int position) {
        // Show vehicle details dialog/bottom sheet
        selectedVehicle = item;
        binding.btnBook.setText("Book " + item.getVehicleName());
        updateFareBreakdown();

//        showVehicleDetails(item);
    }

    @Override
    public void onClickVehicleInfo(VehicleModel item, int position) {
        // Handle vehicle selection
        selectedVehicle = item;
        showVehicleDetailsBottomSheet(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_COUPON && resultCode == RESULT_OK) {
            Coupon selectedCoupon = (Coupon) data.getSerializableExtra("selected_coupon");
            applyCoupon(selectedCoupon);
        }
    }

//    private void applyCoupon(Coupon coupon) {
//        // Assuming you have a method to get the original total price
//        //double originalPrice = getOriginalTotalPrice();
//
//        double discountValue = coupon.getDiscountValue();
//
//        couponId = coupon.getCouponId();
//
//        if(discountValue > 0){
//            isCouponApplied = true;
//            binding.couponLayout.setVisibility(View.VISIBLE);
//            binding.txtCouponAmount.setText("₹"+discountValue);
//            couponDiscountAmount = discountValue;
//        }else{
//            isCouponApplied = false;
//            couponDiscountAmount = 0.0;
//            binding.couponLayout.setVisibility(View.GONE);
//        }
//        System.out.println("discountValue::"+discountValue);
//        //double newPrice = originalPrice - discountValue;
//
//        // Update the UI with the new price
//        //TextView totalPriceTextView = findViewById(R.id.total_price);
//        //totalPriceTextView.setText(String.format("₹ %.2f", newPrice));
//
//    }

    private void applyCoupon(Coupon coupon) {
        if (coupon != null) {
            isCouponApplied = true;
            couponDiscount = coupon.getDiscountValue();
            couponId = coupon.getCouponId();

            // Show applied coupon layout
            binding.couponLayout.setVisibility(View.VISIBLE);
            binding.txtApplycode.setVisibility(View.GONE);

            // Update prices
            updateFareBreakdown();
        }
    }

    private void removeCoupon() {
        isCouponApplied = false;
        couponDiscount = 0.0;
        couponId = -1;

        // Hide coupon layout and show apply button
        binding.couponLayout.setVisibility(View.GONE);
        binding.txtApplycode.setVisibility(View.VISIBLE);

        // Update prices
        updateFareBreakdown();
    }


    private void showVehicleDetailsBottomSheet(VehicleModel vehicle) {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.custome_info, null);

        ImageView imgIcon = sheetView.findViewById(R.id.img_icon);
        TextView txtTitle = sheetView.findViewById(R.id.txt_title);
        TextView txtCapcity = sheetView.findViewById(R.id.txt_capcity);
        LinearLayout capacityLyt = sheetView.findViewById(R.id.capacityLyt);
        ImageView sizeImage = sheetView.findViewById(R.id.sizeImage);
        capacityLyt.setVisibility(View.GONE);
//        RecyclerView guidelinesRcy = sheetView.findViewById(R.id.guidelinesRecyclerView);
//        ProgressBar progressBar = sheetView.findViewById(R.id.progressBar);

        // Setup RecyclerView
//        GuidelinesAdapter guidelinesAdapter = new GuidelinesAdapter();
//        guidelinesRcy.setAdapter(guidelinesAdapter);
//        guidelinesRcy.setLayoutManager(new LinearLayoutManager(this));

        // Load vehicle image
        Glide.with(this)
                .load(vehicle.getVehicleImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(imgIcon);

        Glide.with(this)
                .load(vehicle.getSizeImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(sizeImage);

        // Set vehicle details
        txtTitle.setText(vehicle.getVehicleName());
//        txtCapcity.setText("Capacity: " + vehicle.getWeight() + "Kgs");

        // Fetch and show guidelines
//        fetchGuidelines(guidelinesAdapter, progressBar);


        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    private void fetchGuidelines() {
        // Initially show loading state if needed
        binding.recyclerGuidelines.setVisibility(View.GONE);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("category_id", 2);
        } catch (JSONException e) {
            showError("Error creating request: " + e.getMessage());
            return;
        }

        String url = APIClient.baseUrl + "get_all_guide_lines";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<GuidelineModel> guidelines = new ArrayList<>();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject guidelineJson = results.getJSONObject(i);
                            GuidelineModel guideline = GuidelineModel.fromJson(guidelineJson);
                            guidelines.add(guideline);
                        }

                        // Setup RecyclerView only if we have guidelines
                        if (!guidelines.isEmpty()) {
                            GuidelinesAdapter guidelinesAdapter = new GuidelinesAdapter();
                            binding.recyclerGuidelines.setLayoutManager(new LinearLayoutManager(this));
                            binding.recyclerGuidelines.setAdapter(guidelinesAdapter);
                            guidelinesAdapter.submitList(guidelines);

                            // Force visibility VISIBLE and layout refresh
                            binding.recyclerGuidelines.post(() -> {
                                binding.recyclerGuidelines.setVisibility(View.VISIBLE);
                            });

                            Log.d("Guidelines", "Loaded " + guidelines.size() + " guidelines");
                        } else {
                            binding.recyclerGuidelines.setVisibility(View.GONE);
                            Log.d("Guidelines", "No guidelines found");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        binding.recyclerGuidelines.setVisibility(View.GONE);
                        showError("Error parsing guidelines");
                    }
                },
                error -> {
                    binding.recyclerGuidelines.setVisibility(View.GONE);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        showError("No Guidelines Found");
                    } else {
                        showError("Error loading guidelines");
                    }
                    error.printStackTrace();
                }
        );

        // Add request to queue
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void showVehicleDetails(VehicleModel vehicle) {
//        BottomSheetDialog dialog = new BottomSheetDialog(this);
//        VehicleDetailsBottomSheetBinding detailsBinding = VehicleDetailsBottomSheetBinding.inflate(getLayoutInflater());
//
//        detailsBinding.txtVehicleName.setText(vehicle.getVehicleName());
//        detailsBinding.txtBaseFare.setText(String.format("Base Fare: ₹%.2f", vehicle.getBaseFare()));
//        detailsBinding.txtPricePerKm.setText(String.format("₹%.2f/km", vehicle.getPricePerKm()));
//        detailsBinding.txtCapacity.setText(vehicle.getWeight());
//
//        dialog.setContentView(detailsBinding.getRoot());
//        dialog.show();
    }


    private void initViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);


        binding.txtPickaddress.setText(pickup.getAddress());
        binding.txtDropaddress.setText(drop.getAddress());

        binding.btnBook.setOnClickListener(v -> saveBookingDetails());
    }

    private void saveBookingDetails() {
        if (selectedVehicle == null) {
            showError("Please select the vehicle first to book");
            return;
        }
        if (isScheduledBooking && scheduledTime.isEmpty()) {
            showError("Please select a schedule time");
            return;
        }
        showLoading(true);

        try {
            cityId = preferenceManager.getStringValue("city_id");
            String customerId = preferenceManager.getStringValue("customer_id");
            String url = APIClient.baseUrl + "generate_new_cab_drivers_booking_id_get_nearby_drivers_with_fcm_token";


            String serverAccessToken = AccessToken.getAccessToken();

            double pricePerKm = selectedVehicle.getPricePerKm();
            double beforeCouponAmount = pricePerKm;
            if (isCouponApplied) {
                pricePerKm = pricePerKm - couponDiscount;
            }
            if (pricePerKm <= selectedVehicle.getBaseFare()) {
                pricePerKm = selectedVehicle.getBaseFare();
            }

            if (totalDistance > selectedVehicle.getOutStationDistance()) {
                priceTypeId = "2";
            }

            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("price_type", priceTypeId);
                jsonBody.put("radius_km", 5);
                jsonBody.put("vehicle_id", selectedVehicle.getVehicleId());
                jsonBody.put("customer_id", customerId);
                jsonBody.put("pickup_lat", pickup.getLat());
                jsonBody.put("pickup_lng", pickup.getLog());
                jsonBody.put("destination_lat", drop.getLat());
                jsonBody.put("destination_lng", drop.getLog());
                jsonBody.put("distance", totalDistance);
                jsonBody.put("time", exactTime);
                jsonBody.put("total_price", pricePerKm);
                jsonBody.put("base_price", selectedVehicle.getBaseFare());
                jsonBody.put("gst_amount", 0);
                jsonBody.put("igst_amount", 0);
                jsonBody.put("payment_method", "NA");
                jsonBody.put("city_id", cityId);
                jsonBody.put("pickup_address", pickup.getAddress());
                jsonBody.put("drop_address", drop.getAddress());
                jsonBody.put("server_access_token", serverAccessToken);
                jsonBody.put("coupon_applied", isCouponApplied ? "Yes" : "No");
                jsonBody.put("coupon_id", couponId);
                jsonBody.put("coupon_amount", couponDiscount);
                jsonBody.put("before_coupon_amount", beforeCouponAmount);
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
                showError("Error creating request body: " + e.getMessage());
                return;
            }

            Log.d("CabSaveBooking", "Request Body: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            showLoading(false);
                            Log.d("CabSaveBooking", "Response: " + response.toString());

                            JSONArray results = response.getJSONArray("result");
                            if (results.length() > 0) {
                                String bookingId = "";

                                bookingId = results.getJSONObject(0).getString("booking_id");


                                if (!isScheduledBooking) {
                                    Intent intent = new Intent(this, CabSearchingActivity.class);
                                    intent.putExtra("booking_id", bookingId);
                                    intent.putExtra("pickup", pickup);
                                    intent.putExtra("drop", drop);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, "Your booking has been scheduled. A driver will be assigned at the scheduled time.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish(); // Optional: Ensures current activity is destroyed
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

    private void getIntentData() {
        totalDistance = getIntent().getDoubleExtra("total_distance", 0.0);
        totalDuration = getIntent().getLongExtra("total_time", 0);
        cityId = preferenceManager.getStringValue("city_id");
    }


    private void loadVehicles() {
        showLoading(true);

        // First, fetch peak hour prices
        fetchPeakHourPrices(cityId, () -> {
            try {
                if (cityId.isEmpty()) {
                    showError("We do not serve this route");
                    finish();
                    return;
                }

                if (totalDistance > 30) {
                    priceTypeId = "2";
                }
                String url = APIClient.baseUrl + "all_vehicles_with_price_details";
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("category_id", 2);
                jsonBody.put("price_type_id", priceTypeId);
                jsonBody.put("city_id", cityId);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        response -> {
                            showLoading(false);
                            try {
                                List<VehicleModel> vehiclesList = new ArrayList<>();
                                JSONArray jsonArray = response.getJSONArray("results");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject vehicleObject = jsonArray.getJSONObject(i);
                                    int vehicleId = vehicleObject.getInt("vehicle_id");

                                    // Get base price from vehicle
                                    double baseStartingPricePerKm = vehicleObject.getDouble("starting_price_per_km");

                                    // Check if peak hour price applies
                                    double finalPricePerKm = getPeakHourPrice(vehicleId, baseStartingPricePerKm);
                                    System.out.println("finalPricePerKm::" + finalPricePerKm);
                                    System.out.println("totalDistance::" + totalDistance);
                                    double fixedTotalPrice = totalDistance * finalPricePerKm;
                                    System.out.println("fixedTotalPrice::" + fixedTotalPrice);

                                    // Format to 2 decimal places
                                    DecimalFormat df = new DecimalFormat("0.00");
                                    String formattedPrice = df.format(fixedTotalPrice);
                                    System.out.println("formattedPrice::" + formattedPrice);
                                    double roundedPrice = Double.parseDouble(formattedPrice);
                                    System.out.println("roundedPrice::" + roundedPrice);
                                    VehicleModel vehicle = new VehicleModel(
                                            vehicleId,
                                            vehicleObject.getString("vehicle_name"),
                                            vehicleObject.getString("image"),
                                            vehicleObject.getDouble("base_fare"),
                                            roundedPrice,
                                            vehicleObject.getString("size_image"),
                                            vehicleObject.getString("weight"),
                                            vehicleObject.getInt("outstation_distance")
                                    );
                                    vehiclesList.add(vehicle);
                                }

                                if (vehiclesList.isEmpty()) {
                                    showError("No vehicle price available for this location");
                                } else {
                                    vehicleAdapter = new VehicleAdapter(
                                            this,
                                            vehiclesList,
                                            this,
                                            totalDistance
                                    );
                                    binding.recyclerVehicles.setAdapter(vehicleAdapter);
                                }
                            } catch (Exception e) {
                                showError("Error loading vehicles: " + e.getMessage());
                            }
                        },
                        error -> {
                            showLoading(false);
                            handleVolleyError(error);
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
                showError("Failed to load vehicles: " + e.getMessage());
            }
        });
    }

    private List<JSONObject> peakHourPrices = new ArrayList<>();

    private void fetchPeakHourPrices(String cityId, Runnable onComplete) {
        try {
            String url = APIClient.baseUrl + "get_peak_hour_prices";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("city_id", cityId);
            jsonBody.put("category_id", 2);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            JSONArray peakHours = response.getJSONArray("peak_hours");
                            peakHourPrices.clear();
                            for (int i = 0; i < peakHours.length(); i++) {
                                JSONObject peakHour = peakHours.getJSONObject(i);
                                if (peakHour.getInt("status") == 1) {  // Only consider active peak hours
                                    peakHourPrices.add(peakHour);
                                }
                            }
                            onComplete.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                            onComplete.run();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        onComplete.run();
                    }
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            e.printStackTrace();
            onComplete.run();
        }
    }

    private double getPeakHourPrice(int vehicleId, double basePrice) {
        try {
            // Get current time
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            //TODO::Check the hours and minutes timing here.
            // Check each peak hour price
            System.out.println("currentHour::" + currentHour);
            System.out.println("currentMinute::" + currentMinute);
            for (JSONObject peakHour : peakHourPrices) {
                int peakHourVehicleId = peakHour.getInt("vehicle_id");
                System.out.println("peakHourVehicleId::" + peakHourVehicleId);
                System.out.println("vehicleId::" + vehicleId);
                if (peakHourVehicleId == vehicleId) {
                    String startTime = peakHour.getString("start_time");
                    String endTime = peakHour.getString("end_time");
                    System.out.println("startTime::" + startTime);
                    System.out.println("endTime::" + endTime);
                    // Parse times
                    String[] startParts = startTime.split(":");
                    String[] endParts = endTime.split(":");

                    int startHour = Integer.parseInt(startParts[0]);
                    int startMinute = Integer.parseInt(startParts[1]);
                    int endHour = Integer.parseInt(endParts[0]);
                    int endMinute = Integer.parseInt(endParts[1]);
                    System.out.println("startHour::" + startHour + " startMinute::" + startMinute + " endHour::" + endHour + " endMinute::" + endMinute);
                    // Convert current time to minutes for easier comparison
                    int currentTimeInMinutes = currentHour * 60 + currentMinute;
                    int startTimeInMinutes = startHour * 60 + startMinute;
                    int endTimeInMinutes = endHour * 60 + endMinute;
                    System.out.println("currentTimeInMinutes::" + currentTimeInMinutes);
                    System.out.println("startTimeInMinutes::" + startTimeInMinutes);
                    System.out.println("endTimeInMinutes::" + endTimeInMinutes);
                    // Check if current time falls within peak hours
                    if (currentTimeInMinutes >= startTimeInMinutes &&
                            currentTimeInMinutes <= endTimeInMinutes) {
                        double pricePerKm = peakHour.getDouble("price_per_km");
                        System.out.println("returning pricePerKm::" + pricePerKm);
                        return pricePerKm;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return base price if no peak hour price applies
        return basePrice;
    }

    private void loadVehiclesOld() {
        showLoading(true);

        try {
            if (cityId.isEmpty()) {
                showError("We do not serve this route");
                finish();
                return;
            }

            if (totalDistance > 30) {
                priceTypeId = "2";
            }

            String url = APIClient.baseUrl + "all_vehicles_with_price_details";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("category_id", 2); // Replace with your category ID
            jsonBody.put("price_type_id", priceTypeId);
            jsonBody.put("city_id", cityId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        showLoading(false);
                        try {
                            List<VehicleModel> vehiclesList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("results");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject vehicleObject = jsonArray.getJSONObject(i);
                                double startingPricePerKm = vehicleObject.getDouble("starting_price_per_km");
                                double fixedTotalPrice = totalDistance * startingPricePerKm;

// Format to 2 decimal places
                                DecimalFormat df = new DecimalFormat("0.00");
                                String formattedPrice = df.format(fixedTotalPrice);

// If you need the double value back
                                double roundedPrice = Double.parseDouble(formattedPrice);

// For example: 3.308 * 35.0 = 115.78
                                System.out.println("Total Distance: " + df.format(totalDistance) + " km");
                                System.out.println("Price per km: " + df.format(startingPricePerKm) + " Rs");
                                System.out.println("Total Price: " + formattedPrice + " Rs");

                                VehicleModel vehicle = new VehicleModel(
                                        vehicleObject.getInt("vehicle_id"),
                                        vehicleObject.getString("vehicle_name"),
                                        vehicleObject.getString("image"),
                                        vehicleObject.getDouble("base_fare"),
                                        roundedPrice,
                                        vehicleObject.getString("size_image"),
                                        vehicleObject.getString("weight"),
                                        vehicleObject.getInt("outstation_distance")
                                );
                                vehiclesList.add(vehicle);
                            }

                            if (vehiclesList.isEmpty()) {
                                showError("No vehicle price available for this location");
                            } else {
                                vehicleAdapter = new VehicleAdapter(
                                        this,
                                        vehiclesList,
                                        this,
                                        totalDistance
                                );
                                binding.recyclerVehicles.setAdapter(vehicleAdapter);
                            }
                        } catch (Exception e) {
                            showError("Error loading vehicles: " + e.getMessage());
                        }
                    },
                    error -> {
                        showLoading(false);
                        handleVolleyError(error);
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
            showError("Failed to load vehicles: " + e.getMessage());
        }
    }


    private void handleVolleyError(VolleyError error) {
        if (error instanceof NoConnectionError) {
            showError("No internet connection");
        } else if (error instanceof TimeoutError) {
            showError("Request timed out");
        } else if (error instanceof ServerError) {
            if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                showError("No vehicle service available for this location");
            } else {
                showError("Server error");
            }
        } else {
            showError("Error: " + error.getMessage());
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}