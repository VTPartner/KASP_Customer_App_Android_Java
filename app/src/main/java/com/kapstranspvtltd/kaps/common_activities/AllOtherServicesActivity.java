package com.kapstranspvtltd.kaps.common_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.common_activities.adapters.OtherServiceAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.OtherService;
import com.kapstranspvtltd.kaps.databinding.ActivityAllOtherServicesBinding;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.DriverPickupLocationActivity;
import com.kapstranspvtltd.kaps.handyman_customer_app.activities.HandymanWorkLocationActivity;
import com.kapstranspvtltd.kaps.jcb_crane_customer_app.activities.JcbCraneWorkLocationActivity;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AllOtherServicesActivity extends AppCompatActivity implements OtherServiceAdapter.OnServiceClickListener {
    private ActivityAllOtherServicesBinding binding;
    private OtherServiceAdapter adapter;
    private List<OtherService> servicesList = new ArrayList<>();
    private RequestQueue requestQueue;
    private String subCategoryId;
    private int categoryId,subCatID;

    String categoryName,subCatName;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Glb.serviceID = -1;
        Glb.serviceName = "";
        binding = ActivityAllOtherServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Get extras
        subCategoryId = getIntent().getStringExtra("sub_cat_id");
        categoryName = getIntent().getStringExtra("category_name");
        categoryId = getIntent().getIntExtra("category_id", -1);
        subCatName = getIntent().getStringExtra("sub_cat_name");
        subCatID = getIntent().getIntExtra("sub_cat_id", -1);

        setupViews();
        fetchOtherServices();
    }

    private void setupViews() {
        // Setup RecyclerView
        adapter = new OtherServiceAdapter(this, servicesList, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchOtherServices);

        // Setup back button
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void fetchOtherServices() {
        showLoading(true);

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        String url = APIClient.baseUrl + "get_all_sub_services";
        JSONObject params = new JSONObject();
        try {
            params.put("sub_cat_id", subCategoryId);
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    showLoading(false);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        servicesList.clear();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject serviceJson = results.getJSONObject(i);
                            OtherService service = new OtherService(
                                    serviceJson.getInt("service_id"),
                                    serviceJson.getString("service_name"),
                                    serviceJson.getString("service_image"),
                                    serviceJson.getDouble("price_per_hour"),
                                    serviceJson.getDouble("service_base_price")
                            );
                            servicesList.add(service);
                        }

                        adapter.updateServices(servicesList);

                        // If list is empty, show empty state
                        if (servicesList.isEmpty()) {
//                            showEmptyState();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Error parsing data");
                    }
                },
                error -> {
                    showLoading(false);
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorResponse = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            JSONObject errorJson = new JSONObject(errorResponse);
                            if (errorJson.has("message") && errorJson.getString("message").contains("No Data Found")) {
                                handleNoDataFound();
                            } else {
                                showError("Network error occurred");
                            }
                        } catch (JSONException e) {
                            showError("Network error occurred");
                        }
                    } else {
                        showError("Network error occurred");
                    }
                });

        requestQueue.add(request);
    }


    private void handleNoDataFound() {
        Intent intent;

        // Handle navigation based on category_id
        switch (categoryId) {
            case 3:
                intent = new Intent(this, JcbCraneWorkLocationActivity.class);
                break;
            case 4:
                intent = new Intent(this, DriverPickupLocationActivity.class);
                break;
            case 5:
                intent = new Intent(this, HandymanWorkLocationActivity.class);
                break;
            default:
                return; // Exit if category_id doesn't match
        }

//        // Add extras to intent
//        intent.putExtra("category_name", categoryName);
//        intent.putExtra("category_id", categoryId);
//        intent.putExtra("service_base_price", serviceBasePrice);
//        intent.putExtra("sub_cat_id", subCatId);
//        intent.putExtra("sub_cat_name", subCatName);
//        intent.putExtra("sub_cat_base_price", serviceBasePrice);
//        intent.putExtra("sub_cat_per_hour_price", pricePerHour);

        // Start activity and finish current one
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        binding.shimmerLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            binding.shimmerLayout.startShimmer();
        } else {
            binding.shimmerLayout.stopShimmer();
        }
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceClick(OtherService service) {
        // Handle service selection
        handleNoDataFound();
        Glb.serviceID = service.getServiceId();
        Glb.serviceName = service.getServiceName();
        Glb.service_base_price = String.valueOf(service.getServiceBasePrice());
//        Intent resultIntent = new Intent(this, ServiceDurationActivity.class);
//        resultIntent.putExtra("category_name", categoryName);
//        resultIntent.putExtra("category_id", categoryId);
//        resultIntent.putExtra("service_id", service.getServiceId());
//        resultIntent.putExtra("service_name", service.getServiceName());
//        resultIntent.putExtra("service_base_price", service.getServiceBasePrice());
//        resultIntent.putExtra("sub_cat_id",subCatID);
//        resultIntent.putExtra("sub_cat_name", subCatName);
//        setResult(RESULT_OK, resultIntent);
//        startActivity(resultIntent);
//        finish();
    }
}