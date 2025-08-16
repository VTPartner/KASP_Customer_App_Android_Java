package com.kapstranspvtltd.kaps.fragments;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kapstranspvtltd.kaps.activities.OngoingGoodsDetailActivity;
import com.kapstranspvtltd.kaps.activities.models.AllServicesHome;
import com.kapstranspvtltd.kaps.activities.models.SliderModel;
import com.kapstranspvtltd.kaps.activities.goods_service_booking_activities.GoodsPickupMapLocationActivity;
import com.kapstranspvtltd.kaps.adapters.OfferSliderAdapter;
import com.kapstranspvtltd.kaps.adapters.ServiceAdapter;
import com.kapstranspvtltd.kaps.cab_customer_app.activities.CabBookingPickupLocationActivity;
import com.kapstranspvtltd.kaps.coins.CoinsHomeScreenActivity;
import com.kapstranspvtltd.kaps.common_activities.AllServiceActivity;
import com.kapstranspvtltd.kaps.fcm.AccessToken;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.polygon.Polygon;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.model.AppContent;
import com.kapstranspvtltd.kaps.utility.AppContentManager;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.FragmentHomeSelectBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeSelectFragment extends Fragment {

    private FragmentHomeSelectBinding binding;
    private CustPrograssbar custPrograssbar;
    private Polygon polygon;
    public static HomeSelectFragment homeSelectFragment;

    // Shimmer views
    private ShimmerFrameLayout shimmerServices;
    private ShimmerFrameLayout shimmerCoins;
    private ShimmerFrameLayout shimmerOffers;

    // Scheduled bookings data
    private List<JSONObject> scheduledBookings = new ArrayList<>();
    
    // Service availability data
    private List<Integer> availableServices = new ArrayList<>();
    private String currentPincode = "";

    public static HomeSelectFragment getInstance() {
        return homeSelectFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Polygon getPolygon() {
        return polygon;
    }

    private ServiceAdapter serviceAdapter;
    private OfferSliderAdapter offerAdapter;

    PreferenceManager preferenceManager;

    private ExecutorService executorService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeSelectBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        preferenceManager = new PreferenceManager(this.getContext());
        homeSelectFragment = this;
        custPrograssbar = new CustPrograssbar();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize shimmer views
        initializeShimmerViews();

        getLocation();
        getZone();
        setupRecyclerViews();
        getFCMToken();
        
        // Check if we already have a saved pincode
        String savedPincode = preferenceManager.getStringValue("current_pin_code");
        if (savedPincode != null && !savedPincode.isEmpty() && !savedPincode.equals("NA")) {
            currentPincode = savedPincode;
            checkServiceAvailability();
        }

        boolean liveRide = preferenceManager.getBooleanValue("live_ride");
        String currentBookingId = preferenceManager.getStringValue("current_booking_id");

        if(liveRide){
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.live_ride)
                    .into(binding.appLogo);
            binding.liveRide.setVisibility(View.VISIBLE);
            binding.liveRide.setOnClickListener(v->{
                if(currentBookingId !=null && currentBookingId.isEmpty() == false) {
                    Intent intent = new Intent(requireContext(), OngoingGoodsDetailActivity.class);
                    intent.putExtra("booking_id", currentBookingId);
                    intent.putExtra("customer_id", preferenceManager.getStringValue("customer_id"));
                    startActivity(intent);
                }
            });
        }else{
            binding.liveRide.setVisibility(View.GONE);
        }

        binding.coinsEarnedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), CoinsHomeScreenActivity.class);
                startActivity(intent);
            }
        });

        // Set up scheduled ride click listener
        binding.scheduledRide.setOnClickListener(v -> {
            showScheduledBookingsDialog();
        });

        return view;
    }

    private void initializeShimmerViews() {
        shimmerServices = binding.getRoot().findViewById(R.id.shimmerServices);
        shimmerCoins = binding.getRoot().findViewById(R.id.shimmerCoins);
        shimmerOffers = binding.getRoot().findViewById(R.id.shimmerOffers);
    }

    private void showShimmerEffects() {
        // Show shimmer for services
        if (shimmerServices != null) {
            shimmerServices.setVisibility(View.VISIBLE);
            shimmerServices.startShimmer();
            binding.servicesRecyclerView.setVisibility(View.GONE);
        }

        // Show shimmer for coins
        if (shimmerCoins != null) {
            shimmerCoins.setVisibility(View.VISIBLE);
            shimmerCoins.startShimmer();
            binding.coinsEarnedLayout.setVisibility(View.GONE);
        }

        // Show shimmer for offers
        if (shimmerOffers != null) {
            shimmerOffers.setVisibility(View.VISIBLE);
            shimmerOffers.startShimmer();
            binding.recyclerBanner.setVisibility(View.GONE);
        }
    }

    private void hideShimmerEffects() {
        // Hide shimmer for services
        if (shimmerServices != null) {
            shimmerServices.setVisibility(View.GONE);
            shimmerServices.stopShimmer();
            binding.servicesRecyclerView.setVisibility(View.VISIBLE);
        }

        // Hide shimmer for coins
        if (shimmerCoins != null) {
            shimmerCoins.setVisibility(View.GONE);
            shimmerCoins.stopShimmer();
            binding.coinsEarnedLayout.setVisibility(View.VISIBLE);
        }

        // Hide shimmer for offers
        if (shimmerOffers != null) {
            shimmerOffers.setVisibility(View.GONE);
            shimmerOffers.stopShimmer();
            binding.recyclerBanner.setVisibility(View.VISIBLE);
        }
    }

    private void hideServicesShimmer() {
        if (shimmerServices != null) {
            shimmerServices.setVisibility(View.GONE);
            shimmerServices.stopShimmer();
            binding.servicesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void hideCoinsShimmer() {
        if (shimmerCoins != null) {
            shimmerCoins.setVisibility(View.GONE);
            shimmerCoins.stopShimmer();
            binding.coinsEarnedLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideOffersShimmer() {
        if (shimmerOffers != null) {
            shimmerOffers.setVisibility(View.GONE);
            shimmerOffers.stopShimmer();
            binding.recyclerBanner.setVisibility(View.VISIBLE);
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Save token locally
                    preferenceManager.saveStringValue("fcm_token",token);

                    // Upload token to server
                    updateAuthToken(token);
                });
    }

    private void updateAuthToken(String deviceToken) {
        Log.d("FCMToken", "Updating FCM token");

        String customerId = preferenceManager.getStringValue("customer_id");
        if (customerId.isEmpty() || deviceToken == null || deviceToken.isEmpty()) {
            return;
        }

        // Show shimmer while loading
        showShimmerEffects();

        executorService.execute(() -> {
            try {
                String serverToken = AccessToken.getAccessToken();
                System.out.println("serverToken::"+serverToken);
                String url = APIClient.baseUrl + "update_firebase_customer_token";
                System.out.println("deviceToken::"+deviceToken);
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("customer_id", customerId);
                jsonBody.put("authToken", deviceToken);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        response -> {
                            String message = response.optString("message");
                            Log.d("Auth", "Token update response: " + message);
                            fetchControlSettings();
                            fetchServices();
                            fetchCoinsSummary();
                            fetchTodaysScheduledBookings();
                            setupOffers();
                        },
                        error -> {
                            Log.e("Auth", "Error updating token: " + error.getMessage());
                            error.printStackTrace();
                            hideShimmerEffects();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);

            } catch (Exception e) {
                Log.e("Auth", "Error in token update process: " + e.getMessage());
                e.printStackTrace();
                hideShimmerEffects();
            }
        });
    }


    private void fetchTodaysScheduledBookings() {
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        if (customerId.isEmpty() || fcmToken.isEmpty()) {
            Log.e("ScheduledBookings", "Customer ID or FCM token is empty");
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "get_todays_scheduled_bookings",
                    requestBody,
                    response -> {
                        try {
                            String status = response.optString("status", "");
                            int count = response.optInt("count", 0);

                            Log.d("ScheduledBookings", "Response: " + response.toString());

                            if ("success".equals(status) && count > 0) {
                                // Parse scheduled bookings
                                JSONArray results = response.optJSONArray("results");
                                scheduledBookings.clear();

                                if (results != null) {
                                    for (int i = 0; i < results.length(); i++) {
                                        scheduledBookings.add(results.getJSONObject(i));
                                    }
                                }

                                // Show scheduled ride button
                                binding.scheduledRide.setVisibility(View.VISIBLE);
                                Log.d("ScheduledBookings", "Found " + count + " scheduled bookings, showing button");

                            } else {
                                // Hide scheduled ride button if no bookings
                                binding.scheduledRide.setVisibility(View.GONE);
                                Log.d("ScheduledBookings", "No scheduled bookings found, hiding button");
                            }

                        } catch (JSONException e) {
                            Log.e("ScheduledBookings", "Error parsing response: " + e.getMessage());
                            binding.scheduledRide.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Log.e("ScheduledBookings", "Error fetching scheduled bookings: " + error.getMessage());
                        binding.scheduledRide.setVisibility(View.GONE);
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

            // Add retry policy
            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e("ScheduledBookings", "Error creating request: " + e.getMessage());
            binding.scheduledRide.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    // NEW METHOD: Show scheduled bookings dialog
    private void showScheduledBookingsDialog() {
        if (scheduledBookings.isEmpty()) {
            Toast.makeText(getContext(), "No scheduled bookings for today", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        // Create a simple layout for showing scheduled bookings
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackground(getResources().getDrawable(R.drawable.bg_purple_bottom));
        layout.setPadding(32, 32, 32, 32);

        // Title
        TextView title = new TextView(getContext());
        title.setText("Today's Scheduled Bookings");
        title.setTextSize(18);
        title.setTextColor(getResources().getColor(R.color.white));

//        title.setTextStyle(android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);

        // Add each booking
        for (JSONObject booking : scheduledBookings) {
            try {
                LinearLayout bookingLayout = new LinearLayout(getContext());
                bookingLayout.setOrientation(LinearLayout.VERTICAL);

                bookingLayout.setPadding(16, 16, 16, 16);
                bookingLayout.setBackground(getResources().getDrawable(R.drawable.bg_white_card));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(5, 5, 5, 16);
                bookingLayout.setLayoutParams(params);

                // Booking ID
                TextView bookingIdText = new TextView(getContext());
                bookingIdText.setText("Booking ID: " + booking.optString("booking_id", "N/A"));
                bookingIdText.setTextSize(14);
                bookingIdText.setTextColor(getResources().getColor(R.color.black));
                bookingLayout.addView(bookingIdText);

                // Scheduled Time
                String scheduledTimeStr = booking.optString("scheduled_time", "N/A");
                TextView timeText = new TextView(getContext());
                timeText.setText("Scheduled Time: " + scheduledTimeStr);
                timeText.setTextSize(14);
                timeText.setTextColor(getResources().getColor(R.color.black));
                bookingLayout.addView(timeText);

                // Parse time and compare with current
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date now = new Date();
                Date scheduledTime = sdf.parse(scheduledTimeStr);

                // Driver Assignment Status
                String driverId = booking.optString("driver_id", "-1");
                TextView statusText = new TextView(getContext());
                statusText.setTextSize(14);
                statusText.setTextColor(getResources().getColor(R.color.black));

                if (scheduledTime != null && scheduledTime.before(now)) {
                    if (!driverId.equals("-1")) {
                        statusText.setText("Status: ✅ Driver Assigned");
                    } else {
                        statusText.setText("Status: ❌ No Driver Assigned");
                    }
                } else {
                    statusText.setText("Status: ⏳ Upcoming Booking");
                }

                bookingLayout.addView(statusText);
                layout.addView(bookingLayout);

            } catch (Exception e) {
                Log.e("ScheduledBookings", "Error creating booking view: " + e.getMessage());
            }
        }


        dialog.setContentView(layout);
        dialog.show();
    }

    private void fetchCoinsSummary() {
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "get_coins_summary",
                    requestBody,
                    response -> {
                        int availableCoins = response.optInt("available_coins", 0);
                        String coinAvblTxt = "0 coins available \n Book Now to earn more coins";
                        if(availableCoins > 0 ){
                            coinAvblTxt = availableCoins+" coins available";
                        }
                        binding.tvCoinsCount.setText(coinAvblTxt);
                        hideCoinsShimmer();
                    },
                    error -> {
                        binding.tvCoinsCount.setText("0 coins available \n Book Now to earn more coins");
                        hideCoinsShimmer();
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

            VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);

        } catch (JSONException e) {
            hideCoinsShimmer();
            e.printStackTrace();
        }
    }

    private void setupOffers() {
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", customerId);
        params.put("auth", fcmToken);

        JSONObject jsonBody = new JSONObject();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                APIClient.baseUrl + "get_all_banners",
                new JSONObject(params),
                response -> {
                    try {
                        JSONArray bannersArray = response.getJSONArray("banners");
                        List<SliderModel> activeOffers = new ArrayList<>();

                        for (int i = 0; i < bannersArray.length(); i++) {
                            JSONObject banner = bannersArray.getJSONObject(i);

                            // Only add active banners (status = 1)
                            int status = banner.getInt("status");
                            System.out.println("status::"+status);
                            if (banner.getInt("status") == 1) {
                                // Check if banner is within valid date range
                                String startDate = banner.getString("start_date");
                                String endDate = banner.getString("end_date");

                                if (isDateValid(startDate, endDate)) {
                                    activeOffers.add(new SliderModel(
                                            banner.getString("banner_image"),
                                            banner.getString("banner_title"),
                                            banner.getString("banner_description")
                                    ));
                                }
                            }
                        }

                        if (!activeOffers.isEmpty()) {
                            offerAdapter.setOffers(activeOffers);
                        }
                        hideOffersShimmer();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        hideOffersShimmer();
                        handleError("Error parsing banners data");
                    }
                },
                error -> {
                    hideOffersShimmer();
                    handleError("Error loading banners");
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(getActivity()).add(request);
    }

    private boolean isDateValid(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            Date current = new Date();

            return (start == null || !current.before(start)) &&
                    (end == null || !current.after(end));
        } catch (ParseException e) {
            e.printStackTrace();
            return true; // If dates are invalid, show banner anyway
        }
    }

    private void handleError(String message) {
        // Show error message
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerViews() {
        // Setup Services Grid
        serviceAdapter = new ServiceAdapter();
        binding.servicesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.servicesRecyclerView.setAdapter(serviceAdapter);

        PreferenceManager preferenceManager = new PreferenceManager(requireContext());

        serviceAdapter.setOnServiceClickListener(service -> {
            int categoryId = service.getCategoryId();
            String categoryName = service.getCategoryName();

            // Check if service is available in current area
            if (isServiceAvailable(categoryId)) {
                if (categoryId == 1 || categoryId == 2) {
                    showBookingTypeDialog(categoryId, categoryName, preferenceManager);
                } else {
                    navigateToAllServices(categoryId, categoryName);
                }
            } else {
                showComingSoonDialog(categoryName);
            }
        });

        // Setup Offers Slider
        offerAdapter = new OfferSliderAdapter();
        binding.recyclerBanner.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerBanner.setAdapter(offerAdapter);
    }

    private void showBookingTypeDialog(int categoryId, String categoryName, PreferenceManager preferenceManager) {
        System.out.println("🔄 Showing booking type dialog");
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_booking_type, null);
        dialog.setContentView(sheetView);

        LinearLayout localBooking = sheetView.findViewById(R.id.local_booking);
        LinearLayout outstationBooking = sheetView.findViewById(R.id.outstation_booking);

        // Load dynamic content for booking types
        loadBookingTypeContent(sheetView);

        localBooking.setOnClickListener(v -> {
            System.out.println("📍 Local booking selected");
            saveBookingType("local", categoryId, preferenceManager);
            dialog.dismiss();
            navigateToBookingScreen(categoryId, categoryName, true);
        });

        outstationBooking.setOnClickListener(v -> {
            System.out.println("🚗 Outstation booking selected");
            saveBookingType("outstation", categoryId, preferenceManager);
            dialog.dismiss();
            navigateToBookingScreen(categoryId, categoryName, false);
        });

        dialog.show();
    }

    private void loadBookingTypeContent(View sheetView) {
        // Load local booking content
        AppContent localContent = AppContentManager.getInstance(requireContext())
                .getFirstContentForScreen("booking_type_local");
        if (localContent != null) {
            ImageView localImage = sheetView.findViewById(R.id.local_booking_image);
            TextView localText = sheetView.findViewById(R.id.local_booking_text);
            
            if (localText != null && !localContent.getTitle().equals("NA")) {
                localText.setText(localContent.getTitle());
            }
            
            if (localImage != null && !localContent.getImageUrl().equals("NA")) {
                if (localContent.getImageUrl().startsWith("http")) {
                    Glide.with(this)
                            .load(localContent.getImageUrl())
                            .placeholder(R.drawable.ic_singlepin_inside)
                            .error(R.drawable.ic_singlepin_inside)
                            .into(localImage);
                } else {
                    try {
                        int resourceId = getResources().getIdentifier(
                                localContent.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                requireContext().getPackageName()
                        );
                        if (resourceId != 0) {
                            localImage.setImageResource(resourceId);
                        }
                    } catch (Exception e) {
                        localImage.setImageResource(R.drawable.ic_singlepin_inside);
                    }
                }
            }
        }

        // Load outstation booking content
        AppContent outstationContent = AppContentManager.getInstance(requireContext())
                .getFirstContentForScreen("booking_type_outstation");
        if (outstationContent != null) {
            ImageView outstationImage = sheetView.findViewById(R.id.outstation_booking_image);
            TextView outstationText = sheetView.findViewById(R.id.outstation_booking_text);
            
            if (outstationText != null && !outstationContent.getTitle().equals("NA")) {
                outstationText.setText(outstationContent.getTitle());
            }
            
            if (outstationImage != null && !outstationContent.getImageUrl().equals("NA")) {
                if (outstationContent.getImageUrl().startsWith("http")) {
                    Glide.with(this)
                            .load(outstationContent.getImageUrl())
                            .placeholder(R.drawable.outstation_booking)
                            .error(R.drawable.outstation_booking)
                            .into(outstationImage);
                } else {
                    try {
                        int resourceId = getResources().getIdentifier(
                                outstationContent.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                requireContext().getPackageName()
                        );
                        if (resourceId != 0) {
                            outstationImage.setImageResource(resourceId);
                        }
                    } catch (Exception e) {
                        outstationImage.setImageResource(R.drawable.outstation_booking);
                    }
                }
            }
        }
    }

    private void saveBookingType(String bookingType, int categoryId, PreferenceManager preferenceManager) {
        System.out.println("💾 Saving booking preferences");
        // Save booking type
        preferenceManager.saveStringValue("booking_type", bookingType);

        // Save category ID
        preferenceManager.saveIntValue("booking_category", categoryId);

        // Save timestamp of booking
        preferenceManager.saveLongValue("booking_timestamp", System.currentTimeMillis());

        System.out.println("✅ Saved booking type: " + bookingType + " for category: " + categoryId);
    }

    private void navigateToBookingScreen(int categoryId, String categoryName, boolean isLocal) {
        System.out.println("🔄 Navigating to booking screen");
        System.out.println("📋 Category ID: " + categoryId);
        System.out.println("📝 Category Name: " + categoryName);
        System.out.println("🚩 Is Local: " + isLocal);

        Intent intent;
        if (categoryId == 1) {
            intent = new Intent(getActivity(), GoodsPickupMapLocationActivity.class);
            intent.putExtra("category_id", categoryId);
            intent.putExtra("category_name", categoryName);
            intent.putExtra("cab", categoryId == 2);
            intent.putExtra("is_local", isLocal);

            // Add transition animation flags
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            // Create ActivityOptions for smooth transition
            ActivityOptions options = ActivityOptions.makeCustomAnimation(
                    getActivity(),
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );

            System.out.println("🚀 Starting activity with transition");
            startActivityForResult(intent, 100, options.toBundle());
        } else {
            intent = new Intent(getActivity(), CabBookingPickupLocationActivity.class);
            intent.putExtra("category_id", categoryId);
            intent.putExtra("category_name", categoryName);
            intent.putExtra("cab", categoryId == 2);
            intent.putExtra("is_local", isLocal);
//            showError("Coming Soon 🚀");
            // Add transition animation flags
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            // Create ActivityOptions for smooth transition
            ActivityOptions options = ActivityOptions.makeCustomAnimation(
                    getActivity(),
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );

            System.out.println("🚀 Starting activity with transition");
            startActivityForResult(intent, 100, options.toBundle());
        }


    }

    private void navigateToAllServices(int categoryId, String categoryName) {
        showError("Coming Soon 🚀");
        /*Intent intent = new Intent(getActivity(), AllServiceActivity.class);
        intent.putExtra("category_id", categoryId);
        intent.putExtra("category_name", categoryName);
        intent.putExtra("cab", false);

        // Add transition animation flags
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        // Create ActivityOptions for smooth transition
        ActivityOptions options = ActivityOptions.makeCustomAnimation(
                getActivity(),
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );

        // Start activity with transition
        startActivityForResult(intent, 100, options.toBundle());*/
    }

    private void fetchServices() {
        String customerName = preferenceManager.getStringValue("customer_name");
        if(customerName != null && !customerName.isEmpty() && customerName.equalsIgnoreCase("NA") == false) {
            binding.customerNameText.setText("Hello 👋,\n" + customerName);
        }

        String url = APIClient.baseUrl + "all_services";

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", customerId);
        params.put("auth", fcmToken);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                new JSONObject(params),
                response -> {
                    try {
                        List<AllServicesHome> servicesList = new ArrayList<>();

                        JSONArray jsonArray = response.getJSONArray("results");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject serviceObject = jsonArray.getJSONObject(i);

                            AllServicesHome service = new AllServicesHome(
                                    serviceObject.getInt("category_id"),
                                    serviceObject.getString("category_name"),
                                    serviceObject.getString("category_image"),
                                    serviceObject.getString("description")
                            );

                            servicesList.add(service);
                        }

                        if (servicesList.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                            serviceAdapter.setServices(servicesList);
                        }

                        hideServicesShimmer();

                    } catch (Exception e) {
                        Log.e("FetchServices", "Error parsing response: " + e.getMessage());
                        e.printStackTrace();
                        hideServicesShimmer();
                        showError("Error loading services: " + e.getMessage());
                    }
                },
                error -> {
                    hideServicesShimmer();
                    handleError(error);
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
    }

    private void fetchControlSettings() {
        String url = APIClient.baseUrl + "get_control_settings";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null, // No body needed for this request
                response -> {
                    try {
                        if (response.has("settings")) {
                            JSONObject settings = response.getJSONObject("settings");
                            System.out.println("settings::"+settings);
                            // Save each setting to preferences
                            preferenceManager.saveStringValue("booking_timeout",
                                    settings.optString("booking_timeout", "30"));

                            preferenceManager.saveStringValue("multiple_drops",
                                    settings.optString("multiple_drops", "3"));

                            preferenceManager.saveStringValue("SIGN_UP_BONUS_CUSTOMER_APP",
                                    settings.optString("sign_up_bonus_customer_app", "10"));

                            preferenceManager.saveStringValue("agent_recharge_expiry_show",
                                    settings.optString("agent_recharge_expiry_show", "No"));

                            preferenceManager.saveStringValue("hike_price_show",
                                    settings.optString("hike_price_show", "No"));

                            preferenceManager.saveStringValue("agent_cancel_button_show",
                                    settings.optString("agent_cancel_button_show", "No"));

                            // Save last updated times if needed
                            if (response.has("last_updated")) {
                                JSONObject lastUpdated = response.getJSONObject("last_updated");
                                preferenceManager.saveStringValue("settings_last_updated",
                                        lastUpdated.optString("booking_timeout", "0"));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ControlSettings", "Error parsing settings: " + e.getMessage());
                    }
                },
                error -> Log.e("ControlSettings", "Error fetching settings: " + error.getMessage())
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
    }

    private void showLoading() {
        if (custPrograssbar != null) {
            custPrograssbar.prograssCreate(requireContext());
        }
    }

    private void hideLoading() {
        if (custPrograssbar != null) {
            custPrograssbar.closePrograssBar();
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState(boolean show) {
        // Implement empty state UI
    }

    private void handleError(VolleyError error) {
        if (error instanceof NoConnectionError) {
            showError("No internet connection");
        } else if (error instanceof TimeoutError) {
            showError("Request timed out");
        } else if (error instanceof ServerError) {
            showError("Server error");
        } else {
            showError("Error loading services: " + error.getMessage());
        }
        Log.e("FetchServices", "Error: " + error.getMessage());
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);
        } else {
            FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireActivity());

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            getAddressFromLocation(requireContext(), latitude, longitude);
                        }
                    });
        }
    }

    private void getAddressFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String addressText = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(", ");
                }

                sb.append(address.getLocality()).append(", ");
                sb.append(address.getPostalCode()).append(", ");
                sb.append(address.getCountryName());
                currentPincode = address.getPostalCode();
                preferenceManager.saveStringValue("current_pin_code", currentPincode);
                addressText = sb.toString();
                
                // Check service availability after getting pincode
                if (currentPincode != null && !currentPincode.isEmpty()) {
                    checkServiceAvailability();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if( addressText != null && binding.txtAddress !=null && !addressText.isEmpty())
            binding.txtAddress.setText(addressText);
    }

    private void checkServiceAvailability() {
        if (currentPincode.isEmpty()) {
            // If no pincode, assume all services are available
            availableServices.clear();
            return;
        }

        String fcmToken = preferenceManager.getStringValue("fcm_token");
//        currentPincode = "412105";
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("pincode", currentPincode);
            requestBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "get_service_availability_by_pincode",
                    requestBody,
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray availableServicesArray = response.optJSONArray("available_services");
                                availableServices.clear();
                                
                                if (availableServicesArray != null) {
                                    for (int i = 0; i < availableServicesArray.length(); i++) {
                                        availableServices.add(availableServicesArray.getInt(i));
                                    }
                                }
                                
                                Log.d("ServiceAvailability", "Available services for pincode " + currentPincode + ": " + availableServices);
                            }
                        } catch (JSONException e) {
                            Log.e("ServiceAvailability", "Error parsing response: " + e.getMessage());
                            // If error, assume all services are available
                            availableServices.clear();
                        }
                    },
                    error -> {
                        Log.e("ServiceAvailability", "Error checking service availability: " + error.getMessage());
                        // If error, assume all services are available
                        availableServices.clear();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e("ServiceAvailability", "Error creating request: " + e.getMessage());
            availableServices.clear();
        }
    }

    private boolean isServiceAvailable(int categoryId) {
        if (currentPincode.isEmpty()) {
            return true; // allow all if no pincode
        }

        // Do not assume available if the list is empty
        return availableServices.contains(categoryId);
    }


    private void showComingSoonDialog(String serviceName) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_coming_soon, null);
        dialog.setContentView(sheetView);

        TextView serviceNameText = sheetView.findViewById(R.id.service_name);
        TextView messageText = sheetView.findViewById(R.id.message);
        TextView pincodeText = sheetView.findViewById(R.id.pincode_info);

        serviceNameText.setText(serviceName);
        messageText.setText("This service is not available in your area yet. We're working to expand our services to your location.");
        
        if (!currentPincode.isEmpty()) {
            pincodeText.setText("Your area: " + currentPincode);
            pincodeText.setVisibility(View.VISIBLE);
        } else {
            pincodeText.setVisibility(View.GONE);
        }

        sheetView.findViewById(R.id.btn_ok).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void getZone() {
        // Implement zone fetching logic
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop shimmer animations
        if (shimmerServices != null) {
            shimmerServices.stopShimmer();
        }
        if (shimmerCoins != null) {
            shimmerCoins.stopShimmer();
        }
        if (shimmerOffers != null) {
            shimmerOffers.stopShimmer();
        }
        binding = null;
    }
}