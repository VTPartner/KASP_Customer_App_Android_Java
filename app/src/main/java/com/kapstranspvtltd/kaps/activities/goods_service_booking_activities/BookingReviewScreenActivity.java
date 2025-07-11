package com.kapstranspvtltd.kaps.activities.goods_service_booking_activities;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.models.AllGoodsTypesModel;
import com.kapstranspvtltd.kaps.activities.models.GuidelineModel;
import com.kapstranspvtltd.kaps.activities.models.UpgradePrice;
import com.kapstranspvtltd.kaps.activities.models.VehicleModel;
import com.kapstranspvtltd.kaps.adapters.GoodsTypeAdapter;
import com.kapstranspvtltd.kaps.adapters.GuidelinesAdapter;
import com.kapstranspvtltd.kaps.adapters.VehicleAdapter;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.databinding.ItemDropBinding;
import com.kapstranspvtltd.kaps.fcm.AccessToken;
import com.kapstranspvtltd.kaps.model.Coupon;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.SessionManager;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityBookingReviewScreenBinding;

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

public class BookingReviewScreenActivity extends BaseActivity implements VehicleAdapter.RecyclerTouchListener {

    private ActivityBookingReviewScreenBinding binding;

    private boolean isScheduledBooking = false;
    private String scheduledTime = "";
    private Calendar selectedDateTime = Calendar.getInstance();

    private DropAdapter dropAdapter;
    private List<Drop> dropList = new ArrayList<>();
    private JSONArray dropLocationsArray = new JSONArray();
    private JSONArray dropContactsArray = new JSONArray();
    private int multipleDrops = 0;

    boolean isCouponApplied = false;

    private double tripFare = 0.0;

    private double netFare = 0.0;
    private double finalAmount = 0.0;

    private int minimumWaitingTime = 0;

    int couponId = -1;
    double couponDiscountAmount = 0.0;
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

    private Spinner bodyTypeSpinner;
    private String selectedBodyType = "Any";

    private int coinRewardPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingReviewScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);

        selectedVehicle = Glb.selectedVehicle;

        binding.txtGoodType.setOnClickListener(v -> showGoodsTypeBottomSheet());
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

            minimumWaitingTime = getIntent().getIntExtra("vehicle_minimum_waiting_time",0);

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

        setupBodyTypeSpinner();
        initViews();
        getIntentData();
        //setupRecyclerViews();
        //fetchGoodsTypes();
        // Load all required data
        //loadVehicles();
        fetchGuidelines();
        fetchWalletDetails();
