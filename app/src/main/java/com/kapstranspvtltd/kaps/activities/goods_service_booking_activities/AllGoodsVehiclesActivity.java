package com.kapstranspvtltd.kaps.activities.goods_service_booking_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.models.VehicleModel;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.databinding.ActivityAllGoodsVehiclesBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class AllGoodsVehiclesActivity extends AppCompatActivity implements GoodsVehiclesNewAdapter.OnVehicleSelectedListener {
    private ActivityAllGoodsVehiclesBinding binding;
    private GoodsVehiclesNewAdapter adapter;
    private List<VehicleModel> vehicles = new ArrayList<>();
    private double totalDistance;
    private String totalDuration;

    private String cityId;

    PreferenceManager preferenceManager;

    private String exactTime = "",exactDistance="";

    private double totalDistanceValue = 0; // in kilometers
    private long totalDurationValue = 0;   // in minutes

    private boolean cabService;
    private Pickup pickup;
    private Drop drop;
    private ProgressDialog progressDialog;

    private String priceTypeId ="";

    private List<JSONObject> peakHourPrices = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllGoodsVehiclesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Utility.applyEdgeToEdgePadding(binding.getRoot());
        preferenceManager = new PreferenceManager(this);

        // Get intent data
        totalDistance = getIntent().getDoubleExtra("total_distance", 0.0);
        totalDuration = getIntent().getStringExtra("total_duration");
        exactTime = getIntent().getStringExtra("exact_time");
        exactDistance = getIntent().getStringExtra("exact_distance");
        pickup = getIntent().getParcelableExtra("pickup");
        drop = getIntent().getParcelableExtra("drop");
        cabService = getIntent().getBooleanExtra("cab",false);

        checkIfOutstation(totalDistance);
        setupViews();
        setupRecyclerView();
        loadVehicles();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvTitle.setText("Vehicles [" + totalDistance + " km | " + exactTime + "]");

        binding.swipeRefresh.setOnRefreshListener(this::loadVehicles);

        binding.btnProceed.setOnClickListener(v -> {
            VehicleModel selectedVehicle = adapter.getSelectedVehicle();
            if (selectedVehicle != null) {
                proceedWithBooking(selectedVehicle);
            } else {
                Toast.makeText(this, "Please select a vehicle", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new GoodsVehiclesNewAdapter(this, vehicles, this,totalDistance);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void checkIfOutstation(double kilometers) {
        // Get the outstation distance threshold from preferences
        float outstationThreshold = preferenceManager.getFloatValue("outstation_distance", 30.0f); // Default to 30 if not set

        // Check if the trip is outstation
        boolean isOutstation = kilometers > outstationThreshold;

        // Use isOutstation as needed
        if (isOutstation) {
            // Handle outstation trip
            priceTypeId = "2"; // Set price type for outstation
        } else {
            // Handle local trip
            priceTypeId = "1"; // Set price type for local
        }
    }

    private void loadVehicles() {
        showLoading(true);
        cityId = preferenceManager.getStringValue("city_id");
        // First, fetch peak hour prices
        fetchPeakHourPrices(cityId, () -> {
            try {
                if (cityId == null || cityId.isEmpty()) {
                    showError("We do not serve this route");
                    finish();
                    return;
                }
                String customerId = preferenceManager.getStringValue("customer_id");
                String fcmToken = preferenceManager.getStringValue("fcm_token");

                String url = APIClient.baseUrl + "all_vehicles_with_price_details";
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("category_id", cabService ? 2 : 1);
                jsonBody.put("price_type_id", priceTypeId);
                jsonBody.put("city_id", cityId);
                jsonBody.put("customer_id", customerId);
                jsonBody.put("auth", fcmToken);



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
                                    double baseStartingPricePerKm = vehicleObject.getDouble("starting_price_per_km");

                                    // Check if peak hour price applies
                                    double finalPricePerKm = getPeakHourPrice(vehicleId, baseStartingPricePerKm);
                                    System.out.println("totalDistance for peak hours::"+totalDistance);
                                    double fixedTotalPrice = totalDistance * finalPricePerKm;
                                    System.out.println("fixedTotalPrice::"+fixedTotalPrice);

                                    // Format to 2 decimal places
                                    DecimalFormat df = new DecimalFormat("0.00");
                                    String formattedPrice = df.format(fixedTotalPrice);
                                    double roundedPrice = Double.parseDouble(formattedPrice);

                                    int outstationDistance = vehicleObject.getInt("outstation_distance");

                                    VehicleModel vehicle = new VehicleModel(
                                            vehicleId,
                                            vehicleObject.getString("vehicle_name"),
                                            vehicleObject.getString("image"),
                                            vehicleObject.getDouble("base_fare"),
                                            roundedPrice,
                                            vehicleObject.getString("size_image"),
                                            vehicleObject.getString("weight"),
                                            vehicleObject.getInt("outstation_distance"),
                                            vehicleObject.getInt("minimum_waiting_time")
                                    );
                                    vehiclesList.add(vehicle);
                                }

                                if (vehiclesList.isEmpty()) {
                                    showError("No vehicle price available for this location");
                                } else {
                                    adapter.updateVehicles(vehiclesList);
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

    private void fetchPeakHourPrices(String cityId, Runnable onComplete) {
        try {
            String customerId = preferenceManager.getStringValue("customer_id");
            String fcmToken = preferenceManager.getStringValue("fcm_token");
            String pincodeId = preferenceManager.getStringValue("pincode_id");


            String url = APIClient.baseUrl + "get_peak_hour_prices";
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("city_id", cityId);
            jsonBody.put("pincode_id", pincodeId);
            jsonBody.put("customer_id", customerId);
            jsonBody.put("auth", fcmToken);

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

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onVehicleSelected(VehicleModel vehicle, int position) {
        Glb.selectedVehicle = vehicle;
        binding.btnProceed.setText("Proceed with " + vehicle.getVehicleName());
    }

    public void showVehicleDetailsBottomSheet(VehicleModel vehicle) {
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

    private void proceedWithBooking(VehicleModel vehicle) {
        int outStationDistance = vehicle.getOutStationDistance();
        if(totalDistance > outStationDistance){
            Toast.makeText(this, vehicle.getVehicleName()+" is selected for outstation bookings", Toast.LENGTH_SHORT).show();

        }
        Glb.selectedVehicle = vehicle;
        startActivity(new Intent(this, BookingReviewScreenActivity.class)
                .putExtra("cab",cabService)
                .putExtra("total_distance", totalDistance)
                .putExtra("total_time", totalDuration)
                .putExtra("exact_time", exactTime)
                .putExtra("exact_distance", exactDistance)
                .putExtra("pickup", pickup)
                .putExtra("drop", drop)
                .putExtra("vehicle_id",vehicle.getVehicleId())
                .putExtra("vehicle_name",vehicle.getVehicleName())
                .putExtra("vehicle_base_price",vehicle.getBaseFare())
                .putExtra("vehicle_per_km_price",vehicle.getPricePerKm())
                .putExtra("vehicle_minimum_waiting_time",vehicle.getMinimumWaitingTime())
        );
    }

    private void showLoading(boolean show) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            binding.swipeRefresh.setRefreshing(false);
        }
    }
}