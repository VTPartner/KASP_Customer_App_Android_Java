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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kapstranspvtltd.kaps.activities.OngoingGoodsDetailActivity;
import com.kapstranspvtltd.kaps.activities.models.AllServicesHome;
import com.kapstranspvtltd.kaps.activities.models.SliderModel;
import com.kapstranspvtltd.kaps.activities.pickup_activities.GoodsBookingNewActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.GoodsPickupMapLocationActivity;
import com.kapstranspvtltd.kaps.adapters.OfferSliderAdapter;
import com.kapstranspvtltd.kaps.adapters.ServiceAdapter;
import com.kapstranspvtltd.kaps.cab_customer_app.activities.CabBookingPickupLocationActivity;
import com.kapstranspvtltd.kaps.common_activities.AllServiceActivity;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.polygon.Polygon;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.FragmentHomeSelectBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeSelectFragment extends Fragment {

    private FragmentHomeSelectBinding binding;
    private CustPrograssbar custPrograssbar;
    private Polygon polygon;
    public static HomeSelectFragment homeSelectFragment;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeSelectBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        preferenceManager = new PreferenceManager(this.getContext());
        homeSelectFragment = this;
        custPrograssbar = new CustPrograssbar();


        getLocation();
        getZone();
        setupRecyclerViews();
        fetchServices();
        setupOffers();

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

                    // Pass booking ID and any other necessary data
                    intent.putExtra("booking_id", currentBookingId);
                    intent.putExtra("customer_id", preferenceManager.getStringValue("customer_id"));

                    // Start the activity
                    startActivity(intent);
                }
            });
        }else{
            binding.liveRide.setVisibility(View.GONE);
        }
        return view;
    }

    private void setupOffers() {
//        List<SliderModel> dummyOffers = Arrays.asList(
//                new SliderModel("https://nesscampbell.com/wp-content/uploads/NessCampbellCraneRigging-seotool-19517-WhatAreThe-Blogbanner1-2.jpg"),
//                new SliderModel("https://img.freepik.com/free-psd/delivery-service-horizontal-banner_23-2148881022.jpg"),
//                new SliderModel("https://edit.org/img/blog/snm-handyman-templates-ads-editable-online-repair-maintenance.jpg"),
//                new SliderModel("https://img.freepik.com/premium-vector/delivery-service-ads-promotional-web-banner-template-design_1033790-8771.jpg")
//        );
//        offerAdapter.setOffers(dummyOffers);
        JSONObject jsonBody = new JSONObject();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "get_all_banners",
                jsonBody,
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

                        if (activeOffers.isEmpty()) {
                            // Show no active offers message
//                            binding.noOffersText.setVisibility(View.VISIBLE);
//                            binding.offersViewPager.setVisibility(View.GONE);
                        } else {
//                            binding.noOffersText.setVisibility(View.GONE);
//                            binding.offersViewPager.setVisibility(View.VISIBLE);
                            offerAdapter.setOffers(activeOffers);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        handleError("Error parsing banners data");
                    }
                },
                error -> {

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

        // Hide slider if needed
//        binding.offersViewPager.setVisibility(View.GONE);
//        binding.noOffersText.setVisibility(View.VISIBLE);
    }

    /*private void setupRecyclerViews() {
        // Setup Services Grid
        serviceAdapter = new ServiceAdapter();
        binding.servicesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.servicesRecyclerView.setAdapter(serviceAdapter);

        serviceAdapter.setOnServiceClickListener(service -> {
            int categoryId = service.getCategoryId();
            String categoryName = service.getCategoryName();

//            if(categoryId != 1 ) {
//               // showError("This feature is coming soon");
//                return;
//            }

            // Create intent with smooth transition
            if(categoryId == 1) {
                Intent intent = new Intent(getActivity(), GoodsPickupMapLocationActivity.class);
                intent.putExtra("category_id", categoryId);
                intent.putExtra("category_name", categoryName);
                intent.putExtra("cab",false);

                // Add transition animation flags
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                // Create ActivityOptions for smooth transition
                ActivityOptions options = ActivityOptions.makeCustomAnimation(
                        getActivity(),
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );

                // Start activity with transition
                startActivityForResult(intent, 100, options.toBundle());
            }
            else if(categoryId == 2){
                Intent intent = new Intent(getActivity(), CabBookingPickupLocationActivity.class);
                intent.putExtra("category_id", categoryId);
                intent.putExtra("category_name", categoryName);
                intent.putExtra("cab",true);

                // Add transition animation flags
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                // Create ActivityOptions for smooth transition
                ActivityOptions options = ActivityOptions.makeCustomAnimation(
                        getActivity(),
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );

                // Start activity with transition
                startActivityForResult(intent, 100, options.toBundle());
            }else{
                Intent intent = new Intent(getActivity(), AllServiceActivity.class);
                intent.putExtra("category_id", categoryId);
                intent.putExtra("category_name", categoryName);
                intent.putExtra("cab",false);

                // Add transition animation flags
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                // Create ActivityOptions for smooth transition
                ActivityOptions options = ActivityOptions.makeCustomAnimation(
                        getActivity(),
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );

                // Start activity with transition
                startActivityForResult(intent, 100, options.toBundle());

            }
        });



        // Setup Offers Slider
        offerAdapter = new OfferSliderAdapter();
        binding.recyclerBanner.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerBanner.setAdapter(offerAdapter);
    }*/

    private void setupRecyclerViews() {
        // Setup Services Grid
        serviceAdapter = new ServiceAdapter();
        binding.servicesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.servicesRecyclerView.setAdapter(serviceAdapter);

        PreferenceManager preferenceManager = new PreferenceManager(requireContext());

        serviceAdapter.setOnServiceClickListener(service -> {
            int categoryId = service.getCategoryId();
            String categoryName = service.getCategoryName();

            if (categoryId == 1 || categoryId == 2) {
                showBookingTypeDialog(categoryId, categoryName, preferenceManager);
            } else {
                navigateToAllServices(categoryId, categoryName);
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
//            intent = new Intent(getActivity(), GoodsBookingNewActivity.class);
            intent = new Intent(getActivity(), GoodsPickupMapLocationActivity.class);
        } else {
            intent = new Intent(getActivity(), CabBookingPickupLocationActivity.class);
        }

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
    }

    private void navigateToAllServices(int categoryId, String categoryName) {
        Intent intent = new Intent(getActivity(), AllServiceActivity.class);
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
        startActivityForResult(intent, 100, options.toBundle());
    }

    private void fetchServices() {
        showLoading();
        String customerName = preferenceManager.getStringValue("customer_name");
        if(customerName != null && !customerName.isEmpty() && customerName.equalsIgnoreCase("NA") == false) {
            binding.customerNameText.setText("Hello 👋,\n" + customerName);
        }

        String url = APIClient.baseUrl + "all_services";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    try {
                        hideLoading();
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

                    } catch (Exception e) {
                        Log.e("FetchServices", "Error parsing response: " + e.getMessage());
                        e.printStackTrace();
                        showError("Error loading services: " + e.getMessage());
                    }
                },
                error -> {
                    hideLoading();
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
                addressText = sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        binding.txtAddress.setText(addressText);
    }

    private void getZone() {
        // Implement zone fetching logic
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}