package com.kapstranspvtltd.kaps.handyman_customer_app.activities.bookings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kapstranspvtltd.kaps.R;



import com.kapstranspvtltd.kaps.common_activities.adapters.PastServiceOrdersAdapter;
import com.kapstranspvtltd.kaps.common_activities.adapters.RecentServiceBookingsAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.AllServiceBookingsModel;
import com.kapstranspvtltd.kaps.common_activities.models.AllServiceOrdersModel;
import com.kapstranspvtltd.kaps.databinding.ActivityAllDriverBookingsAndOrdersBinding;
import com.kapstranspvtltd.kaps.databinding.ActivityAllHandymanBookingsAndOrdersBinding;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.bookings.DriverOngoingBookingDetailsActivity;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.bookings.DriverOrderDetailsActivity;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllHandymanBookingsAndOrdersActivity extends AppCompatActivity {

    ActivityAllHandymanBookingsAndOrdersBinding binding;
    private PastServiceOrdersAdapter pastAdapter;
    private PreferenceManager preferenceManager;
    private CustPrograssbar custPrograssbar;

    private RecentServiceBookingsAdapter recentAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllHandymanBookingsAndOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        fetchData();
    }

    private void initViews() {
        preferenceManager = new PreferenceManager(this);
        custPrograssbar = new CustPrograssbar();

        // Setup Recent Bookings RecyclerView
        recentAdapter = new RecentServiceBookingsAdapter(this,
                (booking, position) -> {
                    // Create intent to launch TripDetailsActivity
                    Intent intent = new Intent(this, HandymanOngoingBookingDetailsActivity.class);

                    // Pass booking ID and any other necessary data
                    intent.putExtra("booking_id", booking.getBooking_id());
                    intent.putExtra("customer_id", preferenceManager.getStringValue("customer_id"));

                    // Start the activity
                    startActivity(intent);
                });
        binding.recycleviewRecent.setLayoutManager(new LinearLayoutManager(this));
        binding.recycleviewRecent.setAdapter(recentAdapter);

        // Setup Past Orders RecyclerView
        pastAdapter = new PastServiceOrdersAdapter(this,
                (order, position) -> {
                    // Create intent to launch TripDetailsActivity
                    Intent intent = new Intent(this, HandymanOrderDetailsActivity.class);

                    // Pass booking ID and any other necessary data
                    intent.putExtra("order_id", order.getOrder_id());
                    // Start the activity
                    startActivity(intent);
                });
        binding.recycleviewPast.setLayoutManager(new LinearLayoutManager(this));
        binding.recycleviewPast.setAdapter(pastAdapter);
    }

    private void fetchData() {
        fetchRecentBookings();
        fetchPastOrders();
    }

    private void fetchRecentBookings() {
        showLoading();
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        String url = APIClient.baseUrl + "customers_all_handyman_bookings";

        JSONObject params = new JSONObject();
        try {
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, params,
                response -> {
                    try {
                        List<AllServiceBookingsModel> bookings = parseBookings(response);
                        updateRecentBookingsUI(bookings);
                    } catch (Exception e) {
                        showError("Failed to parse bookings: " + e.getMessage());
                    }
                    hideLoading();
                },
                error -> {
                    hideLoading();
                    updateRecentBookingsUI(null);
//                    showError(error.getMessage());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void fetchPastOrders() {
        showLoading();

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        System.out.println("customerId::"+customerId);
        String url = APIClient.baseUrl + "customers_all_handyman_orders";

        try {
            JSONObject params = new JSONObject();
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    params,
                    response -> {
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                            List<AllServiceOrdersModel> orders = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                AllServiceOrdersModel order = new AllServiceOrdersModel(
                                        json.getString("booking_id"),
                                        json.getString("booking_timing"),
                                        json.getString("booking_status"),
                                        json.getString("total_price"),
                                        json.getString("payment_method"),
                                        json.getString("service_name"),
                                        json.getString("sub_cat_name"),
                                        json.getString("distance"),
                                        json.getString("total_time"),
                                        json.getString("pickup_address")
                                );
                                orders.add(order);
                            }

                            hideLoading();
                            updatePastOrdersUI(orders);

                        } catch (JSONException e) {
                            hideLoading();
                            showError("Failed to parse response: " + e.getMessage());
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        hideLoading();
                        String errorMessage = error.getMessage();
                        updatePastOrdersUI(null);
//                        showError(errorMessage != null ? errorMessage : "Network request failed");
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

        } catch (JSONException e) {
            hideLoading();
            showError("Error creating request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePastOrdersUI(List<AllServiceOrdersModel> orders) {
        if(binding == null) return;
        if (orders == null || orders.isEmpty()) {
            binding.recycleviewPast.setVisibility(View.GONE);
            binding.lvlNotfoundPast.setVisibility(View.VISIBLE);
        } else {
            binding.recycleviewPast.setVisibility(View.VISIBLE);
            binding.lvlNotfoundPast.setVisibility(View.GONE);
            pastAdapter.setOrders(orders);
        }
    }

    private List<AllServiceBookingsModel> parseBookings(JSONObject response) throws JSONException {
        List<AllServiceBookingsModel> bookings = new ArrayList<>();
        JSONArray results = response.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject json = results.getJSONObject(i);
            AllServiceBookingsModel booking = new AllServiceBookingsModel(
                    json.getString("booking_id"),
                    json.getString("booking_timing"),
                    json.getString("booking_status"),
                    json.getString("total_price"),
                    json.getString("payment_method"),
                    json.getString("service_name"),
                    json.getString("sub_cat_name"),
                    json.getString("distance"),
                    json.getString("total_time"),
                    json.getString("pickup_address")
            );
            if (!booking.getBooking_status().equals("End Trip")) {
                bookings.add(booking);
            }
        }
        return bookings;
    }

    private void updateRecentBookingsUI(List<AllServiceBookingsModel> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            binding.recycleviewRecent.setVisibility(View.GONE);
            binding.lvlNotfoundRecent.setVisibility(View.VISIBLE);
        } else {
            binding.recycleviewRecent.setVisibility(View.VISIBLE);
            binding.lvlNotfoundRecent.setVisibility(View.GONE);
            recentAdapter.setBookings(bookings);
        }
    }

    private void showLoading() {
        custPrograssbar.prograssCreate(this);
    }

    private void hideLoading() {
        custPrograssbar.closePrograssBar();
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            message = "Something went wrong"; // Default error message
        }
        if (this != null) {  // Check if fragment is attached
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}