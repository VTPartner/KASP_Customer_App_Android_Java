package com.kapstranspvtltd.kaps.common_activities;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.common_activities.adapters.ServiceAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.Service;
import com.kapstranspvtltd.kaps.databinding.ActivityAllServiceBinding;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllServiceActivity extends BaseActivity implements ServiceAdapter.OnServiceClickListener {

    private ActivityAllServiceBinding binding;
    private ServiceAdapter adapter;
    private List<Service> serviceList;
    private RequestQueue requestQueue;

    PreferenceManager preferenceManager;

    Service serviceSelected;

    int categoryId;

    String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        categoryId = getIntent().getIntExtra("category_id", -1);
        categoryName = getIntent().getStringExtra("category_name");

        if(categoryId == -1){
            showError("Please select the service first");
            finish();
            return;
        }
        // Initialize RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Initialize RecyclerView
        setupRecyclerView();

        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchServices);

        // Setup back button
        binding.backButton.setOnClickListener(v -> onBackPressed());

        // Proceed Button
        binding.proceedButton.setOnClickListener(v -> proceedToNextScreen());

        // Initial data fetch
        fetchServices();
    }

    private void proceedToNextScreen() {
        if(serviceSelected == null){
            showError("Please select a service to proceed with");
            return;
        }
        binding.proceedButton.setText("Proceed with "+serviceSelected.getSubCatName());

        Glb.categoryName = categoryName;
        Glb.categoryId = categoryId;
        Glb.service_base_price = serviceSelected.getServiceBasePrice();
        Glb.sub_cat_id =serviceSelected.getSubCatId();
        Glb.sub_cat_name = serviceSelected.getSubCatName();
        Glb.sub_cat_base_price = serviceSelected.getServiceBasePrice();
        Glb.sub_cat_per_hour_price = serviceSelected.getPricePerHour();

        Intent intent = new Intent(this, AllOtherServicesActivity.class);

        intent.putExtra("category_name", categoryName);
        intent.putExtra("category_id", categoryId);
        intent.putExtra("service_base_price", serviceSelected.getServiceBasePrice());
        intent.putExtra("sub_cat_id", serviceSelected.getSubCatId());
        intent.putExtra("sub_cat_name", serviceSelected.getSubCatName());
        intent.putExtra("sub_cat_base_price", serviceSelected.getServiceBasePrice());
        intent.putExtra("sub_cat_per_hour_price", serviceSelected.getPricePerHour());
        startActivity(intent);
    }

    private void setupRecyclerView() {
        serviceList = new ArrayList<>();
        adapter = new ServiceAdapter(this, serviceList, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void fetchServices() {
        showLoading(true);

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        String url = APIClient.baseUrl + "get_all_sub_categories";
        Map<String, Object> params = new HashMap<>();
        params.put("cat_id", categoryId);
        params.put("customer_id", customerId);
        params.put("auth", fcmToken);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                new JSONObject(params),
                response -> {
                    showLoading(false);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<Service> newServices = new ArrayList<>();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject serviceJson = results.getJSONObject(i);
                            Service service = new Service(
                                    serviceJson.getString("sub_cat_id"),
                                    serviceJson.getString("sub_cat_name"),
                                    serviceJson.getString("image"),
                                    serviceJson.getString("price_per_hour"),
                                    serviceJson.getString("service_base_price")
                            );
                            newServices.add(service);
                        }

                        serviceList.clear();
                        serviceList.addAll(newServices);
                        adapter.notifyDataSetChanged();

                        if (serviceList.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
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
                                showEmptyState(true);
                            } else {
                                showError("Network error occurred");
                            }
                        } catch (JSONException e) {
                            showError("Network error occurred");
                        }
                    } else {
                        showError("Network error occurred");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void showLoading(boolean show) {
        binding.shimmerLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            binding.shimmerLayout.startShimmer();
        } else {
            binding.shimmerLayout.stopShimmer();
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void showEmptyState(boolean show) {
        // Implement empty state UI logic here
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onServiceClick(Service service) {
        // Handle service click here
        binding.proceedButton.setText("Proceed with "+service.getSubCatName());
        serviceSelected = service;
//        Toast.makeText(this, "Selected: " + service.getSubCatName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}