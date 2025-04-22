package com.kapstranspvtltd.kaps.activities.goods_service_booking_activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kapstranspvtltd.kaps.LocationViewModel;
import com.kapstranspvtltd.kaps.MyApplication;
import com.kapstranspvtltd.kaps.activities.models.AllVehicleModel;
import com.kapstranspvtltd.kaps.adapters.GoodsVehicleAdapter;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityAllVehiclesBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllVehicles extends AppCompatActivity {
    private static final String TAG = "AllVehicles";
    private ActivityAllVehiclesBinding binding;
    private GoodsVehicleAdapter vehicleAdapter;
    private AllVehicleModel selectedVehicle;
    private float totalDistance = 0f;
    private int totalTime = 0;
    private double totalPrice = 0.0;


    private String categoryId = "";
    private String categoryName = "";
    private LocationViewModel locationViewModel;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllVehiclesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        preferenceManager = new PreferenceManager(this);
        locationViewModel = ((MyApplication) getApplication()).locationViewModel;

        setupViews();
    }

    private void calculateDistanceAndTimeWithGoogleMaps() {
        Location pickupLocation = locationViewModel.getPickupLocation();
        Location dropLocation = locationViewModel.getDropLocation();

        if (pickupLocation == null || dropLocation == null) {
            showError("Location details are missing");
            return;
        }

        String origin = pickupLocation.getLatitude() + "," + pickupLocation.getLongitude();
        String destination = dropLocation.getLatitude() + "," + dropLocation.getLongitude();
        String apiKey = APIClient.MAP_KEY;

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                "origins=" + origin + "&" +
                "destinations=" + destination + "&" +
                "mode=driving&" +
                "key=" + apiKey;

        makeRequest(url, 0);
    }

    private void makeRequest(String url, final int retryCount) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray rows = response.getJSONArray("rows");
                        if (rows.length() > 0) {
                            JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
                            if (elements.length() > 0) {
                                JSONObject element = elements.getJSONObject(0);

                                if (element.getString("status").equals("OK")) {
                                    JSONObject distance = element.getJSONObject("distance");
                                    JSONObject duration = element.getJSONObject("duration");

                                    totalDistance = (distance.getInt("value") / 1000f);
                                    totalTime = (duration.getInt("value") / 60);

                                    SharedPreferences prefs = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putFloat("total_distance", totalDistance);
                                    editor.putInt("total_time", totalTime);
                                    editor.apply();

                                    Log.d("DistanceMatrix", "Distance: " + totalDistance + " km");
                                    Log.d("DistanceMatrix", "Time: " + totalTime + " minutes");

                                    loadVehicles();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DistanceMatrix", "Error parsing response: " + e.getMessage());
                        handleDistanceError(retryCount);
                    }
                },
                error -> {
                    Log.e("DistanceMatrix", "Error: " + error.getMessage());
                    handleDistanceError(retryCount);
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

    private void handleDistanceError(int retryCount) {
        if (retryCount < 3) {
            Log.d("DistanceMatrix", "Retrying... Attempt " + (retryCount + 1));
            long delayMillis = 1000L * (retryCount + 1);
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                makeRequest(buildDistanceMatrixUrl(), retryCount + 1);
//            }, delayMillis);
        } else {
            Log.d("DistanceMatrix", "Falling back to basic calculation");
            calculateBasicDistanceAndTime();
        }
    }

    private void calculateBasicDistanceAndTime() {
        Location pickupLocation = locationViewModel.getPickupLocation();
        Location dropLocation = locationViewModel.getDropLocation();

        if (pickupLocation != null && dropLocation != null) {
            totalDistance = calculateDistance(
                    pickupLocation.getLatitude(),
                    pickupLocation.getLongitude(),
                    dropLocation.getLatitude(),
                    dropLocation.getLongitude()
            );
            totalTime = calculateTime(totalDistance);

            SharedPreferences prefs = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("total_distance", totalDistance);
            editor.putInt("total_time", totalTime);
            editor.apply();

            Log.d("DistanceMatrix", "Fallback Distance: " + totalDistance + " km");
            Log.d("DistanceMatrix", "Fallback Time: " + totalTime + " minutes");

            loadVehicles();
        }
    }

    private void setupViews() {
        binding.backButton.setOnClickListener(v -> finish());
        calculateDistanceAndTimeWithGoogleMaps();

        Location pickupLocation = locationViewModel.getPickupLocation();
        Location dropLocation = locationViewModel.getDropLocation();

        if (pickupLocation != null && dropLocation != null) {
            totalDistance = calculateDistance(
                    pickupLocation.getLatitude(),
                    pickupLocation.getLongitude(),
                    dropLocation.getLatitude(),
                    dropLocation.getLongitude()
            );
            totalTime = calculateTime(totalDistance);
        }

        vehicleAdapter = new GoodsVehicleAdapter(
                this,
                vehicle -> {
                    selectedVehicle = vehicle;
                    updateProceedButton(vehicle);
                },
                totalDistance,
                this::showVehicleSizeBottomSheet
        );

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(vehicleAdapter);
        binding.recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        binding.swipeRefreshLayout.setOnRefreshListener(this::loadVehicles);

        binding.proceedButton.setOnClickListener(v -> {
            if (selectedVehicle == null) {
                Toast.makeText(this, "Please select a vehicle to proceed", Toast.LENGTH_SHORT).show();
                return;
            }
            saveSelectedVehicleDetails(selectedVehicle);
        });
    }

    private void loadVehicles() {
        showLoading(true);

        try {
            String cityId = preferenceManager.getStringValue("city_id");
            System.out.println("cityId: " + cityId);
            if (cityId.isEmpty()) {
                showError("We do not serve this route");
                finish();
                return;
            }

            Pair<Integer, Float> priceDetails = calculatePriceTypeAndDistance();
            String url = APIClient.baseUrl + "all_vehicles_with_price_details";

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("category_id", categoryId);
            jsonBody.put("price_type_id", String.valueOf(priceDetails.first));
            jsonBody.put("city_id", cityId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        showLoading(false);
                        try {
                            List<AllVehicleModel> vehiclesList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("results");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject vehicleObject = jsonArray.getJSONObject(i);
                                AllVehicleModel vehicle = new AllVehicleModel(
                                        vehicleObject.getInt("vehicle_id"),
                                        vehicleObject.getString("vehicle_name"),
                                        vehicleObject.getString("image"),
                                        vehicleObject.getString("size_image"),
                                        vehicleObject.getDouble("base_fare"),
                                        vehicleObject.getDouble("starting_price_per_km"),
                                        vehicleObject.getString("weight")
                                );
                                vehiclesList.add(vehicle);
                            }

                            if (vehiclesList.isEmpty()) {
                                showError("No vehicle price available for this location");
                            } else {
                                showVehiclesList(vehiclesList);
                            }

                        } catch (Exception e) {
                            Log.e("LoadVehicles", "Error parsing response: " + e.getMessage());
                            e.printStackTrace();
                            showError("Error loading vehicles: " + e.getMessage());
                        }
                    },
                    error -> {
                        showLoading(false);
                        handleLoadVehiclesError(error);
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

    private void handleLoadVehiclesError(VolleyError error) {
        if (error instanceof NoConnectionError) {
            showError("No internet connection");
        } else if (error instanceof TimeoutError) {
            showError("Request timed out");
        } else if (error instanceof ServerError) {
            if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                showError("No vehicle price available for this location");
            } else {
                showError("Server error");
            }
        } else {
            showError("Error loading vehicles: " + error.getMessage());
        }
        Log.e("LoadVehicles", "Error: " + error.getMessage());
    }

    private void showVehiclesList(List<AllVehicleModel> vehicles) {
        binding.recyclerView.setVisibility(View.VISIBLE);
        vehicleAdapter.submitList(vehicles);
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0] / 1000; // Convert to kilometers
    }

    private int calculateTime(float distanceInKm) {
        double averageSpeedKmh = 30.0;
        return (int) (distanceInKm / averageSpeedKmh * 60); // Convert to minutes
    }

    private double calculateTotalPrice(double perKmPrice, double basePrice) {
        double distancePrice = totalDistance * perKmPrice;
        return distancePrice <= basePrice ? basePrice : distancePrice;
    }

    private void showLoading(boolean show) {
        if (show) {
            binding.shimmerLayout.setVisibility(View.VISIBLE);
            binding.shimmerLayout.startShimmer();
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showVehicleSizeBottomSheet(String imageUrl) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_vehicle_size, null);

        ImageView vehicleSizeImage = view.findViewById(R.id.vehicleSizeImage);
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(vehicleSizeImage);

        bottomSheet.setContentView(view);
        bottomSheet.show();
    }

    private void saveSelectedVehicleDetails(AllVehicleModel vehicle) {
        SharedPreferences prefs = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
        double totalPrice = calculateTotalPrice(vehicle.getPerKmPrice(), vehicle.getBasePrice());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pickup_lat", String.valueOf(locationViewModel.getPickupLocation().getLatitude()));
        editor.putString("pickup_lng", String.valueOf(locationViewModel.getPickupLocation().getLongitude()));
        editor.putString("drop_lat", String.valueOf(locationViewModel.getDropLocation().getLatitude()));
        editor.putString("drop_lng", String.valueOf(locationViewModel.getDropLocation().getLongitude()));
        editor.putFloat("total_distance", totalDistance);
        editor.putString("total_time", String.valueOf(totalTime));
        editor.putInt("vehicle_id", vehicle.getVehicleId());
        editor.putString("vehicle_name", vehicle.getVehicleName());
        editor.putString("vehicle_image", vehicle.getVehicleImage());
        editor.putString("total_price", String.valueOf(totalPrice));
        editor.putInt("base_price", (int) vehicle.getBasePrice());
        editor.putInt("category_id", Integer.parseInt(categoryId));
        editor.apply();

        System.out.println("totalTime::" + totalTime);
        System.out.println("totalDistance::" + totalDistance);
        System.out.println("totalPrice::" + totalPrice);

        startActivity(new Intent(this, BookingDetailsReviewActivity.class));
    }

    private void updateProceedButton(AllVehicleModel vehicle) {
        binding.proceedButton.setText("Proceed with " + vehicle.getVehicleName());
    }

    private Pair<Integer, Float> calculatePriceTypeAndDistance() {
        Location pickupLocation = locationViewModel.getPickupLocation();
        Location dropLocation = locationViewModel.getDropLocation();
        int priceTypeId = 1;
        float distance = 0f;

        if (pickupLocation != null && dropLocation != null) {
            distance = calculateDistance(
                    pickupLocation.getLatitude(),
                    pickupLocation.getLongitude(),
                    dropLocation.getLatitude(),
                    dropLocation.getLongitude()
            );
            if (distance > 30) priceTypeId = 2;
        }

        return new Pair<>(priceTypeId, distance);
    }
}