//        fetchVehicleCoinRewardPoints(selectedVehicle.getVehicleId());
        if (cabService) {
            binding.goodsLyt.setVisibility(View.GONE);
        }

        binding.txtApplycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingReviewScreenActivity.this, CouponCodeActivity.class);
                startActivityForResult(intent, 1000);
            }
        });

        //setting up multiple drop locations
        setupDropLocations();

        //schedule bookings
        setupScheduledBookings();

    }

    private void fetchVehicleCoinRewardPoints(int vehicleId) {
        showLoading(true);
        JSONObject params = new JSONObject();
        try {
            params.put("vehicle_id", vehicleId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = APIClient.baseUrl + "get_vehicle_coin_reward_points";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                params,
                response -> {
                    showLoading(false);
                    try {
                        if (response.has("coin_reward_points")) {
                            coinRewardPoints = response.getInt("coin_reward_points");

                            // Set the value to your TextView
                            binding.txtCoinRewardPoints.setText("You'll get "+String.valueOf(coinRewardPoints)+" coins on this order ✨");
                        } else if (response.has("message")) {
                            showError(response.getString("message"));
                            binding.txtCoinRewardPoints.setText("You'll get 0 coins on this order ✨");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Failed to parse coin reward points");
                        binding.txtCoinRewardPoints.setText("You'll get 0 coins on this order ✨");
                    }
                },
                error -> {
                    error.printStackTrace();
                    showLoading(false);
                    showError("Failed to fetch coin reward points");
                    binding.txtCoinRewardPoints.setText("You'll get 0 coins on this order ✨");
                }) {
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

                            return;
                        }

                        if (response.has("results")) {
                            JSONObject results = response.getJSONObject("results");
                            updateWalletUI(results);
                        } else {

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Failed to parse response");

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
        walletBalance = walletDetails.getDouble("current_balance"); // <-- Save balance for later use
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

        binding.checkServiceDetails.setOnClickListener(v -> finish());
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

    private void createDropLocationsJson() {
        try {
            dropLocationsArray = new JSONArray();
            dropContactsArray = new JSONArray();

            for (Drop drop : dropList) {
                // Add drop location details
                JSONObject dropLocation = new JSONObject();
                dropLocation.put("lat", drop.getLat());
                dropLocation.put("lng", drop.getLog());
                dropLocation.put("address", drop.getAddress());
                dropLocationsArray.put(dropLocation);

                // Add drop contact details
                JSONObject dropContact = new JSONObject();
                dropContact.put("name", drop.getRname());
                dropContact.put("mobile", drop.getRmobile());
                dropContactsArray.put(dropContact);
            }

            // Set multipleDrops value
            multipleDrops = dropList.size() > 1 ? dropList.size() : 0;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupDropLocations() {
        // Get dropList from SessionManager
        dropList = SessionManager.dropList;

        // Setup RecyclerView for drops
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerDrop.setLayoutManager(layoutManager);
        binding.recyclerDrop.setItemAnimator(new DefaultItemAnimator());

        dropAdapter = new DropAdapter(dropList);
        binding.recyclerDrop.setAdapter(dropAdapter);



        // Create JSON objects for drop locations
        createDropLocationsJson();
    }

    private void setupBodyTypeSpinner() {
        bodyTypeSpinner = binding.bodyTypeSpinner;

        // Create array of body types
        String[] bodyTypes = new String[]{"Any", "Open Body", "Close Body"};

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                bodyTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to spinner
        bodyTypeSpinner.setAdapter(adapter);

        // Set default selection
        bodyTypeSpinner.setSelection(0);

        // Handle selection
        bodyTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBodyType = bodyTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBodyType = "Any";
            }
        });
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

    private void setupRecyclerViews() {
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        binding.recyclerVehicles.setLayoutManager(layoutManager);
//        binding.recyclerVehicles.setItemAnimator(new DefaultItemAnimator());
//
//        // Initialize adapter with empty list
//        vehicleAdapter = new VehicleAdapter(
//                this,
//                new ArrayList<>(),
//                this,
//                totalDistance
//        );
//        binding.recyclerVehicles.setAdapter(vehicleAdapter);
    }

    // Implement RecyclerTouchListener methods
    @Override
    public void onClickVehicleItem(VehicleModel item, int position) {
        // Show vehicle details dialog/bottom sheet
        selectedVehicle = item;
        binding.btnBook.setText("Book " + item.getVehicleName());
        // Show/hide body type spinner based on vehicle ID
        binding.bodyTypeLayout.setVisibility(item.getVehicleId() != 2 ? View.VISIBLE : View.GONE);

        updateFareBreakdown();
    }

    @Override
    public void onClickVehicleInfo(VehicleModel item, int position) {
        // Handle vehicle selection
        selectedVehicle = item;
        showVehicleDetailsBottomSheet(item);
//        binding.btnBook.setText("Book "+item.getVehicleName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_COUPON && resultCode == RESULT_OK) {
            Coupon selectedCoupon = (Coupon) data.getSerializableExtra("selected_coupon");
            applyCoupon(selectedCoupon);
        }
    }

    private void applyCoupon(Coupon coupon) {
        double discountValue = coupon.getDiscountValue();
        couponId = coupon.getCouponId();

        if (discountValue > 0) {
            isCouponApplied = true;
            couponDiscountAmount = discountValue;
        } else {
            isCouponApplied = false;
            couponDiscountAmount = 0.0;
        }

        updateFareBreakdown();
    }

    /*private void applyCoupon(Coupon coupon) {
        // Assuming you have a method to get the original total price
        //double originalPrice = getOriginalTotalPrice();

        double discountValue = coupon.getDiscountValue();

        couponId = coupon.getCouponId();

        if(discountValue > 0){
            isCouponApplied = true;
            binding.couponLayout.setVisibility(View.VISIBLE);
            binding.txtCouponAmount.setText("₹"+discountValue);
            couponDiscountAmount = discountValue;
        }else{
            isCouponApplied = false;
            couponDiscountAmount = 0.0;
            binding.couponLayout.setVisibility(View.GONE);
        }
        System.out.println("discountValue::"+discountValue);
        //double newPrice = originalPrice - discountValue;

        // Update the UI with the new price
        //TextView totalPriceTextView = findViewById(R.id.total_price);
        //totalPriceTextView.setText(String.format("₹ %.2f", newPrice));

        // Optionally, store the applied coupon for further use
        // e.g., in a shared preference or a member variable
    }*/

    private void updateFareBreakdown() {
        // Format currency
        DecimalFormat df = new DecimalFormat("0.00");
        if (selectedVehicle != null) {


            tripFare = selectedVehicle.getPricePerKm();


            double baseFare = selectedVehicle.getBaseFare();
            if (tripFare < baseFare) {
                binding.applyCouponLayout.setVisibility(View.GONE);
                tripFare = baseFare;
            } else {
                binding.applyCouponLayout.setVisibility(View.VISIBLE);
            }

            // Update trip fare
            binding.tripFareAmount.setText(String.format("₹%s", df.format(tripFare)));

            // Show/hide coupon discount
            if (isCouponApplied && couponDiscountAmount > 0) {
                binding.couponLayout.setVisibility(View.VISIBLE);
                binding.couponDiscountAmount.setText(String.format("-₹%s", df.format(couponDiscountAmount)));
            } else {
                binding.couponLayout.setVisibility(View.GONE);
            }

            // Calculate net fare
            netFare = tripFare - couponDiscountAmount;
            binding.netFareAmount.setText(String.format("₹%s", df.format(netFare)));

            // Calculate final amount (rounded)
            // Update final amount with adjusted price if available
            finalAmount = adjustedPrice > 0 ? adjustedPrice : Math.round(netFare);
            binding.finalAmount.setText(String.format("₹%d", (int)finalAmount));
            binding.bottomTotalAmount.setText(String.format("₹%d", (int)finalAmount));

            // Calculate coinRewardPoints as (finalAmount / 100)
            coinRewardPoints = (int) finalAmount / 100;

            binding.txtCoinRewardPoints.setText("You'll get " + coinRewardPoints + " coins on this order ✨");

            // Update base fare note if needed
            if (selectedVehicle != null) {
                binding.baseFareNote.setText(String.format(
                        "If amount is less than base fare then you have to pay base fare. ₹%.2f",
                        selectedVehicle.getBaseFare()
                ));
            }


        }
    }

    private void fetchGoodsTypes() {
        showLoading(true);
        String url = APIClient.baseUrl + "get_all_goods_types";
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", customerId);
        params.put("auth", fcmToken);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    showLoading(false);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<AllGoodsTypesModel> goodsTypesList = new ArrayList<>();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject goodsTypeJson = results.getJSONObject(i);
                            AllGoodsTypesModel model = AllGoodsTypesModel.fromJson(goodsTypeJson);
                            if (model != null) {
                                goodsTypesList.add(model);
                            }
                        }

                        // Set first item as default if available
                        if (!goodsTypesList.isEmpty()) {
                            selectedGoodsType = goodsTypesList.get(0);
                            updateGoodsTypeDisplay();
                        }

                        // Update adapter
                        goodsTypeAdapter = new GoodsTypeAdapter(goodsTypesList, goodsType -> {
                            selectedGoodsType = goodsType;
                            updateGoodsTypeDisplay();
                            bottomSheetDialog.dismiss();
                        });
                        goodsTypeAdapter.submitList(goodsTypesList);
                        recyclerView.setAdapter(goodsTypeAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Error parsing goods types");
                    }
                },
                error -> {
                    showLoading(false);
//                    handleError(error);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void showGoodsTypeBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_goods_type_bottom_sheet, null);
        fetchGoodsTypes();
        // Initialize views
        ImageView closeButton = sheetView.findViewById(R.id.closeButton);
        EditText searchBox = sheetView.findViewById(R.id.searchBox);
        recyclerView = sheetView.findViewById(R.id.recyclerView);
        ProgressBar progressBar = sheetView.findViewById(R.id.progressBar);
        TextView noResultsText = sheetView.findViewById(R.id.noResultsText);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        goodsTypeAdapter = new GoodsTypeAdapter(goodsTypesList, goodsType -> {
            selectedGoodsType = goodsType;
            updateGoodsTypeDisplay();
            bottomSheetDialog.dismiss();
        });
        recyclerView.setAdapter(goodsTypeAdapter);

        // Setup search with TextWatcher
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                goodsTypeAdapter.filter(s.toString());
                // Show/hide no results text
                noResultsText.setVisibility(goodsTypeAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });

        // Setup close button
        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void updateGoodsTypeDisplay() {
        if (selectedGoodsType != null) {
            binding.txtGoodType.setText(selectedGoodsType.getGoodsTypeName());
            // Save selected goods type ID if needed
            SharedPreferences.Editor editor = getSharedPreferences("booking_prefs", MODE_PRIVATE).edit();
            editor.putInt("goods_type_id", selectedGoodsType.getGoodsTypeId());
            editor.putString("goods_type_name", selectedGoodsType.getGoodsTypeName());
            editor.apply();
        }
    }

    private void showVehicleDetailsBottomSheet(VehicleModel vehicle) {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.custome_info, null);

        ImageView imgIcon = sheetView.findViewById(R.id.img_icon);
        TextView txtTitle = sheetView.findViewById(R.id.txt_title);
        TextView txtCapcity = sheetView.findViewById(R.id.txt_capcity);
        ImageView sizeImage = sheetView.findViewById(R.id.sizeImage);
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
        txtCapcity.setText("Capacity: " + vehicle.getWeight() + "Kgs");

        // Fetch and show guidelines
//        fetchGuidelines(guidelinesAdapter, progressBar);


        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
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
            requestBody.put("category_id", cabService ? 2 : 1); // Replace with your category ID
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
                        binding.recyclerGuidelines.setAdapter(guidelinesAdapter);
                        binding.recyclerGuidelines.setLayoutManager(new LinearLayoutManager(this));
                        guidelinesAdapter.submitList(guidelines);
                        System.out.println("Guidelines loaded: " + guidelines.size());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Error parsing guidelines");
                    }
                },
                error -> {
//                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        showError("No Guidelines Found");
                    } else {
                        showError("Error loading guidelines");
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

        binding.txtPickupcontect.setText(pickup.getRname() + " \n " + pickup.getRmobile());
        binding.txtDropcontect.setText(drop.getRname() + " \n " + drop.getRmobile());
        binding.txtPickaddress.setText(pickup.getAddress());
        binding.txtDropaddress.setText(drop.getAddress());

//        binding.btnBook.setOnClickListener(v -> saveBookingDetails());
        boolean showHikePrice = preferenceManager.getStringValue("hike_price_show", "No")
                .equalsIgnoreCase("Yes");

        binding.btnBook.setOnClickListener(v -> {
            if (showHikePrice) {

                showPriceAdjustmentSheet();
            } else {
                showPaymentMethodDailog();
//                saveBookingDetails();
            }
        });

        //Vehicle details
        if (selectedVehicle != null) {
            binding.serviceVehicleName.setText(selectedVehicle.getVehicleName());
            binding.serviceDurationDetails.setText("[" + totalDistance + " km  - " + exactTime + "]");
            binding.minimumTimeWaiting.setText("Free Waiting time "+minimumWaitingTime+" mins");
            binding.btnBook.setText("Book " + selectedVehicle.getVehicleName());

            // Show/hide body type spinner based on vehicle ID
            binding.bodyTypeLayout.setVisibility(selectedVehicle.getVehicleId() != 2 ? View.VISIBLE : View.GONE);

            // Load vehicle image
            Glide.with(this)
                    .load(selectedVehicle.getVehicleImage())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(binding.vehicleImg);

            updateFareBreakdown();
        } else {
            showError("No vehicle selected");
            finish();
        }

        txtGoodType = binding.txtGoodType;
    }

    private double walletBalance = 0.0; // Set this after fetching wallet details
    private double totalAmount = 0.0;   // Set this as per your calculation

    private void showPaymentMethodDailog() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.pay_via_wallet_layout, null);
        LinearLayout payOnDelivery = sheetView.findViewById(R.id.payCashLyt);
        TextView txtBooktrip = sheetView.findViewById(R.id.btn_booktrip);
        LinearLayout lvlWallet = sheetView.findViewById(R.id.lvl_wallet);

        Switch useWalletBalance = sheetView.findViewById(R.id.use_from_wallet_balance_switch);
        TextView txtTotalAmount = sheetView.findViewById(R.id.txt_total);
        TextView txtWalletBalance = sheetView.findViewById(R.id.txt_wallet_balance);
