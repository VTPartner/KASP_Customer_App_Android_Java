package com.kapstranspvtltd.kaps.common_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.common_activities.adapters.ScheduledBookingsAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.ScheduledBooking;
import com.kapstranspvtltd.kaps.databinding.ActivityScheduledBookingsBinding;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduledBookingsActivity extends AppCompatActivity {
    private ActivityScheduledBookingsBinding binding;
    private ScheduledBookingsAdapter adapter;
    private PreferenceManager preferenceManager;
    private List<ScheduledBooking> scheduledBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduledBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        setupToolbar();
        setupRecyclerView();
        fetchScheduledBookings();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ScheduledBookingsAdapter(this, (booking, position) -> {
            // Handle booking click based on category_id
            navigateToBookingDetails(booking);
        });
        binding.recyclerScheduledBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerScheduledBookings.setAdapter(adapter);
    }

    private void fetchScheduledBookings() {
        showLoading();
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");
        String url = APIClient.baseUrl + "get_scheduled_bookings";

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
                        List<ScheduledBooking> bookings = parseScheduledBookings(response);
                        updateUI(bookings);
                    } catch (Exception e) {
                        showError("Failed to parse bookings: " + e.getMessage());
                    }
                    hideLoading();
                },
                error -> {
                    hideLoading();
                    updateUI(null);
                    // showError(error.getMessage());
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

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    private List<ScheduledBooking> parseScheduledBookings(JSONObject response) throws JSONException {
        List<ScheduledBooking> scheduledBookings = new ArrayList<>();

        if (response.getBoolean("status")) {
            JSONArray bookingsArray = response.getJSONArray("data");

            for (int i = 0; i < bookingsArray.length(); i++) {
                JSONObject bookingObj = bookingsArray.getJSONObject(i);
                ScheduledBooking booking = new ScheduledBooking();

                booking.setSchedule_id(bookingObj.getString("schedule_id"));
                booking.setBooking_id(bookingObj.getString("booking_id"));
                booking.setScheduled_time(bookingObj.getString("scheduled_time"));
                booking.setCategory_id(bookingObj.getString("category_id"));
                booking.setScheduled_date(bookingObj.getString("scheduled_date"));
                booking.setCategory_name(bookingObj.getString("category_name"));
                booking.setCategory_image(bookingObj.getString("category_image"));
                booking.setPickup_address(bookingObj.getString("pickup_address"));
                booking.setTotal_price(bookingObj.getString("total_price"));
                booking.setBooking_status(bookingObj.getString("booking_status"));
                booking.setService_name(bookingObj.optString("service_name", ""));
                booking.setSub_cat_name(bookingObj.optString("sub_cat_name", ""));

//                String scheduledTimeStr = bookingObj.getString("scheduled_time");
//                long epochMillis = (long) (Double.parseDouble(scheduledTimeStr) * 1000); // Convert to milliseconds
//
//                Date date = new Date(epochMillis);
//                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
//                String readableTime = sdf.format(date);
//
//// Set it to your booking model or UI
//                booking.setScheduled_time(readableTime);

                // For goods and cab services, include drop address
                if (bookingObj.has("drop_address")) {
                    booking.setDrop_address(bookingObj.getString("drop_address"));
                }

                scheduledBookings.add(booking);
            }
        }

        return scheduledBookings;
    }

    private void updateUI(List<ScheduledBooking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            binding.recyclerScheduledBookings.setVisibility(View.GONE);
            binding.layoutNoBookings.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerScheduledBookings.setVisibility(View.VISIBLE);
            binding.layoutNoBookings.setVisibility(View.GONE);
            adapter.setBookings(bookings);
        }
    }


    private void navigateToBookingDetails(ScheduledBooking booking) {
        String categoryId = booking.getCategory_id();
        Intent intent = null;

//        switch (categoryId) {
//            case "1": // Goods
//                intent = new Intent(this, GoodsBookingDetailsActivity.class);
//                break;
//            case "2": // Cab
//                intent = new Intent(this, CabBookingDetailsActivity.class);
//                break;
//            case "3": // JCB & Crane
//                intent = new Intent(this, JcbCraneBookingDetailsActivity.class);
//                break;
//            case "4": // Driver
//                intent = new Intent(this, DriverBookingDetailsActivity.class);
//                break;
//            case "5": // Handyman
//                intent = new Intent(this, HandymanBookingDetailsActivity.class);
//                break;
//        }

        if (intent != null) {
            intent.putExtra("booking_id", booking.getBooking_id());
            startActivity(intent);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}