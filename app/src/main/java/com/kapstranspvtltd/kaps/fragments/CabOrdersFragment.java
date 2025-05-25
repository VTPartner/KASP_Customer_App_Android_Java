package com.kapstranspvtltd.kaps.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kapstranspvtltd.kaps.activities.OngoingGoodsDetailActivity;
import com.kapstranspvtltd.kaps.activities.OrderDetailsScreenActivity;
import com.kapstranspvtltd.kaps.activities.models.AllGoodsOrders;
import com.kapstranspvtltd.kaps.activities.models.Booking;
import com.kapstranspvtltd.kaps.adapters.PastOrdersAdapter;
import com.kapstranspvtltd.kaps.adapters.RecentBookingsAdapter;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.databinding.FragmentCabOrdersBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CabOrdersFragment extends Fragment {

    private RecentBookingsAdapter recentAdapter;

    FragmentCabOrdersBinding binding;
    private PastOrdersAdapter pastAdapter;
    private PreferenceManager preferenceManager;
    private CustPrograssbar custPrograssbar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCabOrdersBinding.inflate(inflater, container, false);
        initViews();
        fetchData();
        return binding.getRoot();
    }

    private void initViews() {
        preferenceManager = new PreferenceManager(requireContext());
        custPrograssbar = new CustPrograssbar();

        // Setup Recent Bookings RecyclerView
        recentAdapter = new RecentBookingsAdapter(requireContext(),
                (booking, position) -> {
                    // Create intent to launch TripDetailsActivity
                    Intent intent = new Intent(requireContext(), OngoingGoodsDetailActivity.class);

                    // Pass booking ID and any other necessary data
                    intent.putExtra("booking_id", booking.getBooking_id());
                    intent.putExtra("cab", true);
                    intent.putExtra("customer_id", preferenceManager.getStringValue("customer_id"));

                    // Start the activity
                    startActivity(intent);
                });
        binding.recycleviewRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycleviewRecent.setAdapter(recentAdapter);

        // Setup Past Orders RecyclerView
        pastAdapter = new PastOrdersAdapter(requireContext(),
                (order, position) -> {
                    // Create intent to launch TripDetailsActivity
                    Intent intent = new Intent(requireContext(), OrderDetailsScreenActivity.class);

                    // Pass booking ID and any other necessary data
                    intent.putExtra("order_id", order.getOrder_id());
                    intent.putExtra("cab", true);
                    // Start the activity
                    startActivity(intent);
                });
        binding.recycleviewPast.setLayoutManager(new LinearLayoutManager(requireContext()));
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
        String url = APIClient.baseUrl + "customers_all_cab_bookings";

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
                        List<Booking> bookings = parseBookings(response);
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

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void fetchPastOrders() {
        showLoading();

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");
        System.out.println("customerId::"+customerId);
        String url = APIClient.baseUrl + "customers_all_cab_orders";

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
                            List<AllGoodsOrders> orders = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                AllGoodsOrders order = new AllGoodsOrders(
                                        json.getString("order_id"),
                                        json.getString("vehicle_name"),
                                        json.getString("vehicle_image"),
                                        json.getString("booking_timing"),
                                        json.getString("total_time"),
                                        json.getString("total_price"),
                                        json.getString("payment_method"),
                                        "NA",
                                        "NA",
                                        "NA",
                                        "NA",
                                        json.getString("pickup_address"),
                                        json.getString("drop_address")
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

            VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);

        } catch (JSONException e) {
            hideLoading();
            showError("Error creating request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePastOrdersUI(List<AllGoodsOrders> orders) {
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

    private List<Booking> parseBookings(JSONObject response) throws JSONException {
        List<Booking> bookings = new ArrayList<>();
        JSONArray results = response.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject json = results.getJSONObject(i);
            Booking booking = new Booking(
                    json.getString("booking_id"),
                    json.getString("vehicle_name"),
                    json.getString("vehicle_image"),
                    json.getString("booking_timing"),
                    json.getString("total_time"),
                    json.getString("booking_status"),
                    json.getString("total_price"),
                    json.getString("payment_method"),
                    "NA",
                    "NA",
                    "NA",
                    "NA",
                    json.getString("pickup_address"),
                    json.getString("drop_address")
            );
            if (!booking.getBooking_status().equals("End Trip")) {
                bookings.add(booking);
            }
        }
        return bookings;
    }

    private void updateRecentBookingsUI(List<Booking> bookings) {
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
        custPrograssbar.prograssCreate(requireContext());
    }

    private void hideLoading() {
        custPrograssbar.closePrograssBar();
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            message = "Something went wrong"; // Default error message
        }
        if (isAdded() && getContext() != null) {  // Check if fragment is attached
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}