if(adjustedPrice <=0){
    totalAmount = finalAmount;
}
        // Set wallet balance and total
        txtWalletBalance.setText(String.format("₹%.2f", walletBalance));
        txtTotalAmount.setText(String.format("Total ₹%.2f", totalAmount));

        // Enable switch only if wallet balance > 10
        useWalletBalance.setEnabled(walletBalance > 1);

        // Add "Pay on Delivery" option
//        TextView payOnDelivery = new TextView(this);
//        payOnDelivery.setText("Pay on Delivery");
//        payOnDelivery.setTextSize(16);
//        payOnDelivery.setPadding(16, 16, 16, 16);
//        payOnDelivery.setTextColor(getResources().getColor(R.color.black));
//        listView.addView(payOnDelivery);

        // Handle wallet switch and payment logic
        useWalletBalance.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && walletBalance > 1) {
                double usedWallet = Math.min(walletBalance, totalAmount);
                txtTotalAmount.setText(String.format("Total ₹%.2f", totalAmount - usedWallet));
                txtBooktrip.setVisibility(View.VISIBLE);
                payOnDelivery.setVisibility(View.GONE);
            } else {
                txtTotalAmount.setText(String.format("Total ₹%.2f", totalAmount));
                txtBooktrip.setVisibility(View.GONE);
                payOnDelivery.setVisibility(View.VISIBLE);
            }
        });

        // Default state
        if (walletBalance > 1) {
            useWalletBalance.setChecked(false);
            txtBooktrip.setVisibility(View.GONE);
            payOnDelivery.setVisibility(View.VISIBLE);
        } else {
            useWalletBalance.setChecked(false);
            useWalletBalance.setEnabled(false);
            txtBooktrip.setVisibility(View.GONE);
            payOnDelivery.setVisibility(View.VISIBLE);
        }

        // Book trip with wallet
        txtBooktrip.setOnClickListener(v -> {
            double usedWallet = Math.min(walletBalance, totalAmount);
            saveBookingDetails(usedWallet, "WALLET");
            mBottomSheetDialog.dismiss();
        });

        // Book trip with pay on delivery
        payOnDelivery.setOnClickListener(v -> {
            saveBookingDetails(0.0, "COD");
            mBottomSheetDialog.dismiss();
        });

        // Reset price if dialog is cancelled
        mBottomSheetDialog.setOnCancelListener(dialog -> {
            adjustedPrice = originalPrice;
            finalAmount = originalPrice;
            totalAmount = originalPrice;
            binding.finalAmount.setText(String.format("₹%d", (int) finalAmount));
            binding.bottomTotalAmount.setText(String.format("₹%d", (int) finalAmount));
            updateFareBreakdown();


        });

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
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

        titleText.setText("Set Your Own Delivery Price\nfor Quick Goods Booking");
        priceText.setText("Better offers increase your chances\nof getting a driver faster");
        // Set initial values
        originalPrice = finalAmount;
        adjustedPrice = originalPrice;
        tvAdjustedPrice.setText("₹" + (int)adjustedPrice);
        btnConfirmPrice.setText("Book " + selectedVehicle.getVehicleName() + " for ₹" + (int)adjustedPrice);

        // Fetch upgrade prices
        fetchUpgradePrices(selectedVehicle.getVehicleId(), priceRangeContainer, priceSlider,tvAdjustedPrice,btnConfirmPrice);

        // Handle close
        ivClose.setOnClickListener(v -> {
            adjustedPrice = originalPrice;
            updateFareBreakdown();
            priceAdjustmentDialog.dismiss();
        });

        // Handle cancel (user taps outside or presses back)
        priceAdjustmentDialog.setOnCancelListener(dialog -> {
            adjustedPrice = originalPrice;
            updateFareBreakdown();
        });

        // Handle confirm
        btnConfirmPrice.setOnClickListener(v -> {
            finalAmount = adjustedPrice;
            totalAmount = finalAmount;
            updateFareBreakdown();
            priceAdjustmentDialog.dismiss();
            showPaymentMethodDailog();
            //saveBookingDetails();
        });

        priceAdjustmentDialog.show();
    }

    private void fetchUpgradePrices(int vehicleId, LinearLayout container, Slider slider, TextView tvAdjustedPrice, Button btnConfirmPrice) {
        try {
            String customerId = preferenceManager.getStringValue("customer_id");
            String fcmToken = preferenceManager.getStringValue("fcm_token");

            JSONObject requestBody = new JSONObject();
            requestBody.put("vehicle_id", vehicleId);
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "get_vehicle_upgrade_prices",
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
                String.format("Book %s for ₹%d", selectedVehicle.getVehicleName(), (int)adjustedPrice) :
                String.format("Book %s for ₹%d (+₹%d)", selectedVehicle.getVehicleName(), (int)adjustedPrice, (int)upgrade);
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

    private void saveBookingDetails(double walletUsed, String paymentMethod) {

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
            String fcmToken = preferenceManager.getStringValue("fcm_token");
            String url = APIClient.baseUrl + "generate_new_goods_drivers_booking_id_get_nearby_drivers_with_fcm_token";


            String serverAccessToken = AccessToken.getAccessToken();

            double pricePerKm = selectedVehicle.getPricePerKm();
            double beforeCouponAmount = pricePerKm;
            if (isCouponApplied) {
                pricePerKm = pricePerKm - couponDiscountAmount;
            }
            if (pricePerKm <= selectedVehicle.getBaseFare()) {
                pricePerKm = selectedVehicle.getBaseFare();
            }
            int goodsTypeID = 1;

            if (selectedGoodsType != null) {
                goodsTypeID = selectedGoodsType.getGoodsTypeId();
            }

            if (totalDistance > selectedVehicle.getOutStationDistance()) {
                priceTypeId = "2";
            }
            System.out.println("totalPrice before hike price::"+pricePerKm);
            if(adjustedPrice>0){
                adjustedPrice-=pricePerKm;
            }
            pricePerKm+=adjustedPrice;
            System.out.println("adjustedPrice::"+adjustedPrice);
            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("price_type", priceTypeId);
                jsonBody.put("booking_type_locations", priceTypeId.equalsIgnoreCase("1") ? 1 : 2); //it is save whether local or outstation booking
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
                jsonBody.put("goods_type_id", goodsTypeID);
//                jsonBody.put("payment_method", "NA");
                jsonBody.put("city_id", cityId);
                jsonBody.put("sender_name", pickup.getRname());
                jsonBody.put("sender_number", pickup.getRmobile());
                jsonBody.put("receiver_name", drop.getRname());
                jsonBody.put("receiver_number", drop.getRmobile());
                jsonBody.put("pickup_address", pickup.getAddress());
                jsonBody.put("drop_address", drop.getAddress());
                jsonBody.put("server_access_token", serverAccessToken);
                jsonBody.put("coupon_applied", isCouponApplied ? "Yes" : "No");
                jsonBody.put("coupon_id", couponId);
                jsonBody.put("coupon_amount", couponDiscountAmount);
                jsonBody.put("before_coupon_amount", beforeCouponAmount);
                jsonBody.put("hike_price", adjustedPrice);

                jsonBody.put("wallet_amount_used", walletUsed);
                jsonBody.put("payment_method", paymentMethod);
                jsonBody.put("coin_to_be_given", coinRewardPoints);

                jsonBody.put("is_scheduled", isScheduledBooking);
                if (isScheduledBooking) {
                    if (scheduledTime.isEmpty()) {
                        showError("Please select a schedule time");
                        return;
                    }
                    jsonBody.put("scheduled_time", scheduledTime);
                }
                jsonBody.put("body_type", selectedBodyType);
                jsonBody.put("drop_locations", dropLocationsArray);
                jsonBody.put("drop_contacts", dropContactsArray);
                jsonBody.put("multiple_drops", multipleDrops);
                jsonBody.put("auth", fcmToken);

            } catch (JSONException e) {
                e.printStackTrace();
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
                            Log.d("SaveBooking", "Response: " + response.toString());

                            JSONArray results = response.getJSONArray("result");
                            if (results.length() > 0) {
                                String bookingId = "";
                                Glb.addStopClicked = false;
                                if (cabService) {
                                    bookingId = results.getJSONObject(0).getString("cab_booking_id");
                                } else {
                                    bookingId = results.getJSONObject(0).getString("booking_id");
                                }


                                if(adjustedPrice>0){
                                    finalAmount-=adjustedPrice;
                                    binding.finalAmount.setText(String.format("₹%d", (int)finalAmount));
                                    binding.bottomTotalAmount.setText(String.format("₹%d", (int)finalAmount));
                                    adjustedPrice=0;
                                }

                                if (!isScheduledBooking) {
                                    Intent intent = new Intent(this, SearchingGoodsDriverActivity.class);
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
        try {
            totalDistance = getIntent().getDoubleExtra("total_distance", 0.0);
            totalDuration = getIntent().getLongExtra("total_time", 0);
            cityId = preferenceManager.getStringValue("city_id");

            // Validate distance
            if (totalDistance <= 0) {
                showError("You will be charged the base fare because the distance is less then 1 km");
                finish();
                return;
            }

            // Log the values for debugging
            Log.d("BookingReview", "Distance: " + totalDistance + " km");
            Log.d("BookingReview", "Duration: " + formatDuration(totalDuration));

        } catch (Exception e) {
            Log.e("BookingReview", "Error getting intent data", e);
            showError("Error processing route data");
            finish();
        }
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

                String url = APIClient.baseUrl + "all_vehicles_with_price_details";
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("category_id", cabService ? 2 : 1);
                jsonBody.put("price_type_id", "1");
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

                                    //

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
//                                    VehicleModel vehicle = new VehicleModel(
//                                            vehicleId,
//                                            vehicleObject.getString("vehicle_name"),
//                                            vehicleObject.getString("image"),
//                                            vehicleObject.getDouble("base_fare"),
//                                            roundedPrice,
//                                            vehicleObject.getString("size_image"),
//                                            vehicleObject.getString("weight"),
//                                            vehicleObject.getInt("outstation_distance")
//                                    );
//                                    vehiclesList.add(vehicle);
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
//                                    binding.recyclerVehicles.setAdapter(vehicleAdapter);
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






    private void handleVolleyError(VolleyError error) {
        if (error instanceof NoConnectionError) {
            showError("No internet connection");
        } else if (error instanceof TimeoutError) {
            showError("Request timed out");
        } else if (error instanceof ServerError) {
            if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                showError("No data available for this location");
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


    private double calculatePrice(VehicleModel vehicle) {
        double fixedTotalPrice = totalDistance * vehicle.getPricePerKm();
        // Add base fare if applicable
        fixedTotalPrice += vehicle.getBaseFare();
        return fixedTotalPrice;
    }

    class DropAdapter extends RecyclerView.Adapter<DropAdapter.MyViewHolder> {
        private final List<Drop> dropList;
        private final Context context;

        public DropAdapter(List<Drop> dropList) {
            this.dropList = dropList;
            this.context = BookingReviewScreenActivity.this;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDropBinding itemBinding = ItemDropBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Drop item = dropList.get(position);
            holder.binding.txtDropaddress.setText(item.getAddress());
            holder.binding.imgDelete.setVisibility(View.GONE);

            // Create numbered marker
            Bitmap numberedMarker = drawTextToBitmap(context,
                    R.drawable.ic_destination_long,
                    String.valueOf(position + 1));

            // Set the numbered marker to the ImageView
            holder.binding.dropMarker.setImageBitmap(numberedMarker);
        }

        @Override
        public int getItemCount() {
            return dropList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ItemDropBinding binding;

            MyViewHolder(ItemDropBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    public Bitmap drawTextToBitmap(Context gContext, int gResId, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap =
                BitmapFactory.decodeResource(resources, gResId);

        Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (16 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 3;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

}